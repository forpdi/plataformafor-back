package org.forrisco.core.policy.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagePolicyPermissionTest {

	@DisplayName("ManagePolicyPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManagePolicyPermission permission = new ManagePolicyPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Política", displayName);
	}

	@DisplayName("ManagePolicyPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManagePolicyPermission permission = new ManagePolicyPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManagePolicyPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManagePolicyPermission permission = new ManagePolicyPermission();

		String description = permission.getDescription();

		assertEquals("Criar política, Editar informações da política, Excluir política, Cadastrar itens e subitens de uma política, "
			+ "Editar itens e subitens de uma política, Excluir itens e subitens de uma política", description);
	}
}