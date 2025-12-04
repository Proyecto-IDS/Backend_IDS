package com.arsw.ids_ia.service.chat.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arsw.ids_ia.model.chat.AIPrivateMessage;
import com.arsw.ids_ia.repository.AIPrivateMessageRepository;
import com.arsw.ids_ia.service.chat.AIPrivateChatService;

@Service
@Transactional
public class AIPrivateChatServiceImpl implements AIPrivateChatService {

    @Autowired
    private AIPrivateMessageRepository messageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AIPrivateMessage> getMessagesByMeetingAndUser(Long meetingId, Long userId) {
        return messageRepository.findByMeetingIdAndUserIdOrderByCreatedAtAsc(meetingId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIPrivateMessage> getAdminMessagesByMeeting(Long meetingId) {
        // Temporarily return empty list until we fix the repository issue
        return new java.util.ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIPrivateMessage> getAllMessagesByMeeting(Long meetingId) {
        return messageRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId);
    }

    @Override
    public AIPrivateMessage saveMessage(AIPrivateMessage message) {
        return messageRepository.save(message);
    }
}