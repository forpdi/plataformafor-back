package org.forpdi.planning.fields.dto;

import org.forpdi.planning.fields.actionplan.ActionPlan;

public record ActionPlanDto(ActionPlan actionPlan, Long levelInstanceId, String begin, String end, Long id, Boolean checked,
		Boolean notChecked, Boolean finished, Long userId, String description, Long linkedGoalId) {
}
