package org.forrisco.risk.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageRiskPermissionTest {

	@DisplayName("ManageRiskPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManageRiskPermission permission = new ManageRiskPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Risco", displayName);
	}

	@DisplayName("ManageRiskPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManageRiskPermission permission = new ManageRiskPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.MANAGER.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManageRiskPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManageRiskPermission permission = new ManageRiskPermission();

		String description = permission.getDescription();

		assertEquals("Criar riscos de unidades e subunidades, Editar informações de riscos de unidades e subunidades, "
			+ "Excluir riscos de unidades e subunidades, Cadastrar ações de prevenção dos riscos, Editar ações de prevenção dos riscos, Excluir ações de prevenção dos riscos", description);
	}
}