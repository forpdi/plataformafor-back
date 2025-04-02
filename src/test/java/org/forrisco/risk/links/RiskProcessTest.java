package org.forrisco.risk.links;

import org.forrisco.core.process.Process;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskProcessTest {

	@DisplayName("RiskProcess Criação do Objeto com todos os seus campos.")
	@Test
	public void testRiskProcessCreation() {

		RiskProcess riskProcess = new RiskProcess();

		Risk testRisk = new Risk();
		Process testProcess = new Process();
		String testLinkFPDI = "http://example.com/fpdi-process";

		riskProcess.setRisk(testRisk);
		riskProcess.setProcess(testProcess);
		riskProcess.setLinkFPDI(testLinkFPDI);

		assertEquals(testRisk, riskProcess.getRisk(), "O objeto Risk deveria ser igual ao definido.");
		assertEquals(testProcess, riskProcess.getProcess(), "O objeto Process deveria ser igual ao definido.");
		assertEquals(testLinkFPDI, riskProcess.getLinkFPDI(), "O linkFPDI deveria ser igual ao definido.");
	}

	@DisplayName("RiskProcess Criação do Objeto com o construtor vazio.")
	@Test
	public void testRiskProcessCreationEmptyConstructor() {
		RiskProcess riskProcess = new RiskProcess();

		assertNotNull(riskProcess, "O objeto RiskProcess não deveria ser nulo após a criação.");
		assertNull(riskProcess.getRisk(), "O objeto Risk padrão deveria ser nulo.");
		assertNull(riskProcess.getProcess(), "O objeto Process padrão deveria ser nulo.");
		assertNull(riskProcess.getLinkFPDI(), "O linkFPDI padrão deveria ser nulo.");
	}

	@DisplayName("RiskProcess Criação do Objeto com o construtor que copia o objeto.")
	@Test
	public void testRiskProcessCreationObjectConstructor() {

		RiskProcess original = new RiskProcess();
		String testLinkFPDI = "http://example.com/fpdi-process";
		original.setLinkFPDI(testLinkFPDI);

		RiskProcess copied = new RiskProcess(original);

		assertNotNull(copied, "O objeto copiado não deveria ser nulo.");
		assertEquals(original.getLinkFPDI(), copied.getLinkFPDI(), "O linkFPDI do objeto copiado deveria ser igual ao do original.");
	}
}