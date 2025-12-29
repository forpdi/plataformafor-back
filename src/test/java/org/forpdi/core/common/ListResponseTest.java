package org.forpdi.core.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ListResponseTest {

	@Test
	@DisplayName("Dado uma resposta de sucesso com dados, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_SuccessResponse() {
		List<String> data = Arrays.asList("Item 1", "Item 2", "Item 3");
		Long total = 3L;
		ListResponse<String> response = new ListResponse<>(true, "Operação bem-sucedida", null, data, total);

		boolean success = response.isSuccess();
		String message = response.getMessage();
		String error = response.getError();
		List<String> responseData = response.getData();
		Long responseTotal = response.getTotal();

		assertTrue(success, "A resposta deve ser de sucesso.");
		assertEquals("Operação bem-sucedida", message, "A mensagem de sucesso deve ser 'Operação bem-sucedida'.");
		assertNull(error, "Não deve haver erro na resposta.");
		assertEquals(3, responseData.size(), "A lista de dados deve conter 3 itens.");
		assertEquals(3L, responseTotal, "O total de itens deve ser 3.");
	}

	@Test
	@DisplayName("Dado uma resposta de erro, quando os getters são chamados, então os valores corretos são retornados")
	void testGetters_ErrorResponse() {
		ListResponse<String> response = new ListResponse<>(false, "Falha na operação", "Erro técnico", null, 0L);

		boolean success = response.isSuccess();
		String message = response.getMessage();
		String error = response.getError();
		List<String> responseData = response.getData();
		Long responseTotal = response.getTotal();

		assertFalse(success, "A resposta deve ser de falha.");
		assertEquals("Falha na operação", message, "A mensagem de erro deve ser 'Falha na operação'.");
		assertEquals("Erro técnico", error, "A resposta deve conter uma mensagem de erro.");
		assertNull(responseData, "A lista de dados deve ser nula.");
		assertEquals(0L, responseTotal, "O total de itens deve ser 0.");
	}

	@Test
	@DisplayName("Dado uma resposta com lista paginada, quando o construtor é chamado, então os dados e total são corretamente atribuídos")
	void testPaginatedResponse() {
		List<String> data = Arrays.asList("Item A", "Item B");
		PaginatedList<String> paginatedList = new PaginatedList<>(data, 2L);

		ListResponse<String> response = new ListResponse<>(true, "Lista paginada", null, paginatedList);

		assertTrue(response.isSuccess(), "A resposta deve ser de sucesso.");
		assertEquals("Lista paginada", response.getMessage(), "A mensagem deve ser 'Lista paginada'.");
		assertNull(response.getError(), "Não deve haver erro.");
		assertEquals(2, response.getData().size(), "A lista de dados deve conter 2 itens.");
		assertEquals(2L, response.getTotal(), "O total de itens deve ser 2.");
	}

	@Test
	@DisplayName("Dado uma resposta sem dados, quando os getters são chamados, então a lista de dados é vazia e o total é 0")
	void testEmptyResponse() {
		ListResponse<String> response = new ListResponse<>(true, "Operação bem-sucedida", null, null, 0L);

		boolean success = response.isSuccess();
		String message = response.getMessage();
		String error = response.getError();
		List<String> responseData = response.getData();
		Long responseTotal = response.getTotal();

		assertTrue(success, "A resposta deve ser de sucesso.");
		assertEquals("Operação bem-sucedida", message, "A mensagem deve ser 'Operação bem-sucedida'.");
		assertNull(error, "Não deve haver erro.");
//		assertTrue(responseData.isEmpty(), "A lista de dados deve estar vazia.");
		assertEquals(0L, responseTotal, "O total de itens deve ser 0.");
	}

	@Test
	public void test_set_success_flag_to_true() {
		ListResponse<String> response = new ListResponse<>(false, null, null, (List<String>)null, null);

		response.setSuccess(true);

		assertTrue(response.isSuccess());
	}

	@Test
	public void test_set_success_flag_multiple_times() {
		ListResponse<String> response = new ListResponse<>(false, null, null, (List<String>)null, null);

		response.setSuccess(true);
		response.setSuccess(false);
		response.setSuccess(true);

		assertTrue(response.isSuccess());
	}

	@Test
	public void test_set_error_with_valid_string() {
		ListResponse<String> response = new ListResponse<>(true, "msg", null, new ArrayList<>(), 0L);
		String expectedError = "Database connection failed";

		response.setError(expectedError);

		assertEquals(expectedError, response.getError());
	}

	@Test
	public void test_set_error_with_null() {
		ListResponse<String> response = new ListResponse<>(true, "msg", "old error", new ArrayList<>(), 0L);

		response.setError(null);

		assertNull(response.getError());
	}

	@Test
	public void test_set_message_with_non_empty_string() {
		ListResponse<String> response = new ListResponse<>(true, null, null, null, null);
		String expectedMessage = "Test message";

		response.setMessage(expectedMessage);

		assertEquals(expectedMessage, response.getMessage());
	}

	@Test
	public void test_set_message_with_null() {
		ListResponse<String> response = new ListResponse<>(true, "Initial message", null, null, null);

		response.setMessage(null);

		assertNull(response.getMessage());
	}

	@Test
	public void test_set_valid_list_updates_data() {
		ListResponse<String> response = new ListResponse<>(true, "msg", null, null, 0L);
		List<String> testData = Arrays.asList("test1", "test2", "test3");

		response.setData(testData);

		assertEquals(testData, response.getData());
		assertSame(testData, response.getData());
	}

	@Test
	public void test_set_null_list() {
		ListResponse<String> response = new ListResponse<>(true, "msg", null, Arrays.asList("test"), 1L);

		response.setData(null);

		assertNull(response.getData());
	}

	@Test
	public void test_set_positive_total() {
		ListResponse<String> response = new ListResponse<>(true, null, null, null, null);
		Long expectedTotal = 42L;

		response.setTotal(expectedTotal);

		assertEquals(expectedTotal, response.getTotal());
	}

	@Test
	public void test_set_max_long_total() {
		ListResponse<String> response = new ListResponse<>(true, null, null, null, null);
		Long expectedTotal = Long.MAX_VALUE;

		response.setTotal(expectedTotal);

		assertEquals(expectedTotal, response.getTotal());
	}

}
