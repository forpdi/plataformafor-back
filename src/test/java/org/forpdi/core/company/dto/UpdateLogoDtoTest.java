package org.forpdi.core.company.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateLogoDtoTest {

    @Test
    @DisplayName("Deve instanciar UpdateLogoDto corretamente")
    void testUpdateLogoDtoCreation() {
        Long companyId = 1L;
        String fileLink = "https://example.com/logo.png";

        UpdateLogoDto dto = new UpdateLogoDto(companyId, fileLink);

        assertNotNull(dto, "O objeto UpdateLogoDto não deve ser nulo.");
        assertEquals(companyId, dto.companyId(), "O ID da empresa deve ser igual ao fornecido.");
        assertEquals(fileLink, dto.fileLink(), "O link do arquivo deve ser igual ao fornecido.");
    }
}

