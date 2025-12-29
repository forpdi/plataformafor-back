package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.table.TableInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class TableFieldsDtoTest {

    @Test
    public void testCreateTableFieldsDto_WithValidData() {
        TableInstance tableInstance = new TableInstance();
        Long tableFieldsId = 123L;

        TableFieldsDto dto = new TableFieldsDto(tableInstance, tableFieldsId);

        assertEquals(tableInstance, dto.tableInstance(), "O TableInstance não foi armazenado corretamente no DTO.");
        assertEquals(tableFieldsId, dto.tableFieldsId(), "O tableFieldsId não foi armazenado corretamente no DTO.");
    }

    @Test
    public void testCreateTableFieldsDto_WithNullValues() {
        TableInstance tableInstance = null;
        Long tableFieldsId = null;

        TableFieldsDto dto = new TableFieldsDto(tableInstance, tableFieldsId);

        assertNull(dto.tableInstance(), "O TableInstance deveria ser nulo.");
        assertNull(dto.tableFieldsId(), "O tableFieldsId deveria ser nulo.");
    }

    @Test
    public void testCreateTableFieldsDto_WithNullTableInstance() {
        TableInstance tableInstance = null;
        Long tableFieldsId = 456L;

        TableFieldsDto dto = new TableFieldsDto(tableInstance, tableFieldsId);

        assertNull(dto.tableInstance(), "O TableInstance deveria ser nulo.");
        assertEquals(tableFieldsId, dto.tableFieldsId(), "O tableFieldsId não foi armazenado corretamente no DTO.");
    }

    @Test
    public void testCreateTableFieldsDto_WithNullTableFieldsId() {
        TableInstance tableInstance = new TableInstance();
        Long tableFieldsId = null;

        TableFieldsDto dto = new TableFieldsDto(tableInstance, tableFieldsId);

        assertEquals(tableInstance, dto.tableInstance(), "O TableInstance não foi armazenado corretamente no DTO.");
        assertNull(dto.tableFieldsId(), "O tableFieldsId deveria ser nulo.");
    }
}

