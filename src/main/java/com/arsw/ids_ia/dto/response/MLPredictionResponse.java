package com.arsw.ids_ia.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response del modelo ML de AWS Lambda.
 * Contiene la predicción, probabilidades y clasificación del ataque.
 */
public class MLPredictionResponse {

    private String prediction;
    private Map<String, Double> probabilities;
    private String state;
    
    @JsonProperty("attack_probability")
    private Double attackProbability;
    
    private String category;
    
    @JsonProperty("standard_protocol")
    private String standardProtocol;

    public MLPredictionResponse() {
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Map<String, Double> probabilities) {
        this.probabilities = probabilities;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getAttackProbability() {
        return attackProbability;
    }

    public void setAttackProbability(Double attackProbability) {
        this.attackProbability = attackProbability;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStandardProtocol() {
        return standardProtocol;
    }

    public void setStandardProtocol(String standardProtocol) {
        this.standardProtocol = standardProtocol;
    }
}
