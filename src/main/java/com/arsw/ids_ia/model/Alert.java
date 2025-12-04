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
public class Alert extends com.arsw.ids_ia.dto.MLFieldsBase {
    public static class Builder {
        private String packetId;
        private String incidentId;
        private String severity;
        private Double score;
        private String modelVersion = "v1.0";
        private Instant timestamp = Instant.now();
        private Long warRoomId;
        private Double attackProbability;
        private String prediction;
        private String category;
        private String standardProtocol;
        private String probabilities;

        public Builder packetId(String packetId) { this.packetId = packetId; return this; }
        public Builder incidentId(String incidentId) { this.incidentId = incidentId; return this; }
        public Builder severity(String severity) { this.severity = severity; return this; }
        public Builder score(Double score) { this.score = score; return this; }
        public Builder modelVersion(String modelVersion) { this.modelVersion = modelVersion; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder warRoomId(Long warRoomId) { this.warRoomId = warRoomId; return this; }
        public Builder attackProbability(Double attackProbability) { this.attackProbability = attackProbability; return this; }
        public Builder prediction(String prediction) { this.prediction = prediction; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder standardProtocol(String standardProtocol) { this.standardProtocol = standardProtocol; return this; }
        public Builder probabilities(String probabilities) { this.probabilities = probabilities; return this; }

        public Alert build() {
            return new Alert(this);
        }
    }

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

    // Campos del modelo ML (heredados de MLFieldsBase)
    // Las anotaciones @Column están en MLFieldsBase

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

    public Alert(Builder builder) {
        this.packetId = builder.packetId;
        this.incidentId = builder.incidentId;
        this.severity = builder.severity;
        this.score = builder.score;
        this.modelVersion = builder.modelVersion;
        this.timestamp = builder.timestamp;
        this.warRoomId = builder.warRoomId;
        this.attackProbability = builder.attackProbability;
        this.prediction = builder.prediction;
        this.category = builder.category;
        this.standardProtocol = builder.standardProtocol;
        this.probabilities = builder.probabilities;
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

    // Los métodos MLFields se heredan de MLFieldsBase
}
