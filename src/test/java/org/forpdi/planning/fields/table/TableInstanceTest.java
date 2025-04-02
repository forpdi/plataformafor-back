package org.forpdi.planning.fields.table;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class TableInstanceTest {

	@Test
	@DisplayName("Quando o campo creation for obtido, então deve retornar a data de criação corretamente")
	void givenTableInstance_whenGetCreation_thenShouldReturnCorrectCreationDate() {
		Date expectedDate = new Date();
		TableInstance tableInstance = new TableInstance();
		tableInstance.setCreation(expectedDate);

		Date creationDate = tableInstance.getCreation();

		assertEquals(expectedDate, creationDate, "A data de criação deve ser a correta.");
	}

	@Test
	@DisplayName("Quando o campo tableFields for configurado, então deve retornar o valor correto")
	void givenTableInstance_whenGetTableFields_thenShouldReturnCorrectTableFields() {
		TableFields tableFields = new TableFields(); 
		TableInstance tableInstance = new TableInstance();
		tableInstance.setTableFields(tableFields);

		TableFields retrievedTableFields = tableInstance.getTableFields();

		assertEquals(tableFields, retrievedTableFields, "Os TableFields configurados devem ser retornados.");
	}

	@Test
	@DisplayName("Quando os valores da tabela forem configurados, então deve retornar a lista de valores corretamente")
	void givenTableInstance_whenGetTableValues_thenShouldReturnCorrectTableValues() {
		TableValues tableValue1 = new TableValues(); 
		TableValues tableValue2 = new TableValues();
		List<TableValues> tableValues = Arrays.asList(tableValue1, tableValue2);
		TableInstance tableInstance = new TableInstance();
		tableInstance.setTableValues(tableValues);

		List<TableValues> retrievedTableValues = tableInstance.getTableValues();

		assertEquals(tableValues, retrievedTableValues, "Os valores da tabela configurados devem ser retornados.");
	}

	@Test
	@DisplayName("Quando o campo tableFieldsId for configurado, então deve retornar o valor correto")
	void givenTableInstance_whenGetTableFieldsId_thenShouldReturnCorrectTableFieldsId() {
		Long expectedTableFieldsId = 123L;
		TableInstance tableInstance = new TableInstance();
		tableInstance.setTableFieldsId(expectedTableFieldsId);

		Long tableFieldsId = tableInstance.getTableFieldsId();

		assertEquals(expectedTableFieldsId, tableFieldsId, "O campo tableFieldsId deve ser o correto.");
	}

	@Test
	@DisplayName("Quando o campo exportTableFieldsId for configurado, então deve retornar o valor correto")
	void givenTableInstance_whenGetExportTableFieldsId_thenShouldReturnCorrectExportTableFieldsId() {
		Long expectedExportTableFieldsId = 456L;
		TableInstance tableInstance = new TableInstance();
		tableInstance.setExportTableFieldsId(expectedExportTableFieldsId);

		Long exportTableFieldsId = tableInstance.getExportTableFieldsId();

		assertEquals(expectedExportTableFieldsId, exportTableFieldsId, "O campo exportTableFieldsId deve ser o correto.");
	}

	@Test
	@DisplayName("Quando o campo creation não for configurado, então deve retornar a data atual")
	void givenTableInstance_whenCreationNotSet_thenShouldReturnCurrentDate() {
		TableInstance tableInstance = new TableInstance();

		Date creationDate = tableInstance.getCreation();

		assertNotNull(creationDate, "A data de criação não pode ser nula.");
	}

}

