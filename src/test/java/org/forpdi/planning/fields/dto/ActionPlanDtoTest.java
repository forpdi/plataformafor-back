package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.actionplan.ActionPlan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class ActionPlanDtoTest {

    @Test
    public void testCreateActionPlanDto_AllFieldsProvided() {
        ActionPlan actionPlan = new ActionPlan();
        Long levelInstanceId = 1L;
        String begin = "2024-01-01";
        String end = "2024-12-31";
        Long id = 10L;
        Boolean checked = true;
        Boolean notChecked = false;
        Boolean finished = true;
        Long userId = 100L;
        String description = "Action plan description";
        Long linkedGoalId = 200L;

        ActionPlanDto actionPlanDto = new ActionPlanDto(
            actionPlan, levelInstanceId, begin, end, id, checked,
            notChecked, finished, userId, description, linkedGoalId
        );

        assertEquals(actionPlan, actionPlanDto.actionPlan(), "O campo 'actionPlan' não está correto.");
        assertEquals(levelInstanceId, actionPlanDto.levelInstanceId(), "O campo 'levelInstanceId' não está correto.");
        assertEquals(begin, actionPlanDto.begin(), "O campo 'begin' não está correto.");
        assertEquals(end, actionPlanDto.end(), "O campo 'end' não está correto.");
        assertEquals(id, actionPlanDto.id(), "O campo 'id' não está correto.");
        assertEquals(checked, actionPlanDto.checked(), "O campo 'checked' não está correto.");
        assertEquals(notChecked, actionPlanDto.notChecked(), "O campo 'notChecked' não está correto.");
        assertEquals(finished, actionPlanDto.finished(), "O campo 'finished' não está correto.");
        assertEquals(userId, actionPlanDto.userId(), "O campo 'userId' não está correto.");
        assertEquals(description, actionPlanDto.description(), "O campo 'description' não está correto.");
        assertEquals(linkedGoalId, actionPlanDto.linkedGoalId(), "O campo 'linkedGoalId' não está correto.");
    }

    @Test
    public void testCreateActionPlanDto_SomeFieldsNull() {
        ActionPlan actionPlan = null;
        Long levelInstanceId = null;
        String begin = null;
        String end = "2024-12-31";
        Long id = 10L;
        Boolean checked = null;
        Boolean notChecked = null;
        Boolean finished = true;
        Long userId = null;
        String description = null;
        Long linkedGoalId = 200L;

        ActionPlanDto actionPlanDto = new ActionPlanDto(
            actionPlan, levelInstanceId, begin, end, id, checked,
            notChecked, finished, userId, description, linkedGoalId
        );

        assertNull(actionPlanDto.actionPlan(), "O campo 'actionPlan' deveria ser nulo.");
        assertNull(actionPlanDto.levelInstanceId(), "O campo 'levelInstanceId' deveria ser nulo.");
        assertNull(actionPlanDto.begin(), "O campo 'begin' deveria ser nulo.");
        assertEquals(end, actionPlanDto.end(), "O campo 'end' não está correto.");
        assertEquals(id, actionPlanDto.id(), "O campo 'id' não está correto.");
        assertNull(actionPlanDto.checked(), "O campo 'checked' deveria ser nulo.");
        assertNull(actionPlanDto.notChecked(), "O campo 'notChecked' deveria ser nulo.");
        assertEquals(finished, actionPlanDto.finished(), "O campo 'finished' não está correto.");
        assertNull(actionPlanDto.userId(), "O campo 'userId' deveria ser nulo.");
        assertNull(actionPlanDto.description(), "O campo 'description' deveria ser nulo.");
        assertEquals(linkedGoalId, actionPlanDto.linkedGoalId(), "O campo 'linkedGoalId' não está correto.");
    }

    @Test
    public void testCreateActionPlanDto_NoFieldsProvided() {
        ActionPlan actionPlan = null;
        Long levelInstanceId = null;
        String begin = null;
        String end = null;
        Long id = null;
        Boolean checked = null;
        Boolean notChecked = null;
        Boolean finished = null;
        Long userId = null;
        String description = null;
        Long linkedGoalId = null;

        ActionPlanDto actionPlanDto = new ActionPlanDto(
            actionPlan, levelInstanceId, begin, end, id, checked,
            notChecked, finished, userId, description, linkedGoalId
        );

        assertNull(actionPlanDto.actionPlan(), "O campo 'actionPlan' deveria ser nulo.");
        assertNull(actionPlanDto.levelInstanceId(), "O campo 'levelInstanceId' deveria ser nulo.");
        assertNull(actionPlanDto.begin(), "O campo 'begin' deveria ser nulo.");
        assertNull(actionPlanDto.end(), "O campo 'end' deveria ser nulo.");
        assertNull(actionPlanDto.id(), "O campo 'id' deveria ser nulo.");
        assertNull(actionPlanDto.checked(), "O campo 'checked' deveria ser nulo.");
        assertNull(actionPlanDto.notChecked(), "O campo 'notChecked' deveria ser nulo.");
        assertNull(actionPlanDto.finished(), "O campo 'finished' deveria ser nulo.");
        assertNull(actionPlanDto.userId(), "O campo 'userId' deveria ser nulo.");
        assertNull(actionPlanDto.description(), "O campo 'description' deveria ser nulo.");
        assertNull(actionPlanDto.linkedGoalId(), "O campo 'linkedGoalId' deveria ser nulo.");
    }
}

