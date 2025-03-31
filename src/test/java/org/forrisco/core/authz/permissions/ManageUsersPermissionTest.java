package org.forrisco.core.authz.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManageUsersPermissionTest {

	@DisplayName("ManageUsersPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		ManageUsersPermission permission = new ManageUsersPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Gerenciar Usuários", displayName);
	}

	@DisplayName("ManageUsersPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		ManageUsersPermission permission = new ManageUsersPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("ManageUsersPermission Obter descrição.")
	@Test
	void testGetDescription() {
		ManageUsersPermission permission = new ManageUsersPermission();

		String description = permission.getDescription();

		assertEquals("Cadastrar usuários, Listar usuários, Excluir usuário, Bloquear Usuários,"
			+ " Consultar informações do usuário, Editar usuário, Alterar permissões locais do usuário, Importar usuários", description);
	}
}