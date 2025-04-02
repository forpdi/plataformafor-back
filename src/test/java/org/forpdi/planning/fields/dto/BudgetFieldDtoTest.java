package org.forpdi.planning.fields.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class BudgetFieldDtoTest {

    @Test
    public void testCreateBudgetFieldDto_WithValidData() {
        Long id = 1L;
        Long subAction = 101L;
        String name = "Field Name";
        String committed = "5000.00";
        String realized = "4500.00";
        Long instanceId = 202L;
        Long idBudgetElement = 303L;

        BudgetFieldDto dto = new BudgetFieldDto(id, subAction, name, committed, realized, instanceId, idBudgetElement);

        assertEquals(id, dto.id(), "O ID está incorreto.");
        assertEquals(subAction, dto.subAction(), "O subAction está incorreto.");
        assertEquals(name, dto.name(), "O nome está incorreto.");
        assertEquals(committed, dto.committed(), "O valor 'committed' está incorreto.");
        assertEquals(realized, dto.realized(), "O valor 'realized' está incorreto.");
        assertEquals(instanceId, dto.instanceId(), "O instanceId está incorreto.");
        assertEquals(idBudgetElement, dto.idBudgetElement(), "O idBudgetElement está incorreto.");
    }

    @Test
    public void testCreateBudgetFieldDto_WithNullValues() {
        Long id = null;
        Long subAction = null;
        String name = null;
        String committed = null;
        String realized = null;
        Long instanceId = null;
        Long idBudgetElement = null;

        BudgetFieldDto dto = new BudgetFieldDto(id, subAction, name, committed, realized, instanceId, idBudgetElement);

        assertNull(dto.id(), "O ID deveria ser nulo.");
        assertNull(dto.subAction(), "O subAction deveria ser nulo.");
        assertNull(dto.name(), "O nome deveria ser nulo.");
        assertNull(dto.committed(), "O valor 'committed' deveria ser nulo.");
        assertNull(dto.realized(), "O valor 'realized' deveria ser nulo.");
        assertNull(dto.instanceId(), "O instanceId deveria ser nulo.");
        assertNull(dto.idBudgetElement(), "O idBudgetElement deveria ser nulo.");
    }

    @Test
    public void testCreateBudgetFieldDto_WithMixedValues() {
        Long id = 1L;
        Long subAction = null;
        String name = "Field Name";
        String committed = null;
        String realized = "4500.00";
        Long instanceId = null;
        Long idBudgetElement = 303L;

        BudgetFieldDto dto = new BudgetFieldDto(id, subAction, name, committed, realized, instanceId, idBudgetElement);

        assertEquals(id, dto.id(), "O ID está incorreto.");
        assertNull(dto.subAction(), "O subAction deveria ser nulo.");
        assertEquals(name, dto.name(), "O nome está incorreto.");
        assertNull(dto.committed(), "O valor 'committed' deveria ser nulo.");
        assertEquals(realized, dto.realized(), "O valor 'realized' está incorreto.");
        assertNull(dto.instanceId(), "O instanceId deveria ser nulo.");
        assertEquals(idBudgetElement, dto.idBudgetElement(), "O idBudgetElement está incorreto.");
    }
}

