package org.forpdi.planning.structure.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateGoalDtoTest {


    @Test
    @DisplayName("Deve criar um UpdateGoalDto com valores corretos")
    void test_update_goal_dto_creation() {
        Long id = 123L;
        Boolean openCloseGoal = true;
        String url = "http://example.com";
        String reached = "100";

        UpdateGoalDto dto = new UpdateGoalDto(id, openCloseGoal, url, reached);

        assertNotNull(dto, "O objeto UpdateGoalDto não deve ser nulo");
        assertEquals(id, dto.id(), "O ID deve ser igual ao fornecido");
        assertEquals(openCloseGoal, dto.openCloseGoal(), "O openCloseGoal deve ser igual ao fornecido");
        assertEquals(url, dto.url(), "A URL deve ser igual à fornecida");
        assertEquals(reached, dto.reached(), "O reached deve ser igual ao fornecido");
    }
}
