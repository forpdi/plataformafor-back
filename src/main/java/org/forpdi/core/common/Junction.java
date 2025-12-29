package org.forpdi.core.common;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

public abstract class Junction implements Criterion {

	private final List<Criterion> criterions;
	
	public Junction(List<Criterion> criterions) {
		this.criterions = criterions;
	}

	public Junction add(Criterion criterion) {
		criterions.add(criterion);
		return this;
	}

	public List<Criterion> getCriterions() {
		return criterions;
	}
	
	protected Predicate[] getGroupedPredicates(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		Predicate[] predicates = new Predicate[criterions.size()];
		for (int i = 0; i < criterions.size(); i++) {
			predicates[i] = criterions.get(i).getPredicate(builder, propertiesContext);
		}

		return predicates;
	}
}
