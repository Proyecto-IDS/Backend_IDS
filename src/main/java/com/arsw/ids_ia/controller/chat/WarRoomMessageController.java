package com.arsw.ids_ia.controller.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.dto.chat.WarRoomMessageRequest;
import com.arsw.ids_ia.dto.chat.WarRoomMessageResponse;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.chat.WarRoomMessage;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.chat.WarRoomMessageService;

@RestController
@RequestMapping("/api/warroom/messages")
public class WarRoomMessageController {
    @Autowired
    private WarRoomMessageService messageService;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<WarRoomMessageResponse>> getMessages(Long meetingId) {
        List<WarRoomMessage> messages = messageService.getMessagesByMeetingId(meetingId);
        List<WarRoomMessageResponse> response = messages.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WarRoomMessageResponse> sendMessage(@RequestBody WarRoomMessageRequest request, @AuthenticationPrincipal Jwt jwt) {
        User sender = userRepository.findByEmail(jwt.getClaim("email")).orElse(null);
        Meeting meeting = meetingRepository.findById(request.getMeetingId()).orElse(null);
        if (sender == null || meeting == null) {
            return ResponseEntity.badRequest().build();
        }
        WarRoomMessage message = WarRoomMessage.builder()
            .meeting(meeting)
            .sender(sender)
            .content(request.getContent())
            .role(request.getRole())
            .createdAt(java.time.LocalDateTime.now())
            .build();
        WarRoomMessage saved = messageService.saveMessage(message);
        return ResponseEntity.ok(toResponse(saved));
    }

    private WarRoomMessageResponse toResponse(WarRoomMessage message) {
        WarRoomMessageResponse resp = new WarRoomMessageResponse();
        resp.setId(message.getId());
        resp.setMeetingId(message.getMeeting().getId());
        resp.setSenderEmail(message.getSender().getEmail());
        resp.setSenderName(message.getSender().getName());
        resp.setContent(message.getContent());
        resp.setRole(message.getRole());
        resp.setCreatedAt(message.getCreatedAt());
        return resp;
    }
}
