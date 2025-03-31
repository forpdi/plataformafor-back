package org.forrisco.risk.links;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskIndicatorTest {

	@DisplayName("RiskIndicator Criação do Objeto.")
	@Test
	public void testRiskIndicatorCreation() {

		RiskIndicator riskIndicator = new RiskIndicator();

		Risk testRisk = new Risk();
		StructureLevelInstance testStructure = new StructureLevelInstance();
		testStructure.setId(4L);
		String testLinkFPDI = "http://example.com/fpdi-indicator";
		String testName = "Test Risk Indicator";

		riskIndicator.setRisk(testRisk);
		riskIndicator.setStructure(testStructure);
		riskIndicator.setLinkFPDI(testLinkFPDI);
		riskIndicator.setName(testName);

		assertEquals(testRisk, riskIndicator.getRisk(), "O objeto Risk deveria ser igual ao definido.");
		assertEquals(testStructure, riskIndicator.getStructure(), "O objeto Structure deveria ser igual ao definido.");
		assertEquals(testLinkFPDI, riskIndicator.getLinkFPDI(), "O linkFPDI deveria ser igual ao definido.");
		assertEquals(testName, riskIndicator.getName(), "O nome deveria ser igual ao definido.");
	}

}