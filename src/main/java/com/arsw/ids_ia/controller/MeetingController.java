package com.arsw.ids_ia.controller;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Meeting meeting = meetingService.createMeeting(request, userDetails.getUsername());
        MeetingResponse response = new MeetingResponse(
            meeting.getId(),
            meeting.getCode(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getStartTime().toString(),
            meeting.getEndTime().toString(),
            meeting.getCreator().getEmail(),
            meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet())
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/join")
    public ResponseEntity<MeetingResponse> joinMeeting(
            @Valid @RequestBody JoinMeetingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Meeting meeting = meetingService.joinMeeting(request, userDetails.getUsername());
        MeetingResponse response = new MeetingResponse(
            meeting.getId(),
            meeting.getCode(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getStartTime().toString(),
            meeting.getEndTime().toString(),
            meeting.getCreator().getEmail(),
            meeting.getParticipants().stream().map(User::getEmail).collect(Collectors.toSet())
        );
        return ResponseEntity.ok(response);
    }
}