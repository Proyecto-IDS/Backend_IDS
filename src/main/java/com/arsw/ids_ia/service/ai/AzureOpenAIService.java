package com.arsw.ids_ia.service.ai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.credential.AzureKeyCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for integrating with Azure OpenAI API using the official SDK
 * Provides natural language responses for security incident assistance
 */
@Service
public class AzureOpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AzureOpenAIService.class);
    
    private final OpenAIClient client;
    private final String deploymentName;
    private final boolean isConfigured;
    
    public AzureOpenAIService(
        @Value("${azure.openai.api.key:}") String apiKey,
        @Value("${azure.openai.endpoint:}") String endpoint,
        @Value("${azure.openai.deployment.name:}") String deploymentName
    ) {
        this.deploymentName = deploymentName;
        
        // Check if all required configuration is present
        this.isConfigured = apiKey != null && !apiKey.isEmpty() && 
                           endpoint != null && !endpoint.isEmpty() &&
                           deploymentName != null && !deploymentName.isEmpty();
        
        if (isConfigured) {
            try {
                this.client = new OpenAIClientBuilder()
                    .credential(new AzureKeyCredential(apiKey))
                    .endpoint(endpoint)
                    .buildClient();
                logger.info("Azure OpenAI client initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Azure OpenAI client: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize Azure OpenAI client", e);
            }
        } else {
            this.client = null;
            logger.warn("Azure OpenAI not configured - missing required properties");
        }
    }
    
    public boolean isConfigured() {
        return isConfigured && client != null;
    }
    
    /**
     * Generate AI response using Azure OpenAI SDK
     * @param systemPrompt Context and instructions for the AI
     * @param userMessage User's question or request
     * @return AI-generated response
     */
    public String chat(String systemPrompt, String userMessage) {
        if (!isConfigured()) {
            logger.warn("Azure OpenAI not configured, returning fallback message");
            return "⚠️ La IA no está configurada. Por favor, configura Azure OpenAI en application.properties";
        }
        
        try {
            // Create messages list
            List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(systemPrompt),
                new ChatRequestUserMessage(userMessage)
            );
            
            // Configure chat options
            ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
            // Note: GPT-5 mini only supports default values for temperature (1.0) and max_tokens
            // Removing setMaxTokens and setTemperature for full compatibility
            
            // Make the API call
            ChatCompletions chatCompletions = client.getChatCompletions(deploymentName, chatCompletionsOptions);
            
            // Extract response
            if (chatCompletions != null && !chatCompletions.getChoices().isEmpty()) {
                ChatChoice choice = chatCompletions.getChoices().get(0);
                ChatResponseMessage message = choice.getMessage();
                String content = message.getContent();
                
                logger.info("Azure OpenAI response received successfully (tokens used: {})", 
                    chatCompletions.getUsage() != null ? chatCompletions.getUsage().getTotalTokens() : "unknown");
                
                return content != null ? content : "❌ La IA no pudo generar una respuesta válida.";
            }
            
            logger.warn("Azure OpenAI API returned no valid choices");
            return "❌ No pude generar una respuesta. Intenta reformular tu pregunta.";
            
        } catch (Exception e) {
            logger.error("Error calling Azure OpenAI API: {}", e.getMessage(), e);
            return "❌ Error temporal de la IA. Intenta de nuevo en unos momentos.";
        }
    }
}