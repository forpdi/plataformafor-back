package org.forrisco.risk.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisição para exportação de riscos em CSV.
 * <p>
 *  • {@code planRiskId}  — obrigatório<br>
 *  • {@code filters}     — JSON serializado ou {@code null}
 */
public record RiskExportRequestDto(
        @NotNull Long planRiskId,
        String filters            // pode ser null
) {}
