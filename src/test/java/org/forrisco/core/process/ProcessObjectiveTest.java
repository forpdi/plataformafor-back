package org.forrisco.core.process;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessObjectiveTest {

	@DisplayName("ProcessObjective Criação do objeto com todos os seus campos.")
	@Test
	public void testProcessObjectiveObjectCreation() {

		Process process = new Process();
		process.setName("Test Process");
		process.setId(1L);

		String description = "Test objective description";

		ProcessObjective objective = new ProcessObjective();
		objective.setProcess(process);
		objective.setDescription(description);

		assertNotNull(objective);
		assertEquals(process, objective.getProcess());
		assertEquals(description, objective.getDescription());
	}

}