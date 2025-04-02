package org.forpdi.core.storage.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.utils.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Erick Alves
 *
 */
@RestController
public class FileController extends AbstractController {
	
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private ArchiveBS archiveBS;
	@Autowired
	private StorageService storageService;
	
	/**
	 * Upload de arquivo
	 * 
	 * @param void.
	 * @return String Url que representa a foto do usuário.
	 */
	@PostMapping(value = Consts.FILES_ENDPOINT_BASE_URL + "/upload")
	public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			Archive archive = new Archive();
			archive.setCompany(this.domain.get().getCompany());
			archive.setName(fileName);
			Archive saved = archiveBS.save(archive);
			String storageFileName = String.format("%d.%s", saved.getId(), FilenameUtils.getExtension(fileName));

			storageService.uploadFile(file.getInputStream(), request.getContentType(), storageFileName);
			return this.success(storageFileName);
		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	@GetMapping(Consts.FILES_ENDPOINT_BASE_URL + "/{file}")
	public ResponseEntity<?> downloadFile(@PathVariable String file) {
		try {
			Long archiveId = file.contains(".")
					? Long.parseLong(FilenameUtils.getBaseName(file))
					: Long.parseLong(file);

			Archive archive = archiveBS.exists(archiveId, Archive.class);
			if (archive == null) {
				throw new IllegalArgumentException("File not found.");
			}
			if (!archive.getCompany().getId().equals(this.domain.get().getCompany().getId())) {
				throw new RuntimeException("Unauthorized access.");
			}

			try (InputStream in = storageService.retrieveFile(archive.getFileLink());
					OutputStream out = this.response.getOutputStream()) {

				this.response.reset();
				this.response.setHeader("Content-Type", "application/" + archive.getExtension());
				this.response.setHeader("Content-Disposition", "inline; filename=\"" + archive.getName() + "\"");
				IOUtils.copy(in, out);
			}

			return nothing();

		} catch (IOException e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
}
