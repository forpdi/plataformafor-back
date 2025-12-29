package org.forpdi.planning.attribute.types;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Date;

class TextFieldTest {

	@Test
	@DisplayName("Deve retornar o nome do widget corretamente")
	void testGetWidget() {
		TextField textField = new TextField();
		assertEquals("TextField", textField.getWidget(), "O nome do widget deve ser 'TextField'");
	}

	@Test
	@DisplayName("Deve retornar o display name corretamente")
	void testGetDisplayName() {
		TextField textField = new TextField();
		assertEquals("Campo de Texto", textField.getDisplayName(), "O display name deve ser 'Campo de Texto'");
	}

	@Test
	@DisplayName("Deve retornar um wrapper não nulo")
	void testGetWrapper() {
		TextField textField = new TextField();
		assertNotNull(textField.getWrapper(), "O wrapper não deve ser nulo");
		assertTrue(textField.getWrapper() instanceof TextField.Wrapper, "O wrapper deve ser do tipo TextField.Wrapper");
	}

	@Test
	@DisplayName("Deve converter valores de view para banco corretamente")
	void testWrapperToDatabase() {
		TextField.Wrapper wrapper = new TextField.Wrapper();

		assertEquals("", wrapper.toDatabase(null), "Valores nulos devem ser convertidos para string vazia");
		assertEquals("", wrapper.toDatabase("  "), "Espaços em branco devem ser convertidos para string vazia");

		assertEquals("teste", wrapper.toDatabase(" teste "), "Valores devem ser 'trimados' corretamente");
	}

	@Test
	@DisplayName("Deve retornar valores de banco para view sem alterações")
	void testWrapperFromDatabase() {
		TextField.Wrapper wrapper = new TextField.Wrapper();

		assertNull(wrapper.fromDatabase(null), "Valores nulos do banco devem continuar nulos na view");

		assertEquals("teste", wrapper.fromDatabase("teste"), "Valores do banco devem ser retornados sem alterações");
	}

	@Test
	@DisplayName("Deve identificar corretamente se o wrapper é numérico ou de data")
	void testWrapperNumericalAndDateChecks() {
		TextField.Wrapper wrapper = new TextField.Wrapper();
		assertFalse(wrapper.isNumerical(), "O campo de texto não deve ser numérico");
		assertFalse(wrapper.isDate(), "O campo de texto não deve ser de data");
	}

	@Test
	@DisplayName("Deve retornar prefixo e sufixo como strings vazias")
	void testWrapperPrefixAndSuffix() {
		TextField.Wrapper wrapper = new TextField.Wrapper();
		assertEquals("", wrapper.prefix(), "O prefixo deve ser uma string vazia");
		assertEquals("", wrapper.suffix(), "O sufixo deve ser uma string vazia");
	}

	@Test
	@DisplayName("Deve retornar nulo para métodos relacionados a data e numéricos")
	void testWrapperDateAndNumericalMethods() {
		TextField.Wrapper wrapper = new TextField.Wrapper();

		assertNull(wrapper.fromDatabaseDate(new Date()), "Método fromDatabaseDate deve retornar nulo");
		assertNull(wrapper.toDatabaseDate("01/01/2022"), "Método toDatabaseDate deve retornar nulo");

		assertNull(wrapper.fromDatabaseNumerical(123.45), "Método fromDatabaseNumerical deve retornar nulo");
		assertNull(wrapper.toDatabaseNumerical("123.45"), "Método toDatabaseNumerical deve retornar nulo");
	}
}
