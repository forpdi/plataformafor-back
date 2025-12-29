package org.forpdi.planning.plan.dto;

import org.forpdi.planning.plan.PlanMacro;

public record DuplicatePlanMacroDto(PlanMacro macro, Boolean keepPlanLevel, Boolean keepPlanContent,
		Boolean keepDocSection, Boolean keepDocContent) {
}
