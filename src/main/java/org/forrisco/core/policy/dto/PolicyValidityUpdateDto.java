package org.forrisco.core.policy.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

public record PolicyValidityUpdateDto(
		@NotNull(message = "A nova data de término é obrigatória.")
		Date validityEnd
) {
}
