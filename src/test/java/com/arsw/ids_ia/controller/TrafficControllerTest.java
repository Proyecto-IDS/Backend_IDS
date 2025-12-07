package com.arsw.ids_ia.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.arsw.ids_ia.dto.request.NetworkTrafficFeatures;
import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.service.TrafficAnalysisService;

@ExtendWith(MockitoExtension.class)
class TrafficControllerTest {

    @Mock
    private TrafficAnalysisService analysisService;

    @InjectMocks
    private TrafficController trafficController;

    private Alert mockAlert;
    private NetworkTrafficFeatures mockFeatures;

    @BeforeEach
    void setUp() {
        mockAlert = new Alert();
        mockAlert.setId(1L);
        mockAlert.setSeverity("HIGH");
        
        mockFeatures = new NetworkTrafficFeatures();
    }

    @Test
    void testUploadTrafficFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.json", 
            "application/json", 
            "{\"data\":\"test\"}".getBytes()
        );
        
        List<Alert> alerts = Arrays.asList(mockAlert);
        when(analysisService.analyzeTrafficFile(any(MultipartFile.class))).thenReturn(alerts);
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(1, response.getBody().get("alertsCreated"));
        verify(analysisService, times(1)).analyzeTrafficFile(file);
    }

    @Test
    void testUploadTrafficFile_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "empty.json", 
            "application/json", 
            new byte[0]
        );
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File is empty", response.getBody().get("error"));
        verify(analysisService, never()).analyzeTrafficFile(any());
    }

    @Test
    void testUploadTrafficFile_InvalidFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.txt", 
            "text/plain", 
            "test".getBytes()
        );
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("Invalid file format"));
        verify(analysisService, never()).analyzeTrafficFile(any());
    }

    @Test
    void testUploadTrafficFile_IllegalArgumentException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.json", 
            "application/json", 
            "invalid".getBytes()
        );
        
        when(analysisService.analyzeTrafficFile(any())).thenThrow(new IllegalArgumentException("Invalid format"));
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid format", response.getBody().get("error"));
    }

    @Test
    void testUploadTrafficFile_GenericException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.json", 
            "application/json", 
            "test".getBytes()
        );
        
        when(analysisService.analyzeTrafficFile(any())).thenThrow(new RuntimeException("Processing error"));
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("Failed to process file"));
    }

    @Test
    void testUploadTrafficFile_NoAlertsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.json", 
            "application/json", 
            "{}".getBytes()
        );
        
        when(analysisService.analyzeTrafficFile(any())).thenReturn(Arrays.asList());
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().get("alertsCreated"));
    }

    @Test
    void testAnalyzeSinglePacket_ThreatDetected() {
        when(analysisService.analyzeSinglePacket(any())).thenReturn(mockAlert);
        
        ResponseEntity<Map<String, Object>> response = trafficController.analyzeSinglePacket(mockFeatures);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertTrue((Boolean) response.getBody().get("alertCreated"));
        assertEquals("Threat detected - alert created", response.getBody().get("message"));
        assertNotNull(response.getBody().get("alert"));
        verify(analysisService, times(1)).analyzeSinglePacket(mockFeatures);
    }

    @Test
    void testAnalyzeSinglePacket_NoThreat() {
        when(analysisService.analyzeSinglePacket(any())).thenReturn(null);
        
        ResponseEntity<Map<String, Object>> response = trafficController.analyzeSinglePacket(mockFeatures);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertFalse((Boolean) response.getBody().get("alertCreated"));
        assertTrue(response.getBody().get("message").toString().contains("no threat detected"));
        verify(analysisService, times(1)).analyzeSinglePacket(mockFeatures);
    }

    @Test
    void testAnalyzeSinglePacket_Exception() {
        when(analysisService.analyzeSinglePacket(any())).thenThrow(new RuntimeException("Analysis failed"));
        
        ResponseEntity<Map<String, Object>> response = trafficController.analyzeSinglePacket(mockFeatures);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").toString().contains("Failed to analyze packet"));
    }

    @Test
    void testAnalyzeSinglePacket_NullFeatures() {
        when(analysisService.analyzeSinglePacket(null)).thenThrow(new NullPointerException("Features required"));
        
        ResponseEntity<Map<String, Object>> response = trafficController.analyzeSinglePacket(null);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testUploadTrafficFile_MultipleAlerts() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.json", 
            "application/json", 
            "test".getBytes()
        );
        
        Alert alert1 = new Alert();
        Alert alert2 = new Alert();
        Alert alert3 = new Alert();
        List<Alert> alerts = Arrays.asList(alert1, alert2, alert3);
        
        when(analysisService.analyzeTrafficFile(any())).thenReturn(alerts);
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().get("alertsCreated"));
    }

    @Test
    void testUploadTrafficFile_ServiceThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.json", 
            "application/json", 
            "{\"data\":\"test\"}".getBytes()
        );
        
        when(analysisService.analyzeTrafficFile(any())).thenThrow(new RuntimeException("Processing error"));
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void testUploadTrafficFile_NullFilename() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            null, 
            "application/json", 
            "{\"data\":\"test\"}".getBytes()
        );
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(analysisService, never()).analyzeTrafficFile(any());
    }

    @Test
    void testUploadTrafficFile_CsvFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "traffic.csv", 
            "text/csv", 
            "duration,protocol_type,service\n10,tcp,http".getBytes()
        );
        
        List<Alert> alerts = Arrays.asList(mockAlert);
        when(analysisService.analyzeTrafficFile(any())).thenReturn(alerts);
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get("alertsCreated"));
    }

    @Test
    void testUploadTrafficFile_LargeFile() throws Exception {
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "large_traffic.json", 
            "application/json", 
            largeContent
        );
        
        when(analysisService.analyzeTrafficFile(any())).thenReturn(Arrays.asList(mockAlert));
        
        ResponseEntity<Map<String, Object>> response = trafficController.uploadTrafficFile(file);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(analysisService).analyzeTrafficFile(file);
    }
}

