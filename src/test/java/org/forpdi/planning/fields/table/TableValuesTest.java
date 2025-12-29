package org.forpdi.planning.fields.table;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

class TableValuesTest {

	@Test
	@DisplayName("Dado uma instância de TableValues com valores válidos, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ValidTableValues() {
		TableInstance tableInstance = new TableInstance();
		TableStructure tableStructure = new TableStructure();

		TableValues tableValues = new TableValues();
		tableValues.setValue("Sample Value");
		tableValues.setValueAsNumber(123.45);
		tableValues.setValueAsDate(new Date());
		tableValues.setTableInstance(tableInstance);
		tableValues.setTableStructure(tableStructure);
		tableValues.setExportTableStructureId(100L);
		tableValues.setExportTableInstanceId(200L);

		String value = tableValues.getValue();
		Double valueAsNumber = tableValues.getValueAsNumber();
		Date valueAsDate = tableValues.getValueAsDate();
		TableInstance retrievedTableInstance = tableValues.getTableInstance();
		TableStructure retrievedTableStructure = tableValues.getTableStructure();
		Long exportTableStructureId = tableValues.getExportTableStructureId();
		Long exportTableInstanceId = tableValues.getExportTableInstanceId();

		assertEquals("Sample Value", value, "O valor deve ser 'Sample Value'.");
		assertEquals(123.45, valueAsNumber, "O valor numérico deve ser 123.45.");
		assertNotNull(valueAsDate, "A data não deve ser nula.");
		assertEquals(tableInstance, retrievedTableInstance, "A instância da tabela deve ser a mesma.");
		assertEquals(tableStructure, retrievedTableStructure, "A estrutura da tabela deve ser a mesma.");
		assertEquals(100L, exportTableStructureId, "O ID da estrutura da tabela de exportação deve ser 100.");
		assertEquals(200L, exportTableInstanceId, "O ID da instância da tabela de exportação deve ser 200.");
	}

	@Test
	@DisplayName("Dado uma instância de TableValues sem valores definidos, quando os getters são chamados, então os valores retornados devem ser nulos ou default")
	void testGetters_EmptyTableValues() {
		TableValues tableValues = new TableValues();

		String value = tableValues.getValue();
		Double valueAsNumber = tableValues.getValueAsNumber();
		Date valueAsDate = tableValues.getValueAsDate();
		TableInstance retrievedTableInstance = tableValues.getTableInstance();
		TableStructure retrievedTableStructure = tableValues.getTableStructure();
		Long exportTableStructureId = tableValues.getExportTableStructureId();
		Long exportTableInstanceId = tableValues.getExportTableInstanceId();

		assertNull(value, "O valor deve ser null por padrão.");
		assertNull(valueAsNumber, "O valor numérico deve ser null por padrão.");
		assertNull(valueAsDate, "A data deve ser null por padrão.");
		assertNull(retrievedTableInstance, "A instância da tabela deve ser null por padrão.");
		assertNull(retrievedTableStructure, "A estrutura da tabela deve ser null por padrão.");
		assertNull(exportTableStructureId, "O ID da estrutura da tabela de exportação deve ser null por padrão.");
		assertNull(exportTableInstanceId, "O ID da instância da tabela de exportação deve ser null por padrão.");
	}

	@Test
	@DisplayName("Dado uma instância de TableValues com valor numérico, quando o setter de valueAsNumber é chamado, então o valor numérico é alterado corretamente")
	void testSetValueAsNumber() {
		TableValues tableValues = new TableValues();
		tableValues.setValueAsNumber(500.75);

		tableValues.setValueAsNumber(800.50);

		assertEquals(800.50, tableValues.getValueAsNumber(), "O valor numérico deve ser 800.50.");
	}

	@Test
	@DisplayName("Dado uma instância de TableValues com valor de data, quando o setter de valueAsDate é chamado, então o valor de data é alterado corretamente")
	void testSetValueAsDate() {
		TableValues tableValues = new TableValues();
		Date currentDate = new Date();
		tableValues.setValueAsDate(currentDate);

		Date newDate = new Date(currentDate.getTime() + 1000000);
		tableValues.setValueAsDate(newDate);

		assertEquals(newDate, tableValues.getValueAsDate(), "A data deve ser alterada para a nova data.");
	}

	@Test
	@DisplayName("Dado uma instância de TableValues com exportTableStructureId, quando o setter é chamado, então o ID de exportação da estrutura da tabela é atribuído corretamente")
	void testSetExportTableStructureId() {
		TableValues tableValues = new TableValues();

		tableValues.setExportTableStructureId(555L);

		assertEquals(555L, tableValues.getExportTableStructureId(), "O ID da estrutura da tabela de exportação deve ser 555.");
	}

	@Test
	@DisplayName("Dado uma instância de TableValues com exportTableInstanceId, quando o setter é chamado, então o ID de exportação da instância da tabela é atribuído corretamente")
	void testSetExportTableInstanceId() {
		TableValues tableValues = new TableValues();

		tableValues.setExportTableInstanceId(999L);

		assertEquals(999L, tableValues.getExportTableInstanceId(), "O ID da instância da tabela de exportação deve ser 999.");
	}
}
