package org.forpdi.core.version.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VersionDtoTest {

	@DisplayName("VersionDto Criação do Dto.")
	@Test
	public void testVersionDtoCreate(){

		Long id = 1L;
		String numberVersion = "1.0.0";
		Date releaseDate = Date.from(Instant.now());
		String infoFor="infoFor";
		String infoPdi="infoPdi";
		String infoRisco="infoRisc";

		VersionDto versionDto = new VersionDto(id, numberVersion, releaseDate, infoFor, infoPdi, infoRisco);

		assertEquals(id, versionDto.id(), "Os Id's não correspondem");
		assertEquals(numberVersion, versionDto.numberVersion(), "O número da versão são diferentes");
		assertEquals(releaseDate, versionDto.releaseDate(), "A data de lançamento são diferentes");
		assertEquals(infoFor, versionDto.infoFor(), "As informações do For não correspondem");
		assertEquals(infoPdi, versionDto.infoPdi(), "As informações do Pdi não correspondem");
		assertEquals(infoRisco, versionDto.infoRisco(), "As informações do Risco não correspondem");
	}
}