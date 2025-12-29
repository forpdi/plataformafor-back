package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.table.TableFields;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TableFieldsToSaveDtoTest {


    @Test
    @DisplayName("Should create TableFieldsToSaveDto with valid TableFields")
    void test_table_fields_dto_creation() {
        TableFields tableFields = new TableFields();

        TableFieldsToSaveDto dto = new TableFieldsToSaveDto(tableFields);

        assertNotNull(dto, "The TableFieldsToSaveDto object should not be null");
        assertEquals(tableFields, dto.tableFields(), "The TableFields should match the provided instance");
    }
}
