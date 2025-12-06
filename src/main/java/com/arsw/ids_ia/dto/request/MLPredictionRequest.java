package com.arsw.ids_ia.dto.request;

/**
 * Request para el endpoint de predicción del modelo ML.
 * Envuelve las features en el formato esperado por AWS Lambda.
 */
public class MLPredictionRequest {
    
    private NetworkTrafficFeatures features;

    public MLPredictionRequest() {
    }

    public MLPredictionRequest(NetworkTrafficFeatures features) {
        this.features = features;
    }

    public NetworkTrafficFeatures getFeatures() {
        return features;
    }

    public void setFeatures(NetworkTrafficFeatures features) {
        this.features = features;
    }
}
