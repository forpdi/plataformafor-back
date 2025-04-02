package org.forpdi.core.user.authz.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RestoreDataPermissionTest {

    @Test
    @DisplayName("Verifica se o nome de exibição para restaurar dados está correto")
    public void test_get_display_name_returns_correct_value() {
        RestoreDataPermission permission = new RestoreDataPermission();
    
        String displayName = permission.getDisplayName();
    
        assertEquals("Restaurar/Importar Dados", displayName);
    }

    @Test
    @DisplayName("Verifica se o nome de exibição para restaurar dados não é nulo ou vazio")
    public void test_get_display_name_not_null_or_empty() {
        RestoreDataPermission permission = new RestoreDataPermission();
    
        String displayName = permission.getDisplayName();
    
        assertNotNull(displayName);
        assertFalse(displayName.isEmpty());
    }

    @Test
    @DisplayName("Verifica se o nível de acesso exigido para restaurar dados é de Administrador da Empresa")
    public void test_get_required_access_level_returns_company_admin_value() {
        RestoreDataPermission permission = new RestoreDataPermission();

        int requiredAccessLevel = permission.getRequiredAccessLevel();

        assertEquals(50, requiredAccessLevel);
    }

    @Test
    @DisplayName("Verifica se a descrição não contém caracteres malformados")
    public void test_description_contains_no_malformed_characters() {
        RestoreDataPermission permission = new RestoreDataPermission();

        String description = permission.getDescription();

        assertFalse(description.contains("\uFFFD"), "Description contains malformed characters");
    }
}