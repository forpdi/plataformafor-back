package org.forpdi.core.common;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

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
