package com.arsw.ids_ia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.arsw.ids_ia.model.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByCode(String code);
    
    List<Meeting> findByStatus(String status);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Meeting m JOIN m.participants p WHERE m.id = :meetingId AND p.id = :userId")
    boolean isUserParticipant(@Param("meetingId") Long meetingId, @Param("userId") Long userId);
}