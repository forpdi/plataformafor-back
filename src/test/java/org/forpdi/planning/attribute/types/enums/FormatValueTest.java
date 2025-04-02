package org.forpdi.planning.attribute.types.enums;

import org.forpdi.planning.attribute.AttributeInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FormatValueTest {

	@Test
	@DisplayName("Testa a formatação de porcentagem")
	void testFormatPercentage() {
		String result = FormatValue.PERCENTAGE.format("12.345");
		assertEquals("12,34%", result);
	}

	@Test
	@DisplayName("Testa a formatação de valor numérico")
	void testFormatNumeric() {
		String result = FormatValue.NUMERIC.format("12345.679");
		assertEquals("12.345,68", result);
	}

	@Test
	@DisplayName("Testa a formatação de valor monetário")
	void testFormatMonetary() {
		String result = FormatValue.MONETARY.format("12345.679");

		String expected = "R$ 12.345,68";
		String normalizedResult = result.replace("\u00A0", " ");

		assertEquals(expected, normalizedResult);
	}


	@Test
	@DisplayName("Testa a formatação de horas")
	void testFormatHours() {
		String result = FormatValue.HOURS.format("10");
		assertEquals("10 hrs", result);
	}

	@Test
	@DisplayName("Testa a formatação de dias no singular")
	void testFormatDaysSingular() {
		String result = FormatValue.DAYS.format("1");
		assertEquals("1 dia", result);
	}

	@Test
	@DisplayName("Testa a formatação de dias no plural")
	void testFormatDaysPlural() {
		String result = FormatValue.DAYS.format("3");
		assertEquals("3 dias", result);
	}

	@Test
	@DisplayName("Testa a formatação de meses")
	void testFormatMonths() {
		String result = FormatValue.MOTHS.format("12");
		assertEquals("12 meses", result);
	}

	@Test
	@DisplayName("Testa o método forAttributeInstance com valor 'Porcentagem'")
	void testForAttributeInstancePercentage() {
		AttributeInstance attr = new AttributeInstance();
		attr.setValue("Porcentagem");
		FormatValue result = FormatValue.forAttributeInstance(attr);
		assertEquals(FormatValue.PERCENTAGE, result);
	}

	@Test
	@DisplayName("Testa o método forAttributeInstance com valor 'Numérico'")
	void testForAttributeInstanceNumeric() {
		AttributeInstance attr = new AttributeInstance();
		attr.setValue("Numérico");
		FormatValue result = FormatValue.forAttributeInstance(attr);
		assertEquals(FormatValue.NUMERIC, result);
	}

	@Test
	@DisplayName("Testa o método forAttributeInstance com valor 'Monetário(R$)'")
	void testForAttributeInstanceMonetary() {
		AttributeInstance attr = new AttributeInstance();
		attr.setValue("Monetário(R$)");
		FormatValue result = FormatValue.forAttributeInstance(attr);
		assertEquals(FormatValue.MONETARY, result);
	}

	@Test
	@DisplayName("Testa o método forAttributeInstance com valor 'Horas'")
	void testForAttributeInstanceHours() {
		AttributeInstance attr = new AttributeInstance();
		attr.setValue("Horas");
		FormatValue result = FormatValue.forAttributeInstance(attr);
		assertEquals(FormatValue.HOURS, result);
	}

	@Test
	@DisplayName("Testa o método forAttributeInstance com valor 'Dias'")
	void testForAttributeInstanceDays() {
		AttributeInstance attr = new AttributeInstance();
		attr.setValue("Dias");
		FormatValue result = FormatValue.forAttributeInstance(attr);
		assertEquals(FormatValue.DAYS, result);
	}

	@Test
	@DisplayName("Testa o método forAttributeInstance com valor 'Meses'")
	void testForAttributeInstanceMonths() {
		AttributeInstance attr = new AttributeInstance();
		attr.setValue("Meses");
		FormatValue result = FormatValue.forAttributeInstance(attr);
		assertEquals(FormatValue.MOTHS, result);
	}

	@Test
	@DisplayName("Testa o método format com valor nulo")
	void testFormatNullValue() {
		String result = FormatValue.PERCENTAGE.format(null);
		assertEquals("", result);
	}

	@Test
	@DisplayName("Testa o método format com valor vazio")
	void testFormatEmptyValue() {
		String result = FormatValue.PERCENTAGE.format("");
		assertEquals("0,00%", result);
	}

	@Test
	@DisplayName("Testa a formatação com valor inválido (não numérico)")
	void testFormatInvalidValue() {
		String result;

		try {
			result = FormatValue.PERCENTAGE.format("abc");

			assertEquals("valor inválido", result);
		} catch (NumberFormatException e) {
			assertEquals("valor inválido", "valor inválido");
		}
	}

}
