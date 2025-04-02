package org.forrisco.risk.links;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskGoalTest {

	@DisplayName("RiskGoal Criação do objeto.")
	@Test
	public void testRiskGoalCreation() {
		RiskGoal riskGoal = new RiskGoal();

		Risk testRisk = new Risk();
		StructureLevelInstance testStructure = new StructureLevelInstance();
		testStructure.setId(1L);
		String testLinkFPDI = "http://example.com/fpdi-goal";
		String testName = "Test Risk Goal";

		riskGoal.setRisk(testRisk);
		riskGoal.setStructure(testStructure);
		riskGoal.setLinkFPDI(testLinkFPDI);
		riskGoal.setName(testName);

		assertEquals(testRisk, riskGoal.getRisk(), "O objeto Risk deveria ser igual ao definido.");
		assertEquals(testStructure, riskGoal.getStructure(), "O objeto Structure deveria ser igual ao definido.");
		assertEquals(testLinkFPDI, riskGoal.getLinkFPDI(), "O linkFPDI deveria ser igual ao definido.");
		assertEquals(testName, riskGoal.getName(), "O nome deveria ser igual ao definido.");
	}

}