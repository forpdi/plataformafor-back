package org.forrisco.risk.dto;

import jakarta.validation.constraints.NotNull;

public record RiskExportAxesCSVRequest(@NotNull Long planId, @NotNull Long selectedAxis, @NotNull Integer selectedYear) {
}
