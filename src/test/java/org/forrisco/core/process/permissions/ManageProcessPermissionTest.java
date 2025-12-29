package org.forrisco.core.process.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageProcessPermissionTest {

	@DisplayName("ManageProcessPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManageProcessPermission permission = new ManageProcessPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Processos", displayName);
	}

	@DisplayName("ManageProcessPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManageProcessPermission permission = new ManageProcessPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.MANAGER.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManageProcessPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManageProcessPermission permission = new ManageProcessPermission();

		String description = permission.getDescription();

		assertEquals("Cadastrar processos em unidade e subunidade, Editar processos em unidade e subunidade, Excluir processos em unidade e subunidade", description);
	}
}