package org.forpdi.planning.structure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class LevelInstanceIdDtoTest {


    @Test
    @DisplayName("Deve criar um LevelInstanceIdDto com valor positivo válido")
    void test_level_instance_id_dto_creation() {
        Long levelInstanceId = 123L;

        LevelInstanceIdDto dto = new LevelInstanceIdDto(levelInstanceId);

        assertNotNull(dto, "O objeto LevelInstanceIdDto não deve ser nulo.");
        assertEquals(levelInstanceId, dto.levelInstanceId(), "O ID da instância de nível deve ser igual ao fornecido.");
    }
}
