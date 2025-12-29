package org.forpdi.core.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class IdDtoTest {

    @Test
    void testCreateIdDto_WithValidId() {
        Long validId = 123L;

        IdDto dto = new IdDto(validId);

        assertEquals(validId, dto.id(), "O ID não foi armazenado corretamente no DTO.");
    }

    @Test
    void testCreateIdDto_WithNullId() {
        Long nullId = null;

        IdDto dto = new IdDto(nullId);

        assertNull(dto.id(), "O ID armazenado deveria ser nulo.");
    }
}

