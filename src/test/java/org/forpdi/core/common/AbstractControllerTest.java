package org.forpdi.core.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

class ConcreteController extends AbstractController {
}

@ExtendWith(MockitoExtension.class)
class AbstractControllerTest {
	@InjectMocks
	private ConcreteController controller;

	@Test
	@DisplayName("Deve retornar uma resposta de 'notFound'")
	void testNotForbidden() {
		ResponseEntity<Response<Serializable>> callResponse = controller.forbidden();
		assertEquals(HttpServletResponse.SC_FORBIDDEN, callResponse.getStatusCodeValue());
		assertEquals("Forbidden", callResponse.getBody().getError());
		assertEquals("You do not have permission for this action.", callResponse.getBody().getMessage());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'notFound'")
	void testNotFound() {
		ResponseEntity<Response<Serializable>> callResponse = controller.notFound();
		assertEquals(HttpServletResponse.SC_NOT_FOUND, callResponse.getStatusCodeValue());
		assertEquals("Not found", callResponse.getBody().getError());
		assertEquals(null, callResponse.getBody().getMessage());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'fail' com erro e mensagem padrão")
	void testFailDefault() {
		ResponseEntity<Response<Serializable>> callResponse = controller.fail();
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, callResponse.getStatusCodeValue());
		assertEquals(null, callResponse.getBody().getError());
		assertEquals(null, callResponse.getBody().getMessage());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'fail' com mensagem personalizada")
	void testFailWithMessage() {
		String message = "Invalid input";
		ResponseEntity<Response<Serializable>> callResponse = controller.fail(message);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, callResponse.getStatusCodeValue());
		assertEquals(null, callResponse.getBody().getError());
		assertEquals(message, callResponse.getBody().getMessage());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'success' sem dados")
	void testSuccessWithoutData() {
		ResponseEntity<Response<Serializable>> callResponse = controller.success();
		assertEquals(200, callResponse.getStatusCodeValue());
		assertEquals("", callResponse.getBody().getMessage());
		assertEquals(null, callResponse.getBody().getData());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'success' com mensagem personalizada")
	void testSuccessWithMessage() {
		String message = "Operation successful";
		ResponseEntity<Response<Serializable>> callResponse = controller.success(message);
		assertEquals(200, callResponse.getStatusCodeValue());
		assertEquals(message, callResponse.getBody().getMessage());
		assertEquals(null, callResponse.getBody().getData());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'success' apenas com dados")
	void testSuccessWithOnlyData() {
		Integer data = 3;
		ResponseEntity<Response<Serializable>> callResponse = controller.success(data);
		assertEquals(200, callResponse.getStatusCodeValue());
		assertEquals(null, callResponse.getBody().getMessage());
		assertEquals(3, callResponse.getBody().getData());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'success' com dados e mensagem personalizada")
	void testSuccessWithDataAndMessage() {
		String message = "Operation successful";
		String data = "Test Data";
		ResponseEntity<Response<String>> callResponse = controller.success(data, message);
		assertEquals(200, callResponse.getStatusCodeValue());
		assertEquals(message, callResponse.getBody().getMessage());
		assertEquals(data, callResponse.getBody().getData());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'success' com lista e total sem menssagem")
	void testSuccessWithListAndTotalWithouMessage() {
		List<String> list = Arrays.asList("Item1", "Item2");
		Long total = 2L;
		String message = "List retrieved successfully";

		ResponseEntity<ListResponse<String>> callResponse = controller.success(list, total);
		assertEquals(200, callResponse.getStatusCodeValue());
		assertEquals(null, callResponse.getBody().getMessage());
		assertEquals(list, callResponse.getBody().getData());
		assertEquals(total, callResponse.getBody().getTotal());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de a partir de uma 'success' com lista e total. Passado PaginatedList.")
	void testSuccessWithPaginatedList() {
		PaginatedList<String> paginatedList = new PaginatedList<>();
		paginatedList.setList(List.of("A", "B", "C"));
		paginatedList.setTotal(3L);

		ResponseEntity<ListResponse<String>> callResponse = controller.success(paginatedList);
		assertEquals(200, callResponse.getStatusCodeValue());
		assertNull(callResponse.getBody().getMessage());
		assertEquals(paginatedList.getList(), callResponse.getBody().getData());
		assertEquals(paginatedList.getTotal(), callResponse.getBody().getTotal());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'success' com lista e total")
	void testSuccessWithListAndTotal() {
		List<String> list = Arrays.asList("Item1", "Item2");
		Long total = 2L;
		String message = "List retrieved successfully";

		ResponseEntity<ListResponse<String>> callResponse = controller.success(list, total, message);
		assertEquals(200, callResponse.getStatusCodeValue());
		assertEquals(message, callResponse.getBody().getMessage());
		assertEquals(list, callResponse.getBody().getData());
		assertEquals(total, callResponse.getBody().getTotal());
	}

	@Test
	@DisplayName("Deve retornar uma resposta de 'nothing'")
	void testNothing() {
		ResponseEntity<?> callResponse = controller.nothing();
		assertEquals(204, callResponse.getStatusCodeValue());
	}
}
