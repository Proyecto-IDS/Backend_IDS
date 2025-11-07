package com.arsw.ids_ia.controller;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.service.AlertService;
import com.arsw.ids_ia.service.MeetingService;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;
    private final MeetingService meetingService;

    public AlertController(AlertService service, MeetingService meetingService) {
        this.service = service;
        this.meetingService = meetingService;
    }

    @GetMapping
    public List<Alert> list(@RequestParam(value = "limit", required = false, defaultValue = "1000") int limit) {
        return service.recent(limit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        Optional<Alert> alertOpt = service.getById(id);
        if (alertOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Alert alert = alertOpt.get();
        Map<String, Object> response = createAlertResponse(alert);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-incident/{incidentId}")
    public ResponseEntity<Map<String, Object>> getByIncidentId(@PathVariable String incidentId) {
        Optional<Alert> alertOpt = service.getByIncidentId(incidentId);
        if (alertOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Alert alert = alertOpt.get();
        Map<String, Object> response = createAlertResponse(alert);
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createAlertResponse(Alert alert) {
        Map<String, Object> response = new HashMap<>();
        
        // Basic alert information
        response.put("id", alert.getId());
        response.put("incidentId", alert.getIncidentId());
        response.put("packetId", alert.getPacketId());
        response.put("severity", alert.getSeverity());
        response.put("score", alert.getScore());
        response.put("modelVersion", alert.getModelVersion());
        response.put("timestamp", alert.getTimestamp());
        response.put("createdAt", alert.getTimestamp());
        response.put("updatedAt", alert.getTimestamp());
        response.put("warRoomId", alert.getWarRoomId());
        
        // Mock data for now - these would come from a proper incident management system
        response.put("type", "Incidente INC-2024-001");
        response.put("source", "PKT-001");
        response.put("status", alert.getWarRoomId() != null ? "contenido" : "no-conocido");
        response.put("relatedAssets", List.of());
        response.put("notes", "Agrega notas para el equipo de respuesta.");
        response.put("timeline", List.of());
        response.put("aiSummary", "El backend proporcionará un resumen con hallazgos de la IA.");
        
        // Add meeting information if warRoomId exists
        if (alert.getWarRoomId() != null) {
            try {
                Meeting meeting = meetingService.getMeetingById(alert.getWarRoomId());
                if (meeting != null) {
                    response.put("warRoomCode", meeting.getCode());
                    response.put("warRoomStartTime", meeting.getStartTime() != null ? 
                        meeting.getStartTime().atZone(ZoneOffset.UTC).toInstant().toString() : null);
                    response.put("warRoomDuration", meeting.getDurationSeconds());
                    
                    // Update status based on meeting status
                    if ("ENDED".equals(meeting.getStatus()) || "RESOLVED".equals(meeting.getStatus())) {
                        response.put("status", "contenido");
                    }
                }
            } catch (Exception e) {
                // If meeting not found or error, continue without meeting data
            }
        }
        
        return response;
    }

    @PostMapping
    public ResponseEntity<Alert> create(@Validated @RequestBody Alert alert) {
        Alert created = service.create(alert);
        return ResponseEntity.created(URI.create("/api/alerts/" + created.getId())).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", service.count());
    }

    @GetMapping("/count/by-severity")
    public Map<String, Long> countBySeverity() {
        Map<String, Long> counts = new HashMap<>();
        List<Alert> allAlerts = service.recent(Integer.MAX_VALUE);
        counts.put("total", (long) allAlerts.size());
        counts.put("critical", allAlerts.stream().filter(a -> "critical".equalsIgnoreCase(a.getSeverity())).count());
        counts.put("high", allAlerts.stream().filter(a -> "high".equalsIgnoreCase(a.getSeverity())).count());
        counts.put("medium", allAlerts.stream().filter(a -> "medium".equalsIgnoreCase(a.getSeverity())).count());
        counts.put("low", allAlerts.stream().filter(a -> "low".equalsIgnoreCase(a.getSeverity())).count());
        return counts;
    }

    @GetMapping("/today")
    public List<Alert> today() {
        return service.today();
    }

    @GetMapping("/today/count")
    public Map<String, Long> todayCount() {
        List<Alert> todayAlerts = service.today();
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", (long) todayAlerts.size());
        counts.put("critical", todayAlerts.stream().filter(a -> "critical".equalsIgnoreCase(a.getSeverity())).count());
        counts.put("high", todayAlerts.stream().filter(a -> "high".equalsIgnoreCase(a.getSeverity())).count());
        return counts;
    }

    @GetMapping("/resolved")
    public List<Alert> getResolvedIncidents() {
        return service.getResolvedIncidents();
    }
}