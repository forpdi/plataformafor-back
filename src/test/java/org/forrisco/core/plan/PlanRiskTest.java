package org.forrisco.core.plan;

import org.forrisco.core.policy.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanRiskTest {

	@DisplayName("PlanRisk Criação do Objeto.")
	@Test
	void testPlanRiskObjectCreation() {
		PlanRisk planRisk = new PlanRisk();

		String testName = "Test PlanRisk Name";
		String testDescription = "Test PlanRisk Description";
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, 2024);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date testValidityBegin = cal.getTime();

		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		Date testValidityEnd = cal.getTime();

		Policy testPolicy = new Policy();
		boolean testArchived = true;

		planRisk.setName(testName);
		planRisk.setDescription(testDescription);
		planRisk.setValidityBegin(testValidityBegin);
		planRisk.setValidityEnd(testValidityEnd);
		planRisk.setPolicy(testPolicy);
		planRisk.setArchived(testArchived);

		assertEquals(testName, planRisk.getName(), "O nome deveria ser igual ao definido");
		assertEquals(testDescription, planRisk.getDescription(), "A descrição deveria ser igual à definida");
		assertEquals(testValidityBegin, planRisk.getValidityBegin(), "A validade inicial deveria ser igual à definida");
		assertEquals(testValidityEnd, planRisk.getValidityEnd(), "A validade final deveria ser igual à definida");
		assertEquals(testPolicy, planRisk.getPolicy(), "A política deveria ser igual à definida");
		assertTrue(planRisk.isArchived(), "O campo archived deveria ser verdadeiro");
	}
}