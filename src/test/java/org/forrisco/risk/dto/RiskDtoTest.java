package org.forrisco.risk.dto;

import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RiskDtoTest {

	@DisplayName("RiskDto Criação do DTO.")
	@Test
	void testRiskDtoCreation() {
		Risk risk = new Risk();
		risk.setName("Test Risk");
		risk.setCode("R001");
		risk.setReason("Test Reason");
		risk.setResult("Test Result");
		risk.setProbability("High");
		risk.setImpact("Medium");
		risk.setPeriodicity("Monthly");
		risk.setTipology("Strategic");
		risk.setType("Internal");
		risk.setBegin(new Date());
		risk.setOtherTipologies("Other");

		RiskDto riskDto = new RiskDto(risk);

		assertNotNull(riskDto);
		assertEquals("Test Risk", riskDto.risk().getName());
		assertEquals("R001", riskDto.risk().getCode());
	}
}