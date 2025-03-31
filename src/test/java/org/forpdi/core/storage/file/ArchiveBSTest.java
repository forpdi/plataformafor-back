package org.forpdi.core.storage.file;

import org.apache.commons.io.FilenameUtils;
import org.forpdi.core.common.HibernateDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveBSTest {

	@InjectMocks
	ArchiveBS archiveBS;

	@Mock
	HibernateDAO hibernateDAO;

	@DisplayName("ArchiveBS salvar arquivo.")
	@Test
	void testSaveArchive() {

		Archive fileTosave = mock(Archive.class);
		when(hibernateDAO.merge(fileTosave)).thenReturn(fileTosave);
		fileTosave.setId(null);

		Archive savedFile = archiveBS.save(fileTosave);

		assertEquals(fileTosave, savedFile, "O arquivo retornado não corresponde com o mockado");
	}

	@DisplayName("ArchiveBS obter arquivo pelo link. Caso link válido.")
	@Test
	void testGetByFileLinkWithValidFileLink() {

		Archive archiveExpected = new Archive();
		archiveExpected.setId(123L);
		String fileLink = archiveExpected.getFileLink();
		Long archiveId = Long.parseLong(FilenameUtils.getBaseName(fileLink));
		when(hibernateDAO.exists(archiveId, Archive.class)).thenReturn(archiveExpected);

		Archive archiveReturned = archiveBS.getByFileLink(fileLink);

		assertEquals(archiveExpected, archiveReturned,
			"O arquivo retornado pela busca por fileLink não corresponde ao esperado.");
	}

	@DisplayName("ArchiveBS obter arquivo pelo link. Caso link inválido.")
	@Test
	void testGetByFileLinkWithInvalidFileLink() {

		String fileLink = "12B.ext";

		Exception exception = assertThrows(IllegalArgumentException.class,
			() -> archiveBS.getByFileLink(fileLink),
			"Era espera o lançamento da exception IllegalArgumentException devido o fileLink inválido.");

		assertEquals("Invalid fileLink format: " + fileLink, exception.getMessage(),
			"As mensagens entre as exceptions diferem");
	}
}