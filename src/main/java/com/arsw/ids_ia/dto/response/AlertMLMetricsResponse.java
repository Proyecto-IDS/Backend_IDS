package com.arsw.ids_ia.dto.response;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para respuesta de métricas ML de una alerta.
 * Incluye predicción, probabilidades y estado del modelo de Machine Learning.
 */
public class AlertMLMetricsResponse {
    
    private Long alertId;
    
    private String prediction;
    
    @JsonProperty("attack_probability")
    private Double attackProbability;
    
    private String state;
    
    private String category;
    
    @JsonProperty("standard_protocol")
    private String standardProtocol;
    
    private Map<String, Double> probabilities;
    
    /**
     * Constructor vacío requerido para deserialización JSON.
     */
    public AlertMLMetricsResponse() {
    }
    
    /**
     * Constructor completo.
     * 
     * @param alertId ID de la alerta
     * @param prediction Predicción del modelo (normal, ataque, etc.)
     * @param attackProbability Probabilidad de ataque (0.0 - 1.0)
     * @param state Estado del modelo (FALSO_POSITIVO, etc.)
     * @param category Categoría del ataque
     * @param standardProtocol Descripción del protocolo estándar
     * @param probabilities Mapa de probabilidades por tipo de ataque
     */
    public AlertMLMetricsResponse(Long alertId, String prediction, 
                                  Double attackProbability, String state,
                                  String category, String standardProtocol,
                                  Map<String, Double> probabilities) {
        this.alertId = alertId;
        this.prediction = prediction;
        this.attackProbability = attackProbability;
        this.state = state;
        this.category = category;
        this.standardProtocol = standardProtocol;
        this.probabilities = probabilities;
    }
    
    // Getters y Setters
    
    public Long getAlertId() {
        return alertId;
    }
    
    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }
    
    public String getPrediction() {
        return prediction;
    }
    
    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }
    
    @JsonProperty("attack_probability")
    public Double getAttackProbability() {
        return attackProbability;
    }
    
    @JsonProperty("attack_probability")
    public void setAttackProbability(Double attackProbability) {
        this.attackProbability = attackProbability;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    @JsonProperty("standard_protocol")
    public String getStandardProtocol() {
        return standardProtocol;
    }
    
    @JsonProperty("standard_protocol")
    public void setStandardProtocol(String standardProtocol) {
        this.standardProtocol = standardProtocol;
    }
    
    public Map<String, Double> getProbabilities() {
        return probabilities;
    }
    
    public void setProbabilities(Map<String, Double> probabilities) {
        this.probabilities = probabilities;
    }
}
