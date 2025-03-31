package org.forpdi.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.dashboard.admin.GeneralBudgets;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.dashboard.admin.PlanDetails;
import org.forpdi.dashboard.goalsinfo.GoalsInfoBS;
import org.forpdi.dashboard.goalsinfo.GoalsInfoOverview;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.dashboard.manager.IndicatorHistory;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.PolarityMapHelper;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.filters.PeformanceFilterType;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.Polarity;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureHelper;
import org.forpdi.planning.structure.StructureInstanceAttributeHelper;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController extends AbstractController {

	@Autowired
	private StructureBS sbs;
	@Autowired
	private PlanBS planBS;
	@Autowired
	private DashboardBS bs;
	@Autowired
	private AttributeHelper attrHelper;
	@Autowired
	private StructureHelper structHelper;
	@Autowired
	private GoalsInfoBS goalsInfoBS;
	@Autowired
	private PolarityMapHelper polarityMapHelper;
	@Autowired
	private StructureInstanceAttributeHelper structureInstanceAttributeHelper;
	@Autowired
	private CompanyDomainContext domain;
	
	/**
	 * Usuário recupera informações sobre os objetivos
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @return info informações sobre os objetivos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/goals")
	public ResponseEntity<?> adminGoalsInfo(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan) {

		try {
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan plano = this.planBS.retrieveById(plan);
			PaginatedList<StructureLevelInstance> list = this.sbs.listGoalsByResponsible(planMacro, plano);
			return this.success(goalsInfoBS.calculateAdminGoalsInfo(list.getList()));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera eixo estratégico
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @return plan2 Plano contendo a lista de eixos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/performanceStrategicAxis")
	public ResponseEntity<?> performanceStrategicAxis(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {

		try {
			PaginatedList<StructureLevelInstance> list;

			if (plan == null && macro == null) {
				list = this.sbs.listStrategicAxis(null, null, page, pageSize);
			} else if (plan == null) {
				PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
				list = this.sbs.listStrategicAxis(planMacro, null, page, pageSize);
			} else {
				list = this.sbs.listStrategicAxis(null, this.planBS.retrieveById(plan), page, pageSize);
			}
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera objetivos a partir de eixo temático
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param thematicAxis
	 *            ID do eixo temático a ter os dados recuperados
	 * @return plan2 Plano contendo a lista de eixos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/objectivesByThematicAxis")
	public ResponseEntity<?> objectivesByThematicAxis(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long thematicAxis) {

		try {
			PaginatedList<StructureLevelInstance> list;
			Plan plan2 = new Plan();
			StructureLevelInstance thematicAxisLevel = this.sbs.retrieveLevelInstance(thematicAxis);

			if (plan == null && macro == null) {
				list = this.sbs.listObjectivesByThematicAxis(null, null, thematicAxisLevel);
			} else if (plan == null) {
				PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
				list = this.sbs.listObjectivesByThematicAxis(planMacro, null, thematicAxisLevel);
			} else {
				list = this.sbs.listObjectivesByThematicAxis(null, this.planBS.retrieveById(plan), thematicAxisLevel);
			}
			plan2.setLevelInstances(list.getList());
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera detalhes, como número de objetivos e metas, de um plano
	 * de metas
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @return planDetails detalhes recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/planDetails")
	public ResponseEntity<?> adminPlanDetails(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan) {

		try {
			PlanDetails planDetails = new PlanDetails();
			PaginatedList<StructureLevelInstance> goalList;
			PlanMacro planMacro = null;
			Plan plan2 = null;
			long numOfIndicators;
			if (plan == null && macro == null) {
				numOfIndicators = sbs.countAllIndicators();
			} else if (plan == null) {
				planMacro = this.planBS.retrievePlanMacroById(macro);
				numOfIndicators = sbs.countIndicators(planMacro);
			} else {
				plan2 = this.planBS.retrieveById(plan);
				numOfIndicators = sbs.countIndicators(plan2);
			}

			goalList = this.sbs.listGoalsByResponsible(this.planBS.retrievePlanMacroById(macro), this.planBS.retrieveById(plan));
			if (goalList.getTotal() > 0) {
				Double goalsLatePercent = ((double) goalsInfoBS.getNumberOfGoalsLate(goalList.getList()) * 100)
						/ goalList.getTotal();
				planDetails.setGoalsDelayedPerCent(goalsLatePercent);
			} else {
				planDetails.setGoalsDelayedPerCent(0.0);
			}
			planDetails.setNumberOfBudgets(sbs.countBudgets(planMacro, plan2));
			planDetails.setNumberOfObjectives(sbs.countObjective(planMacro, plan2));
			planDetails.setNumberOfIndicators(numOfIndicators);
			planDetails.setNumberOfGoals(goalList.getTotal().intValue());
			return this.success(planDetails);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());

		}
	}


	/**
	 * Usuário recupera lista de orçamentos
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param objective
	 *            ID do objetivo a ter os dados recuperados
	 * @param subAction
	 *            ação orçamentária desejada
	 * @return budgets lista de orçamentos recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/budget")
	public ResponseEntity<?> listBudgets(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long objective,
			@RequestParam(required = false) String subAction) {
		
		try {
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan plan2 = this.planBS.retrieveById(plan);
			StructureLevelInstance obj = this.sbs.retrieveLevelInstance(objective);
			List<Budget> list = this.bs.listBudgets(planMacro, plan2, obj, subAction);
			GeneralBudgets budgets = new GeneralBudgets();
			Double realized = 0.0;
			Double committed = 0.0;
			Double planned = 0.0;
			boolean isId = false;

			List<Long> listIdBudgetsElement = new ArrayList<Long>();
			if (list != null && !list.isEmpty()) {
				listIdBudgetsElement.add(list.get(0).getBudgetElement().getId());
				planned += list.get(0).getBudgetElement().getBudgetLoa();
			}

			for (Budget budget : list) {
				if (budget.getCommitted() != null) {
					committed += budget.getCommitted();
				}

				if (budget.getRealized() != null) {
					realized += budget.getRealized();
				}

				for (Long id : listIdBudgetsElement) {
					if (!id.equals(budget.getBudgetElement().getId())) {
						isId = true;
					}
				}

				if (isId) {
					listIdBudgetsElement.add(budget.getBudgetElement().getId());
					planned += budget.getBudgetElement().getBudgetLoa();
				}

				isId = false;
			}
			budgets.setCommitted(committed);
			budgets.setConducted(realized);
			budgets.setPlanned(planned);
			return this.success(budgets);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());

		}
	}

	/**
	 * Usuário recupera lista de indicadores e suas informações
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param objective
	 *            ID do objetivo a ter os dados recuperados
	 * @return indicatorsList Lista de indicadores recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/indicatorsInformation")
	public ResponseEntity<?> indicatorsInformation(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long objective,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {
		try {
			PaginatedList<StructureLevelInstance> indicatorsList;
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan plan2 = this.planBS.retrieveById(plan);
			StructureLevelInstance objectiveLevel = this.sbs.retrieveLevelInstance(objective);
			indicatorsList = this.sbs.listIndicators(planMacro, plan2, objectiveLevel, page, pageSize);

			for (int i = 0; i < indicatorsList.getList().size(); i++) {
				this.structHelper.fillIndicators(indicatorsList.getList().get(i));
			}

			return this.success(indicatorsList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera lista de informações sobre metas, como responsável e
	 * status
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param indicator
	 *            ID do indicador a ter os dados recuperados
	 * @return goalsList Lista com as informações recuperadas
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/manager/goalsInfoTable")
	public ResponseEntity<?> goalsInfoTable(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) Integer filter) {

		try {
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan planInstance = this.planBS.retrieveById(plan);
			StructureLevelInstance indicatorLevel = this.sbs.retrieveLevelInstance(indicator);
			PeformanceFilterType peformanceFilterType = filter != null
				? PeformanceFilterType.valueOf(filter)
				: null;
			PaginatedList<GoalsInfoTable> goalsList = this.bs.getGoalsInfoTable(planMacro, planInstance, indicatorLevel, page,
					pageSize, peformanceFilterType);

			return this.success(goalsList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera lista de objetivos e suas informações, como nome e data
	 * de criação
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @return objectivesList Lista com as informações e objetivos recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/objectivesInformation")
	public ResponseEntity<?> objectivesInformation(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan) {
		
		try {
			PaginatedList<StructureLevelInstance> objectivesList;

			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan plan2 = this.planBS.retrieveById(plan);
			objectivesList = this.sbs.listObjectives(planMacro, plan2);
			return this.success(objectivesList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera lista de metas e informações sobre metas, como nome e
	 * data de criação
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param indicator
	 *            ID do indicador a ter os dados recuperados
	 * @return goalsList Lista com as informações e metas recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/colaborator/goalsInformation")
	public ResponseEntity<?> goalsInformation(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {
		
		try {
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan plan2 = this.planBS.retrieveById(plan);
			StructureLevelInstance indicatorLevel = this.sbs.retrieveLevelInstance(indicator);
			PaginatedList<StructureLevelInstance> goals = this.sbs.listGoals(planMacro, plan2, indicatorLevel, page, pageSize);
			// recupera todas as AttributeInstance relacionadas as StructureLevelInstance
			List<AttributeInstance> attrInstances = structureInstanceAttributeHelper.listAllAttributeInstanceByLevelInstances(goals.getList());
			Map<Long, List<AttributeInstance>> goalAttrInstanceMap = new HashMap<>();
			for (AttributeInstance attrInstance : attrInstances) {
				List<AttributeInstance> attrInstanceList = goalAttrInstanceMap.get(attrInstance.getLevelInstance().getId());
				if (attrInstanceList == null) {
					attrInstanceList = new LinkedList<>();
					goalAttrInstanceMap.put(attrInstance.getLevelInstance().getId(), attrInstanceList);
				}
				attrInstanceList.add(attrInstance);
			}
			// cria um map para acessar a polaridade atraves do id do goal (meta)
			Map<Long, AttributeInstance> polarityMap = polarityMapHelper.generatePolarityMap(goals.getList());
			// seta os atributos das metas
			structureInstanceAttributeHelper.setAttributes(goals.getList());
			for (StructureLevelInstance goal : goals.getList()) {
				List<AttributeInstance> goalAttrInstances = goal.getAttributeInstanceList();
				AttributeInstance polarity = polarityMap.get(goal.getId());
				if (polarity == null) {
					goal.setPolarity(Polarity.BIGGER_BETTER.getValue());
				} else {
					goal.setPolarity(polarity.getValue());
				}
				List<Attribute> attributes = new ArrayList<>(goalAttrInstances.size());
				for (AttributeInstance attrInstance : goalAttrInstances) {
					attributes.add(attrInstance.getAttribute());
				}
				goal.setAttributeList(attributes);
			}
			return this.success(goals);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Recuperação de uma lista com o histórico de um indicador
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param indicator
	 *            ID do indicador a ter os dados recuperados
	 * @return list lista contendo o histórico do indicador requerido
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/manager/indicatorsHistory")
	public ResponseEntity<?> listIndicatorsHistory(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {

		try {
			PaginatedList<IndicatorHistory> list = this.bs.listIndicatorHistory(macro, plan, indicator, page, pageSize);

			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Usuário recupera lista de filhos de uma instancia para o gráfico
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param levelInstance
	 *            ID do nível
	 * @return plan2 Lista com os filhos da instancia requerida
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/community/levelsonsgraph")
    @CommunityDashboard
	public ResponseEntity<?> listLevelSonsForGraph(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long levelInstance,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {
		
		try {
			PaginatedList<StructureLevelInstance> list;
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			if (plan == null)
				list = this.sbs.retrieveLevelSonsForGraph(planMacro, null, null, page, pageSize);
			else
				list = this.sbs.retrieveLevelSonsForGraph(null, this.planBS.retrieveById(plan), levelInstance, page,
						pageSize);

			if (list.getTotal() > 0) {
				for (int i = 0; i < list.getList().size(); i++) {
					this.structHelper.fillIndicators(list.getList().get(i));
				}
			}

			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Recuperação de metas e informações para o dashboard da comunidade
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @param levelInstance
	 *            ID do nível
	 * @return goalsList Lista com as metas recuperadas
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/community/communityinfotable")
	@CommunityDashboard
	public ResponseEntity<?> communityInfoTable(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long levelInstance,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {

		try {
			PaginatedList<GoalsInfoTable> goalsList;
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
			Plan planObj = plan != null ? this.planBS.retrieveById(plan) : null;

			goalsList = this.bs.getCommunityInfoTable(planMacro, planObj, levelInstance, page, pageSize);

			return this.success(goalsList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Recuperação de detalhes sobre um plano de metas
	 *
	 * @param macro
	 *            ID do plano macro a ter os dados recuperados
	 * @param plan
	 *            ID do plano de metas a ter os dados recuperados
	 * @return planDetails dados do plano de metas recuperado, como número de
	 *         indicadores e objetivos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/community/planDetails")
	@CommunityDashboard
	public ResponseEntity<?> communityPlanDetails(
					@RequestParam(required = false) Long macro,
					@RequestParam(required = false) Long plan) {
		try {
			PlanDetails planDetails = new PlanDetails();
			PlanMacro planMacro = null;
			Plan plan2 = null;
			long numOfIndicators;
			long numOfPlans = 0;
			long numOfGoals = -1;
			double goalsDelayedPercent = -1.0;
			if (plan == null && macro == null) {
				Map<String, Long> counts = this.sbs.countPlansAndIndicators();
				numOfIndicators = counts.get("indicatorCount");
				numOfPlans = counts.get("planCount");
			} else if (plan == null) {
				planMacro = this.planBS.retrievePlanMacroById(macro);
				numOfIndicators = sbs.countIndicators(planMacro);
			} else {
				plan2 = this.planBS.retrieveById(plan);
				numOfIndicators = sbs.countIndicators(plan2);

				numOfGoals = 0;
				goalsDelayedPercent = 0.0;
				List<StructureLevelInstance> allGoals = this.sbs.listGoalsByPlan(plan2);

				numOfGoals = (long) allGoals.size();
				goalsDelayedPercent = numOfGoals > 0
						? ((double) goalsInfoBS.getNumberOfGoalsLate(allGoals) * 100) / numOfGoals
						: 0;
			}
			numOfPlans = sbs.countPlans(planMacro, plan2);

			GoalsInfoOverview goalsInfoOverview = planMacro != null
					? goalsInfoBS.getGoalsInfoOverviewByPlanMacro(planMacro)
					: goalsInfoBS.getGoalsInfoOverviewByCompany(domain.get().getCompany());
			long totalGoals = numOfGoals == -1 ? goalsInfoOverview.getTotal() : numOfGoals;
			long late = goalsInfoOverview.getLate();
			goalsDelayedPercent = goalsDelayedPercent == -1
					? (totalGoals > 0 ? late * 100.0 / totalGoals : 0)
					: goalsDelayedPercent;
			planDetails.setGoalsDelayedPerCent(goalsDelayedPercent);
			planDetails.setNumberOfBudgets(sbs.countBudgets(planMacro, plan2));
			planDetails.setNumberOfObjectives(sbs.countObjective(planMacro, plan2));
			planDetails.setNumberOfIndicators(numOfIndicators);
			planDetails.setNumberOfGoals(totalGoals);
			planDetails.setNumberOfThematicAxis(sbs.countStrategicAxis(planMacro, plan2));
			planDetails.setNumberOfPlans(numOfPlans);

			return this.success(planDetails);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	

	/**
	 * Usuário recupera lista de metas e informações sobre metas, como nome e
	 * data de criação para o gráfico no indicador
	 *
	 * @param indicator
	 *            ID do indicador a ter os dados recuperados
	 * @return goalsList Lista com as informações e metas recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/graphforindicator")
	public ResponseEntity<?> graphForIndicator(
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {

		try {
			PaginatedList<StructureLevelInstance> goalsList;
			StructureLevelInstance indicatorLevel = this.sbs.retrieveLevelInstance(indicator);
			goalsList = this.sbs.listGoalsByIndicatorWithoutResponsible(indicatorLevel, page, pageSize);
			List<StructureLevelInstance> list = new ArrayList<StructureLevelInstance>();
			for (StructureLevelInstance sli : goalsList.getList()) {
				sli.setAttributeInstanceList(new ArrayList<AttributeInstance>());
				sli.setAttributeList(this.sbs.retrieveLevelAttributes(sli.getLevel()));
				AttributeInstance polarity = this.attrHelper.retrievePolarityAttributeInstance(sli.getParent());
				if (polarity == null)
					sli.setPolarity(Polarity.BIGGER_BETTER.getValue());
				else
					sli.setPolarity(polarity.getValue());
				for (Attribute attr : sli.getAttributeList()) {
					AttributeInstance attrInst = this.attrHelper.retrieveAttributeInstance(sli, attr);
					if (attrInst != null)
						sli.getAttributeInstanceList().add(attrInst);
					else
						sli.getAttributeInstanceList().add(new AttributeInstance());
				}
				list.add(sli);
			}
			goalsList.setList(list);
			return this.success(goalsList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

}
