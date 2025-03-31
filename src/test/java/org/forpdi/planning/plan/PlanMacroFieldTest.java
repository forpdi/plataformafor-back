package org.forpdi.planning.plan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanMacroFieldTest {

	@Test
	@DisplayName("Deve definir e obter o rótulo do campo corretamente")
	void givenLabel_whenSetLabel_thenGetLabelShouldReturnCorrectValue() {
		PlanMacroField field = new PlanMacroField();
		String label = "Campo de Teste";

		field.setLabel(label);

		assertEquals(label, field.getLabel(), "O rótulo deveria ser o valor configurado.");
	}

	@Test
	@DisplayName("Deve definir e obter o valor do campo corretamente")
	void givenValue_whenSetValue_thenGetValueShouldReturnCorrectValue() {
		PlanMacroField field = new PlanMacroField();
		String value = "Valor de Teste";

		field.setValue(value);

		assertEquals(value, field.getValue(), "O valor deveria ser o configurado.");
	}

	@Test
	@DisplayName("Deve definir e obter o tipo do campo corretamente")
	void givenType_whenSetType_thenGetTypeShouldReturnCorrectValue() {
		PlanMacroField field = new PlanMacroField();
		String type = "Texto";

		field.setType(type);

		assertEquals(type, field.getType(), "O tipo deveria ser o configurado.");
	}

	@Test
	@DisplayName("Deve associar e obter um PlanMacro corretamente")
	void givenMacro_whenSetMacro_thenGetMacroShouldReturnCorrectValue() {
		PlanMacroField field = new PlanMacroField();
		PlanMacro macro = new PlanMacro();

		field.setMacro(macro);

		assertEquals(macro, field.getMacro(), "O PlanMacro deveria ser o configurado.");
	}

	@Test
	@DisplayName("Deve retornar null para campos não inicializados")
	void whenFieldsNotSet_thenFieldsShouldReturnNullOrDefaultValues() {
		PlanMacroField field = new PlanMacroField();

		assertNull(field.getLabel(), "O rótulo deveria ser null por padrão.");
		assertNull(field.getValue(), "O valor deveria ser null por padrão.");
		assertNull(field.getType(), "O tipo deveria ser null por padrão.");
		assertNull(field.getMacro(), "O PlanMacro deveria ser null por padrão.");
	}
}
