package org.forpdi.core.common;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public final class LikeCriterion implements Criterion {

	private final String propertyName;
	private String pattern;
	private boolean ignoreCase;
	
	public LikeCriterion(String propertyName, String pattern) {
		this.propertyName = propertyName;
		this.pattern = pattern;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		var path = (Path<String>) propertiesContext.getPath(propertyName);
		var pathAsString = path.as(String.class);
		if (ignoreCase) {
			return builder.like(builder.upper(pathAsString), pattern.toUpperCase());
		}
		return builder.like(pathAsString, pattern);
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public LikeCriterion ignoreCase() {
		this.ignoreCase = true;
		return this;
	}
}
