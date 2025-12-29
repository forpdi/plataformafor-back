package org.forrisco.risk;

import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskSharedUnitTest {

	@DisplayName("RiskSharedUnit criação do objeto.")
	@Test
	public void testRiskSharedUnitCreationAndSettersGetters() {

		RiskSharedUnit riskSharedUnit = new RiskSharedUnit();

		Long testId = 1L;
		Risk testRisk = new Risk();
		Unit testUnit = new Unit();

		riskSharedUnit.setId(testId);
		riskSharedUnit.setRisk(testRisk);
		riskSharedUnit.setUnit(testUnit);

		assertEquals(testId, riskSharedUnit.getId(), "O ID deveria ser igual ao definido");
		assertEquals(testRisk, riskSharedUnit.getRisk(), "O objeto Risk deveria ser igual ao definido");
		assertEquals(testUnit, riskSharedUnit.getUnit(), "O objeto Unit deveria ser igual ao definido");
	}
}