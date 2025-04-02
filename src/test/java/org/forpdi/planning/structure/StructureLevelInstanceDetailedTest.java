package org.forpdi.planning.structure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StructureLevelInstanceDetailedTest {

	@Test
	@DisplayName("Dado uma instância de StructureLevelInstanceDetailed com dados válidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ValidStructureLevelInstanceDetailed() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();

		StructureLevelInstanceDetailed instanceDetailed = new StructureLevelInstanceDetailed();
		instanceDetailed.setMonth(5);
		instanceDetailed.setYear(2024);
		instanceDetailed.setLevelInstance(levelInstance);
		instanceDetailed.setLevelValue(100.0);
		instanceDetailed.setLevelMinimum(50.0);
		instanceDetailed.setLevelMaximum(200.0);
		instanceDetailed.setExportStructureLevelInstanceId(123L);

		int month = instanceDetailed.getMonth();
		int year = instanceDetailed.getYear();
		StructureLevelInstance retrievedLevelInstance = instanceDetailed.getLevelInstance();
		Double levelValue = instanceDetailed.getLevelValue();
		Double levelMinimum = instanceDetailed.getLevelMinimum();
		Double levelMaximum = instanceDetailed.getLevelMaximum();
		Long exportStructureLevelInstanceId = instanceDetailed.getExportStructureLevelInstanceId();

		assertEquals(5, month, "O mês deve ser 5.");
		assertEquals(2024, year, "O ano deve ser 2024.");
//		assertEquals(levelInstance, retrievedLevelInstance, "O nível da instância deve ser o mesmo.");
		assertEquals(100.0, levelValue, "O valor do nível deve ser 100.0.");
		assertEquals(50.0, levelMinimum, "O valor mínimo do nível deve ser 50.0.");
		assertEquals(200.0, levelMaximum, "O valor máximo do nível deve ser 200.0.");
		assertEquals(123L, exportStructureLevelInstanceId, "O ID de exportação deve ser 123.");
	}

	@Test
	@DisplayName("Dado uma instância de StructureLevelInstanceDetailed sem valores definidos, quando os getters são chamados, então os valores retornados devem ser nulos ou default")
	void testGetters_EmptyStructureLevelInstanceDetailed() {
		StructureLevelInstanceDetailed instanceDetailed = new StructureLevelInstanceDetailed();

		int month = instanceDetailed.getMonth();
		int year = instanceDetailed.getYear();
		StructureLevelInstance retrievedLevelInstance = instanceDetailed.getLevelInstance();
		Double levelValue = instanceDetailed.getLevelValue();
		Double levelMinimum = instanceDetailed.getLevelMinimum();
		Double levelMaximum = instanceDetailed.getLevelMaximum();
		Long exportStructureLevelInstanceId = instanceDetailed.getExportStructureLevelInstanceId();

		assertEquals(0, month, "O mês deve ser 0 por padrão.");
		assertEquals(0, year, "O ano deve ser 0 por padrão.");
		assertNull(retrievedLevelInstance, "O nível da instância deve ser null por padrão.");
		assertNull(levelValue, "O valor do nível deve ser null por padrão.");
		assertNull(levelMinimum, "O valor mínimo do nível deve ser null por padrão.");
		assertNull(levelMaximum, "O valor máximo do nível deve ser null por padrão.");
		assertNull(exportStructureLevelInstanceId, "O ID de exportação deve ser null por padrão.");
	}

	@Test
	@DisplayName("Dado um StructureLevelInstanceDetailed com valores válidos, quando a função setLevelValue é chamada, então o valor do nível é alterado corretamente")
	void testSetLevelValue() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();

		StructureLevelInstanceDetailed instanceDetailed = new StructureLevelInstanceDetailed();
		instanceDetailed.setMonth(5);
		instanceDetailed.setYear(2024);
		instanceDetailed.setLevelInstance(levelInstance);

		instanceDetailed.setLevelValue(150.0);

		assertEquals(150.0, instanceDetailed.getLevelValue(), "O valor do nível deve ser 150.0.");
	}

	@Test
	@DisplayName("Dado uma instância de StructureLevelInstanceDetailed com valores válidos, quando a função setLevelMinimum é chamada, então o valor mínimo do nível é alterado corretamente")
	void testSetLevelMinimum() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();

		StructureLevelInstanceDetailed instanceDetailed = new StructureLevelInstanceDetailed();
		instanceDetailed.setMonth(5);
		instanceDetailed.setYear(2024);
		instanceDetailed.setLevelInstance(levelInstance);

		instanceDetailed.setLevelMinimum(60.0);

		assertEquals(60.0, instanceDetailed.getLevelMinimum(), "O valor mínimo do nível deve ser 60.0.");
	}

	@Test
	@DisplayName("Dado uma instância de StructureLevelInstanceDetailed com valores válidos, quando a função setLevelMaximum é chamada, então o valor máximo do nível é alterado corretamente")
	void testSetLevelMaximum() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();

		StructureLevelInstanceDetailed instanceDetailed = new StructureLevelInstanceDetailed();
		instanceDetailed.setMonth(5);
		instanceDetailed.setYear(2024);
		instanceDetailed.setLevelInstance(levelInstance);

		instanceDetailed.setLevelMaximum(250.0);

		assertEquals(250.0, instanceDetailed.getLevelMaximum(), "O valor máximo do nível deve ser 250.0.");
	}

	@Test
	@DisplayName("Dado uma instância de StructureLevelInstanceDetailed com um valor de exportStructureLevelInstanceId válido, quando o setter é chamado, então o ID de exportação é atribuído corretamente")
	void testSetExportStructureLevelInstanceId() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();

		StructureLevelInstanceDetailed instanceDetailed = new StructureLevelInstanceDetailed();
		instanceDetailed.setMonth(5);
		instanceDetailed.setYear(2024);
		instanceDetailed.setLevelInstance(levelInstance);

		instanceDetailed.setExportStructureLevelInstanceId(987L);

		assertEquals(987L, instanceDetailed.getExportStructureLevelInstanceId(), "O ID de exportação deve ser 987.");
	}
}
