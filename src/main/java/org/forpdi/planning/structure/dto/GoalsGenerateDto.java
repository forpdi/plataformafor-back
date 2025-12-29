package org.forpdi.planning.structure.dto;

public record GoalsGenerateDto(Long indicatorId, String name, String manager, String responsible, String description, double expected,
		double minimum, double maximum) {

}
