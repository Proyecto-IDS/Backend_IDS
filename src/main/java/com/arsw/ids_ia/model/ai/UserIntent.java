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

        String normalized = normalize(message);

        if (containsAny(normalized, COMPLETION_KEYWORDS)) {
            return COMPLETION;
        }
        if (containsAny(normalized, NEXT_STEP_KEYWORDS)) {
            return NEXT_STEP;
        }
        if (containsAny(normalized, HOW_TO_KEYWORDS)) {
            return HOW_TO;
        }
        if (containsAny(normalized, SEVERITY_KEYWORDS)) {
            return SEVERITY_CHECK;
        }
        if (containsAny(normalized, EXPLANATION_KEYWORDS)) {
            return EXPLANATION;
        }
        if (containsAny(normalized, STATUS_KEYWORDS)) {
            return STATUS_CHECK;
        }
        if (containsAny(normalized, HELP_KEYWORDS)) {
            return HELP;
        }

        return UNKNOWN;
    }

    // Keyword lists
    private static final String[] COMPLETION_KEYWORDS = {
            "listo", "completado", "hecho", "terminado", "finalizado", "ok"
    };
    private static final String[] NEXT_STEP_KEYWORDS = {
            "que hago", "que sigue", "siguiente", "proximo paso", "ahora que"
    };
    private static final String[] HOW_TO_KEYWORDS = {
            "como", "ayuda con", "instrucciones", "pasos para", "guia", "tutorial"
    };
    private static final String[] SEVERITY_KEYWORDS = {
            "es grave", "urgente", "prioridad", "critico", "importante", "riesgo"
    };
    private static final String[] EXPLANATION_KEYWORDS = {
            "por que", "que es", "explica", "detalla", "cual es el problema"
    };
    private static final String[] STATUS_KEYWORDS = {
            "estado", "progreso", "como vamos", "avance", "situacion"
    };
    private static final String[] HELP_KEYWORDS = {
            "ayuda", "help", "auxilio", "no se", "que hacer"
    };

    private static boolean containsAny(String text, String[] needles) {
        for (String n : needles) {
            if (text.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String input) {
        String lower = input.toLowerCase().trim();
        java.text.Normalizer.Form form = java.text.Normalizer.Form.NFD;
        String decomposed = java.text.Normalizer.normalize(lower, form);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
