package org.forpdi.planning.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.system.CriteriaCompanyFilter;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskGoal;
import org.forrisco.risk.links.RiskIndicator;
import org.forrisco.risk.links.RiskStrategy;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructureInstanceFilterBS extends HibernateBusiness {

	@Autowired
	private StructureBS structureBS;
	@Autowired
	private CriteriaCompanyFilter filter;
	
	public PaginatedList<StructureLevelInstance> filterObjectives(List<Long> excludedIds, int page,
			int pageSize, String term) {

		return filterStructureLevelInstances(excludedIds, page, pageSize, term, "objective");
	}
	
	public PaginatedList<StructureLevelInstance> filterIndicators(List<Long> selectedIds, Integer page, Integer pageSize, String term) {
		return filterStructureLevelInstances(selectedIds, page, pageSize, term, "indicator");		
	}
	
	public PaginatedList<StructureLevelInstance> filterGoals(List<Long> selectedIds, Integer page, Integer pageSize, String term) {
		return filterStructureLevelInstances(selectedIds, page, pageSize, term, "goal");		
	}

	private PaginatedList<StructureLevelInstance> filterStructureLevelInstances(List<Long> excludedIds, int page,
			int pageSize, String term, String structureLevelName) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		
		Criteria criteria = structureBS.createLevelInstanceFilterCriteria(structureLevelName);
		Criteria count = structureBS.createLevelInstanceFilterCriteria(structureLevelName);
		
		if (excludedIds != null) {
			criteria.add(Restrictions.not(Restrictions.in("id", excludedIds)));
			count.add(Restrictions.not(Restrictions.in("id", excludedIds)));
		}
		
		if (term != null) {
			criteria.add(Restrictions.like("name", "%" + term + "%").ignoreCase());
			count.add(Restrictions.like("name", "%" + term + "%").ignoreCase());
		}
		
		criteria.setFirstResult((page-1) * pageSize)
		.setMaxResults(pageSize);
		count.setProjection(Projections.countDistinct("id"));
		
		List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");

		result.setList(list);
		result.setTotal((Long) this.filter.filterAndFind(count, "macro.company"));
		return result;
	}

	public PaginatedList<StructureLevelInstance> filterAxes(List<Long> selectedIds, Integer page, Integer pageSize, String term) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		
		if (page == null) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = 5;
		}
		
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("sequence", 0));
		criteria.add(Restrictions.eq("objective", false));
		criteria.add(Restrictions.eq("indicator", false));
		criteria.add(Restrictions.eq("goal", false));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);

		List<StructureLevelInstance> list = new ArrayList<>();
		
		Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class)
			.createAlias("plan", "plan", JoinType.INNER_JOIN)
			.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.in("level", levelList))
			.add(Restrictions.eq("aggregate", false))
			.add(Restrictions.eq("macro.archived", false));
		
		Criteria count = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("plan", "plan", JoinType.INNER_JOIN)
				.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("level", levelList))
				.add(Restrictions.eq("aggregate", false))
				.add(Restrictions.eq("macro.archived", false))
				.setProjection(Projections.countDistinct("id"));
		
		if (selectedIds != null) {
			criteria2.add(Restrictions.not(Restrictions.in("id", selectedIds)));
			count.add(Restrictions.not(Restrictions.in("id", selectedIds)));
		}
		
		if (term != null) {
			criteria2.add(Restrictions.like("name", "%" + term + "%").ignoreCase());
			count.add(Restrictions.like("name", "%" + term + "%").ignoreCase());
		}
		
		criteria2.setFirstResult((page-1) * pageSize)
		.setMaxResults(pageSize);
		
		list.addAll(this.filter.filterAndList(criteria2, StructureLevelInstance.class, "macro.company"));

		result.setList(list);
		result.setTotal((Long) this.filter.filterAndFind(count, "macro.company"));

		return result;
	}

	public List<StructureLevelInstance> listPdiLinkedToRisks(Unit unit) {
		Set<Long> linkedPdiIds = new HashSet<>();
		linkedPdiIds.addAll(this.listPdiIdsLinkedToRisksByUnitId(unit.getId(), RiskAxis.class));
		linkedPdiIds.addAll(this.listPdiIdsLinkedToRisksByUnitId(unit.getId(), RiskStrategy.class));
		linkedPdiIds.addAll(this.listPdiIdsLinkedToRisksByUnitId(unit.getId(), RiskIndicator.class));
		linkedPdiIds.addAll(this.listPdiIdsLinkedToRisksByUnitId(unit.getId(), RiskGoal.class));

        if (linkedPdiIds.isEmpty()) {
        	return Collections.emptyList();
        }
        
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("id", linkedPdiIds));
        
		return this.dao.findByCriteria(criteria, StructureLevelInstance.class);
	}

	private <E extends Serializable> List<Long> listPdiIdsLinkedToRisksByUnitId(Long unitId, Class<E> targetClass) {
	    Criteria criteria;

        criteria = this.dao.newCriteria(targetClass);

	    criteria.add(Restrictions.eq("deleted", false))
	            .createAlias("structure", "structure", JoinType.INNER_JOIN)
	            .createAlias("risk", "risk", JoinType.INNER_JOIN)
	            .createAlias("risk.unit", "unit", JoinType.INNER_JOIN)
	            .add(Restrictions.eq("unit.id", unitId));

	    criteria.setProjection(Projections.property("structure.id"));
	    
	    List<Long> linkedPdiIds = this.dao.findByCriteria(criteria, Long.class);

	    return linkedPdiIds;
	}
}
