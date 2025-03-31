package org.forrisco.core.plan.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagePlanRiskPermissionTest {

	@DisplayName("ManagePlanRiskPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManagePlanRiskPermission permission = new ManagePlanRiskPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Plano de Risco", displayName);
	}

	@DisplayName("ManagePlanRiskPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManagePlanRiskPermission permission = new ManagePlanRiskPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManagePlanRiskPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManagePlanRiskPermission permission = new ManagePlanRiskPermission();

		String description = permission.getDescription();

		assertEquals("Criar plano de risco, Editar informações do plano de risco, Excluir plano de risco, Cadastrar itens e subitens de um plano de risco, "
			+ "Editar itens e subitens de um plano de risco, Excluir itens e subitens de um plano de risco", description);
	}
}