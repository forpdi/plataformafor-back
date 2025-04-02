package org.forrisco.risk.links;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskStrategyTest {

	@DisplayName("RiskStrategy Criação do objeto")
	@Test
	public void testRiskStrategyObjectCreation() {
		RiskStrategy riskStrategy = new RiskStrategy();

		Risk testRisk = new Risk();
		StructureLevelInstance testStructure = new StructureLevelInstance();
		testStructure.setId(1L);
		String testLinkFPDI = "http://example.com/fpdi-strategy";
		String testName = "Mitigação de Risco";

		riskStrategy.setRisk(testRisk);
		riskStrategy.setStructure(testStructure);
		riskStrategy.setLinkFPDI(testLinkFPDI);
		riskStrategy.setName(testName);

		assertEquals(testRisk, riskStrategy.getRisk(), "O objeto Risk deveria ser igual ao definido.");
		assertEquals(testStructure, riskStrategy.getStructure(), "O objeto Structure deveria ser igual ao definido.");
		assertEquals(testLinkFPDI, riskStrategy.getLinkFPDI(), "O linkFPDI deveria ser igual ao definido.");
		assertEquals(testName, riskStrategy.getName(), "O nome deveria ser igual ao definido.");
	}
}