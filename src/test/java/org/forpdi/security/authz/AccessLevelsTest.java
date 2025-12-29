package org.forpdi.security.authz;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccessLevelsTest {

    @Test
    @DisplayName("Obtém nível de acesso correspondente")
    void testGetByLevel() {
        int validLevel = 100;
        int invalidLevel = 999;

        AccessLevels validResult = AccessLevels.getByLevel(validLevel);

        assertEquals(AccessLevels.SYSTEM_ADMIN, validResult, "O nível 100 deveria retornar SYSTEM_ADMIN.");
        assertThrows(IllegalArgumentException.class, 
            () -> AccessLevels.getByLevel(invalidLevel), 
            "Níveis inválidos deveriam lançar IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("Obtém lista ordenada por nível de acesso")
    void testGetOrderedByLevel() {

        List<AccessLevels> ordered = AccessLevels.getOrderedByLevel();

        assertEquals(AccessLevels.SYSTEM_ADMIN, ordered.get(0), "O primeiro elemento deve ser SYSTEM_ADMIN.");
        assertEquals(AccessLevels.NONE, ordered.get(ordered.size() - 1), "O último elemento deve ser NONE.");
    }

    @Test
    @DisplayName("Verifica a conversão de nível de acesso em nome de papel")
    void testAsRoleName() {
        AccessLevels systemAdmin = AccessLevels.SYSTEM_ADMIN;
        AccessLevels none = AccessLevels.NONE;

        String systemAdminRoleName = systemAdmin.asRoleName();
        String noneRoleName = none.asRoleName();

        assertEquals("ROLE_SYSTEM_ADMIN", systemAdminRoleName, "O nome do papel deve ser 'ROLE_SYSTEM_ADMIN'.");
        assertEquals("ROLE_NONE", noneRoleName, "O nome do papel deve ser 'ROLE_NONE'.");
    }

	@Test
	void test_returns_all_access_levels_ordered_by_level() {
		int expectedSize = AccessLevels.values().length;

		List<AccessLevels> result = AccessLevels.getOrderedByLevel();

		assertEquals(expectedSize, result.size());
		assertEquals(AccessLevels.SYSTEM_ADMIN, result.get(0));
		assertEquals(AccessLevels.COMPANY_ADMIN, result.get(1));
		assertEquals(AccessLevels.MANAGER, result.get(2));
		assertEquals(AccessLevels.COLABORATOR, result.get(3));
		assertEquals(AccessLevels.AUDITOR, result.get(4));
		assertEquals(AccessLevels.AUTHENTICATED, result.get(5));
		assertEquals(AccessLevels.NONE, result.get(6));
	}

	@Test
	void test_maintains_order_for_same_level_values() {
		List<AccessLevels> originalOrder = Arrays.asList(AccessLevels.values());
		int firstLevel = originalOrder.get(0).getLevel();
		int secondLevel = originalOrder.get(1).getLevel();

		List<AccessLevels> result = AccessLevels.getOrderedByLevel();

		assertTrue(firstLevel > secondLevel);
		assertEquals(originalOrder.get(0), result.get(0));
		assertEquals(originalOrder.get(1), result.get(1));
	}

	@Test
	void testGetOrderedByLevelDescendingOrder() {
		List<AccessLevels> orderedLevels = AccessLevels.getOrderedByLevel();

		for (int i = 0; i < orderedLevels.size() - 1; i++) {
			assertTrue(orderedLevels.get(i).getLevel() > orderedLevels.get(i + 1).getLevel(),
				"List is not sorted in descending order at index " + i);
		}
	}
}


