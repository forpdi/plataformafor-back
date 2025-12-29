// org.forpdi.planning.structure.dto.DuplicateLevelInstanceOptionsDto.java
package org.forpdi.planning.structure.dto;

import java.io.Serializable;
import java.util.List;

public record DuplicateLevelInstanceOptionsDto(
    boolean needsParent,
    List<DuplicateOptionItemDto> options
) implements Serializable {}
