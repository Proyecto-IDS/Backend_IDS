package com.arsw.ids_ia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arsw.ids_ia.model.Alert;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByOrderByTimestampDesc(Pageable pageable);
    
    @Query("SELECT a FROM Alert a WHERE a.packetId = :packetId AND a.incidentId = :incidentId AND a.severity = :severity")
    Optional<Alert> findDuplicate(@Param("packetId") String packetId, @Param("incidentId") String incidentId, @Param("severity") String severity);
    
    List<Alert> findByIncidentId(String incidentId);
    
    @Query("SELECT a FROM Alert a WHERE a.incidentId = :incidentId ORDER BY a.timestamp DESC LIMIT 1")
    Optional<Alert> findLatestByIncidentId(@Param("incidentId") String incidentId);
    
    @Query("SELECT a FROM Alert a JOIN Meeting m ON a.warRoomId = m.id WHERE m.status = 'ENDED' ORDER BY a.timestamp DESC")
    List<Alert> findResolvedIncidents();
    
    @Query("SELECT a FROM Alert a LEFT JOIN Meeting m ON a.warRoomId = m.id WHERE (m.id IS NULL OR m.status != 'ENDED') ORDER BY a.timestamp DESC")
    List<Alert> findActiveAlertsOrderByTimestampDesc(Pageable pageable);
}
