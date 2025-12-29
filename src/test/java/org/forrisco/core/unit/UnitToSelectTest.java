package org.forrisco.core.unit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UnitToSelectTest {

	public UnitToSelect expectedCreatedUnit;

	@BeforeEach
	public void setup() {

		expectedCreatedUnit =
			new UnitToSelect(2L, "One name", "Abbr.", 1L);
	}

	@DisplayName("UnitToSelect Criação de uma Unidade. Construtor Vazio.")
	@Test
	public void testUnitToSelectCreationWithEmptyConstructor() {

		UnitToSelect unit = new UnitToSelect();
		assertNotNull(unit, "A unidade é nula.");
	}

	@DisplayName("UnitToSelect Criação de uma Unidade. Construtor completo.")
	@Test
	public void testUnitToSelectCreationWithFullConstructor() {

		UnitToSelect createdUnitToSelect =
			new UnitToSelect(expectedCreatedUnit.getId(), expectedCreatedUnit.getName(),
				expectedCreatedUnit.getAbbreviation(), expectedCreatedUnit.getParentId());

		assertEquals(expectedCreatedUnit.getId(), createdUnitToSelect.getId(),
			"Os id's da unidade criada e esperada são diferentes.");
		assertEquals(expectedCreatedUnit.getName(), createdUnitToSelect.getName(),
			"O nome da unidade criada e esperada são diferentes.");
		assertEquals(expectedCreatedUnit.getAbbreviation(), createdUnitToSelect.getAbbreviation(),
			"A abreviação da unidade criada e esperada são diferentes.");
		assertEquals(expectedCreatedUnit.getParentId(), createdUnitToSelect.getParentId(),
			"O parent_id da unidade criada e esperada são diferentes.");
	}

	@DisplayName("UnitToSelect Criação de uma Unidade. Setando parâmetros.")
	@Test
	public void testUnitToSelectCreatedSettingAllParams() {

		UnitToSelect unitToSetParams = new UnitToSelect();
		unitToSetParams.setId(expectedCreatedUnit.getId());
		unitToSetParams.setName(expectedCreatedUnit.getName());
		unitToSetParams.setAbbreviation(expectedCreatedUnit.getAbbreviation());
		unitToSetParams.setParentId(expectedCreatedUnit.getParentId());

		assertEquals(expectedCreatedUnit.getId(), unitToSetParams.getId(),
			"O id setado é diferente do esperado.");
		assertEquals(expectedCreatedUnit.getName(), unitToSetParams.getName(),
			"O nome setado é diferente do esperado.");
		assertEquals(expectedCreatedUnit.getAbbreviation(), unitToSetParams.getAbbreviation(),
			"A abreviação setada é diferente da esperada.");
		assertEquals(expectedCreatedUnit.getParentId(), unitToSetParams.getParentId(),
			"O parent_id setado é diferente do esperado.");
	}
}