package org.forpdi.planning.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateGoalPermissionTest {

	UpdateGoalPermission updateGoalPermission;

	@BeforeEach
	void setup() {
		updateGoalPermission = new UpdateGoalPermission();
	}

	@DisplayName("UpdateGoalPermission Obter o DisplayName.")
	@Test
	void testGetDisplayName() {

		String expectedDisplay = "Atualizar Metas";

		String returnedDisplay = updateGoalPermission.getDisplayName();

		assertEquals(expectedDisplay, returnedDisplay, "O texto retornado não é o esperado.");
	}

	@DisplayName("UpdateGoalPermission Obter o nível de acesso a atualização da Meta..")
	@Test
	void testGetRequiredAccessLevel() {

		int expectedLevel = AccessLevels.COLABORATOR.getLevel();

		int returnedLevel = updateGoalPermission.getRequiredAccessLevel();

		assertEquals(expectedLevel, returnedLevel, "O nível de permissão retornado não é o esperado.");
	}

	@DisplayName("UpdateGoalPermission Obter a descrição das realizadas na atualização da Meta..")
	@Test
	void testGetDescription() {

		String expectedDescription =
			"Inserir valor alcançado, Concluir meta (Colaborador: apenas se for o responsável pela meta)";

		String returnedDescription = updateGoalPermission.getDescription();

		assertEquals(expectedDescription, returnedDescription, "A descrição retornada não é a esperada.");
	}
}