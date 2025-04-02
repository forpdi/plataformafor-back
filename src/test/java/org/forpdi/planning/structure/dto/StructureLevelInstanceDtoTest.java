package org.forpdi.planning.structure.dto;

import org.forpdi.core.common.UnimplementedMethodException;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureLevel;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructureLevelInstanceDtoTest {


	@Test
	void test_throw_unimplemented_method_exception_with_default_constructor() {
		assertThrows(UnimplementedMethodException.class, () -> {
			throw new UnimplementedMethodException();
		});
	}

	@Test
	void test_throw_unimplemented_method_exception_with_null_message() {
		UnimplementedMethodException exception = assertThrows(UnimplementedMethodException.class, () -> {
			throw new UnimplementedMethodException();
		});
		assertNull(exception.getMessage());
	}
	// Create DTO with valid StructureLevelInstance containing all required fields
	@Test
	void test_create_dto_with_valid_structure_level_instance() {
		StructureLevelInstance levelInstance = new StructureLevelInstance();
		levelInstance.setName("Test Level");
		levelInstance.setLevel(new StructureLevel());
		levelInstance.setPlan(new Plan());

		StructureLevelInstanceDto dto = new StructureLevelInstanceDto(levelInstance);

		assertNotNull(dto);
//		assertEquals(levelInstance, dto.levelInstance());
	}

}
