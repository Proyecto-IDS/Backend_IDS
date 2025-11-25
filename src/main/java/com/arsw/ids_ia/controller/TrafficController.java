package com.arsw.ids_ia.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arsw.ids_ia.dto.request.NetworkTrafficFeatures;
import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.service.TrafficAnalysisService;

/**
 * Controlador para análisis de tráfico de red y generación automática de alertas.
 */
@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private static final Logger logger = LoggerFactory.getLogger(TrafficController.class);

    private final TrafficAnalysisService analysisService;

    public TrafficController(TrafficAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Endpoint para subir y analizar un archivo de tráfico.
     * Soporta archivos CSV y JSON.
     * 
     * POST /api/traffic/upload
     * Content-Type: multipart/form-data
     * 
     * @param file Archivo con datos de tráfico de red
     * @return Lista de alertas generadas
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadTrafficFile(
            @RequestParam("file") MultipartFile file) {
        
        try {
            logger.info("Received traffic file upload: {} ({})", 
                file.getOriginalFilename(), file.getContentType());
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".json") && !filename.endsWith(".csv"))) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file format. Only JSON and CSV are supported."));
            }
            
            List<Alert> alerts = analysisService.analyzeTrafficFile(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Traffic file analyzed successfully");
            response.put("alertsCreated", alerts.size());
            response.put("alerts", alerts);
            
            logger.info("File processed successfully: {} alerts created", alerts.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file format: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error processing traffic file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para analizar un único paquete de tráfico.
     * 
     * POST /api/traffic/analyze
     * Content-Type: application/json
     * 
     * Body: NetworkTrafficFeatures (objeto con todas las características)
     * 
     * @param features Características del paquete de red
     * @return Alerta generada o mensaje si no requiere alerta
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeSinglePacket(
            @RequestBody NetworkTrafficFeatures features) {
        
        try {
            logger.info("Analyzing single packet");
            
            Alert alert = analysisService.analyzeSinglePacket(features);
            
            Map<String, Object> response = new HashMap<>();
            
            if (alert == null) {
                response.put("success", true);
                response.put("message", "Traffic analyzed - no threat detected (NORMAL)");
                response.put("alertCreated", false);
                return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("message", "Threat detected - alert created");
            response.put("alertCreated", true);
            response.put("alert", alert);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error analyzing packet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to analyze packet: " + e.getMessage()));
        }
    }
}
