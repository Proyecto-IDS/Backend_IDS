package com.arsw.ids_ia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase base abstracta para campos ML comunes.
 */
public abstract class MLFieldsBase implements MLFields {
    protected String prediction;
    
    @JsonProperty("attack_probability")
    protected Double attackProbability;
    
    protected String category;
    
    @JsonProperty("standard_protocol")
    protected String standardProtocol;
    
    protected String probabilities;

    @Override
    public String getPrediction() { return prediction; }
    @Override
    public void setPrediction(String prediction) { this.prediction = prediction; }
    
    @Override
    @JsonProperty("attack_probability")
    public Double getAttackProbability() { return attackProbability; }
    @Override
    @JsonProperty("attack_probability")
    public void setAttackProbability(Double attackProbability) { this.attackProbability = attackProbability; }
    
    @Override
    public String getCategory() { return category; }
    @Override
    public void setCategory(String category) { this.category = category; }
    
    @Override
    @JsonProperty("standard_protocol")
    public String getStandardProtocol() { return standardProtocol; }
    @Override
    @JsonProperty("standard_protocol")
    public void setStandardProtocol(String standardProtocol) { this.standardProtocol = standardProtocol; }
    
    @Override
    public String getProbabilities() { return probabilities; }
    @Override
    public void setProbabilities(String probabilities) { this.probabilities = probabilities; }
}
