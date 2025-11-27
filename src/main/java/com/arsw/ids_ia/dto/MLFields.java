package com.arsw.ids_ia.dto;

/**
 * Campos ML comunes para Alert y MLPredictionResponse
 */
public interface MLFields {
    String getPrediction();
    void setPrediction(String prediction);

    Double getAttackProbability();
    void setAttackProbability(Double attackProbability);

    String getCategory();
    void setCategory(String category);

    String getStandardProtocol();
    void setStandardProtocol(String standardProtocol);

    String getProbabilities();
    void setProbabilities(String probabilities);
}
