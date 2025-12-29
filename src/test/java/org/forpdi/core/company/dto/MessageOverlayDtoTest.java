package org.forpdi.core.company.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageOverlayDtoTest {

    @Test
    @DisplayName("Deve instanciar MessageOverlayDto corretamente")
    void testMessageOverlayDtoCreation() {
        String key = "mensagem-chave";
        String value = "valor-da-mensagem";

        MessageOverlayDto dto = new MessageOverlayDto(key, value);

        assertNotNull(dto, "O objeto MessageOverlayDto não deve ser nulo.");
        assertEquals(key, dto.key(), "A chave no DTO deve ser igual à fornecida.");
        assertEquals(value, dto.value(), "O valor no DTO deve ser igual ao fornecido.");
    }
}

