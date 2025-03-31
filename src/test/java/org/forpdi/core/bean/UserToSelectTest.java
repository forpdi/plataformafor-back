package org.forpdi.core.bean;

import org.forpdi.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserToSelectTest {

	private UserToSelect userToSelect1;
	private UserToSelect userToSelect2;

	@BeforeEach
	void setUp() {
		userToSelect1 = new UserToSelect(1L, "Usuário 1", 3);
		userToSelect2 = new UserToSelect(2L, "Usuário 2", 2);
	}

	@Test
	@DisplayName("Dado uma lista de UserToSelect, quando convertemos para User, então a lista de User deve ser gerada corretamente")
	void testToUserList() {
		List<UserToSelect> usersToSelect = Arrays.asList(userToSelect1, userToSelect2);

		List<User> users = UserToSelect.toUserList(usersToSelect);

		assertEquals(2, users.size(), "A lista de usuários gerada não tem o tamanho esperado");
		assertEquals(userToSelect1.getId(), users.get(0).getId(), "O ID do usuário não foi mapeado corretamente");
		assertEquals(userToSelect1.getName(), users.get(0).getName(), "O nome do usuário não foi mapeado corretamente");
		assertEquals(userToSelect1.getAccessLevel(), users.get(0).getAccessLevel(), "O nível de acesso do usuário não foi mapeado corretamente");
		assertEquals(userToSelect2.getId(), users.get(1).getId(), "O ID do segundo usuário não foi mapeado corretamente");
		assertEquals(userToSelect2.getName(), users.get(1).getName(), "O nome do segundo usuário não foi mapeado corretamente");
		assertEquals(userToSelect2.getAccessLevel(), users.get(1).getAccessLevel(), "O nível de acesso do segundo usuário não foi mapeado corretamente");
	}

	@Test
	@DisplayName("Dado um UserToSelect, quando obtemos o ID, então o ID deve ser retornado corretamente")
	void testGetId() {
		long expectedId = 1L;
		userToSelect1.setId(expectedId);

		long actualId = userToSelect1.getId();

		assertEquals(expectedId, actualId, "O ID retornado não é o esperado");
	}

	@Test
	@DisplayName("Dado um UserToSelect, quando obtemos o nome, então o nome deve ser retornado corretamente")
	void testGetName() {
		String expectedName = "Usuário 1";
		userToSelect1.setName(expectedName);

		String actualName = userToSelect1.getName();

		assertEquals(expectedName, actualName, "O nome retornado não é o esperado");
	}

	@Test
	@DisplayName("Dado um UserToSelect, quando obtemos o nível de acesso, então o nível de acesso deve ser retornado corretamente")
	void testGetAccessLevel() {
		int expectedAccessLevel = 3;
		userToSelect1.setAccessLevel(expectedAccessLevel);

		int actualAccessLevel = userToSelect1.getAccessLevel();

		assertEquals(expectedAccessLevel, actualAccessLevel, "O nível de acesso retornado não é o esperado");
	}

	@Test
	@DisplayName("Dado um UserToSelect, quando definimos o ID, então o ID deve ser atualizado corretamente")
	void testSetId() {
		long expectedId = 10L;

		userToSelect1.setId(expectedId);

		assertEquals(expectedId, userToSelect1.getId(), "O ID não foi atualizado corretamente");
	}

	@Test
	@DisplayName("Dado um UserToSelect, quando definimos o nome, então o nome deve ser atualizado corretamente")
	void testSetName() {
		String expectedName = "Novo Usuário";

		userToSelect1.setName(expectedName);

		assertEquals(expectedName, userToSelect1.getName(), "O nome não foi atualizado corretamente");
	}

	@Test
	@DisplayName("Dado um UserToSelect, quando definimos o nível de acesso, então o nível de acesso deve ser atualizado corretamente")
	void testSetAccessLevel() {
		int expectedAccessLevel = 5;

		userToSelect1.setAccessLevel(expectedAccessLevel);

		assertEquals(expectedAccessLevel, userToSelect1.getAccessLevel(), "O nível de acesso não foi atualizado corretamente");
	}
}
