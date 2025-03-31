package org.forrisco.core.process;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessLinkedToRiskBeanTest {

	@DisplayName("ProcessLinkedToRiskBean Criação do objeto ProcessLinkedToRiskBean.")
	@Test
	void testProcessLinkedToRiskBeanWithAllParams() {
		ProcessLinkedToRiskBean newProcess = new ProcessLinkedToRiskBean();
		newProcess.setId(1L);
		newProcess.setRiskId(2L);
		newProcess.setUnitId(3L);
		newProcess.setName("Name");

		assertEquals(1L, newProcess.getId());
		assertEquals(2L, newProcess.getRiskId());
		assertEquals(3L, newProcess.getUnitId());
		assertEquals("Name", newProcess.getName());

	}
}