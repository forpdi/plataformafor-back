package org.forrisco.risk.preventiveaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PreventiveActionBeanTest {

	@DisplayName("PreventiveActionBean Criação do objeto, construtor vazio.")
	@Test
	public void testPreventiveActionBeanCreationObjectEmptyConstructor() {
		PreventiveActionBean preventiveAction = new PreventiveActionBean();

		assertNotNull(preventiveAction);
	}

	@DisplayName("PreventiveActionBean Criação do objeto, construtor completo.")
	@Test
	public void testPreventiveActionBeanCreationObjectFullConstructor() {
		long id = 1L;
		long riskId = 100L;
		String riskName = "Financial Risk";
		String riskType = "Strategic";
		long unitId = 200L;
		String action = "Implement controls";
		boolean accomplished = false;
		Date validityBegin = new Date();

		PreventiveActionBean preventiveAction = new PreventiveActionBean(id, riskId, riskName, riskType,
			unitId, action, accomplished, validityBegin);

		assertEquals(id, preventiveAction.getId());
		assertEquals(riskId, preventiveAction.getRiskId());
		assertEquals(riskName, preventiveAction.getRiskName());
		assertEquals(riskType, preventiveAction.getRiskType());
		assertEquals(unitId, preventiveAction.getUnitId());
		assertEquals(action, preventiveAction.getAction());
		assertEquals(accomplished, preventiveAction.isAccomplished());
		assertEquals(validityBegin, preventiveAction.getValidityBegin());
	}

	@DisplayName("PreventiveActionBean Criação do objeto, utilizando setters.")
	@Test
	public void testPreventiveActionBeanCreationObjectSettingParams() {
		long id = 1L;
		long riskId = 100L;
		String riskName = "Financial Risk";
		String riskType = "Strategic";
		long unitId = 200L;
		String action = "Implement controls";
		boolean accomplished = false;
		Date validityBegin = new Date();

		PreventiveActionBean preventiveAction = new PreventiveActionBean();
		preventiveAction.setId(id);
		preventiveAction.setRiskId(riskId);
		preventiveAction.setRiskName(riskName);
		preventiveAction.setRiskType(riskType);
		preventiveAction.setUnitId(unitId);
		preventiveAction.setAction(action);
		preventiveAction.setAccomplished(accomplished);
		preventiveAction.setValidityBegin(validityBegin);


		assertEquals(id, preventiveAction.getId());
		assertEquals(riskId, preventiveAction.getRiskId());
		assertEquals(riskName, preventiveAction.getRiskName());
		assertEquals(riskType, preventiveAction.getRiskType());
		assertEquals(unitId, preventiveAction.getUnitId());
		assertEquals(action, preventiveAction.getAction());
		assertEquals(accomplished, preventiveAction.isAccomplished());
		assertEquals(validityBegin, preventiveAction.getValidityBegin());
	}
}