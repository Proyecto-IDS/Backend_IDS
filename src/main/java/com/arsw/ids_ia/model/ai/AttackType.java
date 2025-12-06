package com.arsw.ids_ia.model.ai;

/**
 * Tipos de ataques que puede detectar el sistema ML
 */
public enum AttackType {
    SQL_INJECTION("SQL Injection", "Inyección SQL detectada"),
    DDOS("DDoS Attack", "Ataque de denegación de servicio"),
    XSS("Cross-Site Scripting", "Script entre sitios"),
    BRUTE_FORCE("Brute Force", "Intento de fuerza bruta"),
    PORT_SCAN("Port Scan", "Escaneo de puertos"),
    MALWARE("Malware", "Software malicioso"),
    PHISHING("Phishing", "Intento de phishing"),
    MAN_IN_THE_MIDDLE("Man in the Middle", "Ataque de intermediario"),
    PRIVILEGE_ESCALATION("Privilege Escalation", "Escalada de privilegios"),
    DATA_EXFILTRATION("Data Exfiltration", "Exfiltración de datos"),
    NORMAL("Normal Traffic", "Tráfico normal"),
    UNKNOWN("Unknown", "Tipo de ataque desconocido");

    private final String displayName;
    private final String description;

    AttackType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convierte una predicción del ML a AttackType
     */
    public static AttackType fromPrediction(String prediction) {
        if (prediction == null || prediction.isEmpty()) {
            return UNKNOWN;
        }

        String normalized = prediction.toUpperCase().replace(" ", "_").replace("-", "_");
        
        // Mapeo de predicciones comunes del ML
        if (normalized.contains("SQL") || normalized.contains("INJECTION")) {
            return SQL_INJECTION;
        } else if (normalized.contains("DDOS") || normalized.contains("DOS")) {
            return DDOS;
        } else if (normalized.contains("XSS") || normalized.contains("SCRIPT")) {
            return XSS;
        } else if (normalized.contains("BRUTE") || normalized.contains("FORCE")) {
            return BRUTE_FORCE;
        } else if (normalized.contains("SCAN") || normalized.contains("PORT")) {
            return PORT_SCAN;
        } else if (normalized.contains("MALWARE") || normalized.contains("VIRUS")) {
            return MALWARE;
        } else if (normalized.contains("PHISHING")) {
            return PHISHING;
        } else if (normalized.contains("MITM") || normalized.contains("MIDDLE")) {
            return MAN_IN_THE_MIDDLE;
        } else if (normalized.contains("PRIVILEGE") || normalized.contains("ESCALATION")) {
            return PRIVILEGE_ESCALATION;
        } else if (normalized.contains("EXFIL") || normalized.contains("DATA")) {
            return DATA_EXFILTRATION;
        } else if (normalized.contains("NORMAL") || normalized.contains("BENIGN")) {
            return NORMAL;
        }

        return UNKNOWN;
    }
}
