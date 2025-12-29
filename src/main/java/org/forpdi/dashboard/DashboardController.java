package org.forpdi.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
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
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.cloudfoundry.AccessLevel;
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
	@Autowired
	private CompanyBS companyBS ;

	/**
	 * Usuário recupera informações sobre os objetivos
	 *
	 * @param macro
	 *              ID do plano macro a ter os dados recuperados
	 * @param plan
	 *              ID do plano de metas a ter os dados recuperados
	 * @return info informações sobre os objetivos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/goals")
	public ResponseEntity<?> adminGoalsInfo(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			UserSession userSession) {
		try {
			Company tenant = null;

			if (macro != null) {
				PlanMacro mac = this.planBS.retrievePlanMacroById(macro);
				if (mac != null)
					tenant = mac.getCompany();
			}
			if (tenant == null && plan != null) {
				Plan p = this.planBS.retrieveById(plan);
				if (p != null && p.getParent() != null)
					tenant = p.getParent().getCompany();
			}

			if (tenant != null) {
				domain.validateTenant(tenant);
			}

			PaginatedList<StructureLevelInstance> list = this.sbs.listAllGoals(
					macro != null ? this.planBS.retrievePlanMacroById(macro) : null,
					plan != null ? this.planBS.retrieveById(plan) : null);

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
	 *              ID do plano macro a ter os dados recuperados
	 * @param plan
	 *              ID do plano de metas a ter os dados recuperados
	 * @return plan2 Plano contendo a lista de eixos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/performanceStrategicAxis")
		public ResponseEntity<?> performanceStrategicAxis(
				@RequestParam(required = false) Long macro,
				@RequestParam(required = false) Long plan,
				@RequestParam(required = false) Long thematicAxis,
				@RequestParam(required = false) Long objective,
				@RequestParam(required = false) Long indicator,
				@RequestParam(required = false) Integer page,
				@RequestParam(required = false) Integer pageSize,
				UserSession userSession) {

			try {
				PaginatedList<StructureLevelInstance> list;
				boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
				
				if (!isSystemAdmin) {
					Long loggedInUserId = userSession.getAuthData().getId();
					Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
					if (userCompany == null) {
						return this.fail("Acesso negado. Usuário não associado a uma empresa.");
					}

					if (plan == null && macro == null) {
						list = this.sbs.listStrategicAxis(null, null, thematicAxis, objective, indicator, page, pageSize);
					}
					
					if (plan == null) {
						PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
						if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
							return this.fail("Acesso negado.");
						}
						list = this.sbs.listStrategicAxis(planMacro, null, thematicAxis, objective, indicator, page, pageSize);
					} else {
						Plan planObj = this.planBS.retrieveById(plan);
						if (planObj != null && !planObj.getParent().getCompany().getId().equals(userCompany.getId())) {
							return this.fail("Acesso negado.");
						}
						list = this.sbs.listStrategicAxis(null, planObj, thematicAxis, objective, indicator, page, pageSize);
					}
				} else {
					if (plan == null && macro == null) {
						list = this.sbs.listStrategicAxis(null, null, thematicAxis, objective, indicator, page, pageSize);
					} else if (plan == null) {
						PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
						if (planMacro != null) {
							domain.validateTenant(planMacro.getCompany());
						}
						list = this.sbs.listStrategicAxis(planMacro, null, thematicAxis, objective, indicator, page, pageSize);
					} else {
						Plan planObj = this.planBS.retrieveById(plan);
						if (planObj != null) {
							domain.validateTenant(planObj.getParent().getCompany());
						}
						list = this.sbs.listStrategicAxis(null, planObj, thematicAxis, objective, indicator, page, pageSize);
					}
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
	 *                     ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                     ID do plano de metas a ter os dados recuperados
	 * @param thematicAxis
	 *                     ID do eixo temático a ter os dados recuperados
	 * @return plan2 Plano contendo a lista de eixos
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/objectivesByThematicAxis")
	public ResponseEntity<?> objectivesByThematicAxis(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long thematicAxis,
			UserSession userSession) {

		try {
			PaginatedList<StructureLevelInstance> list;
			StructureLevelInstance thematicAxisLevel = this.sbs.retrieveLevelInstance(thematicAxis);
			if (thematicAxisLevel == null) {
				return this.notFound();
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}
				if (!thematicAxisLevel.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}

				PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}

				Plan planObj = (plan != null) ? this.planBS.retrieveById(plan) : null;
				if (planObj != null && !planObj.getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				list = this.sbs.listObjectivesByThematicAxis(planMacro, planObj, thematicAxisLevel);
			} else {
				domain.validateTenant(thematicAxisLevel.getPlan().getParent().getCompany());

				PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}

				Plan planObj = (plan != null) ? this.planBS.retrieveById(plan) : null;
				if (planObj != null) {
					domain.validateTenant(planObj.getParent().getCompany());
				}
				list = this.sbs.listObjectivesByThematicAxis(planMacro, planObj, thematicAxisLevel);
			}

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
	 *              ID do plano macro a ter os dados recuperados
	 * @param plan
	 *              ID do plano de metas a ter os dados recuperados
	 * @return planDetails detalhes recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/planDetails")
	public ResponseEntity<?> adminPlanDetails(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			UserSession userSession) {

		try {
			PlanDetails planDetails = new PlanDetails();
			PlanMacro planMacro = null;
			Plan planObj = null;
			long numOfIndicators;
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (plan == null && macro == null) {
					return this.fail("Acesso negado. Especifique um plano ou plano macro.");
				}

				if (plan == null) {
					planMacro = this.planBS.retrievePlanMacroById(macro);
					if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
						return this.fail("Acesso negado.");
					}
					numOfIndicators = sbs.countIndicators(planMacro);
				} else {
					planObj = this.planBS.retrieveById(plan);
					if (planObj != null && !planObj.getParent().getCompany().getId().equals(userCompany.getId())) {
						return this.fail("Acesso negado.");
					}
					numOfIndicators = sbs.countIndicators(planObj);
				}
			} else {
				if (plan == null && macro == null) {
					numOfIndicators = sbs.countAllIndicators();
				} else if (plan == null) {
					planMacro = this.planBS.retrievePlanMacroById(macro);
					if (planMacro != null) {
						domain.validateTenant(planMacro.getCompany());
					}
					numOfIndicators = sbs.countIndicators(planMacro);
				} else {
					planObj = this.planBS.retrieveById(plan);
					if (planObj != null) {
						domain.validateTenant(planObj.getParent().getCompany());
					}
					numOfIndicators = sbs.countIndicators(planObj);
				}
			}

			PaginatedList<StructureLevelInstance> goalList = this.sbs.listGoalsByResponsible(planMacro, planObj);

			if (goalList.getTotal() > 0) {
				Double goalsLatePercent = ((double) goalsInfoBS.getNumberOfGoalsLate(goalList.getList()) * 100)
						/ goalList.getTotal();
				planDetails.setGoalsDelayedPerCent(goalsLatePercent);
			} else {
				planDetails.setGoalsDelayedPerCent(0.0);
			}

			planDetails.setNumberOfBudgets(sbs.countBudgets(planMacro, planObj));
			planDetails.setNumberOfObjectives(sbs.countObjective(planMacro, planObj));
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
	 *                  ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                  ID do plano de metas a ter os dados recuperados
	 * @param objective
	 *                  ID do objetivo a ter os dados recuperados
	 * @param subAction
	 *                  ação orçamentária desejada
	 * @return budgets lista de orçamentos recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/budget")
	public ResponseEntity<?> listBudgets(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long objective,
			@RequestParam(required = false) String subAction,
			UserSession userSession) {

		try {
			PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
			Plan plan2 = (plan != null) ? this.planBS.retrieveById(plan) : null;
			StructureLevelInstance obj = (objective != null) ? this.sbs.retrieveLevelInstance(objective) : null;
			
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (plan2 != null && !plan2.getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (obj != null && !obj.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			} else {
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}
				if (plan2 != null) {
					domain.validateTenant(plan2.getParent().getCompany());
				}
				if (obj != null) {
					domain.validateTenant(obj.getPlan().getParent().getCompany());
				}
			}

			List<Budget> list = this.bs.listBudgets(planMacro, plan2, obj, subAction);
			GeneralBudgets budgets = calculateBudgetsSummary(list);
			return this.success(budgets);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	/**
	 * Calcula o resumo dos orçamentos de forma mais eficiente e limpa.
	 */
	private GeneralBudgets calculateBudgetsSummary(List<Budget> budgetsList) {
		GeneralBudgets budgets = new GeneralBudgets();
		double realized = 0.0;
		double committed = 0.0;
		double planned = 0.0;

		if (budgetsList == null || budgetsList.isEmpty()) {
			budgets.setConducted(realized);
			budgets.setCommitted(committed);
			budgets.setPlanned(planned);
			return budgets;
		}

		Set<Long> processedBudgetElementIds = new HashSet<>();
		for (Budget budget : budgetsList) {
			if (budget.getCommitted() != null) {
				committed += budget.getCommitted();
			}
			if (budget.getRealized() != null) {
				realized += budget.getRealized();
			}
			Long elementId = budget.getBudgetElement().getId();
			if (!processedBudgetElementIds.contains(elementId)) {
				planned += budget.getBudgetElement().getBudgetLoa();
				processedBudgetElementIds.add(elementId);
			}
		}
		budgets.setConducted(realized);
		budgets.setCommitted(committed);
		budgets.setPlanned(planned);
		return budgets;
	}

	/**
	 * Usuário recupera lista de indicadores e suas informações
	 *
	 * @param macro
	 *                  ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                  ID do plano de metas a ter os dados recuperados
	 * @param objective
	 *                  ID do objetivo a ter os dados recuperados
	 * @return indicatorsList Lista de indicadores recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/indicatorsInformation")
	public ResponseEntity<?> indicatorsInformation(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long objective,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			UserSession userSession) {
		try {
			PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
			Plan plan2 = (plan != null) ? this.planBS.retrieveById(plan) : null;
			StructureLevelInstance objectiveLevel = (objective != null) ? this.sbs.retrieveLevelInstance(objective) : null;

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (plan2 != null && !plan2.getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (objectiveLevel != null && !objectiveLevel.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			} else {
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}
				if (plan2 != null) {
					domain.validateTenant(plan2.getParent().getCompany());
				}
				if (objectiveLevel != null) {
					domain.validateTenant(objectiveLevel.getPlan().getParent().getCompany());
				}
			}

			PaginatedList<StructureLevelInstance> indicatorsList = this.sbs.listIndicators(planMacro, plan2,
					objectiveLevel, page, pageSize);

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
	 *                  ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                  ID do plano de metas a ter os dados recuperados
	 * @param indicator
	 *                  ID do indicador a ter os dados recuperados
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
			@RequestParam(required = false) Integer filter,
			UserSession userSession) {

		try {
			PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
			Plan planInstance = (plan != null) ? this.planBS.retrieveById(plan) : null;
			StructureLevelInstance indicatorLevel = (indicator != null) ? this.sbs.retrieveLevelInstance(indicator) : null;

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (planInstance != null && !planInstance.getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (indicatorLevel != null && !indicatorLevel.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			} else {
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}
				if (planInstance != null) {
					domain.validateTenant(planInstance.getParent().getCompany());
				}
				if (indicatorLevel != null) {
					domain.validateTenant(indicatorLevel.getPlan().getParent().getCompany());
				}
			}

			PeformanceFilterType peformanceFilterType = filter != null
					? PeformanceFilterType.valueOf(filter)
					: null;
			PaginatedList<GoalsInfoTable> goalsList = this.bs.getGoalsInfoTable(planMacro, planInstance, indicatorLevel,
					page,
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
	 *              ID do plano macro a ter os dados recuperados
	 * @param plan
	 *              ID do plano de metas a ter os dados recuperados
	 * @return objectivesList Lista com as informações e objetivos recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/admin/objectivesInformation")
	public ResponseEntity<?> objectivesInformation(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			UserSession userSession) {

		try {
			PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
			Plan plan2 = (plan != null) ? this.planBS.retrieveById(plan) : null;

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (plan2 != null && !plan2.getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			} else {
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}
				if (plan2 != null) {
					domain.validateTenant(plan2.getParent().getCompany());
				}
			}

			PaginatedList<StructureLevelInstance> objectivesList = this.sbs.listObjectives(planMacro, plan2);

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
	 *                  ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                  ID do plano de metas a ter os dados recuperados
	 * @param indicator
	 *                  ID do indicador a ter os dados recuperados
	 * @return goalsList Lista com as informações e metas recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/colaborator/goalsInformation")
	public ResponseEntity<?> goalsInformation(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			UserSession userSession) {

		try {
			PlanMacro planMacro = (macro != null) ? this.planBS.retrievePlanMacroById(macro) : null;
			Plan plan2 = (plan != null) ? this.planBS.retrieveById(plan) : null;
			StructureLevelInstance indicatorLevel = (indicator != null) ? this.sbs.retrieveLevelInstance(indicator) : null;

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (plan2 != null && !plan2.getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
				if (indicatorLevel != null && !indicatorLevel.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			} else {
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}
				if (plan2 != null) {
					domain.validateTenant(plan2.getParent().getCompany());
				}
				if (indicatorLevel != null) {
					domain.validateTenant(indicatorLevel.getPlan().getParent().getCompany());
				}
			}

			PaginatedList<StructureLevelInstance> goals = this.sbs.listGoals(planMacro, plan2, indicatorLevel, page,
					pageSize);
			List<AttributeInstance> attrInstances = structureInstanceAttributeHelper
					.listAllAttributeInstanceByLevelInstances(goals.getList());
			Map<Long, List<AttributeInstance>> goalAttrInstanceMap = new HashMap<>();
			for (AttributeInstance attrInstance : attrInstances) {
				List<AttributeInstance> attrInstanceList = goalAttrInstanceMap
						.get(attrInstance.getLevelInstance().getId());
				if (attrInstanceList == null) {
					attrInstanceList = new LinkedList<>();
					goalAttrInstanceMap.put(attrInstance.getLevelInstance().getId(), attrInstanceList);
				}
				attrInstanceList.add(attrInstance);
			}
			Map<Long, AttributeInstance> polarityMap = polarityMapHelper.generatePolarityMap(goals.getList());
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
	 *                  ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                  ID do plano de metas a ter os dados recuperados
	 * @param indicator
	 *                  ID do indicador a ter os dados recuperados
	 * @return list lista contendo o histórico do indicador requerido
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/manager/indicatorsHistory")
	public ResponseEntity<?> listIndicatorsHistory(
			@RequestParam(required = false) Long macro,
			@RequestParam(required = false) Long plan,
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			UserSession userSession) {

		try {
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				if (macro != null) {
					PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
					if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
						return this.fail("Acesso negado.");
					}
				}
				if (plan != null) {
					Plan planInstance = this.planBS.retrieveById(plan);
					if (planInstance != null && !planInstance.getParent().getCompany().getId().equals(userCompany.getId())) {
						return this.fail("Acesso negado.");
					}
				}
				if (indicator != null) {
					StructureLevelInstance indicatorLevel = this.sbs.retrieveLevelInstance(indicator);
					if (indicatorLevel != null && !indicatorLevel.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
						return this.fail("Acesso negado.");
					}
				}
			} else {
				if (macro != null) {
					PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
					if (planMacro != null) {
						domain.validateTenant(planMacro.getCompany());
					}
				}
				if (plan != null) {
					Plan planInstance = this.planBS.retrieveById(plan);
					if (planInstance != null) {
						domain.validateTenant(planInstance.getParent().getCompany());
					}
				}
				if (indicator != null) {
					StructureLevelInstance indicatorLevel = this.sbs.retrieveLevelInstance(indicator);
					if (indicatorLevel != null) {
						domain.validateTenant(indicatorLevel.getPlan().getParent().getCompany());
					}
				}
			}

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
	 *                      ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                      ID do plano de metas a ter os dados recuperados
	 * @param levelInstance
	 *                      ID do nível
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

				if (plan == null) {
					PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
					if (planMacro != null) {
						domain.validateTenant(planMacro.getCompany());
					}
					list = this.sbs.retrieveLevelSonsForGraph(planMacro, null, null, page, pageSize);
				} else {
					Plan planObj = this.planBS.retrieveById(plan);
					if (planObj != null) {
						domain.validateTenant(planObj.getParent().getCompany());
					}

					if (levelInstance != null) {
						StructureLevelInstance levelInstanceObj = this.sbs.retrieveLevelInstance(levelInstance);
						if (levelInstanceObj != null) {
							domain.validateTenant(levelInstanceObj.getPlan().getParent().getCompany());
						}
					}

					list = this.sbs.retrieveLevelSonsForGraph(null, planObj, levelInstance, page, pageSize);
				}

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
	 *                      ID do plano macro a ter os dados recuperados
	 * @param plan
	 *                      ID do plano de metas a ter os dados recuperados
	 * @param levelInstance
	 *                      ID do nível
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
				PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}

				Plan planObj = plan != null ? this.planBS.retrieveById(plan) : null;
				if (planObj != null) {
					domain.validateTenant(planObj.getParent().getCompany());
				}

				if (levelInstance != null) {
					StructureLevelInstance levelInstanceObj = this.sbs.retrieveLevelInstance(levelInstance);
					if (levelInstanceObj != null) {
						domain.validateTenant(levelInstanceObj.getPlan().getParent().getCompany());
					}
				}

				PaginatedList<GoalsInfoTable> goalsList = this.bs.getCommunityInfoTable(planMacro, planObj, levelInstance,
						page, pageSize);

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
	 *              ID do plano macro a ter os dados recuperados
	 * @param plan
	 *              ID do plano de metas a ter os dados recuperados
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
				double goalsCompletedPercent = -1.0;

				if (plan == null && macro == null) {
					Map<String, Long> counts = this.sbs.countPlansAndIndicators();
					numOfIndicators = counts.get("indicatorCount");
					numOfPlans = counts.get("planCount");

				} else if (plan == null) {
					planMacro = this.planBS.retrievePlanMacroById(macro);
					if (planMacro != null) {
						domain.validateTenant(planMacro.getCompany());
					}
					numOfIndicators = sbs.countIndicators(planMacro);
				} else {
					plan2 = this.planBS.retrieveById(plan);
					if (plan2 != null) {
						domain.validateTenant(plan2.getParent().getCompany());
					}
					if (plan2 == null) {
						LOGGER.warn("Plano (subplano) com ID {} não encontrado em communityPlanDetails.", plan);
						numOfIndicators = 0;
						numOfGoals = 0;
						goalsDelayedPercent = 0.0;
						goalsCompletedPercent = 0.0;
					} else {
						numOfIndicators = sbs.countIndicators(plan2);

						List<StructureLevelInstance> allGoals = this.sbs.listGoalsByPlan(plan2);
						numOfGoals = (long) allGoals.size();

						long lateGoalsCount = goalsInfoBS.getNumberOfGoalsLate(allGoals);
						goalsDelayedPercent = numOfGoals > 0
								? ((double) lateGoalsCount * 100.0) / numOfGoals
								: 0.0;

						long completedGoalsCount = goalsInfoBS.getNumberOfGoalsCompleted(allGoals);
						goalsCompletedPercent = (numOfGoals > 0)
								? ((double) completedGoalsCount * 100.0) / numOfGoals
								: 0.0;
					}
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

				long reached = goalsInfoOverview.getReached();

				goalsCompletedPercent = goalsCompletedPercent == -1
						? (totalGoals > 0 ? reached * 100.0 / totalGoals : 0)
						: goalsCompletedPercent;

				planDetails.setGoalsDelayedPerCent(goalsDelayedPercent);
				planDetails.setGoalsCompletedPercent(goalsCompletedPercent);
				planDetails.setNumberOfBudgets(sbs.countBudgets(planMacro, plan2));
				planDetails.setNumberOfObjectives(sbs.countObjective(planMacro, plan2));
				planDetails.setNumberOfIndicators(numOfIndicators);
				planDetails.setNumberOfGoals(totalGoals);
				planDetails.setNumberOfThematicAxis(sbs.countStrategicAxis(planMacro, plan2));
				planDetails.setNumberOfPlans(numOfPlans);

				return this.success(planDetails);

			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error in communityPlanDetails", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}
	/**
	 * Usuário recupera lista de metas e informações sobre metas, como nome e
	 * data de criação para o gráfico no indicador
	 *
	 * @param indicator
	 *                  ID do indicador a ter os dados recuperados
	 * @return goalsList Lista com as informações e metas recuperados
	 *
	 */
	@GetMapping(BASEPATH + "/dashboard/graphforindicator")
	public ResponseEntity<?> graphForIndicator(
			@RequestParam(required = false) Long indicator,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			UserSession userSession) {

		try {
			PaginatedList<StructureLevelInstance> goalsList;
			StructureLevelInstance indicatorLevel = this.sbs.retrieveLevelInstance(indicator);
			
			if (indicatorLevel == null) {
				return this.notFound();
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !indicatorLevel.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			} else {
				domain.validateTenant(indicatorLevel.getPlan().getParent().getCompany());
			}

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