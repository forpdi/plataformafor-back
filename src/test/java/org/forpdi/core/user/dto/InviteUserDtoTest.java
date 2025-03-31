package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InviteUserDtoTest {

    @Test
    @DisplayName("Criação do DTO de convite de usuário")
    public void testInviteUserDtoCreation() {
        String name = "John Doe";
        String email = "johndoe@example.com";
        Integer accessLevel = 3;

        InviteUserDto dto = new InviteUserDto(name, email, accessLevel);

        assertEquals(name, dto.name(), "O nome no DTO não corresponde ao esperado.");
        assertEquals(email, dto.email(), "O email no DTO não corresponde ao esperado.");
        assertEquals(accessLevel, dto.accessLevel(), "O nível de acesso no DTO não corresponde ao esperado.");
    }
}
