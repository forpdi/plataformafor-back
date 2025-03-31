package org.forpdi.core.user.dto;

import org.forpdi.core.user.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegisterInvitedUserDtoTest {

    @Test
    @DisplayName("Criação do DTO de registro de usuário convidado")
    public void testRegisterInvitedUserDtoCreation() {

        User user = new User(); 
        String birthdate = "1990-01-01";
        Boolean termsAccepted = true;

        RegisterInvitedUserDto dto = new RegisterInvitedUserDto(user, birthdate, termsAccepted);

        assertEquals(user, dto.user(), "O usuário no DTO não corresponde ao esperado.");
        assertEquals(birthdate, dto.birthdate(), "A data de nascimento no DTO não corresponde ao esperado.");
        assertEquals(termsAccepted, dto.termsAccepted(), "O status de aceitação dos termos no DTO não corresponde ao esperado.");
    }
}
