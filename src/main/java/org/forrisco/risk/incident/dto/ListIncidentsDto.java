package org.forrisco.risk.incident.dto;

import java.util.List;

public record ListIncidentsDto(List<Long> incidentsId, Integer page, Integer pageSize) {

}
