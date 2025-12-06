package com.arsw.ids_ia.service.ai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.arsw.ids_ia.model.ai.AttackType;

/**
 * Generador de checklists basado en tipo de ataque
 */
@Service
public class ChecklistGenerator {

    /**
     * Genera una checklist de acciones para un tipo de ataque específico
     */
    public List<ChecklistItem> generateChecklist(AttackType attackType, double attackProbability) {
        List<ChecklistItem> items = new ArrayList<>();
        
        switch (attackType) {
            case SQL_INJECTION:
                items.add(new ChecklistItem(1, "Bloquear parámetros SQL maliciosos en WAF", false));
                items.add(new ChecklistItem(2, "Revisar logs de base de datos (últimas 2 horas)", false));
                items.add(new ChecklistItem(3, "Validar prepared statements en código vulnerable", false));
                items.add(new ChecklistItem(4, "Cambiar credenciales si hubo acceso no autorizado", false));
                items.add(new ChecklistItem(5, "Documentar vector de ataque identificado", false));
                break;
                
            case DDOS:
                items.add(new ChecklistItem(1, "Activar rate limiting en firewall perimetral", false));
                items.add(new ChecklistItem(2, "Identificar IPs origen del ataque", false));
                items.add(new ChecklistItem(3, "Configurar blacklist temporal", false));
                items.add(new ChecklistItem(4, "Monitorear ancho de banda y latencia", false));
                items.add(new ChecklistItem(5, "Notificar a ISP si el ataque persiste", false));
                break;
                
            case XSS:
                items.add(new ChecklistItem(1, "Identificar endpoint vulnerable a XSS", false));
                items.add(new ChecklistItem(2, "Sanitizar inputs y outputs en aplicación", false));
                items.add(new ChecklistItem(3, "Implementar Content Security Policy (CSP)", false));
                items.add(new ChecklistItem(4, "Revisar cookies y sesiones comprometidas", false));
                items.add(new ChecklistItem(5, "Actualizar librería de sanitización", false));
                break;
                
            case BRUTE_FORCE:
                items.add(new ChecklistItem(1, "Bloquear IP origen de intentos fallidos", false));
                items.add(new ChecklistItem(2, "Activar CAPTCHA en formulario de login", false));
                items.add(new ChecklistItem(3, "Implementar rate limiting en autenticación", false));
                items.add(new ChecklistItem(4, "Revisar cuentas comprometidas", false));
                items.add(new ChecklistItem(5, "Forzar cambio de contraseñas débiles", false));
                break;
                
            case PORT_SCAN:
                items.add(new ChecklistItem(1, "Identificar IP que realiza el escaneo", false));
                items.add(new ChecklistItem(2, "Bloquear IP en firewall perimetral", false));
                items.add(new ChecklistItem(3, "Verificar servicios expuestos innecesariamente", false));
                items.add(new ChecklistItem(4, "Cerrar puertos no utilizados", false));
                items.add(new ChecklistItem(5, "Revisar logs para otros intentos de reconocimiento", false));
                break;
                
            case MALWARE:
                items.add(new ChecklistItem(1, "Aislar sistema infectado de la red", false));
                items.add(new ChecklistItem(2, "Ejecutar análisis antivirus completo", false));
                items.add(new ChecklistItem(3, "Identificar vector de entrada del malware", false));
                items.add(new ChecklistItem(4, "Revisar persistencia en startup/registry", false));
                items.add(new ChecklistItem(5, "Restaurar desde backup limpio si es necesario", false));
                break;
                
            case PHISHING:
                items.add(new ChecklistItem(1, "Bloquear dominio/IP del sitio de phishing", false));
                items.add(new ChecklistItem(2, "Notificar a usuarios afectados", false));
                items.add(new ChecklistItem(3, "Revisar credenciales comprometidas", false));
                items.add(new ChecklistItem(4, "Forzar reset de contraseñas para usuarios afectados", false));
                items.add(new ChecklistItem(5, "Reportar el sitio a autoridades competentes", false));
                break;
                
            case MAN_IN_THE_MIDDLE:
                items.add(new ChecklistItem(1, "Verificar certificados SSL/TLS", false));
                items.add(new ChecklistItem(2, "Identificar punto de interceptación", false));
                items.add(new ChecklistItem(3, "Forzar HTTPS en todas las comunicaciones", false));
                items.add(new ChecklistItem(4, "Implementar certificate pinning", false));
                items.add(new ChecklistItem(5, "Revisar logs de autenticación comprometida", false));
                break;
                
            case PRIVILEGE_ESCALATION:
                items.add(new ChecklistItem(1, "Identificar cuenta con privilegios elevados", false));
                items.add(new ChecklistItem(2, "Revocar permisos innecesarios", false));
                items.add(new ChecklistItem(3, "Revisar logs de sudo/runas", false));
                items.add(new ChecklistItem(4, "Parchear vulnerabilidad que permitió escalada", false));
                items.add(new ChecklistItem(5, "Auditar permisos de todos los usuarios", false));
                break;
                
            case DATA_EXFILTRATION:
                items.add(new ChecklistItem(1, "Bloquear conexiones salientes sospechosas", false));
                items.add(new ChecklistItem(2, "Identificar datos comprometidos", false));
                items.add(new ChecklistItem(3, "Revisar logs de transferencia de archivos", false));
                items.add(new ChecklistItem(4, "Implementar DLP (Data Loss Prevention)", false));
                items.add(new ChecklistItem(5, "Notificar a usuarios afectados si aplica", false));
                break;
                
            default:
                items.add(new ChecklistItem(1, "Analizar logs del incidente", false));
                items.add(new ChecklistItem(2, "Identificar origen del tráfico sospechoso", false));
                items.add(new ChecklistItem(3, "Implementar medidas de contención", false));
                items.add(new ChecklistItem(4, "Documentar hallazgos", false));
                items.add(new ChecklistItem(5, "Revisar políticas de seguridad", false));
                break;
        }
        
        return items;
    }

    /**
     * Clase interna para representar un item de checklist
     */
    public static class ChecklistItem {
        private int id;
        private String label;
        private boolean done;

        public ChecklistItem(int id, String label, boolean done) {
            this.id = id;
            this.label = label;
            this.done = done;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }
    }
}
