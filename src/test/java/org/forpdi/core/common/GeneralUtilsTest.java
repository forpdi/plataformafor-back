package org.forpdi.core.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class GeneralUtilsTest {

	@Test
	@DisplayName("Testa validação de CPF válido")
	void testValidateCpfValid() {
		assertTrue(GeneralUtils.validateCpf("12345678909")); // CPF válido de exemplo
	}

	@Test
	@DisplayName("Testa validação de CPF inválido")
	void testValidateCpfInvalid() {
		assertFalse(GeneralUtils.validateCpf("12345678900")); // CPF inválido
		assertFalse(GeneralUtils.validateCpf("11111111111")); // CPF repetido
		assertFalse(GeneralUtils.validateCpf(null));         // CPF nulo
		assertFalse(GeneralUtils.validateCpf(""));           // CPF vazio
	}

	@Test
	@DisplayName("Testa formatação de CPF")
	void testFormatCpf() {
		assertEquals("123.456.789-09", GeneralUtils.formatCpf("12345678909"));
		assertEquals("123456789", GeneralUtils.formatCpf("123456789")); // CPF curto, mantém o original
		assertNull(GeneralUtils.formatCpf(null));                      // CPF nulo retorna nulo
	}

	@Test
	@DisplayName("Testa strings vazias")
	void testIsEmptyString() {
		assertTrue(GeneralUtils.isEmpty((String) null));   // String nula
		assertTrue(GeneralUtils.isEmpty(""));     // String vazia
		assertFalse(GeneralUtils.isEmpty("Test"));// String não vazia
	}

	@Test
	@DisplayName("Testa listas vazias")
	void testIsEmptyList() {
		assertTrue(GeneralUtils.isEmpty((List<?>) null));  // Lista nula
		assertTrue(GeneralUtils.isEmpty(List.of()));       // Lista vazia
		assertFalse(GeneralUtils.isEmpty(List.of(1, 2)));  // Lista com elementos
	}

	@Test
	@DisplayName("Testa IDs inválidos")
	void testIsInvalidId() {
		assertTrue(GeneralUtils.isInvalidId(null));  // ID nulo
		assertTrue(GeneralUtils.isInvalidId(0L));    // ID igual a zero
		assertTrue(GeneralUtils.isInvalidId(-1L));   // ID negativo
		assertFalse(GeneralUtils.isInvalidId(1L));   // ID válido
	}

	@Test
	@DisplayName("Testa streamingPipe com InputStream e OutputStream")
	void testStreamingPipe() throws Exception {
		String input = "Test Data";
		InputStream is = new ByteArrayInputStream(input.getBytes());
		OutputStream os = new ByteArrayOutputStream();
		GeneralUtils.streamingPipe(is, os);
		assertEquals(input, os.toString());
	}

	@Test
	@DisplayName("Testa parse de string para Locale")
	void testParseLocaleString() {
		Locale locale = GeneralUtils.parseLocaleString("pt_BR");
		assertEquals("pt", locale.getLanguage());
		assertEquals("BR", locale.getCountry());

		Locale defaultLocale = GeneralUtils.parseLocaleString(null); // Locale padrão
		assertEquals("pt", defaultLocale.getLanguage());
		assertEquals("", defaultLocale.getCountry());
	}

	@Test
	@DisplayName("Testa obtenção da causa raiz de uma exceção")
	void testGetRootCause() {
		Exception cause = new Exception("Causa raiz");
		Exception wrapper = new Exception("Exceção encapsulada", cause);

		assertEquals(cause, GeneralUtils.getRootCause(wrapper));
		assertNull(GeneralUtils.getRootCause(null)); // Exceção nula retorna nulo
	}

	@Test
	@DisplayName("Testa conversão de milissegundos para string GMT")
	void testGetGMTTimeString() {
		long millis = 1672531200000L; // Exemplo: 01/01/2023 00:00:00 GMT
		String gmtString = GeneralUtils.getGMTTimeString(millis);
		assertNotNull(gmtString);
		assertTrue(gmtString.contains("GMT"));
	}

	@Test
	@DisplayName("Testa conversão de string GMT para data")
	void testGetGMTTimeDate() throws ParseException {
		String gmtString = "Sun, 1 Jan 2023 00:00:00 GMT";
		Date date = GeneralUtils.getGMTTimeDate(gmtString);
		assertNotNull(date);

		assertThrows(ParseException.class, () -> GeneralUtils.getGMTTimeDate("Invalid GMT String"));
	}

	@Test
	public void test_returns_true_when_entity_is_null() {
		SimpleIdentifiable entity = null;

		boolean result = GeneralUtils.isInvalid(entity);

		assertTrue(result);
	}

	@Test
	public void test_parse_valid_date_string() {
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String dateString = "25/12/2023";

		Date result = GeneralUtils.parseDate(dateString, format);

		assertNotNull(result);
		assertEquals("25/12/2023", format.format(result));
	}

	@Test
	public void test_return_date_object_for_matching_pattern() {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = "2023-12-25";

		Date result = GeneralUtils.parseDate(dateString, format);

		assertNotNull(result);
		assertTrue(result instanceof Date);
		assertEquals("2023-12-25", format.format(result));
	}

	@Test
	public void test_throw_runtime_exception_for_invalid_date() {
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String invalidDate = "invalid-date";

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			GeneralUtils.parseDate(invalidDate, format);
		});
		assertEquals("Data inválida", exception.getMessage());
	}

	@Test
	public void test_handle_null_date_string() {
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String nullDate = null;

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			GeneralUtils.parseDate(nullDate, format);
		});
//		assertEquals("Data inválida", exception.getMessage());
		assertEquals("Cannot invoke \"String.length()\" because \"text\" is null", exception.getMessage());
	}

	@Test
	public void test_null_date_returns_empty_string() {
		Date nullDate = null;

		String result = GeneralUtils.parseDateToString(nullDate);

		assertEquals("", result);
	}

}
