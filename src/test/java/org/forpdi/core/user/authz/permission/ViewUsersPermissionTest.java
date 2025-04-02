package org.forpdi.core.user.authz.permission;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewUsersPermissionTest {

    @Test
    @DisplayName("Verifica o nome de exibição para permissão de visualização de usuários")
    void testGetDisplayName() {

		ViewUsersPermission permission = new ViewUsersPermission();

        String displayName = permission.getDisplayName();

        assertEquals("Visualizar Usuários", displayName, "O nome exibido não corresponde ao esperado.");
    }

    @Test
    @DisplayName("Verifica o nível de acesso exigido para visualizar usuários")

    void testGetRequiredAccessLevel() {

		ViewUsersPermission permission = new ViewUsersPermission();

        int accessLevel = permission.getRequiredAccessLevel();

        assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), accessLevel, "O nível de acesso requerido não corresponde ao esperado.");
    }

    @Test
    @DisplayName("Verifica a descrição da permissão para visualizar usuários")

    void testGetDescription() {

		ViewUsersPermission permission = new ViewUsersPermission();

        String description = permission.getDescription();

        assertEquals("Listar usuários, Consultar informações de um usuário, Enviar mensagem para um usuário", description, "A descrição não corresponde ao esperado.");
    }
}

