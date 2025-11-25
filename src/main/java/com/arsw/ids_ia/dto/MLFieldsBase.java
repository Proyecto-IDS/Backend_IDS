package com.arsw.ids_ia.dto;

/**
 * Clase base abstracta para campos ML comunes.
 */
public abstract class MLFieldsBase implements MLFields {
    protected String prediction;
    protected Double attackProbability;
    protected String category;
    protected String standardProtocol;
    protected String probabilities;

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
