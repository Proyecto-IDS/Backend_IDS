package com.arsw.ids_ia.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.arsw.ids_ia.service.AlertService;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public List<Alert> list(@RequestParam(value = "limit", required = false, defaultValue = "1000") int limit) {
        return service.recent(limit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> get(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-incident/{incidentId}")
    public ResponseEntity<Alert> getByIncidentId(@PathVariable String incidentId) {
        return service.getByIncidentId(incidentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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