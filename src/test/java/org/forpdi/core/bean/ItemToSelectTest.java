package org.forpdi.core.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemToSelectTest {

	@Test
	@DisplayName("Deve instanciar o objeto ItemToSelect corretamente com o construtor padrão")
	void testDefaultConstructor() {
		ItemToSelect item = new ItemToSelect();

		assertEquals(0, item.getId(), "ID deve ser 0 por padrão");
		assertNull(item.getName(), "Name deve ser nulo por padrão");
	}

	@Test
	@DisplayName("Deve instanciar o objeto ItemToSelect corretamente com o construtor parametrizado")
	void testParameterizedConstructor() {
		long id = 1L;
		String name = "Item Teste";

		ItemToSelect item = new ItemToSelect(id, name);

		assertEquals(id, item.getId(), "ID não foi configurado corretamente");
		assertEquals(name, item.getName(), "Name não foi configurado corretamente");
	}

	@Test
	@DisplayName("Deve configurar e recuperar corretamente o ID")
	void testSetId() {
		ItemToSelect item = new ItemToSelect();

		item.setId(5L);

		assertEquals(5L, item.getId(), "ID não foi configurado corretamente");
	}

	@Test
	@DisplayName("Deve configurar e recuperar corretamente o Name")
	void testSetName() {
		ItemToSelect item = new ItemToSelect();

		item.setName("Novo Item");

		assertEquals("Novo Item", item.getName(), "Name não foi configurado corretamente");
	}
}

