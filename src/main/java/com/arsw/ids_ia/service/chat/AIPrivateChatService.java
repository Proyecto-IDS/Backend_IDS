package com.arsw.ids_ia.service.chat;

import java.util.List;

import com.arsw.ids_ia.model.chat.AIPrivateMessage;

public interface AIPrivateChatService {
    List<AIPrivateMessage> getMessagesByMeetingAndUser(Long meetingId, Long userId);
    List<AIPrivateMessage> getAdminMessagesByMeeting(Long meetingId);
    List<AIPrivateMessage> getAllMessagesByMeeting(Long meetingId);
    AIPrivateMessage saveMessage(AIPrivateMessage message);
}