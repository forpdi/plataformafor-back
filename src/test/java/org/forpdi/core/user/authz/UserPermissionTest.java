package org.forpdi.core.user.authz;

import org.forpdi.core.company.Company;
import org.forpdi.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPermissionTest {

	private UserPermission userPermission;

	@BeforeEach
	void setUp() {
		userPermission = new UserPermission();
	}

	@Test
	@DisplayName("Dado uma permissão, quando setPermission é chamado, então a permissão deve ser definida corretamente")
	void testSetPermission() {
		String permission = "READ_ACCESS";

		userPermission.setPermission(permission);

		assertEquals(permission, userPermission.getPermission(), "A permissão não foi definida corretamente.");
	}

	@Test
	@DisplayName("Dado que revoked é false, quando isRevoked é chamado, então deve retornar false")
	void testIsRevoked() {
		userPermission.setRevoked(false);

		boolean result = userPermission.isRevoked();

		assertFalse(result, "O valor de revoked deveria ser false.");
	}

	@Test
	@DisplayName("Dado que revoked é true, quando isRevoked é chamado, então deve retornar true")
	void testIsRevokedTrue() {
		userPermission.setRevoked(true);

		boolean result = userPermission.isRevoked();

		assertTrue(result, "O valor de revoked deveria ser true.");
	}

	@Test
	@DisplayName("Dado um usuário e uma empresa, quando setUser e setCompany são chamados, então os valores devem ser definidos corretamente")
	void testSetUserAndSetCompany() {
		User user = new User();
		Company company = new Company();

		userPermission.setUser(user);
		userPermission.setCompany(company);

		assertEquals(user, userPermission.getUser(), "O usuário não foi configurado corretamente.");
		assertEquals(company, userPermission.getCompany(), "A empresa não foi configurada corretamente.");
	}
}
