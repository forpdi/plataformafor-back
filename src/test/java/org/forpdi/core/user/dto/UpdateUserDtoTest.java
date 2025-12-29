package org.forpdi.core.user.dto;
import org.forpdi.core.user.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateUserDtoTest {

    @Test
    @DisplayName("Criação do DTO de atualização de usuário")
    public void testUpdateUserDtoCreation() {

        User user = new User();
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";

        UpdateUserDto dto = new UpdateUserDto(user, currentPassword, newPassword);

        assertEquals(user, dto.user(), "O usuário no DTO não corresponde ao esperado.");
        assertEquals(currentPassword, dto.currentPassword(), "A senha atual no DTO não corresponde ao esperado.");
        assertEquals(newPassword, dto.newPassword(), "A nova senha no DTO não corresponde ao esperado.");
    }
}

