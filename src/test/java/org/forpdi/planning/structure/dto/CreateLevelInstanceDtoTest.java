package org.forpdi.planning.structure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CreateLevelInstanceDtoTest {


    @Test
    @DisplayName("Deve criar um CreateLevelInstanceDto com valores válidos")
    void test_create_level_instance_dto_with_valid_values() {
        Long planId = 123L;
        Long levelId = 456L;
        String instanceName = "Test Instance";
        Long parentId = 789L;

        CreateLevelInstanceDto dto = new CreateLevelInstanceDto(planId, levelId, instanceName, parentId);

        assertNotNull(dto, "O objeto CreateLevelInstanceDto não deve ser nulo");
        assertEquals(planId, dto.planId(), "O planId deve ser igual ao fornecido");
        assertEquals(levelId, dto.levelId(), "O levelId deve ser igual ao fornecido");
        assertEquals(instanceName, dto.instanceName(), "O instanceName deve ser igual ao fornecido");
        assertEquals(parentId, dto.parentId(), "O parentId deve ser igual ao fornecido");
    }
}
