package com.arsw.ids_ia.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String KEY_CURRENT_PARTICIPANT_COUNT = "currentParticipantCount";
    private static final String KEY_DURATION_SECONDS = "durationSeconds";
    private static final String MSG_MEETING_NOT_FOUND = "Meeting not found";
    private static final String KEY_WAR_ROOM_ID = "warRoomId";
    
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

        // Create meeting without setting ID (let database generate it)
        Meeting meeting = Meeting.builder()
                .code(generateUniqueCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(LocalDateTime.now(java.time.ZoneOffset.UTC)) // Usar UTC explícitamente
                .endTime(null) // Se establecerá cuando termine
                .durationSeconds(null) // Se calculará cuando termine
                .creator(creator)
                .participants(new HashSet<>())
                .status(STATUS_ACTIVE)
                .currentParticipantCount(0)
                .build();

        try {
            // Use save() instead of saveAndFlush() to avoid forcing immediate database write
            Meeting savedMeeting = meetingRepository.save(meeting);
            
            // Add creator as first participant
            savedMeeting.getParticipants().add(creator);
            savedMeeting.setCurrentParticipantCount(1);
            
            // Final save with all data
            savedMeeting = meetingRepository.saveAndFlush(savedMeeting);
            
            logger.info("Meeting created successfully with ID: {}, Code: {}", 
                savedMeeting.getId(), savedMeeting.getCode());
            
            // Update alerts if incidentId is provided
            if (request.getIncidentId() != null && !request.getIncidentId().isEmpty()) {
                updateAlertsForMeeting(request.getIncidentId(), savedMeeting.getId());
            }
            
            // Broadcast creation event
            broadcastMeetingCreated(savedMeeting, request.getIncidentId());
            
            return savedMeeting;
            
        } catch (Exception e) {
            logger.error("Error creating meeting for user {}: {}", creatorEmail, e.getMessage(), e);
            
            // Check if it's a duplicate key error and provide better error message
            if (e.getMessage() != null && 
                (e.getMessage().contains("duplicate key") || 
                 e.getMessage().contains("constraint violation"))) {
                
                // Try to generate a different code and retry once
                try {
                    meeting.setCode(generateUniqueCode());
                    Meeting retryMeeting = meetingRepository.save(meeting);
                    retryMeeting.getParticipants().add(creator);
                    retryMeeting.setCurrentParticipantCount(1);
                    retryMeeting = meetingRepository.saveAndFlush(retryMeeting);
                    
                    logger.info("Meeting created on retry with ID: {}, Code: {}", 
                        retryMeeting.getId(), retryMeeting.getCode());
                    
                    if (request.getIncidentId() != null && !request.getIncidentId().isEmpty()) {
                        updateAlertsForMeeting(request.getIncidentId(), retryMeeting.getId());
                    }
                    
                    broadcastMeetingCreated(retryMeeting, request.getIncidentId());
                    return retryMeeting;
                    
                } catch (Exception retryEx) {
                    logger.error("Retry failed for creating meeting: {}", retryEx.getMessage());
                    throw new RuntimeException("Failed to create meeting after retry. Please try again.");
                }
            }
            
            throw new RuntimeException("Error creating meeting: " + e.getMessage());
        }
    }

    private void updateAlertsForMeeting(String incidentId, Long meetingId) {
        try {
            var alerts = alertRepository.findByIncidentId(incidentId);
            for (var alert : alerts) {
                alert.setWarRoomId(meetingId);
                alertRepository.save(alert);
            }
            logger.info("Updated {} alerts for incident {} with meeting ID {}", 
                alerts.size(), incidentId, meetingId);
        } catch (Exception e) {
            logger.warn("Failed to update alerts for incident {}: {}", incidentId, e.getMessage());
        }
    }

    private void broadcastMeetingCreated(Meeting meeting, String incidentId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "warroom.created");
            event.put("incidentId", incidentId);
            
            Map<String, Object> warRoomData = new HashMap<>();
            warRoomData.put("id", meeting.getId());
            warRoomData.put("code", meeting.getCode());
            warRoomData.put("title", meeting.getTitle());
            warRoomData.put("startTime", meeting.getStartTime() != null ? meeting.getStartTime().atZone(ZoneOffset.UTC).toInstant().toString() : null);
            warRoomData.put(KEY_CURRENT_PARTICIPANT_COUNT, meeting.getCurrentParticipantCount());
            warRoomData.put("status", meeting.getStatus());
            warRoomData.put(KEY_DURATION_SECONDS, meeting.getDurationSeconds());
            
            event.put("warRoom", warRoomData);
            
            socketHandler.broadcastObject(event);
            logger.info("Broadcasted meeting created event for meeting ID: {}", meeting.getId());
        } catch (Exception e) {
            logger.warn("Failed to broadcast meeting created event: {}", e.getMessage());
        }
    }

    @Override
    @Transactional // Unifica la transacción aquí (Sonar S2229)
    public Meeting joinMeeting(JoinMeetingRequest request, String participantEmail) {
        User participant = userRepository.findByEmail(participantEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Meeting meeting = meetingRepository.findByCode(request.getCode())
                .orElseThrow(() -> new RuntimeException(MSG_MEETING_NOT_FOUND));

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

    // Sin @Transactional para evitar self-invocation incompatible (Sonar S2229)
    public Meeting attemptJoinMeeting(Meeting meeting, User participant, String participantEmail) {
        try {
            // Double-check if user is already in meeting before adding
            if (meeting.getParticipants().stream().anyMatch(p -> p.getEmail().equals(participantEmail))) {
                broadcastJoinEvent(meeting, participantEmail);
                return meeting;
            }
            
            meeting.getParticipants().add(participant);
            meeting = meetingRepository.saveAndFlush(meeting);
            
            // Broadcast warroom.participants event via WebSocket
            broadcastJoinEvent(meeting, participantEmail);
            
            return meeting;
            
        } catch (Exception e) {
            // Handle race condition where user was added between check and save
            if (e.getMessage() != null && 
                (e.getMessage().contains("meeting_participants_pkey") || 
                 e.getMessage().contains("duplicate key value violates unique constraint"))) {
                logger.warn("Race condition detected when adding participant {} to meeting {}: {}", 
                    participantEmail, meeting.getId(), e.getMessage());
                // Handle in separate transaction to avoid rollback issues
                return handleRaceCondition(meeting.getId(), participantEmail);
            } else {
                logger.error("Unexpected error joining meeting: {}", e.getMessage(), e);
                throw new RuntimeException("Error joining meeting: " + e.getMessage());
            }
        }
    }

    // Sin @Transactional para evitar self-invocation incompatible (Sonar S2229); operación sólo lectura
    public Meeting handleRaceCondition(Long meetingId, String participantEmail) {
        // Reload meeting and broadcast event in separate read-only transaction
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException(MSG_MEETING_NOT_FOUND + " after race condition"));
        
        logger.info("User {} already in meeting {}, broadcasting join event", participantEmail, meetingId);
        broadcastJoinEvent(meeting, participantEmail);
        return meeting;
    }
    
    private void broadcastJoinEvent(Meeting meeting, String participantEmail) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.participants");
        event.put(KEY_WAR_ROOM_ID, meeting.getId());
        event.put(KEY_CURRENT_PARTICIPANT_COUNT, meeting.getCurrentParticipantCount());
        event.put("action", "joined");
        event.put("userEmail", participantEmail);
        
        socketHandler.broadcastObject(event);
    }
    
    @Override
    @Transactional
    public Meeting leaveMeeting(Long meetingId, String participantEmail) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException(MSG_MEETING_NOT_FOUND));

        // Remove participant by email to ensure correct removal from ManyToMany relationship
        boolean removed = meeting.getParticipants().removeIf(p -> p.getEmail().equals(participantEmail));
        
        if (!removed) {
            return meeting;
        }
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Broadcast warroom.participants event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.participants");
        event.put(KEY_WAR_ROOM_ID, savedMeeting.getId());
        event.put(KEY_CURRENT_PARTICIPANT_COUNT, savedMeeting.getCurrentParticipantCount());
        event.put("action", "left");
        event.put("userEmail", participantEmail);
        
        socketHandler.broadcastObject(event);
        
        return savedMeeting;
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException(MSG_MEETING_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeetingByCode(String code) {
        return meetingRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException(MSG_MEETING_NOT_FOUND));
    }



    /**
     * Calcula la duración actual de una reunión activa en segundos para tiempo real
     */
    @Override
    public long getCurrentMeetingDurationSeconds(Meeting meeting) {
        if (meeting.getStatus().equals(STATUS_ACTIVE) && meeting.getStartTime() != null) {
            return java.time.Duration.between(meeting.getStartTime(), LocalDateTime.now(java.time.ZoneOffset.UTC)).getSeconds();
        }
        Long duration = meeting.getDurationSeconds();
        return duration != null ? duration : 0L;
    }

    /**
     * Envía actualización de duración en tiempo real via WebSocket
     */
    @Override
    public void broadcastDurationUpdate(Long meetingId) {
        try {
            Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
            if (meeting != null && meeting.getStatus().equals(STATUS_ACTIVE)) {
                long durationSeconds = getCurrentMeetingDurationSeconds(meeting);
                
                Map<String, Object> event = new HashMap<>();
                event.put("type", "warroom.duration.update");
                event.put(KEY_WAR_ROOM_ID, meetingId);
                event.put(KEY_DURATION_SECONDS, durationSeconds);
                event.put("durationMinutes", durationSeconds / 60);
                event.put("timestamp", LocalDateTime.now(java.time.ZoneOffset.UTC).toString());
                
                socketHandler.broadcastObject(event);
                logger.debug("Broadcasted duration update for meeting {}: {} seconds", meetingId, durationSeconds);
            }
        } catch (Exception e) {
            logger.error("Error broadcasting duration update for meeting {}: {}", meetingId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public Meeting markIncidentAsResolved(Long meetingId, String adminEmail) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException(MSG_MEETING_NOT_FOUND));

        // Verify that the user is the creator (admin)
        if (!meeting.getCreator().getEmail().equals(adminEmail)) {
            throw new RuntimeException("Only the meeting creator can mark incident as resolved");
        }

        // Calculate duration and end meeting
        LocalDateTime endTime = LocalDateTime.now(java.time.ZoneOffset.UTC);
        long durationSeconds = java.time.Duration.between(meeting.getStartTime(), endTime).getSeconds();
        
        meeting.setStatus("ENDED");
        meeting.setEndTime(endTime);
        meeting.setDurationSeconds(durationSeconds);
        
        Meeting savedMeeting = meetingRepository.save(meeting);

        // Broadcast meeting ended event via WebSocket
        Map<String, Object> event = new HashMap<>();
        event.put("type", "warroom.resolved");
        event.put(KEY_WAR_ROOM_ID, savedMeeting.getId());
        event.put("resolvedAt", savedMeeting.getEndTime().toString());
        event.put(KEY_DURATION_SECONDS, savedMeeting.getDurationSeconds());
        
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