package org.forpdi.planning.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagePlanPermissionTest {

	ManagePlanPermission managePlanPermission;

	@BeforeEach
	void setup() {

		managePlanPermission = new ManagePlanPermission();
	}

	@DisplayName("ManagePlanPermission Obter o DisplayName.")
	@Test
	void testGetDisplayName() {

		String expectedDisplay = "Gerenciar Planos de Metas";

		String returnedDisplay = managePlanPermission.getDisplayName();

		assertEquals(expectedDisplay, returnedDisplay, "O texto retornado não é o esperado.");
	}

	@DisplayName("ManagePlanPermission Obter o nível de acesso ao Plano.")
	@Test
	void testGetRequiredAccessLevel() {

		int expectedLevel = AccessLevels.MANAGER.getLevel();

		int returnedLevel = managePlanPermission.getRequiredAccessLevel();

		assertEquals(expectedLevel, returnedLevel, "O nível de permissão retornado não é o esperado.");
	}

	@DisplayName("ManagePlanPermission Obter a descrição das ações no Plano.")
	@Test
	void testGetDescription() {

		String expectedDescription = "Criar planos de meta, Criar instância dos níveis na árvore, Excluir instâncias dos níveis, Inserir valores em todos os "
			+ "níveis, Editar valores em todos os níveis, Consultar valores em todos os níveis, "
			+ "Atribuir responsáveis nos níveis, Gerar metas, Concluir metas";

		String returnedDescription = managePlanPermission.getDescription();

		assertEquals(expectedDescription, returnedDescription, "A descrição retornada não é a esperada.");
	}
}