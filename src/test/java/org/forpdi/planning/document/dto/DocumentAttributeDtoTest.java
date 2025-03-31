package org.forpdi.planning.document.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DocumentAttributeDtoTest {


    @Test
    @DisplayName("Should create DocumentAttributeDto with valid parameters")
    void test_document_attribute_dto_creation() {
        Long section = 123L;
        String name = "Test Document";
        String type = "PDF";
        Boolean periodicity = true;

        DocumentAttributeDto dto = new DocumentAttributeDto(section, name, type, periodicity);

        assertNotNull(dto, "DocumentAttributeDto should not be null");
        assertEquals(section, dto.section(), "Section should match the provided value");
        assertEquals(name, dto.name(), "Name should match the provided value");
        assertEquals(type, dto.type(), "Type should match the provided value");
        assertEquals(periodicity, dto.periodicity(), "Periodicity should match the provided value");
    }
}
