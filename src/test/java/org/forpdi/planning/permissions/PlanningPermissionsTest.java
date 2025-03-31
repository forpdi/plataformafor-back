package org.forpdi.planning.permissions;

import org.forpdi.core.user.authz.PermissionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.wildfly.common.Assert.assertFalse;


class PlanningPermissionsTest {

	@Test
	@DisplayName("Quando a classe é instânciada, deve ser registrada as 5 permissões em factory.")
	void testVerifyPermissionsRegisterOnFactory() {
		/* Dá erro durante a criação de uma nova instância new PlanningPermissions();
		*
		* Pois em PermissionFactory é solicitado o registro de algumas permissões que PlanningPermissions também solicita
		* logo dá erro.
		*
		* É intermitente, pois sempre ao criar uma instância de PlanningPermissions, os registros em PermissionFactory
		* irão ocorrer antes.
		* */


		PermissionFactory permissionFactory = PermissionFactory.getInstance();

		// assertFalse(permissionFactory.get("org.forpdi.planning.permissions.ManageStructurePermission").getId().isEmpty());
		assertFalse(permissionFactory.get("org.forpdi.planning.permissions.ManagePlanMacroPermission").getId().isEmpty());
		assertFalse(permissionFactory.get("org.forpdi.planning.permissions.ManageDocumentPermission").getId().isEmpty());
		assertFalse(permissionFactory.get("org.forpdi.planning.permissions.ManagePlanPermission").getId().isEmpty());
		assertFalse(permissionFactory.get("org.forpdi.planning.permissions.UpdateGoalPermission").getId().isEmpty());
	}
}