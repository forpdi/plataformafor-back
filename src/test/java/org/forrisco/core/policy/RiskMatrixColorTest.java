package org.forrisco.core.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskMatrixColorTest {
	@Test
	@DisplayName("Dado a enum, os valores de Id, Name e Code devem corresponder aos declarados.")
	void testEnumValues() {

		assertEquals(0, RiskMatrixColor.RED.getId());
		assertEquals("Vermelho", RiskMatrixColor.RED.getName());
		assertEquals("#f1705f", RiskMatrixColor.RED.getCode());

		assertEquals(1, RiskMatrixColor.GRAY.getId());
		assertEquals("Cinza", RiskMatrixColor.GRAY.getName());
		assertEquals("#c8c8c8", RiskMatrixColor.GRAY.getCode());

		assertEquals(2, RiskMatrixColor.PINK.getId());
		assertEquals("Rosa", RiskMatrixColor.PINK.getName());
		assertEquals("#dd90be", RiskMatrixColor.PINK.getCode());

		assertEquals(3, RiskMatrixColor.ORANGE.getId());
		assertEquals("Laranja", RiskMatrixColor.ORANGE.getName());
		assertEquals("#fcbc70", RiskMatrixColor.ORANGE.getCode());

		assertEquals(4, RiskMatrixColor.GREEN.getId());
		assertEquals("Verde", RiskMatrixColor.GREEN.getName());
		assertEquals("#9cdc9c", RiskMatrixColor.GREEN.getCode());

		assertEquals(5, RiskMatrixColor.BLUE.getId());
		assertEquals("Azul", RiskMatrixColor.BLUE.getName());
		assertEquals("#8cbcdc", RiskMatrixColor.BLUE.getCode());

		assertEquals(6, RiskMatrixColor.YELLOW.getId());
		assertEquals("Amarelo", RiskMatrixColor.YELLOW.getName());
		assertEquals("#fff230", RiskMatrixColor.YELLOW.getCode());

		assertEquals(7, RiskMatrixColor.DEFAULT.getId());
		assertEquals("Default", RiskMatrixColor.DEFAULT.getName());
		assertEquals("#F3F2F3", RiskMatrixColor.DEFAULT.getCode());
	}

	@Test
	@DisplayName("Deve retornar a enum DEFAULT.")
	void testGetDefaultColor() {
		assertEquals(RiskMatrixColor.DEFAULT, RiskMatrixColor.getDefaultColor());
	}

	@Test
	@DisplayName("Deve retornar o código hexadecimal relacionado ao Id.")
	void testGetCodeById() {

		assertEquals("#f1705f", RiskMatrixColor.getCodeById(0));
		assertEquals("#c8c8c8", RiskMatrixColor.getCodeById(1));
		assertEquals("#dd90be", RiskMatrixColor.getCodeById(2));
		assertEquals("#fcbc70", RiskMatrixColor.getCodeById(3));
		assertEquals("#9cdc9c", RiskMatrixColor.getCodeById(4));
		assertEquals("#8cbcdc", RiskMatrixColor.getCodeById(5));
		assertEquals("#fff230", RiskMatrixColor.getCodeById(6));
		assertEquals("#F3F2F3", RiskMatrixColor.getCodeById(7));

		assertEquals("#F3F2F3", RiskMatrixColor.getCodeById(-1));
		assertEquals("#F3F2F3", RiskMatrixColor.getCodeById(100));
	}

	@Test
	@DisplayName("Dado a consulta do Name por Id, deve retornar o nome da Enum relacionada ao id.")
	void testGetNameByIdExistentIdCase() {

		assertEquals("Vermelho", RiskMatrixColor.getNameById(0));
		assertEquals("Cinza", RiskMatrixColor.getNameById(1));
		assertEquals("Rosa", RiskMatrixColor.getNameById(2));
		assertEquals("Laranja", RiskMatrixColor.getNameById(3));
		assertEquals("Verde", RiskMatrixColor.getNameById(4));
		assertEquals("Azul", RiskMatrixColor.getNameById(5));
		assertEquals("Amarelo", RiskMatrixColor.getNameById(6));
		assertEquals("Default", RiskMatrixColor.getNameById(7));

		assertEquals("Default", RiskMatrixColor.getNameById(-1));
		assertEquals("Default", RiskMatrixColor.getNameById(100));
	}

	@Test
	@DisplayName("Dado a consulta do Name por Id, deve retornar 'Default' quando o id não corresponder a nenhum Enum.")
	void testGetNameByIdInvalidIdCase() {

		assertEquals("Default", RiskMatrixColor.getNameById(-1));
		assertEquals("Default", RiskMatrixColor.getNameById(100));
	}
}