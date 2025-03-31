package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ResetPasswordDtoTest {

    @Test
	@DisplayName("Redefinição de senha")
    public void testResetPasswordDtoCreation() {
        String password = "newSecurePassword123";

        ResetPasswordDto dto = new ResetPasswordDto(password);

        assertEquals(password, dto.password(), "A senha no DTO não corresponde ao esperado.");
    }
}
