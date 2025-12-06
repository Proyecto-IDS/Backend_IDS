package com.arsw.ids_ia.controller.chat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.arsw.ids_ia.dto.chat.WarRoomMessageRequest;
import com.arsw.ids_ia.dto.chat.WarRoomMessageResponse;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.chat.WarRoomMessage;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.chat.WarRoomMessageService;
import com.arsw.ids_ia.ws.WarRoomChatSocketHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarRoomMessageController - Unit Tests")
class WarRoomMessageControllerTest {
    @Mock
    private WarRoomMessageService messageService;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WarRoomChatSocketHandler chatSocketHandler;
    @InjectMocks
    private WarRoomMessageController controller;

    private Meeting meeting;
    private User user;
    private WarRoomMessage message;

    @BeforeEach
    void setUp() {
        meeting = new Meeting();
        meeting.setId(1L);
        user = new User();
        user.setId(2L);
        user.setEmail("test@user.com");
        user.setName("Test User");
        message = WarRoomMessage.builder()
                .id(10L)
                .meeting(meeting)
                .sender(user)
                .content("Hello")
                .role("user")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getMessages_returnsList() {
        when(messageService.getMessagesByMeetingId(1L)).thenReturn(Arrays.asList(message));
        ResponseEntity<List<WarRoomMessageResponse>> response = controller.getMessages(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Hello", response.getBody().get(0).getContent());
    }

    @Test
    void sendMessage_persistsAndReturns() {
        WarRoomMessageRequest req = new WarRoomMessageRequest();
        req.setMeetingId(1L);
        req.setContent("Hi!");
        req.setRole("user");
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@user.com");
        when(userRepository.findByEmail("test@user.com")).thenReturn(Optional.of(user));
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(messageService.saveMessage(any())).thenReturn(message);
        // Avoid side effects from websocket broadcasting in unit test
        // No need to stub return since method is void, just ensure mock exists
        ResponseEntity<WarRoomMessageResponse> response = controller.sendMessage(req, jwt);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Hello", response.getBody().getContent());
        assertEquals("Test User", response.getBody().getSenderName());
    }

    @Test
    void sendMessage_returnsBadRequestIfUserOrMeetingMissing() {
        WarRoomMessageRequest req = new WarRoomMessageRequest();
        req.setMeetingId(1L);
        req.setContent("Hi!");
        req.setRole("user");
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("notfound@user.com");
        when(userRepository.findByEmail("notfound@user.com")).thenReturn(Optional.empty());
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<WarRoomMessageResponse> response = controller.sendMessage(req, jwt);
        assertEquals(400, response.getStatusCodeValue());
    }
}
