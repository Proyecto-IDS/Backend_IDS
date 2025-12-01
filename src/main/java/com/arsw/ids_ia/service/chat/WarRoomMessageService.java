package com.arsw.ids_ia.service.chat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arsw.ids_ia.model.chat.WarRoomMessage;
import com.arsw.ids_ia.repository.WarRoomMessageRepository;

@Service
public class WarRoomMessageService {
    @Autowired
    private WarRoomMessageRepository messageRepository;

    public List<WarRoomMessage> getMessagesByMeetingId(Long meetingId) {
        return messageRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId);
    }

    public WarRoomMessage saveMessage(WarRoomMessage message) {
        return messageRepository.save(message);
    }
}
