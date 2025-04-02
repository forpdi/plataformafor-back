package org.forpdi.core.common;

import java.math.BigDecimal;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

public final class Projections {

	private Projections() {
	}
	
	public static SimplePropertyProjection property(String propertyName) {
		return new SimplePropertyProjection(propertyName);
	}
	
	public static AggregateProjection countDistinct(String propertyName) {
		return new AggregateProjection(propertyName, Long.class) {
			
			@Override
			public Selection<?>[] getSelection(CriteriaBuilder builder,
					CriteriaPropertiesContext propertiesContext) {
				Path<?> path = propertiesContext.getPath(propertyName);
				return new Selection<?>[] { builder.countDistinct(path) };
			}
		};
	}

	public static AggregateProjection sum(String propertyName) {
		return new AggregateProjection(propertyName) {
			
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Selection<?>[] getSelection(CriteriaBuilder builder,
					CriteriaPropertiesContext propertiesContext) {
				Path path = propertiesContext.getPath(propertyName);
				return new Selection<?>[] { builder.sum(path) };
			}
		};
	}
	
	public static AggregateProjection avg(String propertyName) {
		return new AggregateProjection(propertyName, BigDecimal.class) {
			
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Selection<?>[] getSelection(CriteriaBuilder builder,
					CriteriaPropertiesContext propertiesContext) {
				Path path = propertiesContext.getPath(propertyName);
				return new Selection<?>[] { builder.avg(path) };
			}
		};
	}

	public static AggregateProjection max(String propertyName) {
		return new AggregateProjection(propertyName) {
			
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Selection<?>[] getSelection(CriteriaBuilder builder,
					CriteriaPropertiesContext propertiesContext) {
				Path path = propertiesContext.getPath(propertyName);
				return new Selection<?>[] { builder.max(path) };
			}
		};
	}

	public static AggregateProjection min(String propertyName) {
		return new AggregateProjection(propertyName) {
			
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Selection<?>[] getSelection(CriteriaBuilder builder,
					CriteriaPropertiesContext propertiesContext) {
				Path path = propertiesContext.getPath(propertyName);
				return new Selection<?>[] { builder.min(path) };
			}
		};
	}

	public static ProjectionList projectionList() {
		return new ProjectionList();
	}
}
