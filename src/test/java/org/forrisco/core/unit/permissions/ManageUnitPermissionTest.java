package org.forrisco.core.unit.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageUnitPermissionTest {

	@DisplayName("ManageUnitPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManageUnitPermission permission = new ManageUnitPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Unidades", displayName);
	}

	@DisplayName("ManageUnitPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManageUnitPermission permission = new ManageUnitPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManageUnitPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManageUnitPermission permission = new ManageUnitPermission();

		String description = permission.getDescription();

		assertEquals("Criar unidade e subunidade, Editar informações de unidade e subunidade, Excluir unidade e subunidade", description);
	}
}