package org.forpdi.core.common;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

public final class Disjunction extends Junction {

	public Disjunction(Criterion... criterions) {
		super(List.of(criterions));
	}
	
	public Disjunction() {
		super(new ArrayList<>());
	}

	@Override
	public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		return builder.or(getGroupedPredicates(builder, propertiesContext));
	}
}
