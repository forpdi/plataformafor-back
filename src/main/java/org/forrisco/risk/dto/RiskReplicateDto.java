package org.forrisco.risk.dto;

import java.util.List;

import org.forrisco.risk.Risk;

public record RiskReplicateDto(Risk risk, List<Long> targetUnitIds) {

}
