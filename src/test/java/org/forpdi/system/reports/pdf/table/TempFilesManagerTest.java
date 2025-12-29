package org.forpdi.system.reports.pdf.table;

import org.forpdi.core.properties.SystemConfigs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TempFilesManagerTest {

	private MockedStatic<SystemConfigs> systemConfigsMock;

	@BeforeEach
	void setUp() {
		systemConfigsMock = Mockito.mockStatic(SystemConfigs.class);
	}

	@AfterEach
	void tearDown() {
		systemConfigsMock.close();
	}

	/**
	 * Apaga todas as pastas no caminho fornecido, da mais interna para a mais externa.
	 *
	 * @param path Caminho do diretório.
	 */
	private void apagarCaminho(String path) {
		File diretorio = new File(path);

		while (diretorio != null && diretorio.exists()) {
			diretorio.delete();
			diretorio = diretorio.getParentFile();
		}
	}

	@Test
	@DisplayName("Dado um caminho de armazenamento válido, getTempDir deve retornar o diretório correto")
	void testGetTempDirValidPath() {
		String validPath = "temp/test/storage";
		systemConfigsMock.when(SystemConfigs::getStorePdfs).thenReturn(validPath);

		File tempDir = TempFilesManager.getTempDir();

		assertNotNull(tempDir);
		assertTrue(tempDir.exists());
		assertTrue(tempDir.isDirectory());
		assertEquals(validPath, tempDir.getPath());

		apagarCaminho(validPath);
	}

	@Test
	@DisplayName("Dado um caminho que é um arquivo, getTempDir deve lançar RuntimeException")
	void testGetTempDirPathIsFile() {
		String pathAsFile = "temp/pathAsFile";
		File file = new File(pathAsFile);

		systemConfigsMock.when(SystemConfigs::getStorePdfs).thenReturn(pathAsFile);

		try {
			assertTrue(file.createNewFile(), "O arquivo deveria ser criado para o teste.");
			assertThrows(RuntimeException.class, TempFilesManager::getTempDir);
		} catch (IOException e) {
		} finally {
			file.delete();
		}
	}

	@Test
	@DisplayName("Dado uma configuração ausente, getTempDir deve lançar IllegalStateException")
	void testGetTempDirMissingConfig() {
		systemConfigsMock.when(SystemConfigs::getStorePdfs).thenReturn(null);

		assertThrows(IllegalStateException.class, TempFilesManager::getTempDir);
	}

	@Test
	@DisplayName("Dado um caminho inválido, getTempDir deve lançar RuntimeException")
	void testGetTempDirInvalidPath() {
		String invalidPath = "";

		systemConfigsMock.when(SystemConfigs::getStorePdfs).thenReturn(invalidPath);

		assertThrows(RuntimeException.class, TempFilesManager::getTempDir);
	}

	@Test
	@DisplayName("Dado um caminho nulo, getTempDir deve lançar RuntimeException")
	void testGetTempDirInvalidNullPath() {
		String invalidPath = null;

		systemConfigsMock.when(SystemConfigs::getStorePdfs).thenReturn(invalidPath);

		assertThrows(RuntimeException.class, TempFilesManager::getTempDir);
	}

	@Test
	@DisplayName("Dado um caminho store.pdfs, getTempDir deve lançar RuntimeException")
	void testGetTempDirInvalidStorePath() {
		String invalidPath = "${store.pdfs}";
		File file = new File(invalidPath);
		systemConfigsMock.when(SystemConfigs::getStorePdfs).thenReturn(invalidPath);
		try {
			assertTrue(file.createNewFile());
		} catch (IOException ignored) {
			fail("Erro ao criar arquivo para o teste.");
		}

		assertThrows(RuntimeException.class, TempFilesManager::getTempDir);
		apagarCaminho(invalidPath);
	}

	@Test
	@DisplayName("cleanTempDir deve excluir apenas os arquivos que correspondem ao termo fornecido")
	void testCleanTempDir() {
		File tempDir = new File("temp/cleanTest");
		assertTrue(tempDir.mkdirs());

		try {
			File matchingFile = new File(tempDir, "match-term-123.txt");
			File nonMatchingFile = new File(tempDir, "other-file.txt");

			assertTrue(matchingFile.createNewFile());
			assertTrue(nonMatchingFile.createNewFile());

			TempFilesManager.cleanTempDir(tempDir, "term");

			assertFalse(matchingFile.exists(), "O arquivo apagado não deveria existir.");
			assertTrue(nonMatchingFile.exists(), "O arquivo não apagado deveria existir.");
			matchingFile.delete();
			nonMatchingFile.delete();
		} catch (IOException e) {
			fail("Erro ao criar arquivos temporários para o teste.");
		} finally {
			apagarCaminho("temp/cleanTest");
		}
	}

	@Test
	@DisplayName("deleteTempFile deve emitir um aviso quando o arquivo não puder ser excluído")
	void testDeleteTempFileWarning() {
		File file = mock(File.class);
		when(file.delete()).thenReturn(false);
		when(file.getAbsolutePath()).thenReturn("mocked/path/to/file.txt");

		TempFilesManager.deleteTempFile(file);

		verify(file, times(1)).delete();
	}
}
