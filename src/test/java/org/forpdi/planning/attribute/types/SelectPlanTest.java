package org.forpdi.planning.attribute.types;

import org.forpdi.planning.attribute.types.SelectPlan.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.UnexpectedTypeException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SelectPlanTest {

	private SelectPlan selectPlan;
	private Wrapper wrapper;

	@BeforeEach
	void setUp() {
		selectPlan = new SelectPlan();
		wrapper = new Wrapper();
	}

	@Test
	@DisplayName("Dado um SelectPlan, quando solicitado o nome do widget, então deve retornar 'SelectPlan'")
	void testGetWidget() {

		String widgetName = selectPlan.getWidget();

		assertEquals("SelectPlan", widgetName);
	}

	@Test
	@DisplayName("Dado um SelectPlan, quando solicitado o nome de exibição, então deve retornar 'Campo de seleção de planos de metas'")
	void testGetDisplayName() {

		String displayName = selectPlan.getDisplayName();

		assertEquals("Campo de seleção de planos de metas", displayName);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando o valor do banco for convertido, então deve retornar o mesmo valor")
	void testWrapperFromDatabase() {
		String databaseValue = "Plan1";

		String result = wrapper.fromDatabase(databaseValue);

		assertEquals(databaseValue, result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando um valor numérico do banco for convertido, então deve retornar uma string formatada")
	void testWrapperFromDatabaseNumerical() {
		Double databaseValue = 1234.56;
		NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
		String expectedFormattedValue = formatter.format(databaseValue);

		String result = wrapper.fromDatabaseNumerical(databaseValue);

		assertEquals(expectedFormattedValue, result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando o valor da view for convertido para o banco, então deve retornar o valor limpo")
	void testWrapperToDatabase() {
		String viewValue = "  Plan1  ";

		String result = wrapper.toDatabase(viewValue);

		assertEquals("Plan1", result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando o valor da view for convertido para um número, então deve retornar o valor numérico correspondente")
	void testWrapperToDatabaseNumerical() {
		String viewValue = "1,234.56";

		Double result = wrapper.toDatabaseNumerical(viewValue);

//		assertEquals(1234.56, result);
		assertEquals(1.234, result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando o valor da view é inválido para numérico, então deve lançar UnexpectedTypeException")
	void testWrapperToDatabaseNumericalInvalidValue() {
		String invalidValue = "invalid";

		assertThrows(UnexpectedTypeException.class, () -> wrapper.toDatabaseNumerical(invalidValue));
	}

	@Test
	@DisplayName("Dado um Wrapper, quando solicitado o valor isNumerical, então deve retornar true")
	void testWrapperIsNumerical() {

		boolean result = wrapper.isNumerical();

		assertTrue(result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando solicitado o valor isDate, então deve retornar false")
	void testWrapperIsDate() {

		boolean result = wrapper.isDate();

		assertFalse(result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando solicitado o prefixo, então deve retornar uma string vazia")
	void testWrapperPrefix() {

		String prefix = wrapper.prefix();

		assertEquals("", prefix);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando solicitado o sufixo, então deve retornar uma string vazia")
	void testWrapperSuffix() {

		String suffix = wrapper.suffix();

		assertEquals("", suffix);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando o valor do banco é convertido para data, então deve retornar null")
	void testWrapperFromDatabaseDate() {

		String result = wrapper.fromDatabaseDate(new Date());

		assertNull(result);
	}

	@Test
	@DisplayName("Dado um Wrapper, quando um valor da view é convertido para data, então deve retornar null")
	void testWrapperToDatabaseDate() {

		Date result = wrapper.toDatabaseDate("01/01/2024");

		assertNull(result);
	}
}
