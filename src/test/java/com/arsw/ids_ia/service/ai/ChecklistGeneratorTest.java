package com.arsw.ids_ia.service.ai;

import org.junit.jupiter.api.Test;
import com.arsw.ids_ia.model.ai.AttackType;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ChecklistGeneratorTest {

    private final ChecklistGenerator checklistGenerator = new ChecklistGenerator();

    @Test
    void testGenerateChecklist_SqlInjection() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.SQL_INJECTION, 0.85);
        
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
        assertTrue(checklist.size() >= 5);
    }

    @Test
    void testGenerateChecklist_DDoS() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.DDOS, 0.90);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_XSS() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.XSS, 0.75);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_BruteForce() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.BRUTE_FORCE, 0.80);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_PortScan() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.PORT_SCAN, 0.70);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_AllAttackTypes() {
        for (AttackType attackType : AttackType.values()) {
            List<ChecklistGenerator.ChecklistItem> checklist = 
                checklistGenerator.generateChecklist(attackType, 0.85);
            assertNotNull(checklist, "Checklist should not be null for " + attackType);
        }
    }

    @Test
    void testGenerateChecklist_Malware() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.MALWARE, 0.95);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
        assertTrue(checklist.size() >= 5);
        assertEquals(1, checklist.get(0).getId());
    }

    @Test
    void testGenerateChecklist_Phishing() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.PHISHING, 0.88);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_ManInTheMiddle() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.MAN_IN_THE_MIDDLE, 0.82);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_PrivilegeEscalation() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.PRIVILEGE_ESCALATION, 0.91);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
    }

    @Test
    void testGenerateChecklist_DataExfiltration() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.DATA_EXFILTRATION, 0.93);
        assertNotNull(checklist);
        assertFalse(checklist.isEmpty());
        assertTrue(checklist.size() == 5);
    }

    @Test
    void testChecklistItem_GettersSetters() {
        ChecklistGenerator.ChecklistItem item = new ChecklistGenerator.ChecklistItem(10, "Test action", false);
        
        assertEquals(10, item.getId());
        assertEquals("Test action", item.getLabel());
        assertFalse(item.isDone());
        
        item.setId(20);
        item.setLabel("Updated action");
        item.setDone(true);
        
        assertEquals(20, item.getId());
        assertEquals("Updated action", item.getLabel());
        assertTrue(item.isDone());
    }

    @Test
    void testChecklistItem_AllItemsStartUndone() {
        List<ChecklistGenerator.ChecklistItem> checklist = 
            checklistGenerator.generateChecklist(AttackType.SQL_INJECTION, 0.85);
        
        assertTrue(checklist.stream().noneMatch(ChecklistGenerator.ChecklistItem::isDone));
    }
}
