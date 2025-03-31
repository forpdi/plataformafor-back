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
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
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
	
	public static final String PATH = BASEPATH +"/planrisk";
	
	/**
	 * Salvar Novo Plano
	 * 
	 * @return ResponseEntity<?>
	 */
	
	@PostMapping( PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> savePlan(@RequestBody PlanRisk planRisk) {
		try {
			Policy policy = this.planRiskBS.exists(planRisk.getPolicy().getId(), Policy.class);
			
			if (policy == null || policy.isDeleted()) {
				return this.fail("O plano de risco solicitada não foi encontrado.");
			}
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
			//planRisk.setName(planRisk.getName());
			//planRisk.setDescription(planRisk.getDescription());
			
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
	public ResponseEntity<?> editPlanRisk(@RequestBody PlanRisk planRisk) {
		try {
			PlanRisk existent = this.planRiskBS.exists(planRisk.getId(), PlanRisk.class);

			if (GeneralUtils.isInvalid(existent)) {
				return this.fail("Plano de risco não encontrado");
			}

			if (existent.getPolicy() == null) {
				return this.fail("Plano sem política associada");
				}

			if ((planRisk.getValidityBegin() == null && planRisk.getValidityEnd() != null) ||
					(planRisk.getValidityEnd() == null && planRisk.getValidityBegin() != null)) {
				return this.fail("Não é permitido preencher somente uma das datas do prazo de vigência");
			}
			if (planRisk.getValidityBegin() != null && planRisk.getValidityEnd() != null
					&& planRisk.getValidityEnd().before(planRisk.getValidityBegin())) {
				return this.fail("A data de início do prazo de vigência não deve ser superior à data de término");
			}

			existent.setName(SanitizeUtil.sanitize(planRisk.getName()));
			existent.setDescription(SanitizeUtil.sanitize(planRisk.getDescription()));
			existent.setValidityBegin(planRisk.getValidityBegin());
			existent.setValidityEnd(planRisk.getValidityEnd());

			this.planRiskBS.persist(existent);
			return this.success(existent);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	@DeleteMapping(PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deletePlanRisk(@PathVariable Long id) {
		try {
			PlanRisk planRisk = this.planRiskBS.exists(id, PlanRisk.class);
			
			if (GeneralUtils.isInvalid(planRisk)) {
				return this.fail("Plano de risco inválido.");
			}
			
			
			//verificar unidades
			PaginatedList<Unit> units= this.unitBS.listUnitsbyPlanRisk(planRisk);
			
			for(Unit unit:units.getList()) {
				if(!this.unitBS.deletableUnit(unit)) {	
					return this.fail("O plano possui unidades que não podem ser deletadas." );
				}
			}
					
			//deletar unidades
			for(Unit unit:units.getList()) {
				//deletar processos desta unidade
				PaginatedList<Process> processes = this.processBS.listProcessByUnit(unit);
				for(Process process :processes.getList()) {
					this.processBS.deleteProcess(process);
				}
				this.unitBS.delete(unit);
			}
			
			
			
			//deletar itens
			PaginatedList<PlanRiskItem> planRiskItem = this.planRiskItemBS.listItensByPlanRisk(planRisk);
			
			for(PlanRiskItem item : planRiskItem.getList()) {
				
				PaginatedList<PlanRiskItemField> fields = this.planRiskItemBS.listFieldsByPlanRiskItem(item);
				
				for(PlanRiskItemField field : fields.getList()) {
					this.planRiskItemBS.delete(field);
				}
				
				this.planRiskItemBS.deleteSubItens(item);
				this.planRiskItemBS.delete(item);
			}
			
			//deletar plano
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
			@RequestParam(required = false) String subitens) throws IOException, DocumentException{
		
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
		
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
			@RequestParam String selecao) {

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
		
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
}
