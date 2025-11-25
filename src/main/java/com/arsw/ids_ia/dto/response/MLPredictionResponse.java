package com.arsw.ids_ia.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response del modelo ML de AWS Lambda.
 * Contiene la predicción, probabilidades y clasificación del ataque.
 */
public class MLPredictionResponse implements com.arsw.ids_ia.dto.MLFields {

    private String prediction;
    private Map<String, Double> probabilities;
    private String probabilitiesStr;
    private String state;
    
    @JsonProperty("attack_probability")
    private Double attackProbability;
    
    private String category;
    
    @JsonProperty("standard_protocol")
    private String standardProtocol;

    public MLPredictionResponse() {
    }


    // MLFields interface methods
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
    public String getProbabilities() { return probabilitiesStr; }
    @Override
    public void setProbabilities(String probabilities) { this.probabilitiesStr = probabilities; }

    // Unique methods
    public Map<String, Double> getProbabilitiesMap() {
        return probabilities;
    }
    public void setProbabilitiesMap(Map<String, Double> probabilities) {
        this.probabilities = probabilities;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}

