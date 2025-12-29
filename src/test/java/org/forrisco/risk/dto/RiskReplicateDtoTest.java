package org.forrisco.risk.dto;

import org.forrisco.risk.Risk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RiskReplicateDtoTest {

	@DisplayName("RiskReplicateDto Criação do DTO.")
	@Test
	void testRiskReplicateDtoCreation() {
		Risk risk = new Risk();
		risk.setName("Test Risk");
		risk.setCode("R001");

		List<Long> targetUnitIds = Arrays.asList(1L, 2L);

		RiskReplicateDto dto = new RiskReplicateDto(risk, targetUnitIds);

		assertNotNull(dto);
		assertEquals(risk, dto.risk());
		assertEquals(targetUnitIds, dto.targetUnitIds());
		assertEquals(2, dto.targetUnitIds().size());
	}
}