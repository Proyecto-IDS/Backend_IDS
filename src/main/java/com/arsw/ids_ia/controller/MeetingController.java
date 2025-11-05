package com.arsw.ids_ia.controller;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.dto.request.CreateMeetingRequest;
import com.arsw.ids_ia.dto.request.JoinMeetingRequest;
import com.arsw.ids_ia.dto.response.MeetingResponse;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.service.MeetingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim("email");
        Meeting meeting = meetingService.createMeeting(request, email);
        MeetingResponse response = new MeetingResponse(
            meeting.getId(),
            meeting.getCode(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getStartTime().toString(),
            meeting.getEndTime().toString(),
            meeting.getCreator().getEmail(),
            meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet()),
            meeting.getCurrentParticipantCount(),
            meeting.getMaxParticipants(),
            meeting.getStatus()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/join")
    public ResponseEntity<MeetingResponse> joinMeeting(
            @Valid @RequestBody JoinMeetingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim("email");
        try {
            Meeting meeting = meetingService.joinMeeting(request, email);
            MeetingResponse response = new MeetingResponse(
                meeting.getId(),
                meeting.getCode(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getStartTime().toString(),
                meeting.getEndTime().toString(),
                meeting.getCreator().getEmail(),
                meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet()),
                meeting.getCurrentParticipantCount(),
                meeting.getMaxParticipants(),
                meeting.getStatus()
            );
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // User is already in the meeting (duplicate key constraint), just return the meeting
            if (e.getMessage() != null && e.getMessage().contains("meeting_participants_pkey")) {
                // Re-fetch the meeting to get current state
                Meeting meeting = meetingService.getMeetingByCode(request.getCode());
                MeetingResponse response = new MeetingResponse(
                    meeting.getId(),
                    meeting.getCode(),
                    meeting.getTitle(),
                    meeting.getDescription(),
                    meeting.getStartTime().toString(),
                    meeting.getEndTime().toString(),
                    meeting.getCreator().getEmail(),
                    meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet()),
                    meeting.getCurrentParticipantCount(),
                    meeting.getMaxParticipants(),
                    meeting.getStatus()
                );
                return ResponseEntity.ok(response);
            }
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.getMeetingById(meetingId);
        MeetingResponse response = new MeetingResponse(
            meeting.getId(),
            meeting.getCode(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getStartTime().toString(),
            meeting.getEndTime().toString(),
            meeting.getCreator().getEmail(),
            meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet()),
            meeting.getCurrentParticipantCount(),
            meeting.getMaxParticipants(),
            meeting.getStatus()
        );
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<MeetingResponse> leaveMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim("email");
        Meeting meeting = meetingService.leaveMeeting(meetingId, email);
        MeetingResponse response = new MeetingResponse(
            meeting.getId(),
            meeting.getCode(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getStartTime().toString(),
            meeting.getEndTime().toString(),
            meeting.getCreator().getEmail(),
            meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet()),
            meeting.getCurrentParticipantCount(),
            meeting.getMaxParticipants(),
            meeting.getStatus()
        );
        return ResponseEntity.ok(response);
    }
}