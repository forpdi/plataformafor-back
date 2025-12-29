package org.forpdi.planning.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManageDocumentPermissionTest {

	private ManageDocumentPermission manageDocumentPermission;

	@BeforeEach
	void setup() {
		manageDocumentPermission = new ManageDocumentPermission();
	}

	@DisplayName("ManageDocumentPermission Obter o DisplayName.")
	@Test
	void testGetDisplayName() {

		String expectedDisplay = "Gerenciar Documento";

		String returnedDisplay = manageDocumentPermission.getDisplayName();

		assertEquals(expectedDisplay, returnedDisplay, "O texto retornado não é o esperado.");
	}

	@DisplayName("ManageDocumentPermission Obter o acesso necessário para o documento.")
	@Test
	void testGetRequiredAccessLevel() {

		int expectedLevel = AccessLevels.MANAGER.getLevel();

		int returnedLevel = manageDocumentPermission.getRequiredAccessLevel();

		assertEquals(expectedLevel, returnedLevel, "O nível de permissão retornado não é o esperado.");

	}

	@DisplayName("ManageDocumentPermission Obter a descrição das ações no documento.")
	@Test
	void testGetDescription() {

		String expectedDescription = "Criar novas seções e subseções, Excluir seções e subseções, Editar seções e subseções,"
			+ " Criar novos campos, Excluir campos, Editar campos, Inserir valores nos campos,"
			+ " Editar valores dos campos, Exportar o documento";

		String returnedDescription = manageDocumentPermission.getDescription();

		assertEquals(expectedDescription, returnedDescription, "A descrição retornada não é a esperada.");

	}
}