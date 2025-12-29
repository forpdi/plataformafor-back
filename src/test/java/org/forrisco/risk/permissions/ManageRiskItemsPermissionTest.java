package org.forrisco.risk.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageRiskItemsPermissionTest {

	@DisplayName("ManageRiskItemsPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManageRiskItemsPermission permission = new ManageRiskItemsPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Monitoramento, Incidentes e Contingenciamento", displayName);
	}

	@DisplayName("ManageRiskItemsPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManageRiskItemsPermission permission = new ManageRiskItemsPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.MANAGER.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManageRiskItemsPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManageRiskItemsPermission permission = new ManageRiskItemsPermission();

		String description = permission.getDescription();

		assertEquals("Cadastrar monitorimento, Editar monitoramento, Excluir monitoramento, Cadastrar incidentes, Editar incidentes, "
			+ "Excluir incidentes, Cadastrar contingenciamento, Editar contingenciamento, Excluir contingenciamento", description);
	}
}