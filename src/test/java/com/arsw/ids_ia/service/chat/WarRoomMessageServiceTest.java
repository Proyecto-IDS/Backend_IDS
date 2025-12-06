package com.arsw.ids_ia.service.chat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.chat.WarRoomMessage;
import com.arsw.ids_ia.repository.WarRoomMessageRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarRoomMessageService - Unit Tests")
class WarRoomMessageServiceTest {
    @Mock
    private WarRoomMessageRepository repository;
    @InjectMocks
    private WarRoomMessageService service;

    private WarRoomMessage message;
    private Meeting meeting;
    private User user;

    @BeforeEach
    void setUp() {
        meeting = new Meeting();
        meeting.setId(1L);
        user = new User();
        user.setId(2L);
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
    void getMessagesByMeetingId_returnsList() {
        when(repository.findByMeetingIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(message));
        List<WarRoomMessage> result = service.getMessagesByMeetingId(1L);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getContent());
    }

    @Test
    void saveMessage_persistsAndReturns() {
        when(repository.save(any())).thenReturn(message);
        WarRoomMessage result = service.saveMessage(message);
        assertNotNull(result);
        assertEquals("Hello", result.getContent());
    }
}
