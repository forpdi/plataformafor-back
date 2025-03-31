package org.forpdi.planning.fields.attachment;

import org.forpdi.core.user.User;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttachmentTest {
	@DisplayName("Attachment Validação do objeto por meio de Setters e Getters.")
	@Test
	void testAttachmentObjectValidation() {

		Attachment attachment = new Attachment();

		String name = "Attachment Name";
		String description = "Attachment Description";
		String fileLink = "http://plataformafor.com.br/file.pdf";
		Date creation = new Date();
		User author = new User();
		StructureLevelInstance levelInstance = new StructureLevelInstance();
		Long exportStructureLevelInstanceId = 1001L;
		levelInstance.setId(exportStructureLevelInstanceId);
		String exportAuthorMail = "author@example.com";

		attachment.setName(name);
		attachment.setDescription(description);
		attachment.setFileLink(fileLink);
		attachment.setCreation(creation);
		attachment.setAuthor(author);
		attachment.setLevelInstance(levelInstance);
		attachment.setExportStructureLevelInstanceId(exportStructureLevelInstanceId);
		attachment.setExportAuthorMail(exportAuthorMail);

		assertEquals(name, attachment.getName(), "O nome do anexo não foi definido corretamente.");
		assertEquals(description, attachment.getDescription(), "A descrição do anexo não foi definida corretamente.");
		assertEquals(fileLink, attachment.getFileLink(), "O link do arquivo não foi definido corretamente.");
		assertEquals(creation, attachment.getCreation(), "A data de criação do anexo não foi definida corretamente.");
		assertEquals(author, attachment.getAuthor(), "O autor do anexo não foi definido corretamente.");
		assertEquals(levelInstance, attachment.getLevelInstance(), "A instância de nível não foi definida corretamente.");
		assertEquals(exportStructureLevelInstanceId, attachment.getExportStructureLevelInstanceId(),
			"O ID de exportação da instância de nível não foi definido corretamente.");
		assertEquals(exportAuthorMail, attachment.getExportAuthorMail(),
			"O e-mail do autor para exportação não foi definido corretamente.");
	}
}