package org.forpdi.core.csvs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportHelperTest {
	private CsvExportHelper csvExportHelper;

	@BeforeEach
	void setUp() {
		csvExportHelper = new CsvExportHelper();
	}

	@Test
	@DisplayName("Verificar se o delimitador CSV padrão é '|'")
	void testGetCsvDelimiter() {
		assertEquals('|', csvExportHelper.getCsvDelimiter(), "O delimitador CSV deve ser '|'");
	}

	@Test
	@DisplayName("Formatar string com aspas para campo CSV corretamente")
	void testFromStringToCsvFieldFormat() {
		String input = "Texto com \"aspas\"";
		String expected = "\"Texto com \"\"aspas\"\"\"|";

		String result = csvExportHelper.fromStringToCsvFieldFormat(input);

		assertEquals(expected, result, "O formato do campo CSV não corresponde ao esperado.");
	}

	@Test
	@DisplayName("Formatar string vazia para campo CSV corretamente")
	void testFromStringToCsvFieldFormatWithEmptyString() {
		String input = "";
		String expected = "\"\"|";

		String result = csvExportHelper.fromStringToCsvFieldFormat(input);

		assertEquals(expected, result, "Um campo vazio deve ser representado como \"\" seguido do delimitador.");
	}

	@Test
	@DisplayName("Formatar valor de desempenho double corretamente para CSV")
	void testFromDoubleToCsvPerformanceFormat() {
		Double performance = 85.456;
		String expected = "85,46%";

		String result = csvExportHelper.fromDoubleToCsvPerformanceFormat(performance);

		assertEquals(expected, result, "O formato de desempenho não corresponde ao esperado.");
	}

	@Test
	@DisplayName("Retornar '-' para valores de desempenho nulos")
	void testFromDoubleToCsvPerformanceFormatWithNullValue() {
		String result = csvExportHelper.fromDoubleToCsvPerformanceFormat(null);

		assertEquals("-", result, "Um valor nulo deve ser representado como '-'.");
	}

	@Test
	@DisplayName("Criar linha CSV com delimitador padrão")
	void testCreateRowWithDefaultDelimiter() {
		String[] values = {"Coluna1", "Coluna2", "Coluna3"};
		String expected = "Coluna1|Coluna2|Coluna3\n";

		String result = csvExportHelper.createRow(values);

		assertEquals(expected, result, "A linha gerada não corresponde ao esperado com o delimitador padrão.");
	}

	@Test
	@DisplayName("Criar linha CSV com delimitador personalizado")
	void testCreateRowWithCustomDelimiter() {
		String[] values = {"Coluna1", "Coluna2", "Coluna3"};
		char delimiter = ',';
		String expected = "Coluna1,Coluna2,Coluna3\n";

		String result = csvExportHelper.createRow(values, delimiter);

		assertEquals(expected, result, "A linha gerada não corresponde ao esperado com um delimitador personalizado.");
	}

	@Test
	@DisplayName("Criar linha CSV com lista de valores e delimitador personalizado")
	void testCreateRowWithListAndCustomDelimiter() {
		List<String> values = Arrays.asList("Valor1", "Valor2", "Valor3");
		char delimiter = ';';
		String expected = "Valor1;Valor2;Valor3\n";

		String result = csvExportHelper.createRow(values, delimiter);

		assertEquals(expected, result, "A linha gerada não corresponde ao esperado com uma lista e delimitador personalizado.");
	}

	@Test
	@DisplayName("Lançar exceção ao criar linha CSV com lista vazia")
	void testCreateRowEmptyList() {
		List<String> values = Arrays.asList();
		char delimiter = '|';

		Exception exception = assertThrows(StringIndexOutOfBoundsException.class, () -> {
			csvExportHelper.createRow(values, delimiter);
		});

		assertNotNull(exception, "Deve lançar uma exceção ao tentar criar uma linha com uma lista vazia.");
	}
}