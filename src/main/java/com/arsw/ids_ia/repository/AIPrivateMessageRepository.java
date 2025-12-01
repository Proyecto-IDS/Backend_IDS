package com.arsw.ids_ia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.arsw.ids_ia.model.chat.AIPrivateMessage;

public interface AIPrivateMessageRepository extends JpaRepository<AIPrivateMessage, Long> {
    
    @Query("SELECT m FROM AIPrivateMessage m WHERE m.meeting.id = :meetingId AND m.user.id = :userId ORDER BY m.createdAt ASC")
    List<AIPrivateMessage> findByMeetingIdAndUserIdOrderByCreatedAtAsc(@Param("meetingId") Long meetingId, @Param("userId") Long userId);
    
    @Query("SELECT m FROM AIPrivateMessage m WHERE m.meeting.id = :meetingId ORDER BY m.createdAt ASC")
    List<AIPrivateMessage> findByMeetingIdOrderByCreatedAtAsc(@Param("meetingId") Long meetingId);
}