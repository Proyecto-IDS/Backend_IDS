package com.arsw.ids_ia.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alerts")
public class Alert implements com.arsw.ids_ia.dto.MLFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String packetId;
    private String incidentId;
    private String severity;
    private Double score;
    private String modelVersion;

    private Instant timestamp;
    
    @Column(name = "war_room_id")
    private Long warRoomId;

    // Campos del modelo ML
    private String prediction;
    private Double attackProbability;
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String standardProtocol;
    
    @Column(columnDefinition = "TEXT")
    private String probabilities; // JSON string con todas las probabilidades

    public Alert() {
    }

    public Alert(String packetId, String incidentId, String severity, Double score, String modelVersion, Instant timestamp) {
        this.packetId = packetId;
        this.incidentId = incidentId;
        this.severity = severity;
        this.score = score;
        this.modelVersion = modelVersion;
        this.timestamp = timestamp;
    }

    public Alert(String packetId, String incidentId, String severity, Double attackProbability, 
                 String prediction, String category, String standardProtocol, String probabilities) {
        this.packetId = packetId;
        this.incidentId = incidentId;
        this.severity = severity;
        this.score = attackProbability;
        this.attackProbability = attackProbability;
        this.prediction = prediction;
        this.category = category;
        this.standardProtocol = standardProtocol;
        this.probabilities = probabilities;
        this.timestamp = Instant.now();
        this.modelVersion = "v1.0";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Long getWarRoomId() {
        return warRoomId;
    }

    public void setWarRoomId(Long warRoomId) {
        this.warRoomId = warRoomId;
    }

    @Override
    public String getPrediction() { return prediction; }
    @Override
    public void setPrediction(String prediction) { this.prediction = prediction; }
    @Override
    public Double getAttackProbability() { return attackProbability; }
    @Override
    public void setAttackProbability(Double attackProbability) { this.attackProbability = attackProbability; }
    @Override
    public String getCategory() { return category; }
    @Override
    public void setCategory(String category) { this.category = category; }
    @Override
    public String getStandardProtocol() { return standardProtocol; }
    @Override
    public void setStandardProtocol(String standardProtocol) { this.standardProtocol = standardProtocol; }
    @Override
    public String getProbabilities() { return probabilities; }
    @Override
    public void setProbabilities(String probabilities) { this.probabilities = probabilities; }
}
