package org.forpdi.core.version;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VersionHistoryTest {

	@Test
	@DisplayName("Deve definir e obter o número da versão corretamente")
	void testSetAndGetNumberVersion() {
		VersionHistory versionHistory = new VersionHistory();
		String numberVersion = "1.0.0";
		versionHistory.setnumberVersion(numberVersion);

		assertEquals(numberVersion, versionHistory.getnumberVersion(), "O número da versão deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter a data de lançamento corretamente")
	void testSetAndGetReleaseDate() {
		VersionHistory versionHistory = new VersionHistory();
		Date releaseDate = new Date();
		versionHistory.setReleaseDate(releaseDate);

		assertEquals(releaseDate, versionHistory.getReleaseDate(), "A data de lançamento deve ser configurada e recuperada corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter as informações de 'infoFor' corretamente")
	void testSetAndGetInfoFor() {
		VersionHistory versionHistory = new VersionHistory();
		String infoFor = "Informações sobre funcionalidades";
		versionHistory.setInfoFor(infoFor);

		assertEquals(infoFor, versionHistory.getInfoFor(), "As informações 'infoFor' devem ser configuradas e recuperadas corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter as informações de 'infoPdi' corretamente")
	void testSetAndGetInfoPdi() {
		VersionHistory versionHistory = new VersionHistory();
		String infoPdi = "Informações sobre o PDI";
		versionHistory.setInfoPdi(infoPdi);

		assertEquals(infoPdi, versionHistory.getInfoPdi(), "As informações 'infoPdi' devem ser configuradas e recuperadas corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter as informações de 'infoRisco' corretamente")
	void testSetAndGetInfoRisco() {
		VersionHistory versionHistory = new VersionHistory();
		String infoRisco = "Informações sobre risco";
		versionHistory.setInfoRisco(infoRisco);

		assertEquals(infoRisco, versionHistory.getInfoRisco(), "As informações 'infoRisco' devem ser configuradas e recuperadas corretamente");
	}

	@Test
	@DisplayName("Deve inicializar a data de lançamento com a data atual")
	void testDefaultReleaseDate() {
		VersionHistory versionHistory = new VersionHistory();

		assertNotNull(versionHistory.getReleaseDate(), "A data de lançamento deve ser inicializada automaticamente");
	}
}
