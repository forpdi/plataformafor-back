package org.forpdi.security.auth;

import org.forpdi.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class AuthDataTest {

	private AuthData authDataFromMap;
	private AuthData authDataFromUser;

	@BeforeEach
	void setUp() {
		Map<String, Object> authDataMap = new HashMap<>();
		authDataMap.put("id", 1L);
		authDataMap.put("name", "Test User");
		authDataMap.put("email", "test@example.com");
		authDataMap.put("accessLevel", 3);

		authDataFromMap = new AuthData(authDataMap);

		User user = new User();
		user.setId(2L);
		user.setName("Another User");
//		user.setUsername("another@example.com");
		user.setAccessLevel(5);

		authDataFromUser = new AuthData(user);
	}

	@Test
	@DisplayName("Deve inicializar corretamente com dados de um Map")
	void testConstructorFromMap() {
		assertEquals(1L, authDataFromMap.getId());
		assertEquals("Test User", authDataFromMap.getName());
		assertEquals("test@example.com", authDataFromMap.getEmail());
		assertEquals(3, authDataFromMap.getAccessLevel());
	}

	@Test
	@DisplayName("Deve inicializar corretamente com dados de um User")
	void testConstructorFromUser() {
		assertEquals(2L, authDataFromUser.getId());
		assertEquals("Another User", authDataFromUser.getName());
//		assertEquals("another@example.com", authDataFromUser.getEmail());
		assertEquals(5, authDataFromUser.getAccessLevel());
	}

	@Test
	@DisplayName("Deve permitir alterar e recuperar o campo id")
	void testSetAndGetId() {
		authDataFromMap.setId(10L);
		assertEquals(10L, authDataFromMap.getId());
	}

	@Test
	@DisplayName("Deve permitir alterar e recuperar o campo name")
	void testSetAndGetName() {
		authDataFromMap.setName("Updated Name");
		assertEquals("Updated Name", authDataFromMap.getName());
	}

	@Test
	@DisplayName("Deve permitir alterar e recuperar o campo email")
	void testSetAndGetEmail() {
		authDataFromMap.setEmail("updated@example.com");
		assertEquals("updated@example.com", authDataFromMap.getEmail());
	}

	@Test
	@DisplayName("Deve permitir alterar e recuperar o campo accessLevel")
	void testSetAndGetAccessLevel() {
		authDataFromMap.setAccessLevel(7);
		assertEquals(7, authDataFromMap.getAccessLevel());
	}
}
