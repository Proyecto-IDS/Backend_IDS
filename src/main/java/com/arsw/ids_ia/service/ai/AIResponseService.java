package com.arsw.ids_ia.service.ai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arsw.ids_ia.model.ai.AttackType;
import com.arsw.ids_ia.model.ai.UserIntent;
import com.arsw.ids_ia.service.ai.AttackKnowledgeBase.AttackKnowledge;

/**
 * Servicio de IA que genera respuestas contextuales usando Google Gemini
 * Mantiene templates locales como fallback si la API no está disponible
 */
@Service
public class AIResponseService {

    private static final Logger logger = LoggerFactory.getLogger(AIResponseService.class);
    
    private final ChecklistGenerator checklistGenerator;
    private final AzureOpenAIService azureOpenAIService;

    @Autowired
    public AIResponseService(ChecklistGenerator checklistGenerator, AzureOpenAIService azureOpenAIService) {
        this.checklistGenerator = checklistGenerator;
        this.azureOpenAIService = azureOpenAIService;
    }

    /**
     * Contexto del incidente para generar respuestas personalizadas
     */
    public static class IncidentContext {
        private AttackType attackType;
        private double attackProbability;
        private String severity;
        private List<ChecklistGenerator.ChecklistItem> checklist;
        private int completedSteps;

        public IncidentContext(AttackType attackType, double attackProbability, String severity) {
            this.attackType = attackType;
            this.attackProbability = attackProbability;
            this.severity = severity;
            this.completedSteps = 0;
        }

        // Getters y setters
        public AttackType getAttackType() { return attackType; }
        public void setAttackType(AttackType attackType) { this.attackType = attackType; }
        public double getAttackProbability() { return attackProbability; }
        public void setAttackProbability(double attackProbability) { this.attackProbability = attackProbability; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<ChecklistGenerator.ChecklistItem> getChecklist() { return checklist; }
        public void setChecklist(List<ChecklistGenerator.ChecklistItem> checklist) { this.checklist = checklist; }
        public int getCompletedSteps() { return completedSteps; }
        public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }
    }

    /**
     * Genera una respuesta de IA basada en el mensaje del usuario y el contexto del incidente
     * Usa Google Gemini si está configurado, sino usa templates locales
     */
    public String generateResponse(String userMessage, IncidentContext context) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "¿En qué puedo ayudarte con este incidente?";
        }

        // Si Azure OpenAI está configurado, usar IA
        if (azureOpenAIService.isConfigured()) {
            logger.info("Using Azure OpenAI for response generation");
            return generateAzureOpenAIResponse(userMessage, context);
        }
        
        // Fallback a templates locales
        logger.info("Azure OpenAI not configured, using local templates");
        return generateTemplateResponse(userMessage, context);
    }
    
    /**
     * Genera respuesta usando Azure OpenAI
     */
    private String generateAzureOpenAIResponse(String userMessage, IncidentContext context) {
        String systemPrompt = buildSystemPrompt(context);
        return azureOpenAIService.chat(systemPrompt, userMessage);
    }
    
    /**
     * Construye el prompt del sistema con contexto del incidente
     */
    private String buildSystemPrompt(IncidentContext context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Eres un asistente de ciberseguridad conversacional. ");
        prompt.append("Cuando te pidan información sobre la alerta o incidente, proporciona los detalles directamente.\n\n");
        
        prompt.append("INFORMACIÓN DEL INCIDENTE (solo úsala cuando te pregunten):\n");
        prompt.append("- Tipo: ").append(context.getAttackType().getDisplayName()).append("\n");
        prompt.append("- Confianza: ").append(String.format("%.1f%%", context.getAttackProbability() * 100)).append("\n");
        prompt.append("- Severidad: ").append(context.getSeverity()).append("\n\n");
        
        // Agregar conocimiento técnico del ataque (solo para referencia)
        AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(context.getAttackType());
        if (knowledge != null) {
            prompt.append("DETALLES TÉCNICOS (solo si preguntan):\n");
            prompt.append(knowledge.getDescription()).append("\n\n");
        }
        
        prompt.append("EJEMPLOS DE RESPUESTAS:\n");
        prompt.append("Si preguntan 'información de la alerta' o 'resumen':\n");
        prompt.append("'📋 **Resumen del Incidente:**\n");
        prompt.append("- **Tipo:** [tipo de ataque]\n");
        prompt.append("- **Confianza:** [porcentaje]%\n");
        prompt.append("- **Severidad:** [nivel]\n");
        prompt.append("- **Descripción:** [explicación simple del ataque]\n\n");
        prompt.append("¿Necesitas que te ayude con algo específico para resolverlo?'\n\n");
        
        prompt.append("REGLAS:\n");
        prompt.append("• Responde de forma natural y conversacional\n");
        prompt.append("• Cuando pidan información, dala directamente usando el formato del ejemplo\n");
        prompt.append("• No repitas saludos en conversaciones activas\n");
        prompt.append("• Sé útil y directo\n\n");
        
        return prompt.toString();
    }
    
    /**
     * Genera respuesta usando templates locales (fallback)
     */
    private String generateTemplateResponse(String userMessage, IncidentContext context) {
        UserIntent intent = UserIntent.detectIntent(userMessage);
        AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(context.getAttackType());

        switch (intent) {
            case NEXT_STEP:
                return generateNextStepResponse(context, knowledge);
            
            case HOW_TO:
                return generateHowToResponse(userMessage, context, knowledge);
            
            case SEVERITY_CHECK:
                return generateSeverityResponse(context);
            
            case EXPLANATION:
                return generateExplanationResponse(context, knowledge);
            
            case COMPLETION:
                return generateCompletionResponse(context);
            
            case STATUS_CHECK:
                return generateStatusResponse(context);
            
            case HELP:
                return generateHelpResponse(context, knowledge);
            
            default:
                return generateDefaultResponse(context, knowledge);
        }
    }

    private String generateNextStepResponse(IncidentContext context, AttackKnowledge knowledge) {
        if (context.getChecklist() == null || context.getChecklist().isEmpty()) {
            context.setChecklist(checklistGenerator.generateChecklist(context.getAttackType(), context.getAttackProbability()));
        }

        // Encontrar primer paso no completado
        for (ChecklistGenerator.ChecklistItem item : context.getChecklist()) {
            if (!item.isDone()) {
                return String.format("📋 Siguiente paso (%d/%d):\n\n%s\n\n¿Necesitas ayuda con este paso?",
                    item.getId(),
                    context.getChecklist().size(),
                    item.getLabel());
            }
        }

        return "✅ ¡Excelente trabajo! Has completado todos los pasos del checklist. El incidente está listo para marcarse como resuelto.";
    }

    private String generateHowToResponse(String userMessage, IncidentContext context, AttackKnowledge knowledge) {
        String normalized = userMessage.toLowerCase();

        // Detectar tema específico en el mensaje
        if (normalized.contains("waf") || normalized.contains("firewall")) {
            return knowledge.getTechnicalGuide("waf");
        } else if (normalized.contains("base de datos") || normalized.contains("database") || normalized.contains("logs")) {
            return knowledge.getTechnicalGuide("database");
        } else if (normalized.contains("código") || normalized.contains("code") || normalized.contains("programación")) {
            return knowledge.getTechnicalGuide("code");
        } else if (normalized.contains("cloudflare")) {
            return knowledge.getTechnicalGuide("cloudflare");
        } else if (normalized.contains("bloquear") || normalized.contains("blocking")) {
            return knowledge.getTechnicalGuide("blocking");
        } else if (normalized.contains("captcha")) {
            return knowledge.getTechnicalGuide("captcha");
        } else if (normalized.contains("sanitiz") || normalized.contains("validar")) {
            return knowledge.getTechnicalGuide("sanitization");
        }

        // Si no detectamos tema específico, dar guía general
        StringBuilder response = new StringBuilder();
        response.append("🔧 Guías técnicas disponibles para ").append(context.getAttackType().getDisplayName()).append(":\n\n");
        
        knowledge.getAllGuides().forEach((key, value) -> {
            response.append("• Pregunta sobre: ").append(key).append("\n");
        });
        
        response.append("\nEjemplo: \"¿Cómo bloqueo esto en WAF?\"");
        return response.toString();
    }

    private String generateSeverityResponse(IncidentContext context) {
        StringBuilder response = new StringBuilder();
        response.append("⚠️ Evaluación de severidad:\n\n");
        response.append(String.format("• Tipo: %s\n", context.getAttackType().getDisplayName()));
        response.append(String.format("• Probabilidad de ataque: %.1f%%\n", context.getAttackProbability() * 100));
        response.append(String.format("• Clasificación: %s\n\n", context.getSeverity().toUpperCase()));

        if (context.getAttackProbability() >= 0.8) {
            response.append("🚨 CRÍTICO: Requiere atención inmediata. Prioriza contención sobre análisis.");
        } else if (context.getAttackProbability() >= 0.6) {
            response.append("⚠️ ALTO: Requiere acción pronta. Sigue el checklist en orden.");
        } else if (context.getAttackProbability() >= 0.3) {
            response.append("⚡ MEDIO: Monitorea y aplica medidas preventivas.");
        } else {
            response.append("ℹ️ BAJO: Revisa logs y documenta hallazgos.");
        }

        return response.toString();
    }

    private String generateExplanationResponse(IncidentContext context, AttackKnowledge knowledge) {
        StringBuilder response = new StringBuilder();
        response.append("📚 Explicación del incidente:\n\n");
        response.append("🎯 Tipo de ataque: ").append(context.getAttackType().getDisplayName()).append("\n\n");
        response.append(knowledge.getDescription()).append("\n\n");
        response.append("🔍 Síntomas comunes:\n");
        
        for (String symptom : knowledge.getSymptoms()) {
            response.append("• ").append(symptom).append("\n");
        }
        
        response.append("\n💡 Usa el comando \"qué hago\" para ver los pasos de mitigación.");
        
        return response.toString();
    }

    private String generateCompletionResponse(IncidentContext context) {
        context.setCompletedSteps(context.getCompletedSteps() + 1);
        
        StringBuilder response = new StringBuilder();
        response.append("✅ ¡Paso completado exitosamente!\n\n");
        response.append(String.format("Progreso: %d/%d pasos\n\n",
            context.getCompletedSteps(),
            context.getChecklist() != null ? context.getChecklist().size() : 0));

        // Sugerir siguiente paso
        if (context.getChecklist() != null) {
            for (ChecklistGenerator.ChecklistItem item : context.getChecklist()) {
                if (!item.isDone()) {
                    response.append(String.format("⏭️ Siguiente: %s", item.getLabel()));
                    break;
                }
            }
        }

        return response.toString();
    }

    private String generateStatusResponse(IncidentContext context) {
        StringBuilder response = new StringBuilder();
        response.append("📊 Estado actual del incidente:\n\n");
        response.append(String.format("• Tipo: %s\n", context.getAttackType().getDisplayName()));
        response.append(String.format("• Severidad: %s\n", context.getSeverity().toUpperCase()));
        
        if (context.getChecklist() != null) {
            long completed = context.getChecklist().stream().filter(ChecklistGenerator.ChecklistItem::isDone).count();
            response.append(String.format("• Progreso: %d/%d pasos completados\n", completed, context.getChecklist().size()));
            response.append(String.format("• Porcentaje: %.0f%%\n\n", (completed * 100.0) / context.getChecklist().size()));
            
            if (completed < context.getChecklist().size()) {
                response.append("📋 Pasos pendientes:\n");
                context.getChecklist().stream()
                    .filter(item -> !item.isDone())
                    .forEach(item -> response.append(String.format("  %d. %s\n", item.getId(), item.getLabel())));
            } else {
                response.append("✅ Todos los pasos completados. Incidente listo para resolverse.");
            }
        }

        return response.toString();
    }

    private String generateHelpResponse(IncidentContext context, AttackKnowledge knowledge) {
        StringBuilder response = new StringBuilder();
        response.append("🤖 Comandos disponibles:\n\n");
        response.append("• \"¿qué hago?\" - Muestra el siguiente paso del checklist\n");
        response.append("• \"¿cómo...?\" - Instrucciones técnicas específicas\n");
        response.append("• \"¿es grave?\" - Evaluación de severidad\n");
        response.append("• \"explica\" - Detalles del tipo de ataque\n");
        response.append("• \"listo\" - Marca paso como completado\n");
        response.append("• \"estado\" - Progreso actual\n\n");
        response.append(String.format("📋 Incidente actual: %s (%s)\n", 
            context.getAttackType().getDisplayName(),
            context.getSeverity().toUpperCase()));
        response.append("\n💡 Tip: Sigue el checklist en orden para una respuesta efectiva.");

        return response.toString();
    }

    private String generateDefaultResponse(IncidentContext context, AttackKnowledge knowledge) {
        return String.format("Entendido. Estoy aquí para ayudarte con este incidente de %s.\n\n" +
            "Puedes preguntarme:\n" +
            "• \"¿qué hago ahora?\" para ver el siguiente paso\n" +
            "• \"¿cómo...?\" para obtener instrucciones técnicas\n" +
            "• \"ayuda\" para ver todos los comandos disponibles",
            context.getAttackType().getDisplayName());
    }

    /**
     * Genera el mensaje inicial de bienvenida cuando se crea un War Room
     */
    public String generateWelcomeMessage(IncidentContext context) {
        StringBuilder welcome = new StringBuilder();
        welcome.append("👋 ¡Hola! Soy tu asistente de ciberseguridad\n\n");
        welcome.append(String.format("🎯 Detectamos: **%s**\n", context.getAttackType().getDisplayName()));
        welcome.append(String.format("📊 Confianza: %.1f%% | Severidad: %s\n\n",
            context.getAttackProbability() * 100,
            context.getSeverity().toUpperCase()));
        
        welcome.append("💬 Puedes preguntarme cualquier cosa, por ejemplo:\n\n");
        welcome.append("🔍 **\"Resúmeme este ataque\"** - Te explico qué está pasando\n");
        welcome.append("⚡ **\"¿Cómo lo bloqueo?\"** - Te doy comandos específicos\n");
        welcome.append("🤔 **\"¿Qué pasa si hago X?\"** - Analizamos consecuencias\n");
        welcome.append("📋 **\"¿Cuál es el siguiente paso?\"** - Te guío paso a paso\n");
        welcome.append("🛠️ **\"Dame el comando para...\"** - Comandos listos para usar\n\n");
        welcome.append("Soy como ChatGPT pero especializado en resolver **exactamente este incidente**. ");
        welcome.append("¿Por dónde empezamos? 🚀");

        return welcome.toString();
    }
}
