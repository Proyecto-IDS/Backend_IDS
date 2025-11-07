package com.arsw.ids_ia.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.service.MeetingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MeetingScheduler.class);
    
    private final MeetingRepository meetingRepository;
    private final MeetingService meetingService;

    /**
     * Envía actualizaciones de duración cada 30 segundos para reuniones activas
     * Esto mantiene el cronómetro actualizado sin sobrecargar el WebSocket
     */
    @Scheduled(fixedRate = 30000) // Cada 30 segundos
    public void broadcastActiveMeetingDurations() {
        try {
            List<Meeting> activeMeetings = meetingRepository.findByStatus("ACTIVE");
            
            for (Meeting meeting : activeMeetings) {
                meetingService.broadcastDurationUpdate(meeting.getId());
            }
            
            if (!activeMeetings.isEmpty()) {
                logger.info("Broadcasted duration updates for {} active meetings", activeMeetings.size());
            }
            
        } catch (Exception e) {
            logger.error("Error in meeting duration scheduler: {}", e.getMessage());
        }
    }
}