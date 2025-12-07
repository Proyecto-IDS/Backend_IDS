package com.arsw.ids_ia.service.ai;

import org.junit.jupiter.api.Test;
import com.arsw.ids_ia.model.ai.AttackType;
import static org.junit.jupiter.api.Assertions.*;

class AttackKnowledgeBaseTest {

    @Test
    void testGetKnowledge_SqlInjection() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.SQL_INJECTION);
        assertNotNull(knowledge);
        assertNotNull(knowledge.getDescription());
        assertTrue(knowledge.getDescription().length() > 0);
    }

    @Test
    void testGetKnowledge_DDoS() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.DDOS);
        assertNotNull(knowledge);
        assertNotNull(knowledge.getDescription());
    }

    @Test
    void testGetKnowledge_XSS() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.XSS);
        assertNotNull(knowledge);
        assertNotNull(knowledge.getDescription());
    }

    @Test
    void testGetKnowledge_BruteForce() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.BRUTE_FORCE);
        assertNotNull(knowledge);
    }

    @Test
    void testGetKnowledge_AllAttackTypes() {
        for (AttackType attackType : AttackType.values()) {
            AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(attackType);
            assertNotNull(knowledge, "Knowledge should not be null for " + attackType);
            assertNotNull(knowledge.getDescription());
        }
    }

    @Test
    void testGetKnowledge_Symptoms() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.SQL_INJECTION);
        String[] symptoms = knowledge.getSymptoms();
        
        assertNotNull(symptoms);
        assertTrue(symptoms.length > 0);
        assertTrue(symptoms.length >= 3);
    }

    @Test
    void testGetKnowledge_TechnicalGuide() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.DDOS);
        String guide = knowledge.getTechnicalGuide("firewall");
        
        assertNotNull(guide);
        assertTrue(guide.length() > 0);
        assertTrue(guide.contains("iptables") || guide.contains("Rate limiting"));
    }

    @Test
    void testGetKnowledge_TechnicalGuide_UnknownTopic() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.XSS);
        String guide = knowledge.getTechnicalGuide("nonexistent_topic");
        
        assertNotNull(guide);
        assertEquals("No hay guía disponible para este tema.", guide);
    }

    @Test
    void testGetKnowledge_GetAllGuides() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.BRUTE_FORCE);
        var allGuides = knowledge.getAllGuides();
        
        assertNotNull(allGuides);
        assertFalse(allGuides.isEmpty());
        assertTrue(allGuides.containsKey("blocking") || allGuides.containsKey("captcha"));
    }

    @Test
    void testGetKnowledge_Malware() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.MALWARE);
        assertNotNull(knowledge);
        assertNotNull(knowledge.getDescription());
        assertTrue(knowledge.getDescription().contains("malicioso"));
    }

    @Test
    void testGetKnowledge_PortScan() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.PORT_SCAN);
        String[] symptoms = knowledge.getSymptoms();
        assertNotNull(symptoms);
        assertTrue(symptoms.length > 0);
    }

    @Test
    void testGetKnowledge_Unknown_ReturnsDefault() {
        AttackKnowledgeBase.AttackKnowledge knowledge = AttackKnowledgeBase.getKnowledge(AttackType.UNKNOWN);
        assertNotNull(knowledge);
        assertTrue(knowledge.getDescription().contains("anómala") || knowledge.getDescription().contains("manual"));
    }
}
