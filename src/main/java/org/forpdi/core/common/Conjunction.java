package org.forpdi.core.common;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

public final class Conjunction extends Junction {

	public Conjunction(Criterion... criterions) {
		super(List.of(criterions));
	}
	
	public Conjunction() {
		super(new ArrayList<>());
	}

	@Override
	public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		return builder.and(getGroupedPredicates(builder, propertiesContext));
	}
}
