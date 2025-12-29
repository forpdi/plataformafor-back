package org.forpdi.planning.fields.dto;

public record FieldBudgetUpdateDto(
    String name,
    String subAction,
    Long id,
    String committed,
    String realized,
    Long idBudgetElement
) {

}