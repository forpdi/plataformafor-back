package org.forpdi.planning.permissions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class PermissionDTOTest {

	@DisplayName("PermissionDTO Criação do PermissionDTO Setando valores.")
	@Test
	void testPermissionDTOCreation() {

		PermissionDTO permissionDTO = new PermissionDTO();

		permissionDTO.setPermission(null);
		permissionDTO.setGranted(null);
		permissionDTO.setAccessLevel(null);
		permissionDTO.setDescription(null);
		permissionDTO.setType(null);

		assertNull(permissionDTO.getPermission());
		assertNull(permissionDTO.isGranted());
		assertNull(permissionDTO.getAccessLevel());
		assertNull(permissionDTO.getDescription());
		assertNull(permissionDTO.getType());
	}
}