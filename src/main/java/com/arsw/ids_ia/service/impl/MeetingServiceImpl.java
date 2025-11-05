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
                .build();

        meeting.getParticipants().add(creator);
        Meeting savedMeeting = meetingRepository.save(meeting);
        
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
        
        logger.info("Broadcasting warroom.created event: incidentId={}, warRoomId={}", request.getIncidentId(), savedMeeting.getId());
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }

    @Override
    @Transactional
    public Meeting joinMeeting(JoinMeetingRequest request, String participantEmail) {
        User participant = userRepository.findByEmail(participantEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Meeting meeting = meetingRepository.findByCode(request.getCode())
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meeting.getParticipants().add(participant);
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Broadcast warroom.participants event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.participants");
        event.put("warRoomId", savedMeeting.getId());
        event.put("currentParticipantCount", savedMeeting.getCurrentParticipantCount());
        event.put("action", "joined");
        event.put("userEmail", participantEmail);
        
        logger.info("Broadcasting warroom.participants event: warRoomId={}, action=joined, user={}, count={}", 
                    savedMeeting.getId(), participantEmail, savedMeeting.getCurrentParticipantCount());
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }
    
    @Override
    @Transactional
    public Meeting leaveMeeting(Long meetingId, String participantEmail) {
        User participant = userRepository.findByEmail(participantEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meeting.getParticipants().remove(participant);
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Broadcast warroom.participants event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.participants");
        event.put("warRoomId", savedMeeting.getId());
        event.put("currentParticipantCount", savedMeeting.getCurrentParticipantCount());
        event.put("action", "left");
        event.put("userEmail", participantEmail);
        
        logger.info("Broadcasting warroom.participants event: warRoomId={}, action=left, user={}, count={}", 
                    savedMeeting.getId(), participantEmail, savedMeeting.getCurrentParticipantCount());
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (meetingRepository.findByCode(code).isPresent());
        return code;
    }
}