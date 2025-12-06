package com.arsw.ids_ia.model.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserIntentTest {

    @Test
    void testUserIntent_Values() {
        UserIntent[] intents = UserIntent.values();
        assertNotNull(intents);
        assertTrue(intents.length > 0);
    }

    @Test
    void testUserIntent_Help() {
        UserIntent intent = UserIntent.HELP;
        assertNotNull(intent);
        assertEquals("HELP", intent.name());
        assertNotNull(intent.getDescription());
    }

    @Test
    void testUserIntent_NextStep() {
        UserIntent intent = UserIntent.NEXT_STEP;
        assertNotNull(intent);
        assertNotNull(intent.getDescription());
    }

    @Test
    void testUserIntent_DetectIntent() {
        UserIntent intent = UserIntent.detectIntent("¿Qué hago ahora?");
        assertNotNull(intent);
        
        UserIntent unknown = UserIntent.detectIntent(null);
        assertEquals(UserIntent.UNKNOWN, unknown);
    }

    @Test
    void testUserIntent_AllHaveDescription() {
        for (UserIntent intent : UserIntent.values()) {
            assertNotNull(intent.getDescription());
        }
    }
}
