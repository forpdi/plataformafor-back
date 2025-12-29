package org.forpdi.planning.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManageStructurePermissionTest {

	ManageStructurePermission manageStructurePermission;

	@BeforeEach
	void setup() {
		manageStructurePermission = new ManageStructurePermission();
	}

	@DisplayName("ManageStructurePermission Obter o DisplayName.")
	@Test
	void testGetDisplayName() {

		String expectedDisplay = "Gerenciar Estruturas de Planos de Metas";

		String returnedDisplay = manageStructurePermission.getDisplayName();

		assertEquals(expectedDisplay, returnedDisplay, "O texto retornado não é o esperado.");
	}

	@DisplayName("ManageStructurePermission Obter o nível de acesso à Estrutura.")
	@Test
	void testGetRequiredAccessLevel() {

		int expectedLevel = AccessLevels.SYSTEM_ADMIN.getLevel();

		int returnedLevel = manageStructurePermission.getRequiredAccessLevel();

		assertEquals(expectedLevel, returnedLevel, "O nível de permissão retornado não é o esperado.");
	}

	@DisplayName("ManageStructurePermission Obter a descrição das ações na Estrutura.")
	@Test
	void testGetDescription() {

		String expectedDescription = "Importar estruturas de Plano de Metas, Excluir estruturas de Plano de Metas, " +
			"Visualizar estruturas de Plano de Metas";

		String returnedDescription = manageStructurePermission.getDescription();

		assertEquals(expectedDescription, returnedDescription, "A descrição retornada não é a esperada.");
	}
}