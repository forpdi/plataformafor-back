package org.forpdi.core.communication.dto;

import java.util.Date;

import org.forpdi.core.user.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CommunicationDtoTest {

    @Test
    public void testCommunicationDto_CreationAndFieldValues() {
        Long id = 1L;
        String title = "Nova Comunicação";
        String message = "Esta é uma mensagem de teste.";
        Date validityBegin = new Date();
        Date validityEnd = new Date(validityBegin.getTime() + 86400000L); 
        User responsible = new User();
        boolean showPopup = true;

        CommunicationDto dto = new CommunicationDto(id, title, message, validityBegin, validityEnd, responsible, showPopup);

        assertEquals(id, dto.id(), "O ID do DTO deve corresponder ao valor fornecido.");
        assertEquals(title, dto.title(), "O título do DTO deve corresponder ao valor fornecido.");
        assertEquals(message, dto.message(), "A mensagem do DTO deve corresponder ao valor fornecido.");
        assertEquals(validityBegin, dto.validityBegin(), "A data de início de validade deve corresponder ao valor fornecido.");
        assertEquals(validityEnd, dto.validityEnd(), "A data de fim de validade deve corresponder ao valor fornecido.");
        assertEquals(responsible, dto.responsible(), "O responsável deve corresponder ao valor fornecido.");
        assertTrue(dto.showPopup(), "O valor de showPopup deve ser verdadeiro.");
    }
}

