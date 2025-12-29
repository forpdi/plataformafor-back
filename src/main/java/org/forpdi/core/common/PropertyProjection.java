package org.forpdi.core.common;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Selection;

public abstract class PropertyProjection implements Projection {
	private String propertyName;

	public PropertyProjection(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public Selection<?> getSingleSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		Selection<?>[] selectionArray = getSelection(builder, propertiesContext);
		
		return selectionArray[0];
	}
}
