package org.forpdi.planning.structure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GoalsGenerateDtoTest {


    @Test
    @DisplayName("Deve criar um GoalsGenerateDto com valores válidos")
    void test_goals_generate_dto_creation_with_valid_values() {
        Long indicatorId = 123L;
        String name = "Meta 2024";
        String manager = "João Silva";
        String responsible = "Maria Santos";
        String description = "Meta anual de vendas";
        double expected = 100.0;
        double minimum = 80.0;
        double maximum = 120.0;

        GoalsGenerateDto dto = new GoalsGenerateDto(indicatorId, name, manager, responsible, description, expected, minimum, maximum);

        assertNotNull(dto, "O objeto GoalsGenerateDto não deve ser nulo");
        assertEquals(indicatorId, dto.indicatorId(), "O ID do indicador deve ser igual ao fornecido");
        assertEquals(name, dto.name(), "O nome deve ser igual ao fornecido");
        assertEquals(manager, dto.manager(), "O gerente deve ser igual ao fornecido");
        assertEquals(responsible, dto.responsible(), "O responsável deve ser igual ao fornecido");
        assertEquals(description, dto.description(), "A descrição deve ser igual ao fornecida");
        assertEquals(expected, dto.expected(), "O valor esperado deve ser igual ao fornecido");
        assertEquals(minimum, dto.minimum(), "O valor mínimo deve ser igual ao fornecido");
        assertEquals(maximum, dto.maximum(), "O valor máximo deve ser igual ao fornecido");
    }
}
