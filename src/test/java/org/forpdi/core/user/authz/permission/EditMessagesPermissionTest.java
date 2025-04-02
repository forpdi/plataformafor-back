package org.forpdi.core.user.authz.permission;

import org.forpdi.security.authz.AccessLevels;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EditMessagesPermissionTest {

    @Test
    @DisplayName("Verifica se o nível de acesso exigido para edição de mensagens é de Administrador da Empresa")
    public void test_required_access_level_is_company_admin() {
        EditMessagesPermission permission = new EditMessagesPermission();

        int requiredLevel = permission.getRequiredAccessLevel();
    
        assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);
        assertEquals(50, requiredLevel);
    }

    @Test
    @DisplayName("Verifica se o acesso de usuário não autenticado é negado")
    public void test_non_authenticated_user_access_denied() {
        EditMessagesPermission permission = new EditMessagesPermission();
    
        int requiredLevel = permission.getRequiredAccessLevel();
        int nonAuthenticatedLevel = AccessLevels.NONE.getLevel();
    
        assertTrue(requiredLevel > nonAuthenticatedLevel);
        assertNotEquals(AccessLevels.AUTHENTICATED.getLevel(), requiredLevel);
    }

    @Test
    @DisplayName("Verifica se os métodos de edição de mensagens são sobrescritos corretamente")
    public void test_methods_override_correctly() {
        EditMessagesPermission permission = new EditMessagesPermission();

        String displayName = permission.getDisplayName();
        assertEquals("Editar Textos do Sistema", displayName);

        int requiredLevel = permission.getRequiredAccessLevel();
        assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);

        String description = permission.getDescription();
        assertEquals("Editar textos e mensagens do sistema para esta instituição.", description);
    }

    @Test
    @DisplayName("Verifica se o nível de permissão é imutável após a criação")
    public void test_permission_level_immutable_after_creation() {
        EditMessagesPermission permission = new EditMessagesPermission();

        int initialLevel = permission.getRequiredAccessLevel();

        int modifiedLevel = permission.getRequiredAccessLevel();

        assertEquals(initialLevel, modifiedLevel);
        assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), initialLevel);
    }
}