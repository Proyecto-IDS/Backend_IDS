package com.arsw.ids_ia.model.ai;

/**
 * Intenciones que puede expresar el usuario en el chat
 */
public enum UserIntent {
    NEXT_STEP("Solicita el próximo paso"),
    HOW_TO("Pide instrucciones técnicas"),
    SEVERITY_CHECK("Pregunta sobre gravedad"),
    EXPLANATION("Solicita explicación del ataque"),
    COMPLETION("Reporta tarea completada"),
    STATUS_CHECK("Consulta estado actual"),
    HELP("Pide ayuda general"),
    UNKNOWN("Intención no identificada");

    private final String description;

    UserIntent(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Detecta la intención del usuario basado en palabras clave
     */
    public static UserIntent detectIntent(String message) {
        if (message == null || message.isEmpty()) {
            return UNKNOWN;
        }

        String normalized = message.toLowerCase().trim();

        // Patrones de completado
        if (normalized.matches(".*(listo|completado|hecho|terminado|finalizado|ok).*")) {
            return COMPLETION;
        }

        // Patrones de siguiente paso
        if (normalized.matches(".*(qu[eé] hago|qu[eé] sigue|siguiente|pr[oó]ximo paso|ahora qu[eé]).*")) {
            return NEXT_STEP;
        }

        // Patrones de cómo hacer
        if (normalized.matches(".*(c[oó]mo|ayuda con|instrucciones|pasos para|gu[ií]a|tutorial).*")) {
            return HOW_TO;
        }

        // Patrones de severidad
        if (normalized.matches(".*(es grave|urgente|prioridad|cr[ií]tico|importante|riesgo).*")) {
            return SEVERITY_CHECK;
        }

        // Patrones de explicación
        if (normalized.matches(".*(por qu[eé]|qu[eé] es|explica|detalla|cu[aá]l es el problema).*")) {
            return EXPLANATION;
        }

        // Patrones de estado
        if (normalized.matches(".*(estado|progreso|c[oó]mo vamos|avance|situaci[oó]n).*")) {
            return STATUS_CHECK;
        }

        // Patrones de ayuda
        if (normalized.matches(".*(ayuda|help|auxilio|no s[eé]|qu[eé] hacer).*")) {
            return HELP;
        }

        return UNKNOWN;
    }
}
