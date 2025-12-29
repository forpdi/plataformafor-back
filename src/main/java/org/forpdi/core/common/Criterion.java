package org.forpdi.core.common;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

public interface Criterion {
	Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext);
}
