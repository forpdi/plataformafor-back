package org.forpdi.core.common;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

public final class SimplePropertyProjection extends PropertyProjection {

	public SimplePropertyProjection(String propertyName) {
		super(propertyName);
	}

	@Override
	public Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		return new Selection<?>[] { propertiesContext.getPath(getPropertyName()) };
	}

}
