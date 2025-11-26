package com.arsw.ids_ia.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.arsw.ids_ia.dto.request.NetworkTrafficFeatures;
import com.arsw.ids_ia.dto.response.MLPredictionResponse;
import com.arsw.ids_ia.model.Alert;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrafficAnalysisService - Pruebas de parsers y análisis")
class TrafficAnalysisServiceTest {

    @Mock
    private MachineLearningService mlService;

    @Mock
    private AlertService alertService;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private TrafficAnalysisService trafficService;

    private NetworkTrafficFeatures testFeatures;
    private MLPredictionResponse testPrediction;
    private Alert testAlert;

    @BeforeEach
    void setUp() {
        testFeatures = new NetworkTrafficFeatures();
        testFeatures.setDuration(0);
        testFeatures.setProtocolType("tcp");
        testFeatures.setService("http");
        testFeatures.setSrcBytes(100);

        testPrediction = new MLPredictionResponse();
        testPrediction.setPrediction("dos");
        testPrediction.setAttackProbability(0.85);
        testPrediction.setCategory("dos");

        testAlert = new Alert.Builder()
            .packetId("PKT-001")
            .incidentId("INC-001")
            .severity("CONOCIDO")
            .attackProbability(0.85)
            .build();
        testAlert.setId(1L);
    }

    @Test
    @DisplayName("Debe analizar paquete y crear alerta cuando probabilidad >= 0.3")
    void shouldAnalyzeSinglePacket_AndCreateAlert() {
        when(mlService.analyzeThreat(any())).thenReturn(testPrediction);
        when(mlService.determineSeverity(0.85)).thenReturn(com.arsw.ids_ia.utils.enums.AlertSeverity.CONOCIDO);
        when(mlService.shouldCreateAlert(0.85)).thenReturn(true);
        when(alertService.create(any())).thenReturn(testAlert);

        Alert result = trafficService.analyzeSinglePacket(testFeatures);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(mlService).analyzeThreat(testFeatures);
        verify(mlService).shouldCreateAlert(0.85);
        verify(alertService).create(any());
    }

    @Test
    @DisplayName("Debe retornar null cuando probabilidad < 0.3 (NORMAL)")
    void shouldReturnNull_WhenTrafficIsNormal() {
        MLPredictionResponse normalPrediction = new MLPredictionResponse();
        normalPrediction.setPrediction("normal");
        normalPrediction.setAttackProbability(0.1);

        when(mlService.analyzeThreat(any())).thenReturn(normalPrediction);
        when(mlService.shouldCreateAlert(0.1)).thenReturn(false);

        Alert result = trafficService.analyzeSinglePacket(testFeatures);

        assertNull(result);
        verify(alertService, never()).create(any());
    }

    @Test
    @DisplayName("Debe parsear archivo JSON correctamente")
    void shouldParseJsonFile() throws IOException {
        String jsonContent = """
            [{
                "duration": 0,
                "protocol_type": "tcp",
                "service": "http",
                "flag": "SF",
                "src_bytes": 100,
                "dst_bytes": 200,
                "count": 1,
                "srv_count": 1,
                "serror_rate": 0.0,
                "srv_serror_rate": 0.0
            }]
            """;

        when(mockFile.getOriginalFilename()).thenReturn("traffic.json");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(jsonContent.getBytes()));
        when(mlService.analyzeThreat(any())).thenReturn(testPrediction);
        when(mlService.determineSeverity(anyDouble())).thenReturn(com.arsw.ids_ia.utils.enums.AlertSeverity.CONOCIDO);
        when(mlService.shouldCreateAlert(anyDouble())).thenReturn(true);
        when(alertService.create(any())).thenReturn(testAlert);

        var result = trafficService.analyzeTrafficFile(mockFile);

        assertEquals(1, result.size());
        verify(alertService, atLeastOnce()).create(any());
    }

    @Test
    @DisplayName("Debe parsear archivo CSV correctamente")
    void shouldParseCsvFile() throws IOException {
        String csvContent = """
            duration,protocol_type,service,flag,src_bytes,dst_bytes,count,srv_count,serror_rate,srv_serror_rate
            0,tcp,http,SF,100,200,1,1,0.0,0.0
            """;

        when(mockFile.getOriginalFilename()).thenReturn("traffic.csv");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(csvContent.getBytes()));
        when(mlService.analyzeThreat(any())).thenReturn(testPrediction);
        when(mlService.determineSeverity(anyDouble())).thenReturn(com.arsw.ids_ia.utils.enums.AlertSeverity.CONOCIDO);
        when(mlService.shouldCreateAlert(anyDouble())).thenReturn(true);
        when(alertService.create(any())).thenReturn(testAlert);

        var result = trafficService.analyzeTrafficFile(mockFile);

        assertEquals(1, result.size());
        verify(alertService, atLeastOnce()).create(any());
    }
}
