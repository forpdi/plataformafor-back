package org.forpdi.planning.fields.dto;

public record FieldBudgetUpdateDto(String name, String subAction, Long id, Double committed,
		Double realized, Long idBudgetElement) {

}
