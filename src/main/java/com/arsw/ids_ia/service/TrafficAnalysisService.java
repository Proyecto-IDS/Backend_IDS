package com.arsw.ids_ia.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.arsw.ids_ia.dto.request.NetworkTrafficFeatures;
import com.arsw.ids_ia.dto.response.MLPredictionResponse;
import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.utils.enums.AlertSeverity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servicio para analizar archivos de tráfico de red y generar alertas.
 */
@Service
public class TrafficAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TrafficAnalysisService.class);

    private final MachineLearningService mlService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;
    

    public TrafficAnalysisService(MachineLearningService mlService, AlertService alertService) {
        this.mlService = mlService;
        this.alertService = alertService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Procesa un archivo de tráfico y genera alertas automáticamente.
     * 
     * @param file Archivo CSV o JSON con datos de tráfico
     * @return Lista de alertas creadas
     * @throws IOException si hay error al leer el archivo
     */
    public List<Alert> analyzeTrafficFile(MultipartFile file) throws IOException {
        logger.info("Processing traffic file upload");

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        List<NetworkTrafficFeatures> trafficData;

        if (filename.endsWith(".json")) {
            trafficData = parseJsonFile(file);
        } else if (filename.endsWith(".csv")) {
            trafficData = parseCsvFile(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only JSON and CSV are supported.");
        }

        logger.info("Parsed {} traffic records from file", trafficData.size());
        return analyzeTrafficBatch(trafficData);
    }

    /**
     * Analiza un único paquete de tráfico.
     * 
     * @param features Características del paquete
     * @return Alerta creada o null si no requiere alerta
     */
    public Alert analyzeSinglePacket(NetworkTrafficFeatures features) {
        logger.info("Analyzing single packet");

        MLPredictionResponse prediction;
        try {
            prediction = mlService.analyzeThreat(features);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to analyze threat for packet", e);
        }

        if (!mlService.shouldCreateAlert(prediction.getAttackProbability())) {
            logger.info("Traffic classified as NORMAL - no alert created");
            return null;
        }

        return createAlertFromPrediction(prediction, generatePacketId());
    }

    /**
     * Analiza un lote de paquetes de tráfico.
     */
    private List<Alert> analyzeTrafficBatch(List<NetworkTrafficFeatures> trafficDataList) {
        List<Alert> createdAlerts = new ArrayList<>();
        int totalPackets = trafficDataList.size();
        logger.info("Procesando lote de {} paquetes, todas las alertas se crearán inmediatamente", totalPackets);

        for (NetworkTrafficFeatures features : trafficDataList) {
            try {
                MLPredictionResponse prediction = mlService.analyzeThreat(features);
                if (mlService.shouldCreateAlert(prediction.getAttackProbability())) {
                    Alert alert = createAlertFromPrediction(prediction, generatePacketId());
                    createdAlerts.add(alert);
                    logger.info("Alerta creada (incidentId: {})", alert.getIncidentId());
                }
            } catch (RuntimeException e) {
                logger.error("Error analizando paquete: {}", e.getMessage());
            }
        }
        logger.info("Lote completado. Total de alertas creadas: {}", createdAlerts.size());
        return createdAlerts;
    }

    /**
     * Crea una alerta a partir de la predicción del modelo.
     */
    private Alert createAlertFromPrediction(MLPredictionResponse prediction, String packetId) {
        try {
            AlertSeverity severity = mlService.determineSeverity(prediction.getAttackProbability());
            String incidentId = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            String probabilitiesJson = objectMapper.writeValueAsString(prediction.getProbabilities());

            Alert alert = new Alert(
                packetId,
                incidentId,
                severity.name(),
                prediction.getAttackProbability(),
                prediction.getPrediction(),
                prediction.getCategory(),
                prediction.getStandardProtocol(),
                probabilitiesJson
            );

            Alert savedAlert = alertService.create(alert);
            logger.info("Alert created: {} - {} ({})", savedAlert.getId(), incidentId, severity);

            return savedAlert;

        } catch (IOException e) {
            throw new IllegalStateException("Failed to create alert from prediction", e);
        }
    }

    /**
     * Parse archivo JSON.
     * Puede ser un array de objetos o un objeto con array de features.
     */
    private List<NetworkTrafficFeatures> parseJsonFile(MultipartFile file) throws IOException {
        try {
            // Intentar parsear como array directo
            return objectMapper.readValue(
                file.getInputStream(), 
                new TypeReference<List<NetworkTrafficFeatures>>() {}
            );
        } catch (Exception e) {
            logger.warn("Failed to parse as array, trying as wrapped object");
            JsonWrapper wrapper = objectMapper.readValue(file.getInputStream(), JsonWrapper.class);
            return TrafficAnalysisService.JsonWrapper.getFeatures();
        }
    }

    /**
     * Parse archivo CSV.
     * Asume que la primera línea es el header con nombres de columnas.
     */
    private List<NetworkTrafficFeatures> parseCsvFile(MultipartFile file) throws IOException {
        List<NetworkTrafficFeatures> trafficData = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            String[] headers = headerLine.split(",");
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    NetworkTrafficFeatures features = parseCsvLine(line, headers);
                    trafficData.add(features);
                } catch (Exception e) {
                    logger.warn("Error parsing CSV line {}: {}", lineNumber, e.getMessage());
                }
            }
        }
        
        return trafficData;
    }

    /**
     * Parse una línea CSV a NetworkTrafficFeatures.
     */
    private NetworkTrafficFeatures parseCsvLine(String line, String[] headers) {
        String[] values = line.split(",");
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        
        for (int i = 0; i < headers.length && i < values.length; i++) {
            String header = headers[i].trim();
            String value = values[i].trim();

            switch (header) {
                case "duration" -> features.setDuration(parseIntOrDefault(value, 0));
                case "protocol_type" -> features.setProtocolType(value);
                case "service" -> features.setService(value);
                case "flag" -> features.setFlag(value);
                case "src_bytes" -> features.setSrcBytes(parseIntOrDefault(value, 0));
                case "dst_bytes" -> features.setDstBytes(parseIntOrDefault(value, 0));
                case "count" -> features.setCount(parseIntOrDefault(value, 0));
                case "srv_count" -> features.setSrvCount(parseIntOrDefault(value, 0));
                case "serror_rate" -> features.setSerrorRate(parseDoubleOrDefault(value, 0.0));
                case "srv_serror_rate" -> features.setSrvSerrorRate(parseDoubleOrDefault(value, 0.0));
                default -> {
                    // No action for unknown header
                }
            }
        }
        
        return features;
    }

    private String generatePacketId() {
        return "PKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Integer parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Clase auxiliar para parsear JSON envuelto
    private static class JsonWrapper {
        private static final List<NetworkTrafficFeatures> features = new ArrayList<>();

        public static List<NetworkTrafficFeatures> getFeatures() {
            return features;
        }
    }
}
