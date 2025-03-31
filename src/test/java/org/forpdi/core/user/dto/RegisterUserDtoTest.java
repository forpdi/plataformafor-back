package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegisterUserDtoTest {

    @Test
    @DisplayName("Criação do DTO de registro de usuário com dados válidos")
    public void test_create_register_user_dto_with_valid_data() {

        RegisterUserDto dto = new RegisterUserDto("Tester Unitário", "tester@unitario.org", "securePassword123", 1);
 
        assertEquals("Tester Unitário", dto.name());
        assertEquals("tester@unitario.org", dto.email());
        assertEquals("securePassword123", dto.password());
        assertEquals(1, dto.accessLevel());
    }

    @Test
    @DisplayName("Manipulação de valores nulos no DTO de registro de usuário")
    public void test_handle_null_values_in_register_user_dto() {

        RegisterUserDto dto = new RegisterUserDto(null, null, null, null);

        assertNull(dto.name());
        assertNull(dto.email());
        assertNull(dto.password());
        assertNull(dto.accessLevel());
    }
}