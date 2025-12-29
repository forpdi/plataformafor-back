// org.forpdi.planning.structure.dto.DuplicateOptionItemDto.java
package org.forpdi.planning.structure.dto;

import java.io.Serializable;

public record DuplicateOptionItemDto(Long id, String label, String path) implements Serializable {}
