package org.forpdi.dashboard;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.dashboard.manager.IndicatorHistory;
import org.forpdi.dashboard.manager.LevelInstanceHistory;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.enums.Periodicity;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.filters.PeformanceFilterType;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.GoalProgressStatus;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.system.CriteriaCompanyFilter;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DashboardBS extends HibernateBusiness {

	@Autowired
	private UserSession userSession;
	@Autowired
	private StructureBS sbs;
	@Autowired
	private PlanBS pbs;
	@Autowired
	private AttributeHelper attrHelper;
	@Autowired
	private CriteriaCompanyFilter filter;
	@Autowired
	private GoalsInfoTableHelper infoTableHelper;
	@Autowired
	private GoalsInfoTableFilterHelper goalsFilterHelper;

	private final Integer PAGESIZE = 5;


	/**
	 * Retorna lista de orçamentos.
	 *
	 * @param macro
	 *            Plano macro no qual será pesquisado a lista de orçamentos.
	 * @param plan
	 *            Plano no qual será pesquisado a lista de orçamentos.
	 * @param obj
	 *            Objetivos no qual será pesquisado a lista de orçamentos.
	 * @param subAction
	 *            Sub Ação Orçamentária no qual será pesquisado a lista de
	 *            orçamentos.
	 * @return List<Budget> Lista de orçamentos
	 */
	public List<Budget> listBudgets(PlanMacro macro, Plan plan, StructureLevelInstance obj, String subAction) {
		Criteria criteria = this.dao.newCriteria(Budget.class);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.add(Restrictions.eq("deleted", false));

		if (obj != null) {
			criteria.add(Restrictions.eq("levelInstance", obj));
			if (subAction != null) {
				criteria.add(Restrictions.eq("subAction", subAction));
			}
		} else if (plan != null) {
			criteria.add(Restrictions.eq("levelInstance.plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}

		List<Budget> budgets = this.filter.filterAndList(criteria, Budget.class, "macro.company");

		if (this.userSession.getAccessLevel() < 50) {
			List<Budget> list2 = new ArrayList<Budget>();
			for (Budget bud : budgets) {
				boolean lvlAdd = false;
				List<StructureLevelInstance> stLvInstList = this.sbs
						.retrieveLevelInstanceSons(bud.getLevelInstance().getId());
				for (StructureLevelInstance stLvInst : stLvInstList) {
					List<Attribute> attributeList = this.sbs.retrieveLevelAttributes(stLvInst.getLevel());
					for (Attribute attr : attributeList) {
						if (attr.getType().equals(ResponsibleField.class.getCanonicalName())) {
							AttributeInstance attrInst = this.attrHelper.retrieveAttributeInstance(stLvInst, attr);
							if (attrInst != null) {
								if (!lvlAdd
										&& Long.parseLong(attrInst.getValue()) == this.userSession.getUser().getId()) {
									list2.add(bud);
									lvlAdd = true;
								}
							}
						}
					}
				}

				StructureLevelInstance lvlI = bud.getLevelInstance();
				List<Attribute> attributeList = this.sbs.retrieveLevelAttributes(lvlI.getLevel());
				for (Attribute attr : attributeList) {
					if (attr.getType().equals(ResponsibleField.class.getCanonicalName())) {
						AttributeInstance attrInst = this.attrHelper.retrieveAttributeInstance(lvlI, attr);
						if (attrInst != null) {
							if (!lvlAdd && Long.parseLong(attrInst.getValue()) == this.userSession.getUser().getId()) {
								list2.add(bud);
								lvlAdd = true;
							}
						}
					}
				}
				while (lvlI.getParent() != null && !lvlAdd) {
					lvlI = this.sbs.retrieveLevelInstance(lvlI.getParent());
					attributeList = this.sbs.retrieveLevelAttributes(lvlI.getLevel());
					for (Attribute attr : attributeList) {
						if (attr.getType().equals(ResponsibleField.class.getCanonicalName())) {
							AttributeInstance attrInst = this.attrHelper.retrieveAttributeInstance(lvlI, attr);
							if (attrInst != null) {
								if (!lvlAdd
										&& Long.parseLong(attrInst.getValue()) == this.userSession.getUser().getId()) {
									list2.add(bud);
									lvlAdd = true;
								}
							}
						}
					}
				}
			}
			budgets = list2;
		}

		return budgets;
	}

	/**
	 * Salvar histórico do indicador.
	 *
	 * @param instance
	 *            Indicador para salvar o histórico.
	 * @param periodicity
	 *            Periocidade (Tempo para salvar histórico do indicador).
	 * @return void.
	 */
	public void saveIndicatorHistory(StructureLevelInstance instance, Periodicity periodicity) {
		if (instance.getNextSave() != null) {
			LevelInstanceHistory history = new LevelInstanceHistory();
			history.setCreation(new Date());
			history.setDeleted(false);
			history.setLevelInstance(instance);
			if (instance.getLevelValue() == null)
				history.setValue(0.0);
			else
				history.setValue(instance.getLevelValue());
			this.dao.persist(history);
		}
		Calendar calendar = Calendar.getInstance();
		if (instance.getNextSave() == null)
			calendar.setTime(new Date());
		else
			calendar.setTime(instance.getNextSave());

		switch (periodicity.getValue()) {
		case 1:
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			break;
		case 2:
			calendar.add(Calendar.WEEK_OF_YEAR, 1);
			break;
		case 3:
			calendar.add(Calendar.WEEK_OF_YEAR, 2);
			break;
		case 4:
			calendar.add(Calendar.MONTH, 1);
			break;
		case 5:
			calendar.add(Calendar.MONTH, 2);
			break;
		case 6:
			calendar.add(Calendar.MONTH, 3);
			break;
		case 7:
			calendar.add(Calendar.MONTH, 6);
			break;
		case 8:
			calendar.add(Calendar.YEAR, 1);
			break;
		case 9:
			calendar.add(Calendar.YEAR, 2);
			break;
		case 10:
			calendar.add(Calendar.YEAR, 3);
			break;
		}

		instance.setNextSave(calendar.getTime());
		this.dao.persist(instance);
	}

	/**
	 * Listar todas as metas pertencentes a um indicador.
	 *
	 * @param macro
	 *            Plano Macro para buscar as metas.
	 * @param plan
	 *            Plano de Metas para buscar as metas.
	 * @param indicator
	 *            Indicador para buscar as metas.
	 * @return PaginatedList<GoalsInfoTable> Lista de Metas.
	 * @throws ParseException
	 */
	public PaginatedList<GoalsInfoTable> getGoalsInfoTable(PlanMacro macro, Plan plan, StructureLevelInstance indicator,
			Integer page, Integer pageSize, PeformanceFilterType type) throws ParseException {
		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		// obtem todos os indices de StructureLevelInstance em que o usuario eh responsavel, inclusive os filhos
		Set<Long> allLevelInstanceIds = this.sbs.retrieveChildResponsibleIds();
		// obtem uma lista com as metas filtradas de acordo com os parametros passados
		if (allLevelInstanceIds.isEmpty()) {
			return new PaginatedList<>(new ArrayList<>(0), 0L);
		}
		List<StructureLevelInstance> goals = this.goalsFilterHelper.goalsFilter(allLevelInstanceIds, macro, plan, indicator, type);
		// faz a paginacao
		long total = goals.size();
		int minIdx = (page - 1) * pageSize;
		int maxIdx = minIdx + pageSize;
		goals = goals.subList(minIdx, maxIdx < goals.size() ? maxIdx : goals.size());
		// seta os indicadores e objetivos das metas
		this.infoTableHelper.setIndicators(goals);
		this.infoTableHelper.setObjectives(goals);
		// gera uma lista com as informacoes das metas
		ArrayList<GoalsInfoTable> goalsInfo = this.infoTableHelper.generateGoalsInfo(goals);
		return new PaginatedList<>(goalsInfo, total);
	}


	/**
	 * Listar histórico dos indicadores.
	 *
	 * @param macro
	 *            Id do plano macro para listar os indicadores.
	 * @param plan
	 *            Id do plano para listar os indicadores.
	 * @param indicator
	 *            Id do indicador.
	 * @return List<IndicatorHistory> Lista do histórico dos indicadores.
	 */
	public PaginatedList<IndicatorHistory> listIndicatorHistory(Long macro, Long plan, Long indicator, Integer page,
			Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PAGESIZE;
		}
		List<IndicatorHistory> list = new ArrayList<>();
		Criteria criteria = this.dao.newCriteria(LevelInstanceHistory.class);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		Criteria counting = this.dao.newCriteria(LevelInstanceHistory.class);
		counting.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		counting.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN);
		counting.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		counting.add(Restrictions.eq("deleted", false));
		counting.setProjection(Projections.countDistinct("id"));
		if (indicator != null) {
			StructureLevelInstance instance = this.sbs.retrieveLevelInstance(indicator);
			criteria.add(Restrictions.eq("levelInstance", instance));
			criteria.setFirstResult((page - 1) * pageSize);
			criteria.setMaxResults(pageSize);
			counting.add(Restrictions.eq("levelInstance", instance));
		} else if (plan != null) {
			Plan plan2 = this.pbs.retrieveById(plan);
			criteria.add(Restrictions.eq("levelInstance.plan", plan2));
		} else if (macro != null) {
			PlanMacro macro2 = this.pbs.retrievePlanMacroById(macro);
			criteria.add(Restrictions.eq("plan.parent", macro2));
		}

		List<LevelInstanceHistory> historyList = this.filter.filterAndList(criteria, LevelInstanceHistory.class,
				"macro.company");
		Long total = this.filter.filterAndFind(counting, "macro.company");

		if (indicator == null) {
			Map<String, ArrayList<Double>> map = new HashMap<>();
			Calendar calendar = Calendar.getInstance();
			for (LevelInstanceHistory history : historyList) {
				calendar.setTime(history.getCreation());
				if (map.get(String.valueOf(calendar.get(Calendar.YEAR))) == null) {
					map.put(String.valueOf(calendar.get(Calendar.YEAR)), new ArrayList<Double>());
				}
				map.get(String.valueOf(calendar.get(Calendar.YEAR))).add(history.getValue());
			}

			for (Map.Entry<String, ArrayList<Double>> entry : map.entrySet()) {
				IndicatorHistory indicatorHistory = new IndicatorHistory();
				indicatorHistory.setPeriod(entry.getKey());
				Double value = 0.0;
				for (Double val : entry.getValue()) {
					value += val;
				}
				indicatorHistory.setValue(value / entry.getValue().size());
				list.add(indicatorHistory);
			}
			Collections.sort(list);
		} else {
			for (LevelInstanceHistory history : historyList) {
				IndicatorHistory indicatorHistory = new IndicatorHistory();
				indicatorHistory.setPeriod(new SimpleDateFormat("dd/MM/yyyy").format(history.getCreation()));
				indicatorHistory.setValue(history.getValue());
				list.add(indicatorHistory);
			}
		}

		PaginatedList<IndicatorHistory> result = new PaginatedList<>();
		result.setList(list);
		if (indicator == null) {
			result.setTotal((long) list.size());
		} else {
			result.setTotal(total);
		}
		return result;
	}

	/**
	 * Cria uma criteria para pegar os filhos de um dado nível
	 *
	 * @param levelInstance
	 * @return Criteria
	 */
	public Criteria filterByInheritedParent(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("parent", levelInstance.getId()));
		if (levelInstance.getLevel().isIndicator()) {
			return criteria;
		}

		criteria.setProjection(Projections.property("id"));
		List<Long> ids = this.dao.findByCriteria(criteria, Long.class);
		Disjunction or = Restrictions.disjunction();
		for (Long id : ids) {
			or.add(Restrictions.eq("parent", id));
		}
		Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
		or.add(Restrictions.eq("parent", levelInstance.getId()));
		criteria2.add(or);
		if (levelInstance.getLevel().isObjective()) {
			return criteria2;
		}

		criteria2.setProjection(Projections.property("id"));
		List<Long> ids2 = this.dao.findByCriteria(criteria2, Long.class);
		Disjunction or2 = Restrictions.disjunction();
		for (Long id : ids2) {
			or2.add(Restrictions.eq("parent", id));
		}
		Criteria criteria3 = this.dao.newCriteria(StructureLevelInstance.class);
		or2.add(or);
		criteria3.add(or2);

		return criteria3;

	}

	/**
	 * Listar todas as metas pertencentes a um indicador.
	 *
	 * @param macro
	 *            Plano Macro para buscar as metas.
	 * @param plan
	 *            Plano de Metas para buscar as metas.
	 * @param indic
	 *            Indicador para buscar as metas.
	 * @return PaginatedList<GoalsInfoTable> Lista de Metas.
	 * @throws ParseException
	 */
	public PaginatedList<GoalsInfoTable> getCommunityInfoTable(PlanMacro macro, Plan plan, Long levelInstance,
			Integer page, Integer pageSize) throws ParseException {

		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}

		StructureLevelInstance lvlInstance = null;
		if (levelInstance != null) {
			lvlInstance = this.sbs.retrieveLevelInstance(levelInstance);
		}

		Criteria criteria;
		Criteria couting;

		if (lvlInstance != null) {
			criteria = this.filterByInheritedParent(lvlInstance);
			couting = this.filterByInheritedParent(lvlInstance);
		} else {
			criteria = this.dao.newCriteria(StructureLevelInstance.class);
			couting = this.dao.newCriteria(StructureLevelInstance.class);
		}

		criteria.createAlias("level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);

		criteria.add(Restrictions.eq("level.goal", true));
		criteria.add(Restrictions.eq("deleted", false));

		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);

		couting.createAlias("level", "level", JoinType.INNER_JOIN);
		couting.createAlias("plan", "plan", JoinType.INNER_JOIN);
		couting.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		couting.add(Restrictions.eq("level.goal", true));
		couting.add(Restrictions.eq("deleted", false));
		couting.setProjection(Projections.countDistinct("id"));

		if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
			couting.add(Restrictions.eq("plan.parent", macro));
		}

		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
			couting.add(Restrictions.eq("plan", plan));
		}

		List<StructureLevelInstance> levelInstances = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");
		Long total = this.filter.filterAndFind(couting, "macro.company");

		for (int i = 0; i < levelInstances.size(); i++) {
			levelInstances.get(i).getLevel().setAttributes(this.sbs.retrieveLevelSonsAttributes(levelInstances.get(i)));
		}

		ArrayList<GoalsInfoTable> goalsInfoTable = new ArrayList<>();
		for (StructureLevelInstance s : levelInstances) {
			List<Attribute> attributeList = this.sbs.retrieveLevelAttributes(s.getLevel());
			attributeList = this.sbs.setAttributesInstances(s, attributeList);
			s.getLevel().setAttributes(attributeList);

			GoalsInfoTable goalAux = new GoalsInfoTable();
			goalAux.setPlanName(s.getPlan().getName());

			StructureLevelInstance indicator = this.sbs.retrieveLevelInstance(s.getParent());
			if (indicator != null) {
				goalAux.setIndicatorName(indicator.getName());

				StructureLevelInstance objective = this.sbs.retrieveLevelInstance(indicator.getParent());
				if (objective != null) {
					goalAux.setObjectiveName(objective.getName());

					StructureLevelInstance strategicAxis = this.sbs.retrieveLevelInstance(objective.getParent());
					if (strategicAxis != null) {
						goalAux.setStrategicAxisName(strategicAxis.getName());
					}
				}
			}

			goalAux.setGoalName(s.getName());

			for (AttributeInstance a : this.sbs.listAttributeInstanceByLevel(s, false)) {
				Attribute attr = this.sbs.retrieveAttribute(a.getAttribute().getId());
				if (attr.isFinishDate() && s.getPlan().getParent().getCompany().isShowMaturity()) {
					goalAux.setFinishDate(a.getValueAsDate());
				} else if (attr.isReachedField()) {
					goalAux.setReached(a.getValueAsNumber());
				} else if (attr.isExpectedField()) {
					goalAux.setExpected(a.getValueAsNumber());
				}
			}

			goalsInfoTable.add(goalAux);
		}

		PaginatedList<GoalsInfoTable> result = new PaginatedList<>();
		result.setList(goalsInfoTable);
		result.setTotal(total);

		return result;
	}

	/**
	 * Listar todas as metas pertencentes a um indicador.
	 *
	 * @param macro
	 *              Plano Macro para buscar as metas.
	 * @param plan
	 *              Plano de Metas para buscar as metas.
	 * @param indic
	 *              Indicador para buscar as metas.
	 * @return PaginatedList<GoalsInfoTable> Lista de Metas.
	 * @throws ParseException
	 */
	public PaginatedList<GoalsInfoTable> getInfoTable(
			PlanMacro macro,
			Plan plan,
			Long levelInstance,
			Long goalId,
			Integer page,
			Integer pageSize,
			String startDateStr,
			String endDateStr,
			Boolean goalStatus,
			Integer progressStatus) throws ParseException {

		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}

		StructureLevelInstance lvlInstance = null;
		if (levelInstance != null) {
			lvlInstance = this.sbs.retrieveLevelInstance(levelInstance);
		}

		Criteria criteria;
		Criteria counting;

		if (lvlInstance != null) {
			criteria = this.filterByInheritedParent(lvlInstance);
			counting = this.filterByInheritedParent(lvlInstance);
		} else {
			criteria = this.dao.newCriteria(StructureLevelInstance.class);
			counting = this.dao.newCriteria(StructureLevelInstance.class);
		}

		criteria.createAlias("level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		counting.createAlias("level", "level", JoinType.INNER_JOIN);
		counting.createAlias("plan", "plan", JoinType.INNER_JOIN);

		if (plan != null) {
			criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
			criteria.createAlias("macro.company", "company", JoinType.INNER_JOIN);
			counting.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
			counting.createAlias("macro.company", "company", JoinType.INNER_JOIN);
		}

		criteria.add(Restrictions.eq("level.goal", true));
		criteria.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("level.goal", true));
		counting.add(Restrictions.eq("deleted", false));
		counting.setProjection(Projections.countDistinct("id"));

		if (macro != null && plan != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
			counting.add(Restrictions.eq("plan.parent", macro));
		}
		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
			counting.add(Restrictions.eq("plan", plan));
		}
		if (goalId != null) {
			criteria.add(Restrictions.eq("id", goalId));
			counting.add(Restrictions.eq("id", goalId));
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date startDate = null;
		Date endDate = null;
		try {
			if (startDateStr != null && !startDateStr.isEmpty()) {
				startDate = sdf.parse(startDateStr);
			}
			if (endDateStr != null && !endDateStr.isEmpty()) {
				endDate = sdf.parse(endDateStr);
			}
		} catch (ParseException e) {
			throw new RuntimeException("Formato de data inválido.");
		}

		if (startDate != null && endDate != null) {
			criteria.add(Restrictions.between("creation", startDate, endDate));
			counting.add(Restrictions.between("creation", startDate, endDate));
		} else if (startDate != null) {
			criteria.add(Restrictions.ge("creation", startDate));
			counting.add(Restrictions.ge("creation", startDate));
		} else if (endDate != null) {
			criteria.add(Restrictions.le("creation", endDate));
			counting.add(Restrictions.le("creation", endDate));
		}

		if (goalStatus != null) {
			criteria.add(Restrictions.eq("closed", goalStatus));
			counting.add(Restrictions.eq("closed", goalStatus));
		}

		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);

		List<StructureLevelInstance> levelInstances = this.filter.filterAndList(criteria,
				StructureLevelInstance.class, "company");
		Long total = this.filter.filterAndFind(counting, "company");

		GoalProgressStatus validProgressStatus = null;

		if(progressStatus != null){
			validProgressStatus = GoalProgressStatus.getById(progressStatus);
			sbs.setGoalStatus(levelInstances, null);
		}

		ArrayList<GoalsInfoTable> goalsInfoTable = new ArrayList<>();
		for (StructureLevelInstance s : levelInstances) {
			List<Attribute> attributeList = this.sbs.retrieveLevelAttributes(s.getLevel());
			attributeList = this.sbs.setAttributesInstances(s, attributeList);
			s.getLevel().setAttributes(attributeList);
			GoalsInfoTable goalAux = new GoalsInfoTable();

			if (s.getPlan() != null) {
				goalAux.setPlanName(s.getPlan().getName());

				Long indicatorId = s.getParent();
				StructureLevelInstance indicator = indicatorId != null ? this.sbs.retrieveLevelInstance(indicatorId)
						: null;
				if (indicator != null) {
					goalAux.setIndicatorName(indicator.getName());
					Long objectiveId = indicator.getParent();
					StructureLevelInstance objective = objectiveId != null
							? this.sbs.retrieveLevelInstance(objectiveId)
							: null;
					if (objective != null) {
						goalAux.setObjectiveName(objective.getName());
						Long strategicAxisId = objective.getParent();
						StructureLevelInstance strategicAxis = strategicAxisId != null
								? this.sbs.retrieveLevelInstance(strategicAxisId)
								: null;
						if (strategicAxis != null) {
							goalAux.setStrategicAxisName(strategicAxis.getName());
						}
					}
				}
			}

			goalAux.setGoalName(s.getName());
			for (AttributeInstance a : this.sbs.listAttributeInstanceByLevel(s, false)) {
				Attribute attr = this.sbs.retrieveAttribute(a.getAttribute().getId());
				if (attr.isFinishDate() && s.getPlan() != null
						&& s.getPlan().getParent().getCompany().isShowMaturity())
					goalAux.setFinishDate(a.getValueAsDate());
				else if (attr.isReachedField())
					goalAux.setReached(a.getValueAsNumber());
				else if (attr.isExpectedField())
					goalAux.setExpected(a.getValueAsNumber());
			}

			if (validProgressStatus != null) {
				if (s.getProgressStatus() == validProgressStatus.getId()) {
					goalAux.setProgressStatus(s.getProgressStatus());
					goalsInfoTable.add(goalAux);
				} else {
					total--;
					continue;
				}
			} else {
				goalsInfoTable.add(goalAux);
			}
		}

		PaginatedList<GoalsInfoTable> result = new PaginatedList<>();
		result.setList(goalsInfoTable);
		result.setTotal(total);

		return result;
	}

}
