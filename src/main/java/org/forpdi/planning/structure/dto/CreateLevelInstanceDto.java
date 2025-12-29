package org.forpdi.planning.structure.dto;

public record CreateLevelInstanceDto(Long planId, Long levelId, String instanceName, Long parentId) {

}
