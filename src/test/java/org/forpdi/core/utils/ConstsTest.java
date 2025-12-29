package org.forpdi.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class ConstsTest {

    @Test
    @DisplayName("Constantes devem ter os valores esperados")
    void constants_haveExpectedValues() {
        assertEquals(5, Consts.MIN_PAGE_SIZE);
        assertEquals(5, Consts.MED_PAGE_SIZE);
        assertEquals("api/file", Consts.FILES_ENDPOINT_BASE_URL);
    }

    @Test
    @DisplayName("Constantes devem ser public static final")
    void constants_arePublicStaticFinal() throws Exception {
        for (String fieldName : new String[] { "MIN_PAGE_SIZE", "MED_PAGE_SIZE", "FILES_ENDPOINT_BASE_URL" }) {
            Field f = Consts.class.getDeclaredField(fieldName);
            int mods = f.getModifiers();
            assertTrue(Modifier.isPublic(mods), fieldName + " deve ser public");
            assertTrue(Modifier.isStatic(mods), fieldName + " deve ser static");
            assertTrue(Modifier.isFinal(mods), fieldName + " deve ser final");
        }
    }

    @Test
    @DisplayName("Constantes não devem ser nulas (para strings)")
    void stringConstants_notNull() {
        assertNotNull(Consts.FILES_ENDPOINT_BASE_URL);
        assertFalse(Consts.FILES_ENDPOINT_BASE_URL.isBlank());
    }
}
