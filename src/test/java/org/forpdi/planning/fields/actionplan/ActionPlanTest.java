package org.forpdi.planning.fields.actionplan;

import org.forpdi.core.user.User;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionPlanTest {
	@Test
	@DisplayName("Deve validar a criação de um objeto ActionPlan, " +
		"definindo seus parâmetros pelos setters e acessando pelos getters.")
	void testGettersAndSetters() {
		ActionPlan actionPlan = new ActionPlan();

		boolean checked = true;
		Boolean notChecked = false;
		String description = "Test Description";
		String responsible = "Test Responsible";
		Date begin = new Date(System.currentTimeMillis() - 86400000);
		Date end = new Date();
		Date creation = new Date(System.currentTimeMillis() - 604800000);
		Long exportStructureLevelInstanceId = 1L;
		String exportResponsibleMail = "responsible@test.com";

		StructureLevelInstance levelInstance = mock(StructureLevelInstance.class);
		StructureLevelInstance linkedGoal = mock(StructureLevelInstance.class);
		User user = mock(User.class);

		actionPlan.setChecked(checked);
		actionPlan.setNotChecked(notChecked);
		actionPlan.setDescription(description);
		actionPlan.setResponsible(responsible);
		actionPlan.setBegin(begin);
		actionPlan.setEnd(end);
		actionPlan.setCreation(creation);
		actionPlan.setExportStructureLevelInstanceId(exportStructureLevelInstanceId);
		actionPlan.setExportResponsibleMail(exportResponsibleMail);
		actionPlan.setLevelInstance(levelInstance);
		actionPlan.setLinkedGoal(linkedGoal);
		actionPlan.setUser(user);

		assertEquals(checked, actionPlan.isChecked());
		assertEquals(notChecked, actionPlan.isNotChecked());
		assertEquals(description, actionPlan.getDescription());
		assertEquals(responsible, actionPlan.getResponsible());
		assertEquals(begin, actionPlan.getBegin());
		assertEquals(end, actionPlan.getEnd());
		assertEquals(creation, actionPlan.getCreation());
		assertEquals(exportStructureLevelInstanceId, actionPlan.getExportStructureLevelInstanceId());
		assertEquals(exportResponsibleMail, actionPlan.getExportResponsibleMail());
		assertEquals(levelInstance, actionPlan.getLevelInstance());
		assertEquals(linkedGoal, actionPlan.getLinkedGoal());
		assertEquals(user, actionPlan.getUser());
	}

	@Test
	@DisplayName("Teste do método getUserResponsibleName com usuário associado")
	void testGetUserResponsibleNameWithUser() {
		ActionPlan actionPlan = new ActionPlan();
		User user = mock(User.class);

		when(user.getName()).thenReturn("Mocked User");

		actionPlan.setUser(user);

		assertEquals("Mocked User", actionPlan.getUserResponsibleName());
	}

	@Test
	@DisplayName("Teste do método getUserResponsibleName sem usuário associado")
	void testGetUserResponsibleNameWithoutUser() {
		ActionPlan actionPlan = new ActionPlan();
		String responsible = "Test Responsible";

		actionPlan.setResponsible(responsible);

		assertEquals(responsible, actionPlan.getUserResponsibleName());
	}

	@Test
	@DisplayName("Teste de permissões. Todas as permissões.")
	void testActionPlanPermissionInfoCaseAllPermissions() {
		ActionPlan.ActionPlanPermissionInfo permissionInfo =
			new ActionPlan.ActionPlanPermissionInfo(true, true, true);

		assertTrue(permissionInfo.hasAccessLevel());
		assertTrue(permissionInfo.isResponsible());
		assertTrue(permissionInfo.isResponsibleForIndicator());
		assertTrue(permissionInfo.hasPermission());
		assertTrue(permissionInfo.hasPermissionToUpdateResponsible());
	}

	@Test
	@DisplayName("Teste de permissões. " +
		"Caso 1: Usuário não responsável pelo plano de ação.")
	void testActionPlanPermissionInfoCase1() {

		ActionPlan.ActionPlanPermissionInfo permissionInfo =
			new ActionPlan.ActionPlanPermissionInfo(true, false, true);

		assertTrue(permissionInfo.hasAccessLevel());
		assertFalse(permissionInfo.isResponsible());
		assertTrue(permissionInfo.isResponsibleForIndicator());
		assertTrue(permissionInfo.hasPermission());
		assertTrue(permissionInfo.hasPermissionToUpdateResponsible());
	}

	@Test
	@DisplayName("Teste de permissões. " +
		"Caso 2: Usuário não responsável pelo plano de ação e indicador.")
	void testActionPlanPermissionInfoCase2() {
		ActionPlan.ActionPlanPermissionInfo permissionInfo =
			new ActionPlan.ActionPlanPermissionInfo(true, false, false);

		assertTrue(permissionInfo.hasAccessLevel());
		assertFalse(permissionInfo.isResponsible());
		assertFalse(permissionInfo.isResponsibleForIndicator());
		assertTrue(permissionInfo.hasPermission());
		assertTrue(permissionInfo.hasPermissionToUpdateResponsible());
	}

	@Test
	@DisplayName("Teste de permissões. " +
		"Caso 3: Usuário apenas responsável pelo indicador.")
	void testActionPlanPermissionInfoCase3() {
		ActionPlan.ActionPlanPermissionInfo permissionInfo =
			new ActionPlan.ActionPlanPermissionInfo(false, false, true);

		assertFalse(permissionInfo.hasAccessLevel());
		assertFalse(permissionInfo.isResponsible());
		assertTrue(permissionInfo.isResponsibleForIndicator());
		assertTrue(permissionInfo.hasPermission());
		assertTrue(permissionInfo.hasPermissionToUpdateResponsible());
	}

	@Test
	@DisplayName("Teste de permissões. " +
		"Caso 4: Usuário apenas responsável pelo plano de ação.")
	void testActionPlanPermissionInfoCase4() {
		ActionPlan.ActionPlanPermissionInfo permissionInfo =
			new ActionPlan.ActionPlanPermissionInfo(false, true, false);

		assertFalse(permissionInfo.hasAccessLevel());
		assertTrue(permissionInfo.isResponsible());
		assertFalse(permissionInfo.isResponsibleForIndicator());
		assertTrue(permissionInfo.hasPermission());
		assertFalse(permissionInfo.hasPermissionToUpdateResponsible());
	}

	@Test
	@DisplayName("Teste de permissões. Sem permissões")
	void testActionPlanPermissionInfoNoPermissions() {
		ActionPlan.ActionPlanPermissionInfo permissionInfo =
			new ActionPlan.ActionPlanPermissionInfo(false, false, false);

		assertFalse(permissionInfo.hasAccessLevel());
		assertFalse(permissionInfo.isResponsible());
		assertFalse(permissionInfo.isResponsibleForIndicator());
		assertFalse(permissionInfo.hasPermission());
		assertFalse(permissionInfo.hasPermissionToUpdateResponsible());
	}


}