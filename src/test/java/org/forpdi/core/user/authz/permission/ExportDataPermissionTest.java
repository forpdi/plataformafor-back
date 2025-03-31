package org.forpdi.core.user.authz.permission;

import org.forpdi.core.user.authz.Permission;
import org.forpdi.security.authz.AccessLevels;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExportDataPermissionTest {

	@Test
	@DisplayName("Verifica o nome de exibição retornado ao exportar dados")
	public void test_get_display_name_returns_export_data() {
		ExportDataPermission permission = new ExportDataPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Exportar Dados", displayName);
	}

	@Test
	@DisplayName("Verifica o tipo de permissão de acesso de referência")
	public void test_permission_type_reference_access() {
		Permission permission = new ExportDataPermission();

		assertEquals("Exportar Dados", permission.getDisplayName());
		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), permission.getRequiredAccessLevel());
		assertTrue(permission.getDescription().contains("exportar todos os dados dos planos"));
	}

	@Test
	@DisplayName("Verifica se a descrição retornada corresponde ao texto esperado")
	public void test_get_description_returns_expected_text() {
		ExportDataPermission permission = new ExportDataPermission();

		String description = permission.getDescription();

		assertEquals("Permite o usuário exportar todos os dados dos planos da instituição em formato "
			+ "passível de restauração em outras instalações do sistema.", description);
	}
}