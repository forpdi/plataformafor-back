package org.forpdi.core.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class UFTest {

	private UF uf;
	private Region mockRegion;

	@BeforeEach
	void setUp() {
		uf = new UF();
		mockRegion = Mockito.mock(Region.class);
	}

	@Test
	@DisplayName("Dado um UF, quando o nome é definido, então o nome deve ser corretamente armazenado")
	void testSetAndGetName() {
		String name = "São Paulo";
		uf.setName(name);

		assertEquals(name, uf.getName(), "O nome deve ser corretamente retornado");
	}

	@Test
	@DisplayName("Dado um UF, quando a sigla é definida, então a sigla deve ser corretamente armazenada")
	void testSetAndGetAcronym() {
		String acronym = "SP";
		uf.setAcronym(acronym);

		assertEquals(acronym, uf.getAcronym(), "A sigla deve ser corretamente retornada");
	}

	@Test
	@DisplayName("Dado um UF, quando a região é associada, então a região deve ser corretamente retornada")
	void testSetAndGetRegion() {
		uf.setRegion(mockRegion);

		assertEquals(mockRegion, uf.getRegion(), "A região associada deve ser corretamente retornada");
	}
}
