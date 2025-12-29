package org.forpdi.core.version.dto;

import java.util.Date;

public record VersionDto(
		Long id,
		String numberVersion,
		Date releaseDate,
		String infoFor,
		String infoPdi,
		String infoRisco) {
}
