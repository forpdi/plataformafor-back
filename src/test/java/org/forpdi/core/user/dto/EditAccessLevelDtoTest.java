package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EditAccessLevelDtoTest {

    @Test
    @DisplayName("Criação do DTO de edição de nível de acesso")
    public void testEditAccessLevelDtoCreation() {

        Long userId = 123L;
        int accessLevel = 5;

        EditAccessLevelDto dto = new EditAccessLevelDto(userId, accessLevel);

        assertEquals(userId, dto.userId(), "O userId no DTO não corresponde ao esperado.");
        assertEquals(accessLevel, dto.accessLevel(), "O nível de acesso no DTO não corresponde ao esperado.");
    }
}

