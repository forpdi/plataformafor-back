package org.forpdi.planning.fields.table;

import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.fields.table.TableFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

class TableStructureTest {

	private TableStructure tableStructure;
	private TableFields tableFields;
	private OptionsField optionField;

	@BeforeEach
	void setUp() {
		tableStructure = new TableStructure();
		tableFields = new TableFields();
		optionField = new OptionsField();

		tableStructure.setTableFields(tableFields);
		tableStructure.setOptionsField(Arrays.asList(optionField));
	}

	@Test
	@DisplayName("Dado um label, quando definimos o label, então ele deve ser corretamente atribuído à propriedade label")
	void testSetLabel() {
		String expectedLabel = "Tabela de Exemplo";

		tableStructure.setLabel(expectedLabel);

		assertEquals(expectedLabel, tableStructure.getLabel());
	}

	@Test
	@DisplayName("Dado um tipo, quando definimos o tipo, então ele deve ser corretamente atribuído à propriedade type")
	void testSetType() {
		String expectedType = "Financeiro";

		tableStructure.setType(expectedType);

		assertEquals(expectedType, tableStructure.getType());
	}

	@Test
	@DisplayName("Dado o campo isInTotal, quando definimos isInTotal, então ele deve refletir o valor esperado")
	void testSetInTotal() {
		boolean expectedIsInTotal = true;

		tableStructure.setInTotal(expectedIsInTotal);

		assertTrue(tableStructure.isInTotal());
	}

	@Test
	@DisplayName("Dado a lista de OptionsField, quando definimos optionsField, então ela deve ser corretamente atribuída")
	void testSetOptionsField() {
		OptionsField newOptionField = new OptionsField();

		tableStructure.setOptionsField(Arrays.asList(newOptionField));

		assertEquals(1, tableStructure.getOptionsField().size());
		assertEquals(newOptionField, tableStructure.getOptionsField().get(0));
	}

	@Test
	@DisplayName("Dado um exportTableFieldsId, quando definimos o ID, então ele deve ser corretamente atribuído")
	void testSetExportTableFieldsId() {
		Long expectedId = 100L;

		tableStructure.setExportTableFieldsId(expectedId);

		assertEquals(expectedId, tableStructure.getExportTableFieldsId());
	}

	@Test
	@DisplayName("Dado um TableFields, quando definimos o tableFields, então o campo de tableFields deve ser corretamente atribuído")
	void testSetTableFields() {
		TableFields expectedTableFields = new TableFields();

		tableStructure.setTableFields(expectedTableFields);

		assertEquals(expectedTableFields, tableStructure.getTableFields());
	}

	@Test
	@DisplayName("Dado um label nulo, quando o label é definido como nulo, então ele deve ser armazenado como nulo")
	void testSetLabelNull() {
		String expectedLabel = null;

		tableStructure.setLabel(expectedLabel);

		assertNull(tableStructure.getLabel());
	}

	@Test
	@DisplayName("Dado um tipo nulo, quando o tipo é definido como nulo, então ele deve ser armazenado como nulo")
	void testSetTypeNull() {
		String expectedType = null;

		tableStructure.setType(expectedType);

		assertNull(tableStructure.getType());
	}

	@Test
	@DisplayName("Dado um valor falso para isInTotal, quando definimos isInTotal, então ele deve refletir o valor esperado")
	void testSetInTotalFalse() {
		boolean expectedIsInTotal = false;

		tableStructure.setInTotal(expectedIsInTotal);

		assertFalse(tableStructure.isInTotal());
	}
}
