package org.forrisco.core.process;

import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessUnitTest {
	@DisplayName("ProcessUnit Criação do objeto com todos os seus campos.")
	@Test
	public void testProcessUnitObjectCreation() {
		Process process = new Process();
		process.setName("Test Process");
		Unit unit = new Unit();
		unit.setName("Test Unit");

		ProcessUnit processUnit = new ProcessUnit();
		processUnit.setProcess(process);
		processUnit.setUnit(unit);

		assertNotNull(processUnit.getProcess());
		assertNotNull(processUnit.getUnit());
		assertEquals("Test Process", processUnit.getProcess().getName());
		assertEquals("Test Unit", processUnit.getUnit().getName());
	}
}