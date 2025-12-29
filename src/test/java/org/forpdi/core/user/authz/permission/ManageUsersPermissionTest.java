package org.forpdi.core.user.authz.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ManageUsersPermissionTest {

    @Test
    @DisplayName("Verifica se o nome de exibição corresponde à permissão de gerenciamento de usuários")
    public void test_get_display_name_returns_manage_users() {
        ManageUsersPermission permission = new ManageUsersPermission();
    
        String displayName = permission.getDisplayName();
    
        assertEquals("Gerenciar Usuários", displayName);
    }

    @Test
    @DisplayName("Verifica se a descrição contém caracteres especiais")
    public void test_get_description_contains_special_chars() {
        ManageUsersPermission permission = new ManageUsersPermission();
    
        String description = permission.getDescription();
    
        assertTrue(description.contains(","));
        assertTrue(description.contains("ç"));
        assertTrue(description.contains("õ"));
        assertEquals(8, description.split(",").length);
    }

    @Test
    @DisplayName("Verifica se o nível de acesso exigido é Administrador da Empresa")
    public void test_get_required_access_level_returns_company_admin() {
        ManageUsersPermission permission = new ManageUsersPermission();

        int requiredAccessLevel = permission.getRequiredAccessLevel();

        assertEquals(50, requiredAccessLevel);
    }
}