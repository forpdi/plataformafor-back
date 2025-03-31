package org.forpdi.core.user.authz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.forpdi.security.authz.AccessLevels;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PermissionFactoryTest {

    @Test
    @DisplayName("Verifica se a instância é singleton")
    void test_get_instance_returns_singleton() {
        PermissionFactory instance1 = PermissionFactory.getInstance();
        PermissionFactory instance2 = PermissionFactory.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Testa se o construtor da fábrica de permissões é privado")
    void test_constructor_is_private() {
        Constructor<?>[] constructors = PermissionFactory.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        Constructor<?> constructor = constructors[0];
        assertFalse(constructor.isAccessible());
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    @DisplayName("Verifica se todas as permissões padrão estão registradas")

    void test_all_default_permissions_registered() {
        PermissionFactory factory = PermissionFactory.getInstance();
    
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.ManageUsersPermission"));
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.ViewUsersPermission"));
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.EditMessagesPermission"));
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.ExportDataPermission"));

    }

    @Test
    @DisplayName("Testa se a permissão de Administrador do Sistema possui o nível de acesso correto")
    void test_system_admin_permission_access_level() {
        PermissionFactory factory = PermissionFactory.getInstance();
        PermissionFactory.SystemAdminPermission permission = factory.new SystemAdminPermission();
    
        int expectedAccessLevel = AccessLevels.SYSTEM_ADMIN.getLevel();
        int actualAccessLevel = permission.getRequiredAccessLevel();
    
        assertEquals(expectedAccessLevel, actualAccessLevel);
    }

    @Test
    @DisplayName("Testa o registro de permissões padrão")
    void test_permissions_registration() {
        PermissionFactory factory = PermissionFactory.getInstance();
    

        assertNotNull(factory.get("org.forrisco.core.authz.permissions.ManageUsersPermission"));
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.ViewUsersPermission"));
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.EditMessagesPermission"));
        assertNotNull(factory.get("org.forrisco.core.authz.permissions.ExportDataPermission"));

    }

    @Test
    @DisplayName("Verifica se a tentativa de registrar permissão nula lança exceção")
    void register_null_permission_throws_exception() {
        PermissionFactory factory = PermissionFactory.getInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            factory.register(null);
        });
    }

	@Test
	@DisplayName("Verifica se as permissões têm os atributos corretos")

	void test_permissions_have_correct_attributes() {
		PermissionFactory factory = PermissionFactory.getInstance();

		Permission exportDataPermission = factory.get("org.forpdi.core.user.authz.permission.ExportDataPermission");
		assertNotNull(exportDataPermission);
		assertEquals("Exportar Dados", exportDataPermission.getDisplayName());
		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), exportDataPermission.getRequiredAccessLevel());
		assertEquals("Permite o usuário exportar todos os dados dos planos da instituição em formato passível de restauração em outras instalações do sistema.", exportDataPermission.getDescription());

		Permission restoreDataPermission = factory.get("org.forpdi.core.user.authz.permission.RestoreDataPermission");
		assertNotNull(restoreDataPermission);
		assertEquals("Restaurar/Importar Dados", restoreDataPermission.getDisplayName());
		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), restoreDataPermission.getRequiredAccessLevel());
		assertEquals("Permite o usuário restaurar todos os dados dos planos de uma instituição a partir de um arquivo exportado do próprio sistema.", restoreDataPermission.getDescription());

		Permission manageUsersPermission = factory.get("org.forpdi.core.user.authz.permission.ManageUsersPermission");
		assertNotNull(manageUsersPermission);
		assertEquals("Gerenciar Usuários", manageUsersPermission.getDisplayName());
		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), manageUsersPermission.getRequiredAccessLevel());
		assertEquals("Cadastrar usuários, Listar usuários, Excluir usuário, Bloquear Usuários, Consultar informações do usuário, Editar usuário, Alterar permissões locais do usuário, Importar usuários", manageUsersPermission.getDescription());
	}
}