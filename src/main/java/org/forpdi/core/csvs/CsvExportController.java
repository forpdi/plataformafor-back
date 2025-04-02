package org.forpdi.core.csvs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.GoalsFilterHelper;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forrisco.risk.RiskFilterParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsvExportController extends AbstractController {
	@Autowired private CsvPlanExport csvPlanExport;
	@Autowired private CsvGoalExport csvGoalsExport;
	@Autowired private CsvRiskExport csvRiskExport;
	@Autowired private CsvLevelInstanceBudgetExport csvLevelInstanceBudgetExport;
	@Autowired private CsvActionPlanExport csvActionPlanExport;
	@Autowired private CsvBudgetExport csvBudgetExport;
	@Autowired private CompanyDomainContext domain;
	@Autowired private PlanBS planBS;
	@Autowired private BudgetBS budgetBS;
	@Autowired private StructureBS structureBS;
	@Autowired private GoalsFilterHelper goalsFilterHelper;
		
	@GetMapping("plan/export/{planMacroId}")
	public ResponseEntity<?> exportPlan(@PathVariable Long planMacroId) {
		try {

			LOGGER.info("Starting export plans {}'...", this.domain.get().getCompany().getName());
			this.response.setHeader("Content-Disposition", String.format("attachment; filename=plans-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			PlanMacro planMacro = this.planBS.retrievePlanMacroById(planMacroId);

			if(planMacro != null) {
				List<Plan> plans = this.planBS.listPlansForPlanMacro(planMacro);
				csvPlanExport.exportCSV(plans, this.response.getOutputStream());
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("plan/budget/export/{levelInstanceId}")
	public ResponseEntity<?> exportLevelInstanceBudget(@PathVariable Long levelInstanceId) {
		try {
			this.response.setHeader("Content-Disposition", String.format("attachment; filename=budget-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			StructureLevelInstance levelInstance = this.structureBS.retrieveLevelInstanceNoDeleted(levelInstanceId);

			if(levelInstance != null) {
				csvLevelInstanceBudgetExport.exportLevelInstaceBudgets(levelInstance, this.response.getOutputStream());
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("api/goal/exportCSV")
	public ResponseEntity<?> exportGoalCsv(
			@RequestParam Long parentId,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String attributesToFilter,
			@RequestParam(required = false) Integer progressStatusId) {

		PaginatedList<StructureLevelInstance> goals = this.goalsFilterHelper.retrieveFilteredGoals(	
			parentId, name, progressStatusId, attributesToFilter, 1, Integer.MAX_VALUE);
			
		this.response.setHeader("Content-Disposition", String.format("attachment; filename=goals-%d-%s.csv",
			domain.get().getCompany().getId(), LocalDateTime.now().toString()));
		this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		try {
			csvGoalsExport.exportGoalsCSV(goals.getList(), this.response.getOutputStream());
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}
	
	@GetMapping("api/field/actionplan/exportCSV")
	public ResponseEntity<?> exportActionPlanCsv(
			@RequestParam Long levelInstanceId,
			@RequestParam(required = false) String description,
			@RequestParam(required = false) Boolean checked,
			@RequestParam(required = false) Boolean notChecked,
			@RequestParam(required = false) Integer year) {

		ArrayList<ActionPlan> actionPlans = new ArrayList<>();
		try {

			StructureLevelInstance levelInstance = this.structureBS.retrieveLevelInstance(levelInstanceId);
			if (levelInstance == null) {
				this.fail("Estrutura incorreta!");
			} else {
				List<Attribute> attributeList = this.structureBS.retrieveLevelAttributes(levelInstance.getLevel());
				PaginatedList<Attribute> attributeListPagined = new PaginatedList<Attribute>();
				attributeListPagined.setList(attributeList);

				attributeListPagined = this.structureBS.setActionPlansAttributes(levelInstance, attributeListPagined, 1,
					Integer.MAX_VALUE, description, checked, notChecked, year);

				for (Attribute attribute : attributeListPagined.getList()) {
					if (attribute.getActionPlans() != null) {
						for (ActionPlan actionPlan : attribute.getActionPlans()) {
							actionPlans.add(actionPlan);
						}
					}
				}
				
				this.response.setHeader("Content-Disposition", String.format("attachment; filename=action-plans-%d-%s.csv",
					domain.get().getCompany().getId(), LocalDateTime.now().toString()));
				this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
				
				csvActionPlanExport.exportLevelInstaceActionPlanCSV(actionPlans, this.response.getOutputStream());
			}

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("budget/exportCSV")
	public ResponseEntity<?> exportBudgets() {
		try {
			Company company = this.domain.get().getCompany();
			LOGGER.info("Starting export budget '{}'...", company.getName());
			this.response.setHeader(
					"Content-Disposition",
					String.format("attachment; filename=budgets-%d-%s.csv",
							company.getId(), 
							LocalDateTime.now().toString()
					));

			this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

			List<BudgetElement> list = this.budgetBS.listBudgetElement(company).getList();

			csvBudgetExport.exportBudgetCSV(list, this.response.getOutputStream());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}
	
	/**
	 * Exportar os riscos de um plano específico em formato CSV.
	 * O arquivo CSV é gerado com base nos parâmetros de filtro fornecidos
	 * 
	 * @param planRiskId ID do plano de risco a ser exportado.
	 * @param filters    Parâmetros de filtros opcionais, no formato JSON.
	 * @return ResponseEntity representando o status da operação.
	 */
	@GetMapping("risk/exportCSV")
	public ResponseEntity<?> exportRiskCsv(
			@RequestParam Long planRiskId,
			@RequestParam(required = false) String filters) {

		this.response.setHeader("Content-Disposition", String.format("attachment; filename=riscos-%d-%s.csv",
			domain.get().getCompany().getId(), LocalDateTime.now().toString()));
		this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		LOGGER.info("Exportando arquivo CSV de riscos");
		try {
			RiskFilterParams filterParams = RiskFilterParams.fromJson(filters);
			csvRiskExport.exportRiskCsv(planRiskId, filterParams, this.response.getOutputStream());
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}

	@GetMapping("risk/linked-axes/exportCSV")
	public ResponseEntity<?> exportAxesRiskCSV(
			@RequestParam Long planId,
			@RequestParam Long selectedAxis,
			@RequestParam Integer selectedYear) {

		this.response.setHeader("Content-Disposition", String.format("attachment; filename=riscos-%d-%s.csv",
			domain.get().getCompany().getId(), LocalDateTime.now().toString()));
		this.response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		LOGGER.info("Exportando arquivo CSV de riscos vinculados a eixos");
		try {
			csvRiskExport.exportAxesRiskCSV(planId, selectedAxis, selectedYear, this.response.getOutputStream());
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return this.nothing();
	}
}
