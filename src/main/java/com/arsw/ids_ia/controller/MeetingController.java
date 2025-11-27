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

    private static final String CLAIM_EMAIL = "email";
    private final MeetingService meetingService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim(CLAIM_EMAIL);
        Meeting meeting = meetingService.createMeeting(request, email);
        MeetingResponse response = createMeetingResponse(meeting);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/join")
    public ResponseEntity<MeetingResponse> joinMeeting(
            @Valid @RequestBody JoinMeetingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim(CLAIM_EMAIL);
        Meeting meeting = meetingService.joinMeeting(request, email);
        MeetingResponse response = createMeetingResponse(meeting);
        return ResponseEntity.ok(response);
    }
    
    private MeetingResponse createMeetingResponse(Meeting meeting) {
        return new MeetingResponse(
            meeting.getId(),
            meeting.getCode(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getStartTime() != null ? meeting.getStartTime().atZone(java.time.ZoneOffset.UTC).toInstant().toString() : null,
            meeting.getEndTime() != null ? meeting.getEndTime().atZone(java.time.ZoneOffset.UTC).toInstant().toString() : null,
            meeting.getCreator().getEmail(),
            meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet()),
            meeting.getCurrentParticipantCount(),
            meeting.getDurationSeconds(),
            meeting.getStatus()
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.getMeetingById(meetingId);
        MeetingResponse response = createMeetingResponse(meeting);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<MeetingResponse> leaveMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim(CLAIM_EMAIL);
        Meeting meeting = meetingService.leaveMeeting(meetingId, email);
        MeetingResponse response = createMeetingResponse(meeting);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{meetingId}/mark-as-resolved")
    public ResponseEntity<MeetingResponse> markIncidentAsResolved(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getClaim(CLAIM_EMAIL);
        Meeting meeting = meetingService.markIncidentAsResolved(meetingId, email);
        MeetingResponse response = createMeetingResponse(meeting);
        return ResponseEntity.ok(response);
    }


}