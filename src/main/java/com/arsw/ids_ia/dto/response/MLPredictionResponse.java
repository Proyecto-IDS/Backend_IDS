package com.arsw.ids_ia.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Response del modelo ML de AWS Lambda.
 * Contiene la predicción, probabilidades y clasificación del ataque.
 */
public class MLPredictionResponse extends com.arsw.ids_ia.dto.MLFieldsBase {

    @JsonProperty("probabilities")
    private Map<String, Double> probabilitiesMap;

    private String state;

    public MLPredictionResponse() {
        // Empty constructor required for serialization/deserialization
    }


    // Los métodos MLFields se heredan de MLFieldsBase

    // Métodos únicos
    public Map<String, Double> getProbabilitiesMap() {
        return probabilitiesMap;
    }
    
    @JsonProperty("probabilities")
    public void setProbabilitiesMap(Map<String, Double> probabilities) {
        this.probabilitiesMap = probabilities;
        // Also set the String version for Alert storage
        if (probabilities != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.probabilities = mapper.writeValueAsString(probabilities);
            } catch (JsonProcessingException e) {
                this.probabilities = probabilities.toString();
            }
        }
    }
    
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}

