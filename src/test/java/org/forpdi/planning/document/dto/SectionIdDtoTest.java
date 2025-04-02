package org.forpdi.planning.document.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SectionIdDtoTest {


    @Test
    @DisplayName("Deve criar um SectionIdDto com valores corretos")
    void test_section_id_dto_creation() {
        Long sectionId = 123L;

        SectionIdDto dto = new SectionIdDto(sectionId);

        assertNotNull(dto, "O objeto SectionIdDto não deve ser nulo.");
        assertEquals(sectionId, dto.sectionId(), "O ID da seção deve ser igual ao fornecido.");
    }
}
