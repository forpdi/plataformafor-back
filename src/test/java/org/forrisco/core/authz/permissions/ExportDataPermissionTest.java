package org.forrisco.core.authz.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportDataPermissionTest {

	@DisplayName("ExportDataPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ExportDataPermission permission = new ExportDataPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Exportar Dados", displayName);
	}

	@DisplayName("ExportDataPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ExportDataPermission permission = new ExportDataPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.COLABORATOR.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ExportDataPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ExportDataPermission permission = new ExportDataPermission();

		String description = permission.getDescription();

		assertEquals("Exportar dados dos planos de risco, Exportar dados de unidades, Exportar dados dos riscos, Exportar dados do painel de bordo", description);
	}
}