package org.forrisco.core.plan;

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
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.frisco.BoardReportGenerator;
import org.forpdi.system.reports.frisco.PlanRiskReportGenerator;
import org.forpdi.system.reports.frisco.PlanRiskReportGenerator.Params;
import org.forrisco.core.item.PlanRiskItem;
import org.forrisco.core.item.PlanRiskItemBS;
import org.forrisco.core.item.PlanRiskItemField;
import org.forrisco.core.policy.Policy;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.DocumentException;



/**
 * @author Juliano Afonso
 * 
 */
@RestController
public class PlanRiskController extends AbstractController {
	
	@Autowired private CompanyDomainContext domain;
	@Autowired private PlanRiskBS planRiskBS;
	@Autowired private PlanRiskItemBS planRiskItemBS;
	@Autowired private UnitBS unitBS;
	@Autowired private ProcessBS processBS;
	@Autowired private PlanRiskReportGenerator planRiskReportGenerator;
	@Autowired private BoardReportGenerator boardReportGenerator;
	@Autowired private CompanyBS companyBS;
	@Autowired
	private UserBS userBS;

	@Autowired private PlanRiskHistoryBS planRiskHistoryBS;
	
	public static final String PATH = BASEPATH +"/planrisk";
	
	/**
	 * Salvar Novo Plano
	 * 
	 * @return ResponseEntity<?>
	 */

	@PostMapping( PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> savePlan(@RequestBody PlanRisk planRisk, UserSession userSession) {
		try {
			PlanRisk planRisk1 = planRiskBS.retrieveByName(planRisk.getName(), null);
			if (planRisk1 != null) {
				return this.fail("Já existe um plano de gestão com este nome.");
			}
			if (planRisk.getPolicy() == null || planRisk.getPolicy().getId() == null) {
				return this.fail("A política associada ao plano de risco é inválida.");
			}
			Policy policy = this.planRiskBS.exists(planRisk.getPolicy().getId(), Policy.class);
			if (policy == null || policy.isDeleted()) {
				return this.fail("A política informada é inválida.");
			}
			if (planRisk.getValidityEnd().after(policy.getValidityEnd()) || planRisk.getValidityBegin().before(policy.getValidityBegin())) {
				return this.fail("Período fora do intervalo de vigência da política de risco vinculada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !policy.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou criar Plano de Risco na Política ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, policy.getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(policy.getCompany());

			if ((planRisk.getValidityBegin() == null && planRisk.getValidityEnd() != null) ||
				(planRisk.getValidityEnd() == null && planRisk.getValidityBegin() != null)) {
				return this.fail("Não é permitido preencher somente uma das datas do prazo de vigência");
			}
			if (planRisk.getValidityBegin() != null && planRisk.getValidityEnd() != null
				&& planRisk.getValidityEnd().before(planRisk.getValidityBegin())) {
				return this.fail("A data de início do prazo de vigência não deve ser superior à data de término");
			}

			planRisk.setId(null);
			planRisk.setPolicy(policy);
			planRiskBS.save(planRisk);

			return this.success(planRisk);

		} catch (Throwable e) {
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Retorna o lista de todos os planos não arquivados.
	 * 
	 * @param page
	 */
	@GetMapping( PATH + "/unarchivedplanrisk")
	public ResponseEntity<?> listPlanRiskUnarchived(@ModelAttribute DefaultParams params) {
		try {
			if (this.domain != null) {
			PaginatedList<PlanRisk> planRisks = this.planRiskBS.listPlanRisk(this.domain.get().getCompany(), params);
			return this.success(planRisks);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna uma lista resumida dos planos da instituição para seleção.
	 * 
	 * @param page
	 */
	@GetMapping( PATH + "/list-to-select")
		@CommunityDashboard
		public ResponseEntity<?> listToSelect(@RequestParam(required = false) Long policyId) {
			try {
				List<ItemToSelect> planRisks = this.planRiskBS.listPlanRisksToSelect(this.domain.get().getCompany(), policyId);
				ListWrapper<ItemToSelect> wrapper = new ListWrapper<>(planRisks);
				return this.success(wrapper);
			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}

	/**
	 * Retorna o plano de risco.
	 * 
	 * @param id
	 * Id do plano de risco.
	 * 
	 * @return PlanRisk Retorna o plano de risco de acordo com o id passado.
	 */
	@GetMapping( PATH + "/{id}")
		@CommunityDashboard
		public ResponseEntity<?> retrievePlan(@PathVariable Long id) {
			try {
				PlanRisk planRisk = this.planRiskBS.exists(id, PlanRisk.class);
				if (planRisk == null) {
					return this.fail("O plano de risco solicitada não foi encontrado.");
				} else {
					domain.validateTenant(planRisk.getPolicy().getCompany());
					return this.success(planRisk);
				}
			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}

	/**
	 * Retorna o plano atualizado.
	 * 
	 * @param PlanRisk planRisk plano de risco a ser atualizado.
	 * 
	 * @return PlanRisk Retorno o plano de risco atualizado.
	 * */
	@PostMapping(PATH + "/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> editPlanRisk(@RequestBody PlanRisk planRisk, UserSession userSession) {
		try {

			PlanRisk existent = this.planRiskBS.exists(planRisk.getId(), PlanRisk.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.fail("Plano de risco não encontrado");
			}
			if (existent.getPolicy() == null) {
				return this.fail("Plano sem política associada");
			}
			PlanRisk planRisk1 = planRiskBS.retrieveByName(planRisk.getName(), existent.getId());
			if (planRisk1 != null) {
				return this.fail("Já existe um plano de gestão com este nome.");
			}
			if (planRisk.getValidityEnd().after(existent.getPolicy().getValidityEnd()) || planRisk.getValidityBegin().before(existent.getPolicy().getValidityBegin())) {
				return this.fail("Período fora do intervalo de vigência da política de risco vinculada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !existent.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar o Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, existent.getId()
					));
					return this.fail("Acesso negado.");
				}
			}
			

			domain.validateTenant(existent.getPolicy().getCompany());

			if ((planRisk.getValidityBegin() == null && planRisk.getValidityEnd() != null) ||
				(planRisk.getValidityEnd() == null && planRisk.getValidityBegin() != null)) {
				return this.fail("Não é permitido preencher somente uma das datas do prazo de vigência");
			}
			if (planRisk.getValidityBegin() != null && planRisk.getValidityEnd() != null
				&& planRisk.getValidityEnd().before(planRisk.getValidityBegin())) {
				return this.fail("A data de início do prazo de vigência não deve ser superior à data de término");
			}

			this.planRiskBS.persist(existent);
			PlanRisk updated = this.planRiskBS.updatePlanRisk(existent, planRisk, userSession);
			return this.success(updated);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@DeleteMapping(PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deletePlanRisk(@PathVariable Long id, UserSession userSession) {
		try {
			PlanRisk planRisk = this.planRiskBS.exists(id, PlanRisk.class);
			if (GeneralUtils.isInvalid(planRisk)) {
				return this.fail("Plano de risco inválido.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar o Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, id
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());

			PaginatedList<Unit> units= this.unitBS.listUnitsbyPlanRisk(planRisk);
			for(Unit unit:units.getList()) {
				if(!this.unitBS.deletableUnit(unit)) {
					return this.fail("O plano possui unidades que não podem ser deletadas." );
				}
			}

			for(Unit unit:units.getList()) {
				PaginatedList<Process> processes = this.processBS.listProcessByUnit(unit);
				for(Process process :processes.getList()) {
					this.processBS.deleteProcess(process);
				}
				this.unitBS.delete(unit);
			}

			PaginatedList<PlanRiskItem> planRiskItem = this.planRiskItemBS.listItensByPlanRisk(planRisk);
			for(PlanRiskItem item : planRiskItem.getList()) {
				PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(item);
				for(PlanRiskItemField field : fields.getList()) {
					this.planRiskItemBS.delete(field);
				}
				this.planRiskItemBS.deleteSubItens(item);
				this.planRiskItemBS.delete(item);
			}

			this.planRiskBS.delete(planRisk);
			return this.success();

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	
	/**
	 * Cria arquivo pdf  para exportar relatório  
	 * 
	 * 
	 * @param title
	 * @param author
	 * @param pre
	 * @param item
	 * @param subitem
	 * @throws DocumentException 
	 * @throws IOException 
	 * 
	 */
	@GetMapping(PATH + "/exportReport")
	public ResponseEntity<?> exportreport(
		@RequestParam String title,
		@RequestParam Long planId,
		@RequestParam(required = false) String itens,
		@RequestParam(required = false) String subitens,
		UserSession userSession) throws IOException, DocumentException{

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			PlanRisk planRisk = this.planRiskBS.exists(planId, PlanRisk.class);
			if (planRisk == null) {
				return this.fail("O Plano de Risco não foi encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar relatório do Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, planId
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());

			String sanitizedTitle = SanitizeUtil.sanitize(title);
			String sanitizedItens = itens != null ? SanitizeUtil.sanitize(itens) : null;
			String sanitizedSubitens = subitens != null ? SanitizeUtil.sanitize(subitens) : null;

			Params params = new Params(sanitizedTitle, planId, sanitizedItens, sanitizedSubitens);
			inputStream = this.planRiskReportGenerator.generateReport(params);

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
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				LOGGER.error("Error while exporting report.", e);
			}
		}
	}
	
	/**
	 * Cria arquivo pdf  para exportar relatório  
	 * 
	 * 
	 * @param title
	 * @param author
	 * @param pre
	 * @param item
	 * @param subitem
	 * @throws DocumentException 
	 * @throws IOException 
	 * 
	 */
	@GetMapping(PATH + "/exportBoardReport")
	public ResponseEntity<?> exportBoardReport(
		@RequestParam String title,
		@RequestParam Long planId,
		@RequestParam String selecao,
		UserSession userSession) {

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			PlanRisk planRisk = this.planRiskBS.exists(planId, PlanRisk.class);
			if (planRisk == null) {
				return this.fail("O Plano de Risco não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar painel do Plano de Risco ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, planId
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());

			BoardReportGenerator.Params params = new BoardReportGenerator.Params(title, planId, selecao);
			inputStream = this.boardReportGenerator.generateReport(params);

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
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				LOGGER.error("Error while exporting report.", e);
			}
		}
	}
	@GetMapping(PATH + "/{id}/history")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> getPlanHistory(
			@PathVariable("id") Long id,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			PlanRisk plan = this.planRiskBS.exists(id, PlanRisk.class);
			if (plan == null) {
				return this.fail("O plano de risco solicitado não foi encontrado.");
			}

			// BOLA/IDOR – valida empresa do usuário vs empresa da política do plano
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar o histórico do Plano ID %d.",
						userSession.getAuthData().getName(), loggedInUserId, plan.getId()
					));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			PaginatedList<PlanRiskHistoryBS.PlanRiskHistoryDTO> hist =
					this.planRiskHistoryBS.listHistoryByPlan(id, params);

			return this.success(hist);
		} catch (Throwable ex) {
			LOGGER.error("Erro inesperado ao buscar histórico do plano: " + id, ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
}
