package org.forpdi.planning.structure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

class StructureLevelInstanceListDtoTest {

	@Test
	@DisplayName("Deve construir o DTO corretamente com dados e total")
	void givenStructureLevelInstanceListDto_whenConstructed_thenShouldReturnCorrectValues() {
		List<StructureLevelInstance> data = Arrays.asList(new StructureLevelInstance(), new StructureLevelInstance());
		List<StructureLevelInstance> parents = Arrays.asList(new StructureLevelInstance());
		Long total = 2L;

		StructureLevelInstanceListDto dto = new StructureLevelInstanceListDto(data, total, parents);

		assertNotNull(dto, "DTO não deve ser nulo.");
		assertEquals(data, dto.getData(), "A lista de dados não foi configurada corretamente.");
		assertEquals(total, dto.getTotal(), "O total não foi configurado corretamente.");
		assertEquals(parents, dto.getParents(), "A lista de pais não foi configurada corretamente.");
	}

	@Test
	@DisplayName("Deve permitir configurar e obter a lista de dados")
	void givenStructureLevelInstanceListDto_whenSetData_thenShouldReturnUpdatedData() {
		StructureLevelInstanceListDto dto = new StructureLevelInstanceListDto(
			Arrays.asList(new StructureLevelInstance()), 1L, Arrays.asList(new StructureLevelInstance()));

		List<StructureLevelInstance> newData = Arrays.asList(new StructureLevelInstance(), new StructureLevelInstance());

		dto.setData(newData);

		assertEquals(newData, dto.getData(), "A lista de dados não foi atualizada corretamente.");
	}

	@Test
	@DisplayName("Deve permitir configurar e obter o total")
	void givenStructureLevelInstanceListDto_whenSetTotal_thenShouldReturnUpdatedTotal() {
		StructureLevelInstanceListDto dto = new StructureLevelInstanceListDto(
			Arrays.asList(new StructureLevelInstance()), 1L, Arrays.asList(new StructureLevelInstance()));

		Long newTotal = 3L;

		dto.setTotal(newTotal);

		assertEquals(newTotal, dto.getTotal(), "O total não foi atualizado corretamente.");
	}

	@Test
	@DisplayName("Deve permitir configurar e obter a lista de pais")
	void givenStructureLevelInstanceListDto_whenSetParents_thenShouldReturnUpdatedParents() {
		StructureLevelInstanceListDto dto = new StructureLevelInstanceListDto(
			Arrays.asList(new StructureLevelInstance()), 1L, Arrays.asList(new StructureLevelInstance()));

		List<StructureLevelInstance> newParents = Arrays.asList(new StructureLevelInstance(), new StructureLevelInstance());

		dto.setParents(newParents);

		assertEquals(newParents, dto.getParents(), "A lista de pais não foi atualizada corretamente.");
	}
}
