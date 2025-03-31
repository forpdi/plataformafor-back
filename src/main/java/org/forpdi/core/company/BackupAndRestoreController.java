package org.forpdi.core.company;


import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.utils.Util;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class BackupAndRestoreController extends AbstractController {

	@Autowired private BackupAndRestoreHelper dbbackup;
	@Autowired private CompanyDomainContext domain;
	
	private static MultipartFile file = null;

	private static synchronized void setFile(MultipartFile multipartFile) {
		file = multipartFile;
	}

	private static synchronized MultipartFile getFile() {
		return file;
	}
	
	
	/**
	 * Backup das tabelas
	 * 
	 *@param id 
	 *		id plano macro
	 *
	 */
	@GetMapping("api/company/export")
	@PreAuthorize(value=AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> validexport() {
		try {
			
			if (this.domain == null || this.domain.get().getCompany() == null) {
				return this.fail("Não há instituição cadastrada para exportar");
			}
			return this.success();
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return this.nothing();
		}
	}
	
	@GetMapping("company/export")
	@PreAuthorize(value=AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> export(@RequestParam(required = false) String ids) {
		try {
			List<Long> planMacroIds = Util.stringListToLongList(ids);
			
			if (planMacroIds.isEmpty()) {
				return this.fail("Não há planos selecionados");
			}
			
			LOGGER.info("Starting export company '{}'...", this.domain.get().getCompany().getName());
			this.response.setHeader("Content-Disposition", String.format("attachment; filename=plans-%d-%s.fbk",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		
			this.dbbackup.export(this.domain.get().getCompany().getId(), this.response.getOutputStream(), planMacroIds);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return this.fail("Unexpected runtime error" + ex.getMessage());
		}
		return this.nothing();
	}

	
	/**
	 * Restaura tabelas a partir de um arquivo
	 *         
	 * @param file
	 * 		arquivo para restore
	 *  
	 * @param id
	 * 		id company
	 */
	@PostMapping("api/company/fbkupload")
	@PreAuthorize(value=AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?>  fbkupload(@RequestBody MultipartFile file) {
		if (file == null) {
			return this.fail("upload falhou");
		}else if (BackupAndRestoreController.getFile() != null) {
			return this.fail("processo de importação já em andamento");
		} else { 
			try {
				BackupAndRestoreController.setFile(file);
				dbbackup.restore(file);

				return this.success("upload completo.");
			} catch (IllegalArgumentException ex) {
				return this.fail(ex.getMessage());
			} catch (Throwable ex) {
				LOGGER.error("IO error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			} finally {
				BackupAndRestoreController.setFile(null);
			}
		}
	}
	

	/**
	 * Restaura tabelas a partir de um arquivo
	 *         
	 */
	@PostMapping("api/company/restore")
	@PreAuthorize(value=AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?>  restore() {
		if (BackupAndRestoreController.getFile() == null) {
			return this.fail("arquivo não especificado");
		}
		
		if(this.domain == null || this.domain.get().getCompany() == null) {
			BackupAndRestoreController.setFile(null);
			return this.fail("Instituição não definida");
		}
		
		
		try {		
			LOGGER.info("Starting restoration for company '{}'...", this.domain.get().getCompany().getName());
			System.out.println(BackupAndRestoreController.getFile().getOriginalFilename());

			LOGGER.info("Done restoration for company '{}'.", this.domain.get().getCompany().getName());
			
			BackupAndRestoreController.setFile(null);
			
			return this.success("Dados importados com sucesso.");
		} catch (Throwable ex) {
			BackupAndRestoreController.setFile(null);
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
}
