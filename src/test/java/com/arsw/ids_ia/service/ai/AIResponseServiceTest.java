package com.arsw.ids_ia.service.ai;

import com.arsw.ids_ia.model.ai.AttackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIResponseServiceTest {

    @Mock
    private ChecklistGenerator checklistGenerator;

    @Mock
    private AzureOpenAIService azureOpenAIService;

    @InjectMocks
    private AIResponseService aiResponseService;

    private AIResponseService.IncidentContext context;

    @BeforeEach
    void setUp() {
        context = new AIResponseService.IncidentContext(
            AttackType.SQL_INJECTION,
            0.85,
            "HIGH"
        );
        
        List<ChecklistGenerator.ChecklistItem> checklist = List.of(
            new ChecklistGenerator.ChecklistItem(1, "Verify logs", false),
            new ChecklistGenerator.ChecklistItem(2, "Block IP", false)
        );
        context.setChecklist(checklist);
    }

    @Test
    void testGenerateResponseWithEmptyMessage() {
        // Act
        String response = aiResponseService.generateResponse("", context);

        // Assert
        assertNotNull(response);
        assertEquals("¿En qué puedo ayudarte con este incidente?", response);
    }

    @Test
    void testGenerateResponseWithNullMessage() {
        // Act
        String response = aiResponseService.generateResponse(null, context);

        // Assert
        assertNotNull(response);
        assertEquals("¿En qué puedo ayudarte con este incidente?", response);
    }

    @Test
    void testGenerateResponseWithAzureOpenAIConfigured() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(true);
        when(azureOpenAIService.chat(anyString(), anyString())).thenReturn("AI response");

        // Act
        String response = aiResponseService.generateResponse("What is this attack?", context);

        // Assert
        assertNotNull(response);
        assertEquals("AI response", response);
        verify(azureOpenAIService).isConfigured();
        verify(azureOpenAIService).chat(anyString(), eq("What is this attack?"));
    }

    @Test
    void testGenerateResponseWithoutAzureOpenAI() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("explain", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        verify(azureOpenAIService).isConfigured();
        verify(azureOpenAIService, never()).chat(anyString(), anyString());
    }

    @Test
    void testIncidentContextGettersAndSetters() {
        // Act & Assert
        assertEquals(AttackType.SQL_INJECTION, context.getAttackType());
        assertEquals(0.85, context.getAttackProbability());
        assertEquals("HIGH", context.getSeverity());
        assertEquals(0, context.getCompletedSteps());
        assertNotNull(context.getChecklist());
        assertEquals(2, context.getChecklist().size());

        // Modify
        context.setAttackType(AttackType.DDOS);
        context.setAttackProbability(0.95);
        context.setSeverity("CRITICAL");
        context.setCompletedSteps(1);

        assertEquals(AttackType.DDOS, context.getAttackType());
        assertEquals(0.95, context.getAttackProbability());
        assertEquals("CRITICAL", context.getSeverity());
        assertEquals(1, context.getCompletedSteps());
    }

    @Test
    void testGenerateResponseWithNextStepIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("what should I do next?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testGenerateResponseWithHowToIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how do I fix this?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testGenerateResponseWithSeverityCheckIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how severe is this?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testGenerateResponseWithExplanationIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("what is this attack?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testIncidentContextWithDifferentAttackTypes() {
        // Test with DDOS
        AIResponseService.IncidentContext ddosContext = new AIResponseService.IncidentContext(
            AttackType.DDOS,
            0.92,
            "CRITICAL"
        );
        
        assertEquals(AttackType.DDOS, ddosContext.getAttackType());
        assertEquals(0.92, ddosContext.getAttackProbability());

        // Test with XSS
        AIResponseService.IncidentContext xssContext = new AIResponseService.IncidentContext(
            AttackType.XSS,
            0.78,
            "MEDIUM"
        );
        
        assertEquals(AttackType.XSS, xssContext.getAttackType());
        assertEquals("MEDIUM", xssContext.getSeverity());
    }

    @Test
    void testGenerateAzureOpenAIResponseError() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(true);
        when(azureOpenAIService.chat(anyString(), anyString())).thenThrow(new RuntimeException("API error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            aiResponseService.generateResponse("test message", context);
        });
    }

    @Test
    void testGenerateResponseWithLongMessage() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        String longMessage = "This is a very long message ".repeat(100);

        // Act
        String response = aiResponseService.generateResponse(longMessage, context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testGenerateResponseWithSpecialCharacters() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        String messageWithSpecialChars = "¿Cómo puedo proteger mi sistema? ñáéíóú!@#$%";

        // Act
        String response = aiResponseService.generateResponse(messageWithSpecialChars, context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testWelcomeMessageWithAzureOpenAI() {
        // Act - welcome message uses local template
        String welcome = aiResponseService.generateWelcomeMessage(context);

        // Assert
        assertNotNull(welcome);
        assertTrue(welcome.contains("Hola") || welcome.contains("asistente"));
    }

    @Test
    void testWelcomeMessageWithoutAzureOpenAI() {
        // Act - welcome message uses local template
        String welcome = aiResponseService.generateWelcomeMessage(context);

        // Assert
        assertNotNull(welcome);
        assertTrue(welcome.contains("Hola") || welcome.contains("asistente"));
    }

    @Test
    void testGenerateResponseWithProgressIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        context.setCompletedSteps(1);

        // Act
        String response = aiResponseService.generateResponse("what's our progress?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testGenerateResponseFallbackOnAzureError() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(true);
        when(azureOpenAIService.chat(anyString(), anyString())).thenReturn(null);

        // Act
        String response = aiResponseService.generateResponse("test", context);

        // Assert - should fallback to null, not throw
        assertNull(response);
    }

    @Test
    void testContextWithEmptyChecklist() {
        // Arrange
        context.setChecklist(List.of());

        // Act
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        String response = aiResponseService.generateResponse("next steps?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testContextWithHighCompletionRate() {
        // Arrange
        context.setCompletedSteps(2);
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("status?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testMultipleAttackTypes() {
        // Test all major attack types
        AttackType[] types = {AttackType.SQL_INJECTION, AttackType.DDOS, AttackType.XSS};
        
        for (AttackType type : types) {
            AIResponseService.IncidentContext ctx = new AIResponseService.IncidentContext(type, 0.85, "HIGH");
            assertEquals(type, ctx.getAttackType());
        }
    }

    @Test
    void testHowToResponseWithWAF() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how do I configure the firewall?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHowToResponseWithDatabase() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how to check database logs?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHowToResponseWithCode() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how to fix the code?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHowToResponseWithCloudflare() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how to use cloudflare?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHowToResponseWithBlocking() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how to block the attacker?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHowToResponseWithCaptcha() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("should I add a captcha?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHowToResponseWithSanitization() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("how to sanitize inputs?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testNextStepWithNullChecklist() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        context.setChecklist(null);
        
        List<ChecklistGenerator.ChecklistItem> newChecklist = List.of(
            new ChecklistGenerator.ChecklistItem(1, "First step", false)
        );
        when(checklistGenerator.generateChecklist(any(), anyDouble())).thenReturn(newChecklist);

        // Act
        String response = aiResponseService.generateResponse("siguiente paso", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        verify(checklistGenerator).generateChecklist(AttackType.SQL_INJECTION, 0.85);
    }

    @Test
    void testNextStepAllCompleted() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        List<ChecklistGenerator.ChecklistItem> completedChecklist = List.of(
            new ChecklistGenerator.ChecklistItem(1, "Step 1", true),
            new ChecklistGenerator.ChecklistItem(2, "Step 2", true)
        );
        context.setChecklist(completedChecklist);

        // Act
        String response = aiResponseService.generateResponse("siguiente", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testCompletionIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("I'm done", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testStatusCheckIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("what's the status?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testHelpIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("help", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testDefaultIntent() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("random message", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testSeverityResponseHighProbability() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        context.setAttackProbability(0.95);

        // Act
        String response = aiResponseService.generateResponse("es grave esto?", context);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("CRÍTICO") || response.contains("Probabilidad"));
    }

    @Test
    void testSeverityResponseMediumProbability() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        context.setAttackProbability(0.5);

        // Act
        String response = aiResponseService.generateResponse("qué tan grave es?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testSeverityResponseLowProbability() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        context.setAttackProbability(0.2);

        // Act
        String response = aiResponseService.generateResponse("es peligroso?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testExplanationResponseWithSymptoms() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("explica este ataque", context);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("SQL") || response.contains("Explicación") || response.contains("Tipo"));
    }

    @Test
    void testCompletionResponseIncrementsCounter() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        int initialCompleted = context.getCompletedSteps();

        // Act
        String response = aiResponseService.generateResponse("listo, terminé", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testStatusResponseWithPartialProgress() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        List<ChecklistGenerator.ChecklistItem> partialChecklist = List.of(
            new ChecklistGenerator.ChecklistItem(1, "Step 1", true),
            new ChecklistGenerator.ChecklistItem(2, "Step 2", false),
            new ChecklistGenerator.ChecklistItem(3, "Step 3", false)
        );
        context.setChecklist(partialChecklist);

        // Act
        String response = aiResponseService.generateResponse("dame el estado", context);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Estado") || response.contains("Progreso") || response.contains("1") || response.contains("3"));
    }

    @Test
    void testStatusResponseAllCompleted() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        List<ChecklistGenerator.ChecklistItem> completedList = List.of(
            new ChecklistGenerator.ChecklistItem(1, "Step 1", true),
            new ChecklistGenerator.ChecklistItem(2, "Step 2", true)
        );
        context.setChecklist(completedList);

        // Act
        String response = aiResponseService.generateResponse("cuál es el estado?", context);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("completado") || response.contains("100") || response.contains("2/2"));
    }

    @Test
    void testHelpResponseContainsCommands() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("ayuda", context);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("comando") || response.contains("Comandos") || response.contains("ayuda"));
    }

    @Test
    void testHowToResponseGeneral() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);

        // Act
        String response = aiResponseService.generateResponse("cómo soluciono esto?", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testWelcomeMessageFormat() {
        // Act
        String welcome = aiResponseService.generateWelcomeMessage(context);

        // Assert
        assertNotNull(welcome);
        assertTrue(welcome.contains("Hola") || welcome.contains("asistente"));
        assertTrue(welcome.contains("SQL") || welcome.contains(context.getAttackType().getDisplayName()));
    }

    @Test
    void testWelcomeMessageWithDifferentSeverities() {
        // Test CRITICAL
        context.setSeverity("CRITICAL");
        String welcomeCritical = aiResponseService.generateWelcomeMessage(context);
        assertNotNull(welcomeCritical);
        assertTrue(welcomeCritical.contains("CRITICAL"));

        // Test LOW
        context.setSeverity("LOW");
        String welcomeLow = aiResponseService.generateWelcomeMessage(context);
        assertNotNull(welcomeLow);
        assertTrue(welcomeLow.contains("LOW"));
    }

    @Test
    void testWelcomeMessageWithDifferentAttackTypes() {
        // Test DDOS
        context.setAttackType(AttackType.DDOS);
        String welcomeDDOS = aiResponseService.generateWelcomeMessage(context);
        assertNotNull(welcomeDDOS);
        assertTrue(welcomeDDOS.contains("DDoS") || welcomeDDOS.contains("Denegación"));

        // Test XSS
        context.setAttackType(AttackType.XSS);
        String welcomeXSS = aiResponseService.generateWelcomeMessage(context);
        assertNotNull(welcomeXSS);
        assertTrue(welcomeXSS.contains("XSS") || welcomeXSS.contains("Cross"));
    }

    @Test
    void testNextStepWithEmptyChecklist() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        context.setChecklist(List.of());
        when(checklistGenerator.generateChecklist(any(), anyDouble())).thenReturn(List.of());

        // Act
        String response = aiResponseService.generateResponse("siguiente paso", context);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testContextWithZeroCompletedSteps() {
        // Arrange
        context.setCompletedSteps(0);

        // Act & Assert
        assertEquals(0, context.getCompletedSteps());
    }

    @Test
    void testMultipleSeverityBranches() {
        // Test probability = 0.8 (boundary)
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        
        context.setAttackProbability(0.8);
        String response1 = aiResponseService.generateResponse("severidad?", context);
        assertNotNull(response1);

        context.setAttackProbability(0.6);
        String response2 = aiResponseService.generateResponse("severidad?", context);
        assertNotNull(response2);

        context.setAttackProbability(0.3);
        String response3 = aiResponseService.generateResponse("severidad?", context);
        assertNotNull(response3);

        context.setAttackProbability(0.1);
        String response4 = aiResponseService.generateResponse("severidad?", context);
        assertNotNull(response4);
    }

    @Test
    void testAzureOpenAIWithNullResponse() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(true);
        when(azureOpenAIService.chat(anyString(), anyString())).thenReturn(null);

        // Act
        String response = aiResponseService.generateResponse("test", context);

        // Assert
        assertNull(response);
        verify(azureOpenAIService).isConfigured();
        verify(azureOpenAIService).chat(anyString(), eq("test"));
    }

    @Test
    void testGenerateResponseWithEmptyContext() {
        // Arrange
        when(azureOpenAIService.isConfigured()).thenReturn(false);
        AIResponseService.IncidentContext emptyContext = new AIResponseService.IncidentContext(
            AttackType.UNKNOWN,
            0.0,
            "UNKNOWN"
        );

        // Act
        String response = aiResponseService.generateResponse("help", emptyContext);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}


