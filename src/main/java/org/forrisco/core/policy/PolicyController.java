package org.forrisco.core.policy;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ItemToSelect;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.frisco.PolicyReportGenerator;
import org.forpdi.system.reports.frisco.PolicyReportGenerator.Params;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.Item;
import org.forrisco.core.item.ItemBS;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.dto.PolicyDto;
import org.forrisco.core.policy.dto.PolicyExportRequestDto;
import org.forrisco.core.policy.dto.PolicyValidityUpdateDto;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.DocumentException;


/**
 * @author Matheus Nascimento
 */
@RestController
public class PolicyController extends AbstractController {
	
	@Autowired private CompanyDomainContext domain;
	@Autowired private PolicyBS policyBS;
	@Autowired private ItemBS itemBS;
	@Autowired private RiskBS riskBS;
	@Autowired private PolicyReportGenerator policyReportGenerator;
	@Autowired private CompanyBS companyBS;
	@Autowired private PolicyHistoryBS policyHistoryBS;
	
	public static final String PATH =  BASEPATH +"/policy";
	
	
	/**
	 * Salvar Nova Política
	 * 
	 * @return ResponseEntity<?>
	 */
	@PostMapping( PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> savePolicy(@RequestBody PolicyDto dto){
		try {
			Policy policy = dto.policy();
			
			if(this.domain == null) {
				return this.fail("Instituição não definida");
			}
			
			this.policyBS.validateDates(policy);
			
			policy.setCompany(this.domain.get().getCompany());
			policy.setDescription(SanitizeUtil.sanitize(policy.getDescription()));
			policy.setId(null);
			this.policyBS.save(policy);
			this.riskBS.saveRiskLevel(policy);
			return this.success(policy);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}


	/**
	 * 
	 * Lista as políticas arquivadas
	 * 
	 * @param page
	 *            Número da página da listagem a ser acessada.
	 * @return PaginatedList<PlanMacro> Lista de planos macro arquivados da
	 *         companhia.
	 */
	@GetMapping( PATH + "/archivedpolicy")
	public ResponseEntity<?> listMacrosArchived(@ModelAttribute DefaultParams params) {
		try {
			if (this.domain != null) {
				PaginatedList<Policy> policy = this.policyBS.listPolicies(this.domain.get().getCompany(), true, params);
				return this.success(policy);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	
	@GetMapping( PATH + "/unarchivedpolicy")
	public ResponseEntity<?> listPolicyUnarchived(@ModelAttribute DefaultParams params) {
		try {
			if (this.domain != null) {
				PaginatedList<Policy> policies = this.policyBS.listPolicies(this.domain.get().getCompany(), false, params);
				return this.success(this.policyBS.listPoliciesHasPlans(policies));
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna política.
	 * 
	 * @param id
	 *            Id da política a ser retornado.
	 * @return Policy Retorna a política de acordo com o id passado.
	 */
	@GetMapping( PATH + "/{id}")
	public ResponseEntity<?> retrievePolicy(@PathVariable Long id, UserSession userSession) {
		try {
			Policy policy = this.policyBS.exists(id, Policy.class);
			if (policy == null) {
				return this.fail("A política solicitada não foi encontrada.");
			}
			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !policy.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar a Política ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, policy.getId(), policy.getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(policy.getCompany());

			policy.setHasLinkedPlans(this.policyBS.hasLinkedPlans(policy));
			return this.success(policy);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna politicas resumidas.
	 * 
	 * @return policies politicas resumidas.
	 * 
	 */
	@GetMapping(PATH + "/list-to-select")
	public ResponseEntity<?> listToSelect() {
		try {
			List<ItemToSelect> policies = this.policyBS.listPoliciesToSelect(this.domain.get().getCompany());
			ListWrapper<ItemToSelect> wrapper = new ListWrapper<>(policies);
			return this.success(wrapper);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/list-to-select-valid")
	public ResponseEntity<?> listToSelectStillValid() {
		try {
			List<ItemToSelect> policies = this.policyBS.listPoliciesToSelectStillValid(this.domain.get().getCompany());
			ListWrapper<ItemToSelect> wrapper = new ListWrapper<>(policies);
			return this.success(wrapper);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	
	/**
	 * Retorna graus de risco.
	 * 
	 * @param id
	 *            Id da política.
	 * @return RiskLevel Retorna os graus da política (id) passada.
	 * 
	 */
	@GetMapping(PATH + "/risklevel/{id}")
		@CommunityDashboard
		public ResponseEntity<?> retrieveRiskLevel(@PathVariable Long id) {
			try {
				Policy policy = this.riskBS.exists(id, Policy.class);
				if (policy == null) {
					return this.fail("A política solicitada não foi encontrado.");
				} else {
					domain.validateTenant(policy.getCompany());
					PaginatedList<RiskLevel> risklevels = this.riskBS.listRiskLevelByPolicy(policy);
					return this.success(risklevels);
				}
			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}
	

	/**
	 * Exclui política.
	 * 
	 * @param id
	 *            Id da política ser excluído.
	 *
	 */
	@DeleteMapping( PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deletePolicy(@PathVariable Long id, UserSession userSession) {
		try {
			Policy policy = this.policyBS.exists(id, Policy.class);
			if (GeneralUtils.isInvalid(policy)) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !policy.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar a Política ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, policy.getId(), policy.getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(policy.getCompany());

			PaginatedList<PlanRisk> plans = this.policyBS.listPlanbyPolicy(policy);
			PaginatedList<RiskLevel>  riskLevels= this.policyBS.listRiskLevelbyPolicy(policy);

			if(plans.getTotal() >0) {
				return this.fail("Impossível excluir política com Plano(s) de Risco vinculado(s)");
			}else {
				PaginatedList<Item> itens = this.itemBS.listItensByPolicy(policy);
				for(Item item : itens.getList()){
					PaginatedList<FieldItem> fields = this.itemBS.listFieldsByItem(item);
					for(FieldItem field : fields.getList()){
						this.itemBS.delete(field);
					}
					this.itemBS.deleteSubitens(item);
					this.itemBS.delete(item);
				}
				for(RiskLevel riskLevel : riskLevels.getList()){
					this.riskBS.delete(riskLevel);
				}
				this.policyBS.delete(policy);
				return this.success();
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Edita política.
	 * 
	 * @param policy
	 *            política a ser alterado com os novos campos.
	 */
	@PostMapping( PATH + "/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> policy(@RequestBody PolicyDto dto, UserSession userSession) {
		try {
			Policy policy = dto.policy();

			Policy existent = this.policyBS.exists(policy.getId(), Policy.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.fail("Política não encontrada");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !existent.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar a Política ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, existent.getId(), existent.getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(existent.getCompany());

			this.policyBS.validateDates(policy);

			PaginatedList<RiskLevel> existentLevels = this.policyBS.listRiskLevelbyPolicy(existent);

			if (this.policyBS.hasLinkedPlans(existent)) {
				return this.fail("A política possui planos de riscos, portanto não pode ser alterada.");
			}

			for (RiskLevel riskLevel : existentLevels.getList()) {
				this.riskBS.delete(riskLevel);
			}

			this.riskBS.saveRiskLevel(policy);

			existent.setName(SanitizeUtil.sanitize(policy.getName()));
			existent.setDescription(SanitizeUtil.sanitize(policy.getDescription()));
			existent.setValidityBegin(policy.getValidityBegin());
			existent.setValidityEnd(policy.getValidityEnd());
			existent.setImpact(policy.getImpact());
			existent.setProbability(policy.getProbability());
			existent.setMatrix(policy.getMatrix());
			existent.setNline(policy.getNline());
			existent.setNcolumn(policy.getNcolumn());
			existent.setPIDescriptions(policy.getPIDescriptions());
			this.policyBS.persist(existent);

			return this.success(existent);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
		
	@PostMapping(PATH + "/{id}/validity-end")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateValidityEnd(
			@PathVariable Long id,
			@RequestBody PolicyValidityUpdateDto dto,
			UserSession userSession) {
		try {
			if (dto == null) {
				return this.fail("Dados para atualização da vigência são obrigatórios.");
			}

			Policy policy = this.policyBS.exists(id, Policy.class);
			if (policy == null) {
				return this.fail("Política não encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !policy.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar a vigência da Política ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, policy.getId(), policy.getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(policy.getCompany());

			Policy updated = this.policyBS.updateValidityEnd(policy, dto.validityEnd(), userSession);
			return this.success(updated);
		} catch (IllegalArgumentException ex) {
			return this.fail(ex.getMessage());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/{id}/history")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> getPolicyHistory(
			@PathVariable Long id,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			Policy policy = this.policyBS.exists(id, Policy.class);
			if (policy == null) {
				return this.fail("A política solicitada não foi encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !policy.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar o histórico da Política ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, policy.getId(), policy.getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(policy.getCompany());

			PaginatedList<PolicyHistoryBS.PolicyHistoryDTO> history =
					this.policyHistoryBS.listHistoryByPolicy(id, params);

			return this.success(history);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	
	/**
	 * Cria arquivo pdf  para exportar relatório  
	 * 
	 * 
	 * @param title
	 * @param author
	 * @param item
	 * @param subitem
	 * @throws DocumentException 
	 * @throws IOException 
	 * 
	 */
	@PostMapping(PATH + "/exportReport")
	public ResponseEntity<?> exportreport(@RequestBody PolicyExportRequestDto request,
		UserSession userSession){

		InputStream inputStream = null;
		OutputStream outputStream = null;
		Long policyId = request.policyId();
		String title = request.title();
		String itens = request.itens();
		String subitens = request.subitens();
		try {
			Policy policy = this.policyBS.exists(policyId, Policy.class);
			if (policy == null) {
				return this.fail("A política solicitada não foi encontrada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !policy.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar relatório da Política ID %d da Empresa ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, policy.getId(), policy.getCompany().getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(policy.getCompany());

			String sanitizedTitle = SanitizeUtil.sanitize(title);
			String sanitizedItens = itens != null ? SanitizeUtil.sanitize(itens) : null;
			String sanitizedSubitens = subitens != null ? SanitizeUtil.sanitize(subitens) : null;

			Params params = new Params(policyId, sanitizedTitle, sanitizedItens, sanitizedSubitens);
			inputStream = this.policyReportGenerator.generateReport(params);

			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"" + title + ".pdf\"");
			outputStream = this.response.getOutputStream();

			IOUtils.copy(inputStream, outputStream);
			return this.nothing();
		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			return this.fail(ex.getMessage());
		} finally {
			Util.closeFile(inputStream);
			Util.closeFile(outputStream);
		}
	}

}
