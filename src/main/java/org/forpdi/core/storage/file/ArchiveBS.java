package org.forpdi.core.storage.file;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

/**
 * Classe de negócios para Arquivos salvos
 * 
 * @author Erick Alves
 *
 */
@Service
public class ArchiveBS extends org.forpdi.core.common.HibernateBusiness {

	public Archive save(Archive archive) {
		archive.setId(null);
		return dao.merge(archive);
	}
	
	public Archive getByFileLink(String fileLink) {
		try {
			Long archiveId = Long.parseLong(FilenameUtils.getBaseName(fileLink));
			return this.exists(archiveId, Archive.class);			
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid fileLink format: " + fileLink);
		}
	}
}
