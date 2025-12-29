package org.forrisco.risk;

import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RiskHistoryTest {

	@DisplayName("RiskHistory Validação da classe por meio dos Setters e Getters.")
	@Test
	public void testRiskHistorySettersAndGetters() {
		RiskHistory riskHistory = new RiskHistory();
		RiskLevel mockRiskLevel = mock(RiskLevel.class);
		Unit mockUnit = mock(Unit.class);

		riskHistory.setRiskLevel(mockRiskLevel);
		riskHistory.setUnit(mockUnit);
		riskHistory.setMonth(5);
		riskHistory.setYear(2024);
		riskHistory.setThreat(true);
		riskHistory.setQuantity(10);

		assertEquals(mockRiskLevel, riskHistory.getRiskLevel());
		assertEquals(mockUnit, riskHistory.getUnit());
		assertEquals(5, riskHistory.getMonth());
		assertEquals(2024, riskHistory.getYear());
		assertTrue(riskHistory.isThreat());
		assertEquals(10, riskHistory.getQuantity());
	}
}