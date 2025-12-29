package org.forrisco.core.unit.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditUnitPermissionTest {

	@DisplayName("EditUnitPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		EditUnitPermission permission = new EditUnitPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Editar Unidades", displayName);
	}

	@DisplayName("EditUnitPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		EditUnitPermission permission = new EditUnitPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.MANAGER.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("EditUnitPermission Obter descrição.")
	@Test
	void testGetDescription() {
		EditUnitPermission permission = new EditUnitPermission();

		String description = permission.getDescription();

		assertEquals("Editar informações de unidade e subunidade", description);
	}
}