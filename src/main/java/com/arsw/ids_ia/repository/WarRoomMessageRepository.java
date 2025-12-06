package com.arsw.ids_ia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arsw.ids_ia.model.chat.WarRoomMessage;

@Repository
public interface WarRoomMessageRepository extends JpaRepository<WarRoomMessage, Long> {
    List<WarRoomMessage> findByMeetingIdOrderByCreatedAtAsc(Long meetingId);
}
