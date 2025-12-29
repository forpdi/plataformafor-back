package org.forrisco.risk.links;

import org.forrisco.core.process.Process;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RiskActivityTest {

	@DisplayName("RiskActivity Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testRiskActivityCreationWithSettersAndGetters() {
		RiskActivity riskActivity = new RiskActivity();
		Risk mockRisk = mock(Risk.class);
		Process mockProcess = mock(Process.class);

		riskActivity.setRisk(mockRisk);
		riskActivity.setProcess(mockProcess);
		riskActivity.setLinkFPDI("http://linkfpdi.com");
		riskActivity.setName("Risk Activity 1");

		assertEquals(mockRisk, riskActivity.getRisk(), "Os dados do risco deveriam ser iguais.");
		assertEquals(mockProcess, riskActivity.getProcess(), "Os dados do processo deveriam ser iguais.");
		assertEquals("http://linkfpdi.com", riskActivity.getLinkFPDI(),
			"Os valores de LinkFPDI deveriam ser iguais.");
		assertEquals("Risk Activity 1", riskActivity.getName(), "Os nomes deveriam ser iguais.");
	}

	@DisplayName("RiskActivity Criação do objeto por meio do construtor vazio.")
	@Test
	public void testRiskActivityCreationWithEmptyConstructor() {
		RiskActivity riskActivity = new RiskActivity();

		assertNotNull(riskActivity, "O objeto não deveria ser nulo.");
	}

	@DisplayName("RiskActivity Criação do objeto por meio construtor que replica o objeto.")
	@Test
	public void testRiskActivityCreationWithFullConstructor() {
		RiskActivity riskActivity = new RiskActivity();

		riskActivity.setLinkFPDI("http://linkfpdi.com");
		riskActivity.setName("Risk Activity 1");

		RiskActivity newRiskActivity = new RiskActivity(riskActivity);

		assertNotNull(newRiskActivity, "O objeto não deveria ser nulo.");
		assertEquals(riskActivity.getId(), newRiskActivity.getId(), "Os ids deveriam ser iguais.");
		assertEquals(riskActivity.getLinkFPDI(), newRiskActivity.getLinkFPDI(),
			"Os valores de LinkFPDI deveriam ser iguais.");
		assertEquals(riskActivity.getName(), newRiskActivity.getName(), "Os nomes deveriam ser iguais.");
	}


}