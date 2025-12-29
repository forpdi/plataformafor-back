package org.forpdi.core.common;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Selection;

public final class SimplePropertyProjection extends PropertyProjection {

	public SimplePropertyProjection(String propertyName) {
		super(propertyName);
	}

	@Override
	public Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		return new Selection<?>[] { propertiesContext.getPath(getPropertyName()) };
	}

}
