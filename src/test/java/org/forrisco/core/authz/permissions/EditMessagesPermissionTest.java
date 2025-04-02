package org.forrisco.core.authz.permissions;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditMessagesPermissionTest {

	@DisplayName("EditMessagesPermission Obter nome de exibição.")
	@Test
	void testGetDisplayName() {
		EditMessagesPermission permission = new EditMessagesPermission();

		String displayName = permission.getDisplayName();

		assertEquals("Editar Textos do Sistema", displayName);
	}

	@DisplayName("EditMessagesPermission Obter permissão necessária")
	@Test
	void testGetRequiredAccessLevel() {
		EditMessagesPermission permission = new EditMessagesPermission();

		int requiredLevel = permission.getRequiredAccessLevel();

		assertEquals(AccessLevels.COMPANY_ADMIN.getLevel(), requiredLevel);
		assertTrue(requiredLevel > AccessLevels.AUTHENTICATED.getLevel());
	}

	@DisplayName("EditMessagesPermission Obter descrição.")
	@Test
	void testGetDescription() {
		EditMessagesPermission permission = new EditMessagesPermission();

		String description = permission.getDescription();

		assertEquals("Editar textos e mensagens do sistema para esta instituição.", description);
	}
}