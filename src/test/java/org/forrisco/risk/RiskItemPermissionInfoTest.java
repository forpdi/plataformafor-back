package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskItemPermissionInfoTest {

	@DisplayName("RiskItemPermissionInfo Criação do objeto com todas as permissões verdadeiras.")
	@Test
	public void testRiskItemPermissionInfoWithAllTrueParams() {
		RiskItemPermissionInfo permissionInfo = new RiskItemPermissionInfo(true, true, true);

		assertNotNull(permissionInfo);
		assertTrue(permissionInfo.isHasPermission());
		assertTrue(permissionInfo.isResponsible());
		assertTrue(permissionInfo.isRiskResponsible());
		assertTrue(permissionInfo.isRiskResponsibleOrHasPermission());
	}

	@DisplayName("RiskItemPermissionInfo Criação do objeto com todas as permissões falsas.")
	@Test
	public void testRiskItemPermissionInfoWithAllParamsFalse() {
		RiskItemPermissionInfo permissionInfo = new RiskItemPermissionInfo(false, false, false);

		assertFalse(permissionInfo.isHasPermission());
		assertFalse(permissionInfo.isResponsible());
		assertFalse(permissionInfo.isRiskResponsible());
		assertFalse(permissionInfo.isRiskResponsibleOrHasPermission());
	}

	@DisplayName("RiskItemPermissionInfo Criação do objeto com todas as permissões alternadas.")
	@Test
	public void testRiskItemPermissionInfoWithMixedValues() {
		RiskItemPermissionInfo permissionInfo = new RiskItemPermissionInfo(false, false, true);

		assertFalse(permissionInfo.isHasPermission());
		assertFalse(permissionInfo.isResponsible());
		assertTrue(permissionInfo.isRiskResponsible());
		assertTrue(permissionInfo.isRiskResponsibleOrHasPermission());
	}

}