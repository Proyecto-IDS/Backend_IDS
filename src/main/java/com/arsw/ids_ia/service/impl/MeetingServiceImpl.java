package com.arsw.ids_ia.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arsw.ids_ia.dto.request.CreateMeetingRequest;
import com.arsw.ids_ia.dto.request.JoinMeetingRequest;
import com.arsw.ids_ia.exception.UnauthorizedException;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.AlertRepository;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.MeetingService;
import com.arsw.ids_ia.utils.enums.Role;
import com.arsw.ids_ia.ws.TrafficSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingServiceImpl.class);
    
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final TrafficSocketHandler socketHandler;

    @Override
    @Transactional
    public Meeting createMeeting(CreateMeetingRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (creator.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only administrators can create meetings");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        var start = LocalDateTime.parse(request.getStartTime(), formatter);
        var end = LocalDateTime.parse(request.getEndTime(), formatter);
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        Meeting meeting = Meeting.builder()
                .code(generateUniqueCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(LocalDateTime.parse(request.getStartTime(), formatter))
                .endTime(LocalDateTime.parse(request.getEndTime(), formatter))
                .creator(creator)
                .participants(new HashSet<>())
                .status("ACTIVE")
                .build();

        // First save the meeting without participants
        Meeting savedMeeting = meetingRepository.saveAndFlush(meeting);
        
        // Then manually add the creator to participants using the saved meeting
        // This ensures the relationship is properly persisted
        savedMeeting.getParticipants().add(creator);
        savedMeeting = meetingRepository.saveAndFlush(savedMeeting);
        
        // Force refresh from database to get accurate participant count
        savedMeeting = meetingRepository.findById(savedMeeting.getId()).orElseThrow();
        
        // If incidentId is provided, update all alerts with that incidentId to link to this meeting
        if (request.getIncidentId() != null && !request.getIncidentId().isEmpty()) {
            var alerts = alertRepository.findByIncidentId(request.getIncidentId());
            for (var alert : alerts) {
                alert.setWarRoomId(savedMeeting.getId());
                alertRepository.save(alert);
            }
        }
        
        // Broadcast warroom.created event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.created");
        event.put("incidentId", request.getIncidentId());
        
        Map<String, Object> warRoomData = new HashMap<>();
        warRoomData.put("id", savedMeeting.getId());
        warRoomData.put("code", savedMeeting.getCode());
        warRoomData.put("title", savedMeeting.getTitle());
        warRoomData.put("startTime", savedMeeting.getStartTime().toString());
        warRoomData.put("endTime", savedMeeting.getEndTime().toString());
        warRoomData.put("currentParticipantCount", savedMeeting.getCurrentParticipantCount());
        
        event.put("warRoom", warRoomData);
        
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }

    @Override
    public Meeting joinMeeting(JoinMeetingRequest request, String participantEmail) {
        User participant = userRepository.findByEmail(participantEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Meeting meeting = meetingRepository.findByCode(request.getCode())
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        // Check if user is already in the meeting using database query
        boolean alreadyJoined = meetingRepository.isUserParticipant(meeting.getId(), participant.getId());
        
        if (alreadyJoined) {
            // Refresh meeting from DB to get accurate participant count
            meeting = meetingRepository.findById(meeting.getId()).get();
            // Still broadcast the event to notify frontend of the user's presence
            broadcastJoinEvent(meeting, participantEmail);
            return meeting;
        }

        // Try to add participant in separate transaction to avoid rollback issues
        return attemptJoinMeeting(meeting, participant, participantEmail);
    }

    @Transactional
    private Meeting attemptJoinMeeting(Meeting meeting, User participant, String participantEmail) {
        try {
            meeting.getParticipants().add(participant);
            meeting = meetingRepository.saveAndFlush(meeting);
            
            // Broadcast warroom.participants event via WebSocket
            broadcastJoinEvent(meeting, participantEmail);
            
            return meeting;
            
        } catch (Exception e) {
            // Handle race condition where user was added between check and save
            if (e.getMessage() != null && e.getMessage().contains("meeting_participants_pkey")) {
                // Handle in separate transaction to avoid rollback issues
                return handleRaceCondition(meeting.getId(), participantEmail);
            } else {
                logger.error("Unexpected error joining meeting: {}", e.getMessage());
                throw e;
            }
        }
    }

    @Transactional(readOnly = true)
    private Meeting handleRaceCondition(Long meetingId, String participantEmail) {
        // Reload meeting and broadcast event in separate read-only transaction
        Meeting meeting = meetingRepository.findById(meetingId).get();
        broadcastJoinEvent(meeting, participantEmail);
        return meeting;
    }    private void broadcastJoinEvent(Meeting meeting, String participantEmail) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.participants");
        event.put("warRoomId", meeting.getId());
        event.put("currentParticipantCount", meeting.getCurrentParticipantCount());
        event.put("action", "joined");
        event.put("userEmail", participantEmail);
        
        socketHandler.broadcastObject(event);
    }
    
    @Override
    @Transactional
    public Meeting leaveMeeting(Long meetingId, String participantEmail) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        // Remove participant by email to ensure correct removal from ManyToMany relationship
        boolean removed = meeting.getParticipants().removeIf(p -> p.getEmail().equals(participantEmail));
        
        if (!removed) {
            return meeting;
        }
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Broadcast warroom.participants event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.participants");
        event.put("warRoomId", savedMeeting.getId());
        event.put("currentParticipantCount", savedMeeting.getCurrentParticipantCount());
        event.put("action", "left");
        event.put("userEmail", participantEmail);
        
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeetingByCode(String code) {
        return meetingRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }

    @Override
    @Transactional
    public Meeting markIncidentAsResolved(Long meetingId, String adminEmail) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        // Verify that the user is the creator (admin)
        if (!meeting.getCreator().getEmail().equals(adminEmail)) {
            throw new RuntimeException("Only the meeting creator can mark incident as resolved");
        }

        // Update meeting status and end time
        meeting.setStatus("ENDED");
        meeting.setEndTime(LocalDateTime.now());
        
        Meeting savedMeeting = meetingRepository.save(meeting);

        // Broadcast meeting ended event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.resolved");
        event.put("warRoomId", savedMeeting.getId());
        event.put("resolvedAt", savedMeeting.getEndTime().toString());
        
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (meetingRepository.findByCode(code).isPresent());
        return code;
    }
}