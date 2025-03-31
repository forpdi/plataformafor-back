package org.forpdi.core.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

	private Response<String> response;

	@BeforeEach
	void setUp() {
		response = new Response<>(true, "Operation successful", null, "Payload data");
	}

	@Test
	@DisplayName("Deve retornar os valores corretos ao criar uma instância")
	void testResponseCreation() {
		assertTrue(response.isSuccess());
		assertEquals("Operation successful", response.getMessage());
		assertNull(response.getError());
		assertEquals("Payload data", response.getData());
	}

	@Test
	@DisplayName("Deve permitir a alteração do campo success")
	void testSetSuccess() {
		response.setSuccess(false);
		assertFalse(response.isSuccess());
	}

	@Test
	@DisplayName("Deve permitir a alteração do campo error")
	void testSetError() {
		response.setError("An error occurred");
		assertEquals("An error occurred", response.getError());
	}

	@Test
	@DisplayName("Deve permitir a alteração do campo message")
	void testSetMessage() {
		response.setMessage("New message");
		assertEquals("New message", response.getMessage());
	}

	@Test
	@DisplayName("Deve permitir a alteração do campo data")
	void testSetData() {
		response.setData("New payload data");
		assertEquals("New payload data", response.getData());
	}

	@Test
	@DisplayName("Deve suportar nulos no campo data")
	void testNullData() {
		response.setData(null);
		assertNull(response.getData());
	}

	@Test
	@DisplayName("Deve funcionar corretamente com tipos genéricos diferentes")
	void testGenericType() {
		Response<Integer> intResponse = new Response<>(false, "Error occurred", "Some error", 123);
		assertFalse(intResponse.isSuccess());
		assertEquals("Error occurred", intResponse.getMessage());
		assertEquals("Some error", intResponse.getError());
		assertEquals(123, intResponse.getData());
	}
}
