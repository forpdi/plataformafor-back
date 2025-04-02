package org.forpdi.core.company.dto;

import org.forpdi.core.notification.dto.SendMessageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SendMessageDtoTest {


    @Test
    @DisplayName("Deve criar um SendMessageDto com valores válidos")
    void test_create_send_message_dto_with_valid_values() {
        String subject = "Test Subject";
        String message = "Test Message";
        Long userId = 123L;

        SendMessageDto dto = new SendMessageDto(subject, message, userId);

        assertNotNull(dto, "O objeto SendMessageDto não deve ser nulo");
        assertEquals(subject, dto.subject(), "O subject deve ser igual ao fornecido");
        assertEquals(message, dto.message(), "A message deve ser igual ao fornecido");
        assertEquals(userId, dto.userId(), "O userId deve ser igual ao fornecido");
    }

    @Test
    @DisplayName("Deve criar um SendMessageDto com subject nulo")
    void test_create_send_message_dto_with_null_subject() {
        String subject = null;
        String message = "Test Message";
        Long userId = 123L;

        SendMessageDto dto = new SendMessageDto(subject, message, userId);

        assertNotNull(dto, "O objeto SendMessageDto não deve ser nulo");
        assertNull(dto.subject(), "O subject deve ser nulo");
        assertEquals(message, dto.message(), "A message deve ser igual ao fornecido");
        assertEquals(userId, dto.userId(), "O userId deve ser igual ao fornecido");
    }
}
