package org.forrisco.risk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.links.RiskAxis;
import org.forrisco.risk.links.RiskGoal;
import org.forrisco.risk.links.RiskIndicator;
import org.forrisco.risk.links.RiskProcessObjective;
import org.forrisco.risk.links.RiskStrategy;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Service;

@Service
public class RiskFilterBS extends HibernateBusiness {

	public void applyFilters(RiskFilterParams filters, Criteria criteria, Criteria count) {
		if (filters.filteringByUnits())
			addUnitsFilter(filters, criteria, count);
		if (filters.filteringByTerm())
			addTermFilter(filters, criteria, count);
		if (filters.filteringByProcesses())
			addProcessesFilter(filters, criteria, count);
		if (filters.filteringByLinkedPdis())
			addLinkedPdisFilter(filters, criteria, count);
		if (filters.filteringByNameOrCode())
			addByNameOrCodeFilter(filters, criteria, count);
		if (filters.filteringByType())
			addByTypeFilter(filters, criteria, count);
		if (filters.filteringByTypologies())
			addByTypologiesFilter(filters, criteria, count);
		if (filters.filteringByResponses())
			addByResponsesFilter(filters, criteria, count);
		if (filters.filteringByLevels())
			addLevelsFilter(filters, criteria, count);
		if (filters.filteringByStartCreation())
			addStartCreationFilter(filters, criteria, count);
		if (filters.filteringByEndCreation())
			addEndCreationFilter(filters, criteria, count);
		if (filters.filteringByArchived())
			addArchivedFilter(filters, criteria, count);
	}

	private void addUnitsFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		Criterion unitFilter = Restrictions.in("unit.id", filters.getUnitIds());
		criteria.add(unitFilter);
		count.add(unitFilter);
	}

	private void addTermFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		Criterion name = Restrictions.like("name", "%" + filters.getTerm() + "%").ignoreCase();
		Criterion manager = Restrictions.like("manager.name", "%" + filters.getTerm() + "%").ignoreCase();
		Criterion responsible = Restrictions.like("user.name", "%" + filters.getTerm() + "%").ignoreCase();

		List<Integer> responseIds = RiskResponse.getIdsByName(filters.getTerm());
		Criterion response = Restrictions.in("response", responseIds);

		Criterion termFilter = Restrictions.or(name, manager, responsible, response);
		criteria.add(termFilter);
		count.add(termFilter);
	}

	private void addProcessesFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		List<Long> riskIds = new ArrayList<>();
		riskIds.addAll(listRiskIdsLinked(filters.getProcessesId(), "process", RiskActivity.class));
		riskIds.addAll(listRiskIdsLinkedToProcessObjectives(filters.getProcessesId()));

		addRiskIdFilter(criteria, count, riskIds);
	}

	private void addLinkedPdisFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		List<Long> riskIds = new ArrayList<>();
		riskIds.addAll(listRiskIdsLinked(filters.getLinkedPdiIds(), "structure", RiskAxis.class));
		riskIds.addAll(listRiskIdsLinked(filters.getLinkedPdiIds(), "structure", RiskStrategy.class));
		riskIds.addAll(listRiskIdsLinked(filters.getLinkedPdiIds(), "structure", RiskIndicator.class));
		riskIds.addAll(listRiskIdsLinked(filters.getLinkedPdiIds(), "structure", RiskGoal.class));

		addRiskIdFilter(criteria, count, riskIds);
	}

	private void addRiskIdFilter(Criteria criteria, Criteria count, List<Long> riskIds) {
		Criterion riskIdFilter = Restrictions.in("id", riskIds);
		criteria.add(riskIdFilter);
		count.add(riskIdFilter);
	}

	private void addByNameOrCodeFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		Criterion name = Restrictions.like("name", "%" + filters.getNameOrCode() + "%").ignoreCase();
		Criterion code = Restrictions.like("code", "%" + filters.getNameOrCode() + "%").ignoreCase();

		criteria.add(Restrictions.or(name, code));
		count.add(Restrictions.or(name, code));
	}

	private void addByTypeFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		Criterion typeFilter = Restrictions.eq("type", filters.getType());
		criteria.add(typeFilter);
		count.add(typeFilter);
	}

	private void addByTypologiesFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		List<String> typologies = filters.getTypologies();
		List<Criterion> criterions = new ArrayList<>(typologies.size());
		for (String typology : typologies) {
			criterions.add(Restrictions.like("tipology", "%" + typology + "%"));
		}

		Criterion[] criterionsArray = criterions.toArray(new Criterion[0]);
		criteria.add(Restrictions.or(criterionsArray));
		count.add(Restrictions.or(criterionsArray));
	}

	private void addByResponsesFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		List<Criterion> criterions = new ArrayList<>(2);
		if (filters.filteringByNoneResponse()) {
			criterions.add(Restrictions.isNull("response"));
		}

		criterions.add(Restrictions.in("response", filters.getResponses()));

		Criterion[] criterionsArray = criterions.toArray(new Criterion[] {});
		criteria.add(Restrictions.or(criterionsArray));
		count.add(Restrictions.or(criterionsArray));
	}

	private void addLevelsFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		List<Criterion> criterions = new ArrayList<>(2);
		if (filters.filteringByNoneLevels()) {
			criterions.add(Restrictions.isNull("level"));
		}
		criterions.add(Restrictions.in("level", filters.getLevels()));

		Criterion[] criterionsArray = criterions.toArray(new Criterion[] {});
		criteria.add(Restrictions.or(criterionsArray));
		count.add(Restrictions.or(criterionsArray));
	}

	private void addStartCreationFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		criteria.add(Restrictions.ge("begin", filters.getStartCreation()));
		count.add(Restrictions.ge("begin", filters.getStartCreation()));
	}

	private void addEndCreationFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		criteria.add(Restrictions.le("begin", filters.getEndCreation()));
		count.add(Restrictions.le("begin", filters.getEndCreation()));
	}

	private void addArchivedFilter(RiskFilterParams filters, Criteria criteria, Criteria count) {
		criteria.add(Restrictions.eq("archived", filters.getArchived()));
		count.add(Restrictions.eq("archived", filters.getArchived()));
	}

	public <E extends Serializable> List<Long> listRiskIdsLinked(List<Long> linkedIds, String linkedColumn,
			Class<E> linkedClass) {
		Criteria criteria;

		criteria = this.dao.newCriteria(linkedClass);

		criteria.add(Restrictions.eq("deleted", false))
				.createAlias(linkedColumn, linkedColumn, JoinType.INNER_JOIN)
				.createAlias("risk", "risk", JoinType.INNER_JOIN)
				.add(Restrictions.in(linkedColumn + ".id", linkedIds))
				.setProjection(Projections.property("risk.id"));

		List<Long> riskIds = this.dao.findByCriteria(criteria, Long.class);
		return riskIds;
	}

	public <E extends Serializable> List<Long> listRiskIdsLinkedToProcessObjectives(List<Long> linkedIds) {
		Criteria criteria;

		criteria = this.dao.newCriteria(RiskProcessObjective.class);

		criteria.add(Restrictions.eq("deleted", false))
				.createAlias("processObjective", "processObjective", JoinType.INNER_JOIN)
				.createAlias("processObjective.process", "process", JoinType.INNER_JOIN)
				.createAlias("risk", "risk", JoinType.INNER_JOIN)
				.add(Restrictions.in("process.id", linkedIds))
				.setProjection(Projections.property("risk.id"));

		List<Long> riskIds = this.dao.findByCriteria(criteria, Long.class);
		return riskIds;
	}
}
