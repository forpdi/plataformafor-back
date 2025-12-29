package org.forpdi.core.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BooleanToIntegerConverterTest {

    private final BooleanToIntegerConverter conv = new BooleanToIntegerConverter();

    @Test
    @DisplayName("convertToDatabaseColumn: null/false → 0; true → 1")
    void convertToDatabaseColumn_basic() {
        assertEquals(0, conv.convertToDatabaseColumn(null));
        assertEquals(0, conv.convertToDatabaseColumn(Boolean.FALSE));
        assertEquals(1, conv.convertToDatabaseColumn(Boolean.TRUE));
    }

    @Test
    @DisplayName("convertToEntityAttribute: null/0/qualquer número ≠1 → false; 1 → true")
    void convertToEntityAttribute_basic() {
        assertEquals(Boolean.FALSE, conv.convertToEntityAttribute(null));
        assertEquals(Boolean.FALSE, conv.convertToEntityAttribute(0));
        assertEquals(Boolean.TRUE,  conv.convertToEntityAttribute(1));
        assertEquals(Boolean.FALSE, conv.convertToEntityAttribute(2));
        assertEquals(Boolean.FALSE, conv.convertToEntityAttribute(-1));
    }
}
