package com.arsw.ids_ia.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.repository.AlertRepository;
import com.arsw.ids_ia.ws.TrafficSocketHandler;

@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository repository;
    private final TrafficSocketHandler socketHandler;

    @Autowired
    public AlertService(AlertRepository repository, @Autowired(required = false) TrafficSocketHandler socketHandler) {
        this.repository = repository;
        this.socketHandler = socketHandler;
    }

    public Alert create(Alert alert) {
        if (alert.getTimestamp() == null) {
            alert.setTimestamp(Instant.now());
        }
        
        // Check for duplicates: same packetId + incidentId + severity
        Optional<Alert> existing = repository.findDuplicate(alert.getPacketId(), alert.getIncidentId(), alert.getSeverity());
        if (existing.isPresent()) {
            logger.warn("Duplicate alert detected: packetId={} incidentId={} severity={} - Using existing alert", 
                alert.getPacketId(), alert.getIncidentId(), alert.getSeverity());
            return existing.get(); // Return existing alert instead of creating duplicate
        }
        
        Alert saved = repository.save(alert);
        logger.info("Alert created id={} packetId={} incidentId={} severity={}", saved.getId(), saved.getPacketId(), saved.getIncidentId(), saved.getSeverity());
        // Broadcast to connected websocket clients
        if (socketHandler != null) {
            try {
                Map<String, Object> alertMap = new HashMap<>();
                alertMap.put("id", saved.getId());
                alertMap.put("packetId", saved.getPacketId());
                alertMap.put("incidentId", saved.getIncidentId());
                alertMap.put("severity", saved.getSeverity());
                alertMap.put("score", saved.getScore());
                alertMap.put("modelVersion", saved.getModelVersion());
                // include snake_case variant used by some frontend code
                alertMap.put("model_version", saved.getModelVersion());
                alertMap.put("timestamp", saved.getTimestamp());

                Map<String, Object> outer = new HashMap<>();
                outer.put("type", "alert");
                outer.put("alert", alertMap);
                logger.info("Broadcasting alert to websocket sessions: {} sessions present", socketHandler == null ? 0 : "(unknown)");
                socketHandler.broadcastObject(outer);
            } catch (Exception ex) {
                logger.warn("Failed to broadcast alert via websocket: {}", ex.getMessage());
                // best-effort - don't break persistence on WS errors
            }
        }
        return saved;
    }

    public Optional<Alert> getById(Long id) {
        return repository.findById(id);
    }

    public Optional<Alert> getByIncidentId(String incidentId) {
        return repository.findLatestByIncidentId(incidentId);
    }

    public List<Alert> recent(int limit) {
        if (limit <= 0) limit = 10;
        return repository.findActiveAlertsOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    public List<Alert> today() {
        Instant startOfDay = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<Alert> allAlerts = repository.findAllByOrderByTimestampDesc(PageRequest.of(0, Integer.MAX_VALUE));
        return allAlerts.stream()
                .filter(alert -> alert.getTimestamp() != null && alert.getTimestamp().isAfter(startOfDay))
                .toList();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }

    public List<Alert> getResolvedIncidents() {
        return repository.findResolvedIncidents();
    }
}
