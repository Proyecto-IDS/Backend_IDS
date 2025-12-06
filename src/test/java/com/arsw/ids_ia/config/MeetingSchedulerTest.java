package com.arsw.ids_ia.config;

import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingSchedulerTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingService meetingService;

    @InjectMocks
    private MeetingScheduler meetingScheduler;

    private Meeting activeMeeting1;
    private Meeting activeMeeting2;

    @BeforeEach
    void setUp() {
        activeMeeting1 = Meeting.builder()
                .id(1L)
                .code("MEET1")
                .status("ACTIVE")
                .build();

        activeMeeting2 = Meeting.builder()
                .id(2L)
                .code("MEET2")
                .status("ACTIVE")
                .build();
    }

    @Test
    void testBroadcastActiveMeetingDurationsWithMultipleMeetings() {
        // Arrange
        List<Meeting> activeMeetings = Arrays.asList(activeMeeting1, activeMeeting2);
        when(meetingRepository.findByStatus("ACTIVE")).thenReturn(activeMeetings);
        doNothing().when(meetingService).broadcastDurationUpdate(anyLong());

        // Act
        meetingScheduler.broadcastActiveMeetingDurations();

        // Assert
        verify(meetingRepository).findByStatus("ACTIVE");
        verify(meetingService).broadcastDurationUpdate(1L);
        verify(meetingService).broadcastDurationUpdate(2L);
    }

    @Test
    void testBroadcastActiveMeetingDurationsWithNoMeetings() {
        // Arrange
        when(meetingRepository.findByStatus("ACTIVE")).thenReturn(Collections.emptyList());

        // Act
        meetingScheduler.broadcastActiveMeetingDurations();

        // Assert
        verify(meetingRepository).findByStatus("ACTIVE");
        verify(meetingService, never()).broadcastDurationUpdate(anyLong());
    }

    @Test
    void testBroadcastActiveMeetingDurationsHandlesException() {
        // Arrange
        when(meetingRepository.findByStatus("ACTIVE")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception, should log error
        meetingScheduler.broadcastActiveMeetingDurations();

        verify(meetingRepository).findByStatus("ACTIVE");
        verify(meetingService, never()).broadcastDurationUpdate(anyLong());
    }

    @Test
    void testBroadcastActiveMeetingDurationsWithServiceException() {
        // Arrange
        List<Meeting> activeMeetings = Arrays.asList(activeMeeting1);
        when(meetingRepository.findByStatus("ACTIVE")).thenReturn(activeMeetings);
        doThrow(new RuntimeException("Broadcast error")).when(meetingService).broadcastDurationUpdate(1L);

        // Act & Assert - Should handle exception gracefully
        meetingScheduler.broadcastActiveMeetingDurations();

        verify(meetingRepository).findByStatus("ACTIVE");
        verify(meetingService).broadcastDurationUpdate(1L);
    }
}
