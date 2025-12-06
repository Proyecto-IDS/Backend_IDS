package com.arsw.ids_ia.service.ai;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AzureOpenAIServiceTest {

    @Test
    void testIsConfiguredWithAllParameters() {
        // Arrange
        String apiKey = "test-api-key";
        String endpoint = "https://test.openai.azure.com";
        String deploymentName = "gpt-4";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(apiKey, endpoint, deploymentName);

        // Assert
        assertTrue(service.isConfigured());
    }

    @Test
    void testIsConfiguredWithMissingApiKey() {
        // Arrange
        String apiKey = "";
        String endpoint = "https://test.openai.azure.com";
        String deploymentName = "gpt-4";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(apiKey, endpoint, deploymentName);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testIsConfiguredWithNullApiKey() {
        // Arrange
        String endpoint = "https://test.openai.azure.com";
        String deploymentName = "gpt-4";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(null, endpoint, deploymentName);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testIsConfiguredWithMissingEndpoint() {
        // Arrange
        String apiKey = "test-api-key";
        String endpoint = "";
        String deploymentName = "gpt-4";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(apiKey, endpoint, deploymentName);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testIsConfiguredWithNullEndpoint() {
        // Arrange
        String apiKey = "test-api-key";
        String deploymentName = "gpt-4";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(apiKey, null, deploymentName);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testIsConfiguredWithMissingDeploymentName() {
        // Arrange
        String apiKey = "test-api-key";
        String endpoint = "https://test.openai.azure.com";
        String deploymentName = "";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(apiKey, endpoint, deploymentName);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testIsConfiguredWithNullDeploymentName() {
        // Arrange
        String apiKey = "test-api-key";
        String endpoint = "https://test.openai.azure.com";

        // Act
        AzureOpenAIService service = new AzureOpenAIService(apiKey, endpoint, null);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testChatWithoutConfiguration() {
        // Arrange
        AzureOpenAIService service = new AzureOpenAIService("", "", "");

        // Act
        String response = service.chat("system prompt", "user message");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("no está configurada"));
    }

    @Test
    void testChatWithNullParameters() {
        // Arrange
        AzureOpenAIService service = new AzureOpenAIService(null, null, null);

        // Act
        String response = service.chat("system prompt", "user message");

        // Assert
        assertNotNull(response);
        assertFalse(service.isConfigured());
    }

    @Test
    void testServiceWithEmptyStrings() {
        // Act
        AzureOpenAIService service = new AzureOpenAIService("", "", "");

        // Assert
        assertFalse(service.isConfigured());
        
        // Test chat returns fallback
        String chatResponse = service.chat("test", "test");
        assertNotNull(chatResponse);
        assertTrue(chatResponse.contains("IA no está configurada"));
    }

    @Test
    void testServiceWithWhitespaceStrings() {
        // Act
        AzureOpenAIService service = new AzureOpenAIService("   ", "   ", "   ");

        // Assert
        // El servicio inicializa con AzureKeyCredential aún con espacios, pero falla en tiempo de ejecución
        assertTrue(service.isConfigured()); // Configurado inicialmente, pero falla al llamar chat
    }

    @Test
    void testChatWithEmptySystemPrompt() {
        // Arrange
        AzureOpenAIService service = new AzureOpenAIService("", "", "");

        // Act
        String response = service.chat("", "user message");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("no está configurada"));
    }

    @Test
    void testChatWithEmptyUserMessage() {
        // Arrange
        AzureOpenAIService service = new AzureOpenAIService("", "", "");

        // Act
        String response = service.chat("system prompt", "");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("no está configurada"));
    }

    @Test
    void testChatWithNullSystemPrompt() {
        // Arrange
        AzureOpenAIService service = new AzureOpenAIService("", "", "");

        // Act
        String response = service.chat(null, "user message");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("no está configurada"));
    }

    @Test
    void testChatWithNullUserMessage() {
        // Arrange
        AzureOpenAIService service = new AzureOpenAIService("", "", "");

        // Act
        String response = service.chat("system prompt", null);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("no está configurada"));
    }

    @Test
    void testServiceInitializationWithValidConfig() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService(
            "valid-key-123",
            "https://valid.openai.azure.com",
            "gpt-4-deployment"
        );

        // Assert
        assertTrue(service.isConfigured());
    }

    @Test
    void testServiceWithPartialConfig_OnlyApiKey() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService("api-key", null, null);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testServiceWithPartialConfig_OnlyEndpoint() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService(null, "https://endpoint.com", null);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testServiceWithPartialConfig_OnlyDeploymentName() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService(null, null, "gpt-4");

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testServiceWithPartialConfig_ApiKeyAndEndpoint() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService("api-key", "https://endpoint.com", null);

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testServiceWithPartialConfig_ApiKeyAndDeployment() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService("api-key", null, "gpt-4");

        // Assert
        assertFalse(service.isConfigured());
    }

    @Test
    void testServiceWithPartialConfig_EndpointAndDeployment() {
        // Arrange & Act
        AzureOpenAIService service = new AzureOpenAIService(null, "https://endpoint.com", "gpt-4");

        // Assert
        assertFalse(service.isConfigured());
    }
}
