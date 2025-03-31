package org.forpdi.core.version;


import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.core.version.dto.VersionDto;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Gabriel Oliveira
 */
@RestController
public class VersionController extends AbstractController {
	
	@Autowired
	private VersionBS bs;
	
	/**
	 * Salvar nova versão
	 * 
	 * @param numberVersion
	 *            Número da versão.
	 * @param releaseDate
	 * 			  Data de lançamento da versão.
	 * @param infoFor, infoPdi, infoRisco
	 * 			  informações sobre cada plataforma.
	 *            
	 */
	@PostMapping("/api/version/new")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> newVersion(@RequestBody VersionDto dto) {
		try {
			VersionHistory versionHistory = new VersionHistory();
			versionHistory.setnumberVersion(SanitizeUtil.sanitize(dto.numberVersion()));
			versionHistory.setReleaseDate(dto.releaseDate());
			versionHistory.setInfoFor(SanitizeUtil.sanitize(dto.infoFor()));
			versionHistory.setInfoPdi(SanitizeUtil.sanitize(dto.infoPdi()));
			versionHistory.setInfoRisco(SanitizeUtil.sanitize(dto.infoRisco()));
			this.bs.saveNewVersion(versionHistory);
			return this.success(versionHistory);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	/**
	 * Retorna uma versão de acordo com seu id.
	 * 
	 * @param id da versão desejada.
	 * @return version, versão desejada que possui o dado id.
	 */
	@GetMapping("/api/version/{id}")
	public ResponseEntity<?> retrieveVersion(@PathVariable Long id) {
		try {
			VersionHistory version = this.bs.retrieveVersionById(id);
			if (version == null) {
				return this.fail("A versão solicitada não foi encontrada.");
			} else {
				return this.success(version);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna versões salvas.
	 * 
	 * @param unitId
	 *            Id da unidade.
	 *            
	 * @return <PaginatedList> Risk
	 */
	@GetMapping("/api/version/list")
	public ResponseEntity<?> listVersions() {
		try {			
			PaginatedList<VersionHistory> versions = this.bs.listVersions();
			return this.success(versions);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Atualizar uma versão no banco de dados.
	 * @param versão, versão a ser atualizada.
	 * @return existent, versão atualizada.
	 */
	@PutMapping("/api/version/update")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> updateVersion(@RequestBody VersionDto version) {
		try {
			VersionHistory existent = this.bs.exists(version.id(), VersionHistory.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			
			existent.setnumberVersion(SanitizeUtil.sanitize(version.numberVersion()));
			existent.setReleaseDate(version.releaseDate());
			existent.setInfoFor(SanitizeUtil.sanitize(version.infoFor()));
			existent.setInfoPdi(SanitizeUtil.sanitize(version.infoPdi()));
			existent.setInfoRisco(SanitizeUtil.sanitize(version.infoRisco()));
			this.bs.persist(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

}
