package com.arsw.ids_ia.service.ai;

import java.util.HashMap;
import java.util.Map;

import com.arsw.ids_ia.model.ai.AttackType;

/**
 * Knowledge base con templates de respuesta por tipo de ataque
 */
public class AttackKnowledgeBase {

    private static final Map<AttackType, AttackKnowledge> KNOWLEDGE_BASE = new HashMap<>();

    static {
        // SQL Injection
        KNOWLEDGE_BASE.put(AttackType.SQL_INJECTION, new AttackKnowledge(
            "Inyección SQL detectada en parámetros de entrada. Permite ejecución de queries maliciosos en la base de datos.",
            new String[]{
                "Parámetros sospechosos: OR 1=1, UNION SELECT, DROP TABLE",
                "Intentos de bypass de autenticación",
                "Queries malformados en logs de aplicación"
            },
            Map.of(
                "waf", "Para bloquear SQLi en WAF:\n• ModSecurity: Activa regla OWASP CRS 942100\n• Cloudflare: Security → WAF → Managed Rules → SQL Injection → Block\n• AWS WAF: Añade SQL injection rule set (AWSManagedRulesSQLiRuleSet)",
                "database", "Revisa logs de base de datos:\n• MySQL: SELECT * FROM mysql.general_log WHERE argument LIKE '%UNION%' OR argument LIKE '%OR 1=1%'\n• PostgreSQL: Revisa pg_stat_statements para queries anómalos\n• Busca patrones: DROP, UNION, SELECT @@version",
                "code", "Solución en código:\n• Usa prepared statements/parametrized queries\n• NUNCA concatenes input del usuario directamente\n• Ejemplo Java: PreparedStatement ps = conn.prepareStatement('SELECT * FROM users WHERE id=?'); ps.setInt(1, userId);\n• Valida y sanitiza TODOS los inputs"
            )
        ));

        // DDoS
        KNOWLEDGE_BASE.put(AttackType.DDOS, new AttackKnowledge(
            "Ataque de denegación de servicio detectado. Alto volumen de requests desde múltiples IPs intentando saturar recursos.",
            new String[]{
                "Picos anormales en requests por segundo",
                "Latencia incrementada en servicios",
                "CPU/memoria al límite en servidores"
            },
            Map.of(
                "firewall", "Rate limiting con iptables:\n```bash\niptables -A INPUT -p tcp --dport 80 -m limit --limit 25/minute --limit-burst 100 -j ACCEPT\niptables -A INPUT -p tcp --dport 80 -j DROP\n```\nBloquea IPs específicas:\n```bash\niptables -A INPUT -s <IP_ATACANTE> -j DROP\n```",
                "cloudflare", "Activar protección DDoS en Cloudflare:\n1. Dashboard → Security → DDoS\n2. Activa 'I'm Under Attack Mode' para challenge a visitantes\n3. Ajusta sensitivity level según intensidad\n4. Revisa Analytics → Traffic para identificar patrones",
                "monitoring", "Métricas a monitorear:\n• Requests per second (normal vs actual)\n• Distribución geográfica de IPs\n• User-Agents sospechosos (bots)\n• Patrones de timing (requests simultáneos)\nUsa: tcpdump, netstat, sar para análisis en tiempo real"
            )
        ));

        // XSS
        KNOWLEDGE_BASE.put(AttackType.XSS, new AttackKnowledge(
            "Cross-Site Scripting detectado. Scripts maliciosos inyectados en páginas web que se ejecutan en navegadores de usuarios.",
            new String[]{
                "Tags <script> en inputs de formularios",
                "Event handlers maliciosos (onerror, onload)",
                "Codificación URL/HTML para evadir filtros"
            },
            Map.of(
                "sanitization", "Sanitizar inputs en backend:\n• Java: usar OWASP Java Encoder o Apache Commons Text\n• Python: bleach.clean(user_input)\n• Node.js: DOMPurify o xss library\nHTML encode SIEMPRE antes de renderizar user input",
                "csp", "Implementar Content Security Policy:\n```html\n<meta http-equiv=\"Content-Security-Policy\" \n      content=\"default-src 'self'; script-src 'self' 'nonce-{random}'; object-src 'none';\">\n```\nO en HTTP header:\n```\nContent-Security-Policy: default-src 'self'; script-src 'self'\n```",
                "validation", "Validación de inputs:\n• Blacklist: <script>, <iframe>, javascript:, onerror=\n• Whitelist preferida: solo permitir caracteres alfanuméricos\n• Escapar: < > \" ' / & \n• Validar longitud máxima de campos"
            )
        ));

        // Brute Force
        KNOWLEDGE_BASE.put(AttackType.BRUTE_FORCE, new AttackKnowledge(
            "Ataque de fuerza bruta detectado. Múltiples intentos de autenticación con credenciales diferentes.",
            new String[]{
                "Múltiples fallos de login desde misma IP",
                "Patrones de timing automático",
                "User-agents de herramientas de hacking (Hydra, Medusa)"
            },
            Map.of(
                "blocking", "Bloquear IP después de X intentos:\n```bash\n# Usando fail2ban\nsudo apt install fail2ban\nsudo systemctl enable fail2ban\n```\nConfiguración /etc/fail2ban/jail.local:\n```\n[sshd]\nenabled = true\nmaxretry = 3\nbantime = 3600\n```",
                "captcha", "Implementar CAPTCHA:\n• Google reCAPTCHA v3 (invisible, score-based)\n• hCaptcha (alternativa privacy-friendly)\n• Activar después de 2-3 intentos fallidos\n• No afecta UX de usuarios legítimos",
                "rate_limiting", "Rate limiting en autenticación:\n• Max 5 intentos por IP cada 15 minutos\n• Aumentar delay exponencial: 1s, 2s, 4s, 8s...\n• Lockout temporal de cuenta tras 10 fallos\n• Notificar al usuario legítimo por email"
            )
        ));

        // Port Scan
        KNOWLEDGE_BASE.put(AttackType.PORT_SCAN, new AttackKnowledge(
            "Escaneo de puertos detectado. Reconocimiento activo buscando servicios expuestos y vulnerabilidades.",
            new String[]{
                "Conexiones TCP/SYN a múltiples puertos",
                "Timeout rápido en conexiones",
                "Patrones secuenciales o aleatorios de puertos"
            },
            Map.of(
                "detection", "Detectar escaneos:\n```bash\n# Ver conexiones sospechosas\nnetstat -an | grep SYN_RECV\n# Analizar con tcpdump\ntcpdump -i eth0 'tcp[tcpflags] & (tcp-syn) != 0'\n```\nSignaturas de nmap:\n• SYN scan: muchos SYN sin ACK\n• NULL scan: packets sin flags",
                "blocking", "Bloquear IP escaneadora:\n```bash\niptables -A INPUT -s <IP> -j DROP\n# O usar portsentry para auto-block\nsudo apt install portsentry\n```\nConfigurar /etc/portsentry/portsentry.conf para bloqueo automático",
                "hardening", "Reducir superficie de ataque:\n• Cerrar puertos innecesarios: `sudo ufw deny <port>`\n• Deshabilitar servicios no usados\n• Firewall solo puertos esenciales (80, 443, 22)\n• Usar port knocking para SSH\n• Cambiar puertos default (SSH de 22 a otro)"
            )
        ));

        // Malware
        KNOWLEDGE_BASE.put(AttackType.MALWARE, new AttackKnowledge(
            "Software malicioso detectado. Puede incluir ransomware, trojans, keyloggers o backdoors.",
            new String[]{
                "Procesos sospechosos consumiendo recursos",
                "Conexiones a IPs/dominios maliciosos",
                "Archivos modificados o encriptados"
            },
            Map.of(
                "isolation", "Aislar sistema inmediatamente:\n```bash\n# Desconectar de red\nsudo ifconfig eth0 down\n# O deshabilitar todas las interfaces\nsudo systemctl stop networking\n```\nNO apagar el sistema (pierdes evidencia en RAM)",
                "analysis", "Analizar malware:\n• ClamAV scan: `clamscan -r /home --infected --remove`\n• Revisar procesos: `ps aux | grep -v ]$`\n• Conexiones: `netstat -tupan | grep ESTABLISHED`\n• Archivos recientes: `find / -mtime -1 -type f`\n• Persistence: revisar /etc/cron*, ~/.bashrc, systemd units",
                "removal", "Remoción y recuperación:\n1. Identificar proceso malicioso: kill -9 <PID>\n2. Eliminar binarios: rm -f /path/to/malware\n3. Limpiar persistencia (cron, startup)\n4. Restaurar desde backup conocido limpio\n5. Cambiar TODAS las credenciales del sistema\n6. Reinstalar OS si es ransomware avanzado"
            )
        ));

        // Default para tipos desconocidos
        KNOWLEDGE_BASE.put(AttackType.UNKNOWN, new AttackKnowledge(
            "Actividad anómala detectada. Se requiere análisis manual para determinar la naturaleza del incidente.",
            new String[]{
                "Comportamiento inusual en logs",
                "Patrones de tráfico no identificados",
                "Alertas de sistemas de detección"
            },
            Map.of(
                "analysis", "Pasos de análisis inicial:\n1. Revisar logs detalladamente\n2. Correlacionar eventos de múltiples fuentes\n3. Identificar IPs/usuarios involucrados\n4. Buscar indicadores de compromiso (IOCs)\n5. Consultar threat intelligence feeds",
                "containment", "Medidas de contención generales:\n• Aislar sistema afectado\n• Bloquear IPs sospechosas\n• Cambiar credenciales\n• Habilitar logging verbose\n• Monitorear tráfico saliente",
                "documentation", "Documentar hallazgos:\n• Timestamp de detección\n• Sistemas afectados\n• Indicadores observados\n• Acciones tomadas\n• Resultado de las medidas\nPreservar evidencia para análisis forense"
            )
        ));
    }

    public static AttackKnowledge getKnowledge(AttackType type) {
        return KNOWLEDGE_BASE.getOrDefault(type, KNOWLEDGE_BASE.get(AttackType.UNKNOWN));
    }

    /**
     * Clase que encapsula el conocimiento de un tipo de ataque
     */
    public static class AttackKnowledge {
        private final String description;
        private final String[] symptoms;
        private final Map<String, String> technicalGuides;

        public AttackKnowledge(String description, String[] symptoms, Map<String, String> technicalGuides) {
            this.description = description;
            this.symptoms = symptoms;
            this.technicalGuides = technicalGuides;
        }

        public String getDescription() {
            return description;
        }

        public String[] getSymptoms() {
            return symptoms;
        }

        public String getTechnicalGuide(String topic) {
            return technicalGuides.getOrDefault(topic, "No hay guía disponible para este tema.");
        }

        public Map<String, String> getAllGuides() {
            return technicalGuides;
        }
    }
}
