package com.arsw.ids_ia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.arsw.ids_ia.dto.request.MLPredictionRequest;
import com.arsw.ids_ia.dto.request.NetworkTrafficFeatures;
import com.arsw.ids_ia.dto.response.MLPredictionResponse;
import com.arsw.ids_ia.utils.enums.AlertSeverity;

/**
 * Servicio para interactuar con el modelo de Machine Learning.
 */
@Service
public class MachineLearningService {

    private static final Logger logger = LoggerFactory.getLogger(MachineLearningService.class);

    @Value("${ml.prediction.endpoint:https://j7gb7hbknl.execute-api.us-east-1.amazonaws.com/predict}")
    private String mlEndpoint;

    private final RestTemplate restTemplate;

    public MachineLearningService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Analiza un paquete de tráfico de red usando el modelo ML.
     * 
     * @param features Características del paquete de red
     * @return Respuesta del modelo con la predicción y probabilidades
     * @throws RuntimeException si hay error al conectar con el modelo
     */
    public MLPredictionResponse analyzeThreat(NetworkTrafficFeatures features) {
        try {
            logger.info("Sending traffic data to ML model at {}", mlEndpoint);
            
            MLPredictionRequest request = new MLPredictionRequest(features);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<MLPredictionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<MLPredictionResponse> response = restTemplate.postForEntity(
                mlEndpoint,
                entity,
                MLPredictionResponse.class
            );
            
            MLPredictionResponse prediction = response.getBody();
            
            if (prediction == null) {
                throw new RuntimeException("ML model returned null response");
            }
            
            logger.info("ML prediction received: {} with probability {}", 
                prediction.getPrediction(), 
                prediction.getAttackProbability());
            
            return prediction;
            
        } catch (Exception e) {
            logger.error("Error calling ML prediction endpoint: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze threat with ML model", e);
        }
    }

    /**
     * Determina la severidad basada en la probabilidad de ataque.
     * 
     * @param attackProbability Probabilidad de ataque (0.0 - 1.0)
     * @return Severidad correspondiente
     */
    public AlertSeverity determineSeverity(double attackProbability) {
        return AlertSeverity.fromAttackProbability(attackProbability);
    }

    /**
     * Verifica si una predicción requiere crear una alerta.
     * Solo se crean alertas si la probabilidad >= 0.3
     * 
     * @param attackProbability Probabilidad de ataque
     * @return true si debe crear alerta, false si es NORMAL
     */
    public boolean shouldCreateAlert(double attackProbability) {
        return attackProbability >= 0.3;
    }
}
