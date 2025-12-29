package org.forpdi.planning.fields.dto;

public record BudgetFieldDto(Long id, Long subAction, String name, String committed, String realized,
		Long instanceId, Long idBudgetElement) {
}
