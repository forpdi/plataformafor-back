package org.forrisco.core.item.dto;

import java.util.List;

import org.forrisco.core.item.PlanRiskItem;
import org.forrisco.core.plan.PlanRisk;

public record DuplicateItemDto(List<PlanRiskItem> itens, PlanRisk planRisk) {

}
