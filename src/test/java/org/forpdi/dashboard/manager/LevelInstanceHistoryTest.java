package org.forpdi.dashboard.manager;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class LevelInstanceHistoryTest {

	@Test
	@DisplayName("Deve permitir definir e obter a data de criação (creation)")
	void givenCreationDate_whenSetCreation_thenGetCreationShouldReturnSameDate() {
		LevelInstanceHistory history = new LevelInstanceHistory();
		Date expectedDate = new Date();

		history.setCreation(expectedDate);

		assertEquals(expectedDate, history.getCreation(), "A data de criação obtida deve ser igual à definida.");
	}

	@Test
	@DisplayName("Deve permitir definir e obter o valor (value)")
	void givenValue_whenSetValue_thenGetValueShouldReturnSameValue() {
		LevelInstanceHistory history = new LevelInstanceHistory();
		Double expectedValue = 123.45;

		history.setValue(expectedValue);

		assertEquals(expectedValue, history.getValue(), "O valor obtido deve ser igual ao definido.");
	}

	@Test
	@DisplayName("Deve permitir definir e obter a instância de nível (levelInstance)")
	void givenLevelInstance_whenSetLevelInstance_thenGetLevelInstanceShouldReturnSameObject() {
		LevelInstanceHistory history = new LevelInstanceHistory();
		StructureLevelInstance expectedInstance = new StructureLevelInstance();

		history.setLevelInstance(expectedInstance);

		assertSame(expectedInstance, history.getLevelInstance(), "A instância de nível obtida deve ser a mesma definida.");
	}

	@Test
	@DisplayName("Deve permitir definir e obter o ID de exportação da instância de nível (exportStructureLevelInstanceId)")
	void givenExportStructureLevelInstanceId_whenSetExportStructureLevelInstanceId_thenGetExportStructureLevelInstanceIdShouldReturnSameValue() {
		LevelInstanceHistory history = new LevelInstanceHistory();
		Long expectedId = 456L;

		history.setExportStructureLevelInstanceId(expectedId);

		assertEquals(expectedId, history.getExportStructureLevelInstanceId(), "O ID de exportação obtido deve ser igual ao definido.");
	}
}
