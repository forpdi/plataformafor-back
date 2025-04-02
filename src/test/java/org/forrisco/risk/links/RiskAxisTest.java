package org.forrisco.risk.links;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskAxisTest {
	@DisplayName("RiskAxis Criação do objeto.")
	@Test
	public void testRiskAxisCreation() {
		RiskAxis riskAxis = new RiskAxis();

		Risk testRisk = new Risk();
		StructureLevelInstance testStructure = new StructureLevelInstance();
		testStructure.setId(3L);
		String testLinkFPDI = "http://example.com/fpdi";
		String testName = "Test Axis";

		riskAxis.setRisk(testRisk);
		riskAxis.setStructure(testStructure);
		riskAxis.setLinkFPDI(testLinkFPDI);
		riskAxis.setName(testName);

		assertEquals(testRisk, riskAxis.getRisk(), "O objeto Risk deveria ser igual ao definido");
		assertEquals(testStructure, riskAxis.getStructure(), "O objeto Structure deveria ser igual ao definido");
		assertEquals(testLinkFPDI, riskAxis.getLinkFPDI(), "O linkFPDI deveria ser igual ao definido");
		assertEquals(testName, riskAxis.getName(), "O nome deveria ser igual ao definido");
	}
}