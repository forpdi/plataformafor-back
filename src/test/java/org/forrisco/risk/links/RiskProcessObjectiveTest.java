package org.forrisco.risk.links;

import org.forrisco.core.process.ProcessObjective;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskProcessObjectiveTest {

	@DisplayName("RiskProcessObjective Criação do Objeto.")
	@Test
	public void testRiskProcessObjectiveCreation() {
		RiskProcessObjective riskProcessObjective = new RiskProcessObjective();

		Risk testRisk = new Risk();
		ProcessObjective testObjective = new ProcessObjective();
		String testLinkFPDI = "http://example.com/fpdi-objective";
		Long testProcessId = 123L;

		riskProcessObjective.setRisk(testRisk);
		riskProcessObjective.setProcessObjective(testObjective);
		riskProcessObjective.setLinkFPDI(testLinkFPDI);
		riskProcessObjective.setProcessId(testProcessId);

		assertEquals(testRisk, riskProcessObjective.getRisk(), "O objeto Risk deveria ser igual ao definido.");
		assertEquals(testObjective, riskProcessObjective.getProcessObjective(), "O objeto ProcessObjective deveria ser igual ao definido.");
		assertEquals(testLinkFPDI, riskProcessObjective.getLinkFPDI(), "O linkFPDI deveria ser igual ao definido.");
		assertEquals(testProcessId, riskProcessObjective.getProcessId(), "O processId deveria ser igual ao definido.");
	}
}