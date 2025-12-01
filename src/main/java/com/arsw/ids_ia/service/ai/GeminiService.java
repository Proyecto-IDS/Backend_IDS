package com.arsw.ids_ia.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for integrating with Google Gemini AI API
 * Provides natural language responses for security incident assistance
 */
@Service
public class GeminiService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";
    
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final Gson gson;
    
    public GeminiService(@Value("${gemini.api.key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.gson = new Gson();
    }
    
    /**
     * Generate AI response using Gemini
     * @param systemPrompt Context and instructions for the AI
     * @param userMessage User's question or request
     * @return AI-generated response
     */
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("Gemini API key not configured, returning fallback message");
            return "⚠️ La IA no está configurada. Por favor, configura la API key de Gemini en application.properties";
        }
        
        try {
            // Build request body
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            
            JsonObject part = new JsonObject();
            String combinedPrompt = systemPrompt + "\n\nUsuario: " + userMessage;
            part.addProperty("text", combinedPrompt);
            parts.add(part);
            
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);
            
            // Configure safety settings to be more permissive for security content
            JsonArray safetySettings = new JsonArray();
            addSafetySetting(safetySettings, "HARM_CATEGORY_HARASSMENT", "BLOCK_NONE");
            addSafetySetting(safetySettings, "HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE");
            addSafetySetting(safetySettings, "HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE");
            addSafetySetting(safetySettings, "HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE");
            requestBody.add("safetySettings", safetySettings);
            
            // Set generation config
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.7);
            generationConfig.addProperty("topK", 40);
            generationConfig.addProperty("topP", 0.95);
            generationConfig.addProperty("maxOutputTokens", 1024);
            requestBody.add("generationConfig", generationConfig);
            
            // Make request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String url = GEMINI_API_URL + "?key=" + apiKey;
            HttpEntity<String> request = new HttpEntity<>(gson.toJson(requestBody), headers);
            
            logger.debug("Calling Gemini API...");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            // Parse response
            JsonObject responseJson = gson.fromJson(response.getBody(), JsonObject.class);
            
            if (responseJson.has("candidates")) {
                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (!candidates.isEmpty()) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    JsonObject contentObj = candidate.getAsJsonObject("content");
                    JsonArray partsArray = contentObj.getAsJsonArray("parts");
                    if (!partsArray.isEmpty()) {
                        String aiResponse = partsArray.get(0).getAsJsonObject().get("text").getAsString();
                        logger.info("Gemini response received successfully");
                        return aiResponse;
                    }
                }
            }
            
            logger.warn("Unexpected response format from Gemini API");
            return "⚠️ La IA no pudo generar una respuesta. Intenta reformular tu pregunta.";
            
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "⚠️ Error al conectar con la IA: " + e.getMessage() + 
                   ". Verifica tu conexión a internet y la configuración de la API key.";
        }
    }
    
    /**
     * Helper method to add safety settings
     */
    private void addSafetySetting(JsonArray settings, String category, String threshold) {
        JsonObject setting = new JsonObject();
        setting.addProperty("category", category);
        setting.addProperty("threshold", threshold);
        settings.add(setting);
    }
    
    /**
     * Check if Gemini service is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
