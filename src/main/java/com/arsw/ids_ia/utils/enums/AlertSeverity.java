package com.arsw.ids_ia.utils.enums;

/**
 * Representa la severidad de una alerta según el modelo de ML.
 * 
 * Clasificación basada en attack_probability:
 * - NORMAL: < 0.3 (inofensivo, no requiere alerta)
 * - FALSO_POSITIVO: 0.3 - 0.7 (requiere monitoreo)
 * - CONOCIDO: 0.7 - 0.9 (amenaza conocida, requiere acción)
 * - CRITICO: >= 0.9 (amenaza crítica, requiere acción inmediata)
 */
public enum AlertSeverity {
    NORMAL,
    FALSO_POSITIVO,
    CONOCIDO,
    CRITICO;

    /**
     * Determina la severidad basada en la probabilidad de ataque.
     * 
     * @param attackProbability Probabilidad de ataque (0.0 - 1.0)
     * @return Severidad correspondiente
     */
    public static AlertSeverity fromAttackProbability(double attackProbability) {
        if (attackProbability < 0.3) {
            return NORMAL;
        } else if (attackProbability < 0.7) {
            return FALSO_POSITIVO;
        } else if (attackProbability < 0.9) {
            return CONOCIDO;
        } else {
            return CRITICO;
        }
    }

    /**
     * Convierte a formato legacy (lowercase) para compatibilidad.
     */
    public String toLegacyFormat() {
        return this.name().toLowerCase();
    }
}
