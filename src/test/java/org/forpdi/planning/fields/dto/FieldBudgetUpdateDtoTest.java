package org.forpdi.planning.fields.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class FieldBudgetUpdateDtoTest {

    @Test
    public void testCreateFieldBudgetUpdateDto_WithValidData() {
        String name = "Field Name";
        String subAction = "Action-001";
        Long id = 1L;
        String committed = "5000.00";
        String realized = "4500.00";
        Long idBudgetElement = 101L;

        FieldBudgetUpdateDto dto = new FieldBudgetUpdateDto(name, subAction, id, committed, realized, idBudgetElement);

        assertEquals(name, dto.name(), "O name está incorreto.");
        assertEquals(subAction, dto.subAction(), "O subAction está incorreto.");
        assertEquals(id, dto.id(), "O ID está incorreto.");
        assertEquals(committed, dto.committed(), "O valor committed está incorreto.");
        assertEquals(realized, dto.realized(), "O valor realized está incorreto.");
        assertEquals(idBudgetElement, dto.idBudgetElement(), "O idBudgetElement está incorreto.");
    }

    @Test
    public void testCreateFieldBudgetUpdateDto_WithNullValues() {
        String name = null;
        String subAction = null;
        Long id = null;
        String committed = null;
        String realized = null;
        Long idBudgetElement = null;

        FieldBudgetUpdateDto dto = new FieldBudgetUpdateDto(name, subAction, id, committed, realized, idBudgetElement);

        assertNull(dto.name(), "O name deveria ser nulo.");
        assertNull(dto.subAction(), "O subAction deveria ser nulo.");
        assertNull(dto.id(), "O ID deveria ser nulo.");
        assertNull(dto.committed(), "O valor committed deveria ser nulo.");
        assertNull(dto.realized(), "O valor realized deveria ser nulo.");
        assertNull(dto.idBudgetElement(), "O idBudgetElement deveria ser nulo.");
    }

    @Test
    public void testCreateFieldBudgetUpdateDto_WithMixedValues() {
        String name = "Field Name";
        String subAction = null;
        Long id = 1L;
        String committed = null;
        String realized = "4500.00";
        Long idBudgetElement = null;

        FieldBudgetUpdateDto dto = new FieldBudgetUpdateDto(name, subAction, id, committed, realized, idBudgetElement);

        assertEquals(name, dto.name(), "O name está incorreto.");
        assertNull(dto.subAction(), "O subAction deveria ser nulo.");
        assertEquals(id, dto.id(), "O ID está incorreto.");
        assertNull(dto.committed(), "O valor committed deveria ser nulo.");
        assertEquals(realized, dto.realized(), "O valor realized está incorreto.");
        assertNull(dto.idBudgetElement(), "O idBudgetElement deveria ser nulo.");
    }
}