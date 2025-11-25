package com.arsw.ids_ia.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response del modelo ML de AWS Lambda.
 * Contiene la predicción, probabilidades y clasificación del ataque.
 */
public class MLPredictionResponse extends com.arsw.ids_ia.dto.MLFieldsBase {

    private Map<String, Double> probabilitiesMap;
    private String probabilitiesStr;
    private String state;

    public MLPredictionResponse() {
    }


    // Los métodos MLFields se heredan de MLFieldsBase

    // Métodos únicos
    public Map<String, Double> getProbabilitiesMap() {
        return probabilitiesMap;
    }
    public void setProbabilitiesMap(Map<String, Double> probabilities) {
        this.probabilitiesMap = probabilities;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}

