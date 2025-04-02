package org.forpdi.planning.structure.dto;

import org.forpdi.planning.structure.StructureLevelInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SaveAttributeInstanceDtoTest {

	@Test
	@DisplayName("Deve criar um SaveAttributeInstanceDto com valores corretos")
	void test_create_dto_with_valid_values() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();
		levelInstance.setId(1L); 

		String url = "http://example.com";

		SaveAttributeInstanceDto dto = new SaveAttributeInstanceDto(levelInstance, url);

		assertNotNull(dto, "O objeto SaveAttributeInstanceDto não deve ser nulo");
		assertEquals(levelInstance, dto.levelInstance(), "A levelInstance deve ser igual à fornecida");
		assertEquals(url, dto.url(), "A URL deve ser igual à fornecida");
	}
}
