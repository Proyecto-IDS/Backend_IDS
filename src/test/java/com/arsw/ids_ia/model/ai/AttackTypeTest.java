package com.arsw.ids_ia.model.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttackTypeTest {

    @Test
    void testAttackType_Values() {
        AttackType[] types = AttackType.values();
        assertNotNull(types);
        assertTrue(types.length > 0);
    }

    @Test
    void testAttackType_SqlInjection() {
        AttackType type = AttackType.SQL_INJECTION;
        assertNotNull(type);
        assertEquals("SQL_INJECTION", type.name());
        assertNotNull(type.getDisplayName());
        assertNotNull(type.getDescription());
    }

    @Test
    void testAttackType_DDoS() {
        AttackType type = AttackType.DDOS;
        assertNotNull(type);
        assertNotNull(type.getDisplayName());
    }

    @Test
    void testAttackType_XSS() {
        AttackType type = AttackType.XSS;
        assertNotNull(type);
        assertNotNull(type.getDisplayName());
    }

    @Test
    void testAttackType_FromPrediction() {
        AttackType type = AttackType.fromPrediction("SQL Injection");
        assertEquals(AttackType.SQL_INJECTION, type);
        
        AttackType unknown = AttackType.fromPrediction(null);
        assertEquals(AttackType.UNKNOWN, unknown);
    }

    @Test
    void testAttackType_AllHaveDisplayName() {
        for (AttackType type : AttackType.values()) {
            assertNotNull(type.getDisplayName());
            assertNotNull(type.getDescription());
        }
    }
}
