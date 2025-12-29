package org.forpdi.planning.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagePlanMacroPermissionTest {

	ManagePlanMacroPermission managePlanMacroPermission;

	@BeforeEach
	void setup() {
		managePlanMacroPermission = new ManagePlanMacroPermission();
	}

	@DisplayName("ManagePlanMacroPermission Obter o DisplayName.")
	@Test
	void testGetDisplayName() {

		String expectedDisplay = "Gerenciar Planos";

		String returnedDisplay = managePlanMacroPermission.getDisplayName();

		assertEquals(expectedDisplay, returnedDisplay, "O texto retornado não é o esperado.");
	}

	@DisplayName("ManagePlanMacroPermission Obter o nível de acesso do Plan Macro.")
	@Test
	void testGetRequiredAccessLevel() {

		int expectedLevel = AccessLevels.COMPANY_ADMIN.getLevel();

		int returnedLevel = managePlanMacroPermission.getRequiredAccessLevel();

		assertEquals(expectedLevel, returnedLevel, "O nível de permissão retornado não é o esperado.");
	}

	@DisplayName("ManagePlanMacroPermission Obter a descrição das ações no Plan Macro.")
	@Test
	void testGetDescription() {

		String expectedDescription =
			"Criar plano macro, Editar informações do plano macro, Duplicar plano macro, Arquivar plano macro";

		String returnedDescription = managePlanMacroPermission.getDescription();

		assertEquals(expectedDescription, returnedDescription, "A descrição retornada não é a esperada.");
	}
}