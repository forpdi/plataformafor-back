package org.forrisco.core.unit.dto;

import java.util.List;

import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;

public record DuplicateUnitDto(List<Unit> units, PlanRisk planRisk) {

}
