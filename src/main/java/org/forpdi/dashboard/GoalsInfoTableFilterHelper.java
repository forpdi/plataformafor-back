package org.forpdi.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.forpdi.dashboard.GoalsParams.GoalsParamsBuilder;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.PolarityMapHelper;
import org.forpdi.planning.filters.PeformanceFilterType;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.Polarity;
import org.forpdi.planning.structure.StructureInstanceAttributeHelper;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoalsInfoTableFilterHelper {
	@Autowired
	private StructureInstanceAttributeHelper structureInstanceAttributeHelper;

	@Autowired
	private PolarityMapHelper polarityMapHelper;
	
	@Autowired
	private EntityManager entityManager;

	/**
	 * @param idsSet conjunto com os ids dos level instances que serão processados
	 * @param macro plano macro pra filtro
	 * @param plan plano pra filtro
	 * @param indicator indicador pra filtro
	 * @param type tipo de performance pra filtro
	 * @return lista de metas filtradas
	 */
	public List<StructureLevelInstance> goalsFilter(Set<Long> idsSet, PlanMacro macro, Plan plan,
			StructureLevelInstance indicator, PeformanceFilterType type) {
		CriteriaQuery<Object[]> criteria = createCriteria(idsSet, macro, plan, indicator);

		List<Object[]> list = entityManager.createQuery(criteria).getResultList();
		List<StructureLevelInstance> goals = extractGoals(list);
		
		structureInstanceAttributeHelper.setAttributes(goals);
		
		List<StructureLevelInstance> goalsFiltered = getFilteredGoals(type, goals);
		return goalsFiltered;
	}

	private CriteriaQuery<Object[]> createCriteria(Set<Long> idsSet, PlanMacro macro, Plan plan, StructureLevelInstance indicator) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<AttributeInstance> root = cq.from(AttributeInstance.class);
		
		cq.multiselect(
				root.get("levelInstance"),
				root.get("levelInstance").get("id"));
		
		List<Predicate> predicates = new ArrayList<>();
		
		predicates.add(root.get("levelInstance").get("id").in(idsSet));
		predicates.add(cb.equal(root.get("levelInstance").get("level").get("goal"), true));
		predicates.add(cb.equal(root.get("deleted"), false));
		if (macro != null) {
			predicates.add(cb.equal(root.get("levelInstance").get("plan").get("parent"), macro));
		}
		if (plan != null) {
			predicates.add(cb.equal(root.get("levelInstance").get("plan"), plan));
		}
		if (indicator != null) {
			predicates.add(cb.equal(root.get("levelInstance").get("parent"), indicator.getId()));
		}
		
		cq.where(predicates.toArray(new Predicate[] {}));
		
		cq.groupBy(root.get("levelInstance").get("id"));
		
		return cq;
	}
	
	private List<StructureLevelInstance> extractGoals(List<Object[]> list) {
		List<StructureLevelInstance> goals = new ArrayList<>(list.size());
		for (Object[] object : list) {
			StructureLevelInstance structureLevelInstance = (StructureLevelInstance) object[0];
			goals.add(structureLevelInstance);
		}
		return goals;
	}
	
	private List<StructureLevelInstance> getFilteredGoals(PeformanceFilterType type,
			List<StructureLevelInstance> goals) {
		List<StructureLevelInstance> filteredGoals;
		if (type == null) {
			filteredGoals = goals;
		} else {
			filteredGoals = new LinkedList<>();
			Map<Long, AttributeInstance> polarityMap = polarityMapHelper.generatePolarityMap(goals);
			for (StructureLevelInstance goal : goals) {
				GoalsParams goalsParams = getGoalsParams(goal);
				AttributeInstance polarity = polarityMap.get(goal.getId());
				if (goalShouldBeFiltered(goalsParams, type, polarity)) {
					filteredGoals.add(goal);
				}
			}
		}
		return filteredGoals;
	}

	private GoalsParams getGoalsParams(StructureLevelInstance goal) {
		GoalsParamsBuilder goalsParamsBuilder = new GoalsParamsBuilder();
		for (AttributeInstance attr : goal.getAttributeInstanceList()) {
			if (attr.getAttribute().isExpectedField()) {
				goalsParamsBuilder.exp(attr.getValueAsNumber());
			} else if (attr.getAttribute().isMaximumField()) {
				goalsParamsBuilder.max(attr.getValueAsNumber());
			} else if (attr.getAttribute().isMinimumField()) {
				goalsParamsBuilder.min(attr.getValueAsNumber());
			} else if (attr.getAttribute().isReachedField()) {
				goalsParamsBuilder.reach(attr.getValueAsNumber());
			} else if (attr.getAttribute().isFinishDate()) {
				goalsParamsBuilder.finish(attr.getValueAsDate());
			}
		}
		GoalsParams goalsParams = goalsParamsBuilder.create();
		return goalsParams;
	}
	
	private boolean goalShouldBeFiltered(GoalsParams goalsParams, PeformanceFilterType type, AttributeInstance polarity) {
		switch (type) {
			case BELOW_MINIMUM:
				return isBelowMinimum(polarity, goalsParams);
			case BELOW_EXPECTED:
				return isBelowExpected(polarity, goalsParams);
			case ENOUGH:
				return isEnough(polarity, goalsParams);
			case ABOVE_MAXIMUM:
				return isAboveMaximum(polarity, goalsParams);
			case NOT_STARTED:
				return isNotStarted(goalsParams);
			default:
				return true;
		}
	}

	private boolean isBelowMinimum(AttributeInstance polarity, GoalsParams goalsParams) {
		Date today = new Date();
		Double min = goalsParams.getMin();
		Double reach = goalsParams.getReach();
		Date finish = goalsParams.getFinish();
		return (reach == null && finish != null && finish.before(today))
				|| (reach != null && min != null && Polarity.polarityComparison(polarity, min, reach));
	}
	
	private boolean isBelowExpected(AttributeInstance polarity, GoalsParams goalsParams) {
		Double min = goalsParams.getMin();
		Double exp = goalsParams.getExp();
		Double reach = goalsParams.getReach();
		return reach != null && Polarity.polarityComparison(polarity, exp, reach)
				&& (Polarity.polarityComparison(polarity, reach, min) || Double.compare(reach, min) == 0);
	}

	private boolean isEnough(AttributeInstance polarity, GoalsParams goalsParams) {
		Double max = goalsParams.getMax();
		Double exp = goalsParams.getExp();
		Double reach = goalsParams.getReach();
		return (Polarity.polarityComparison(polarity, max, reach)
				&& (Polarity.polarityComparison(polarity, reach, exp) || Double.compare(reach, exp) == 0))
				|| (reach != null && max != null && (Double.compare(reach, exp) == 0));
	}

	private boolean isAboveMaximum(AttributeInstance polarity, GoalsParams goalsParams) {
		Double max = goalsParams.getMax();
		Double reach = goalsParams.getReach();
		return Polarity.polarityComparison(polarity, reach, max)
				|| (reach != null && max != null && (Double.compare(reach, max) == 0));
	}

	private boolean isNotStarted(GoalsParams goalsParams) {
		Date today = new Date();
		Double max = goalsParams.getMax();
		Double min = goalsParams.getMin();
		Double exp = goalsParams.getExp();
		Double reach = goalsParams.getReach();
		Date finish = goalsParams.getFinish();
		return reach == null && ((min == null && exp == null && max == null) || (finish != null && finish.after(today)));
	}

}
