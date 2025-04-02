package org.forpdi.core.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;


public final class ProjectionList implements Projection {
	private Map<String, PropertyProjection> projectionsMap = new LinkedHashMap<>();

	public ProjectionList add(PropertyProjection projection, String alias) {
		projectionsMap.put(alias, projection);
		
		return this;
	}
	
	public Collection<PropertyProjection> listProperties() {
		return projectionsMap.values();
	}
	
	@Override
	public Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
		List<Selection<?>> selections = new ArrayList<>(projectionsMap.size());
		for (var projection : projectionsMap.values()) {
			selections.add(projection.getSingleSelection(builder, propertiesContext));
		}
		
		return selections.toArray(new Selection<?>[selections.size()]);
	}
}
