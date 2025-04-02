package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RecoverPasswordDtoTest {

    @Test
    @DisplayName("Criação do DTO de recuperação de senha")
    public void RecoverPasswordDtoCreation() {
        String email = "testes@unitarios.org";

        RecoverPasswordDto dto = new RecoverPasswordDto(email);

        assertEquals(email, dto.email(), "O email no DTO não corresponde ao esperado.");
    }
}
