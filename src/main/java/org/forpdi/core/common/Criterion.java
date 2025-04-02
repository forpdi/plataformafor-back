package org.forpdi.core.common;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

public interface Criterion {
	Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext);
}
