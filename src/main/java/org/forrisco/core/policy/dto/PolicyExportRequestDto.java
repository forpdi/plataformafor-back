package org.forrisco.core.policy.dto;

import jakarta.validation.constraints.NotNull;

public record PolicyExportRequestDto(@NotNull Long policyId,
									 @NotNull String title,
									 String itens,
									 String subitens) {
}
