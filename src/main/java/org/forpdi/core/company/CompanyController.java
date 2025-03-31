package org.forpdi.core.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.dto.CompanyDomainDto;
import org.forpdi.core.company.dto.CompanyDto;
import org.forpdi.core.company.dto.MessageOverlayDto;
import org.forpdi.core.company.dto.UpdateLogoDto;
import org.forpdi.core.properties.CoreMessages;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.JsonUtil;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Renato R. R. de Oliveira
 */
@RestController
public class CompanyController extends AbstractController {
	@Autowired
	private CompanyBS bs;
	@Autowired
	private UserBS userBS;
	@Autowired
	private UserSession session;
	@Autowired
	private CompanyDomainContext companyDomain;
	@Autowired
	private ArchiveBS archiveBS;
	@Autowired
	private StorageService storageService;

	/**
	 * Salva uma nova companhia no banco de dados.
	 * 
	 * @param company,
	 *            nova companhia a ser persistida no banco de dados.
	 * @return company, a nova companhia que acabou de ser persistida no banco
	 *         de dados.
	 */
	@PostMapping("/api/company")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> saveCompany(@RequestBody CompanyDto dto) {
		try {
			Company company = dto.company();
			company.setId(null);
			this.bs.save(company);
			return this.success(company);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualiza uma companhia no banco de dados.
	 * 
	 * @param company, companhia a ser atualizada no banco de dados.
	 * @return existent, companhia que acabou de ser atualizada no banco de
	 *         dados.
	 */
	@PutMapping("/api/company")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> updateCompany(@RequestBody CompanyDto dto) {
		try {
			Company company = dto.company();
			Company existent = this.bs.exists(company.getId(), Company.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			existent.setDescription(company.getDescription());
			existent.setLogo(company.getLogo());
			existent.setName(company.getName());
			existent.setType(company.getType());
			existent.setShowDashboard(company.isShowDashboard());
			existent.setShowMaturity(company.isShowMaturity());
			existent.setShowBudgetElement(company.isShowBudgetElement());
			existent.setEnableForrisco(company.isEnableForrisco());
			existent.setInitials(company.getInitials());
			existent.setCounty(company.getCounty());
			this.bs.persist(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna uma companhia de acordo com seu id.
	 * 
	 * @param id da companhia desejada.
	 * @return company, companhia desejada que possui o dado id.
	 */
	@GetMapping("/api/company/{id}")
	@PreAuthorize(AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> retrieveCompany(@PathVariable Long id) {
		try {
			Company company = this.bs.retrieveCompanyById(id);
			if (company == null) {
				return this.fail("A intituição solicitada não foi encontrada.");
			} else {
				boolean hasPermission = session.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel()
						|| companyDomain.get().getCompany().getId().equals(company.getId());
				if (!hasPermission) {
					this.forbidden();
				}
				return this.success(company);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna uma companhia de acordo com seu id.
	 * 
	 * @param id da companhia desejada.
	 * @return company, companhia desejada que possui o dado id.
	 */
	@PutMapping("/api/company/update-logo")
	@PreAuthorize(AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateLogo(@RequestBody UpdateLogoDto dto) {
		try {
			Company existent = this.bs.retrieveCompanyById(dto.companyId());
			if (existent == null) {
				return this.fail("A intituição solicitada não foi encontrada.");
			} else {
				boolean hasPermission = session.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel()
						|| companyDomain.get().getCompany().getId().equals(existent.getId());
				if (!hasPermission) {
					return this.forbidden();
				} else {
					Archive archive = null;
					if (dto.fileLink() != null) {
						archive = archiveBS.getByFileLink(dto.fileLink());
						if (archive == null) {
							return this.fail("O upload da logo falhou! Tente novamente por favor.");
						}
					}
					existent.setLogoArchive(archive);
					this.bs.save(existent);
					return this.success(existent);
				}
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Lista as companhias limitados a uma dada página.
	 * @param page, página desejada da listagem de companhias.
	 * @return companies, lista de companhias da dada página.
	 */
	@GetMapping("/api/company")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> listCompanies(@ModelAttribute DefaultParams params) {
		try {
			PaginatedList<Company> companies = this.bs.search(params);
			return this.success(companies);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Deleta uma companhia específica, conforme um dado id.
	 * @param id da companhia a ser deletada.
	 * @return ResponseEntity<?>
	 */
	@DeleteMapping("/api/company/{id}")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> delete(@PathVariable Long id) {
		try {
			Company existent = this.bs.exists(id, Company.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			
			List<CompanyDomain> companyDomain = this.bs.retrieveCompanyByDomain(existent);
			
			if (companyDomain.size() == 0) {
				this.userBS.deleteAllCompanyUsers(existent);
				existent.setDeleted(true);
				this.bs.persist(existent);
				return this.success(existent);
			} else {
				return this.fail("Esta instituição não pode ser deletada, pois existe um domínio associado á ela.");
			}
			
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar um novo domínio no banco de dados.
	 * @param domain, novo domínio a ser salvo.
	 * @return domain, novo domínio que acabou de ser salvo.
	 */
	@PostMapping("/api/companydomain")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> saveDomain(@RequestBody CompanyDomainDto dto) {
		try {
			CompanyDomain domain = dto.domain();

			domain.setId(null);
			if (domain.getCompany() == null) {
				return this.fail("Companhia não informada");
			}
			Company company = this.bs.retrieveCompanyById(domain.getCompany().getId());
			if (company == null) {
				return this.fail("Companhia não encontrada");
			}
			
			if (this.bs.alreadyExistsURL(domain)) {
				return this.fail("A URL informada já está cadastrada!");
			}
			
			CompanyDomain existent = this.bs.retrieveByHost(domain.getHost());
			if (existent == null) {
				this.bs.persist(domain);
				return this.success(domain);
			} else {
				return this.fail("Já existe um domínio com este HOST.");
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Atualizar um domínio no banco de dados.
	 * @param domain, domínio a atualizado.
	 * @return existent, domínio atualizado.
	 */
	@PutMapping("/api/companydomain")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> updateDomain(@RequestBody CompanyDomainDto dto) {
		try {
			CompanyDomain domain = dto.domain();
			CompanyDomain existent = this.bs.exists(domain.getId(), CompanyDomain.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			
			CompanyDomain cd = this.bs.retrieveByHost(domain.getHost());
			if (cd !=null && !cd.getHost().equals(existent.getHost())) {
				return this.fail("Já existe um domínio com este host registrado");
			}
		
			if (this.bs.alreadyExistsURL(domain)) {
				return this.fail("A URL informada já está cadastrada!");
			}
			
			existent.setHost(domain.getHost());
			existent.setBaseUrl(domain.getBaseUrl());
			existent.setTheme(domain.getTheme());
			if (!GeneralUtils.isInvalid(domain.getCompany())
					&& !existent.getCompany().getId().equals(domain.getCompany().getId())) {
				existent.setCompany(domain.getCompany());
			}
			this.bs.persist(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna um domínio do banco de dados, conforme um dado id.
	 * @param id, referente ao domínio desejado.
	 * @return domain, domínio desejado.
	 */
	@GetMapping("/api/companydomain/{id}")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> retrieveCompanyDomain(@PathVariable Long id) {
		try {
			CompanyDomain domain = this.bs.exists(id, CompanyDomain.class);
			if (domain == null) {
				return this.fail("O domínio solicitado não foi encontrado.");
			} else {
				return this.success(domain);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Lista os domínios, limitados a uma dada página.
	 * @param page, página desejada dos domínios.
	 * @param term, termo usado para pesquisa de dominios.
	 * @return domains, lista dos domínios da dada página.
	 */
	@GetMapping("/api/companydomain")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> listCompanyDomains(@ModelAttribute DefaultParams params) {
		try {
			PaginatedList<CompanyDomain> domains = this.bs.searchDomain(params);
			return this.success(domains);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Excluir um domínio, especificado pelo id.
	 * @param id, referente ao domínio a ser deletado.
	 * @return ResponseEntity<?>.
	 */
	@DeleteMapping("/api/companydomain/{id}")
	@PreAuthorize(AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> deleteDomain(@PathVariable Long id) {
		try {
			CompanyDomain existent = this.bs.exists(id, CompanyDomain.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			this.bs.remove(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	@PostMapping("/api/company/messages")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateMessageOverlay(@RequestBody MessageOverlayDto dto) {
		try {
			CompanyDomain domain = this.bs.currentDomain();
			if (domain == null) {
				return this.notFound();
			} else {
				this.bs.updateMessageOverlay(domain.getCompany(), dto.key(), dto.value());
				return this.success(true);
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	@GetMapping("/api/company/messages")
	public ResponseEntity<?> getMessages() {
		CompanyDomain domain = this.bs.currentDomain();
		CoreMessages msg = new CoreMessages(CoreMessages.DEFAULT_LOCALE);
		if (domain != null) {
			Map<String, String> messagesOverlays = this.bs.retrieveMessagesOverlay(domain.getCompany());
			msg.setOverlay(messagesOverlays);
		}
		try {
			this.response.setCharacterEncoding("UTF-8");
			this.response.addHeader("Content-Type", "application/json"); 
			this.response.getWriter().print(msg.getJSONMessages());
		} catch (IOException ex) {
			LOGGER.error("Unexpected runtime error", ex);
		}
		
		return this.nothing();
	}
	
	@GetMapping("/api/company/logo")
	public ResponseEntity<?> getLogo() {
		try {
			Archive archive = companyDomain.get().getCompany().getLogoArchive();
			if (archive == null) {
				return this.nothing();
			}

			try (InputStream in = storageService.retrieveFile(archive.getFileLink());
					OutputStream out = this.response.getOutputStream()) {

				this.response.reset();
				this.response.setHeader("Content-Type", "application/" + archive.getExtension());
				this.response.setHeader("Content-Disposition", "inline; filename=\"" + archive.getName() + "\"");
				IOUtils.copy(in, out);
			}

			return this.nothing();

		} catch (IOException e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}
}
