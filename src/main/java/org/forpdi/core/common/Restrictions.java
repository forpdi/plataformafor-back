package org.forpdi.core.common;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public final class Restrictions {

	private Restrictions() {
	}
	
	public static Criterion eq(String propertyName, Object value) {
		return new Criterion() {
			
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return builder.equal(propertiesContext.getPath(propertyName), value);
			}
		}; 
	}
	
	public static Criterion ne(String propertyName, Object value) {
		return new Criterion() {
			
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return builder.notEqual(propertiesContext.getPath(propertyName), value);
			}
		}; 
	}
	
	public static <Y extends Comparable<? super Y>> Criterion gt(String propertyName, Y value) {
		return new Criterion() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				var rootExpression = (Expression<Y>) propertiesContext.getPath(propertyName);
				return builder.greaterThan(rootExpression, value);
			}
		}; 
	}

	public static <Y extends Comparable<? super Y>> Criterion ge(String propertyName, Y value) {
		return new Criterion() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				var rootExpression = (Expression<Y>) propertiesContext.getPath(propertyName);
				return builder.greaterThanOrEqualTo(rootExpression, value);
			}
		}; 
	}

	public static <Y extends Comparable<? super Y>> Criterion lt(String propertyName, Y value) {
		return new Criterion() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				var rootExpression = (Expression<Y>) propertiesContext.getPath(propertyName);
				return builder.lessThan(rootExpression, value);
			}
		}; 
	}

	public static <Y extends Comparable<? super Y>> Criterion le(String propertyName, Y value) {
		return new Criterion() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				var rootExpression = (Expression<Y>) propertiesContext.getPath(propertyName);
				return builder.lessThanOrEqualTo(rootExpression, value);
			}
		}; 
	}

	public static Criterion in(String propertyName, Collection<?> values) {
		return new Criterion() {
			
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				Expression<?> rootExpression = propertiesContext.getPath(propertyName);
				return rootExpression.in(values);
			}
		}; 
	}

	public static Criterion isNull(String propertyName) {
		return new Criterion() {
			
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return builder.isNull(propertiesContext.getPath(propertyName));
			}
		}; 
	}

	public static Criterion isNotNull(String propertyName) {
		return new Criterion() {
			
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return builder.isNotNull(propertiesContext.getPath(propertyName));
			}
		}; 
	}

	public static <Y extends Comparable<? super Y>> Criterion between(String propertyName, Y low, Y high) {
		return new Criterion() {
			
			@SuppressWarnings("unchecked")
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				var rootExpression = (Expression<Y>) propertiesContext.getPath(propertyName);
				return builder.between(rootExpression, low, high);
			}
		}; 
	}

	public static Criterion not(Criterion criterion) {
		return new Criterion() {
			
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return builder.not(criterion.getPredicate(builder, propertiesContext));
			}
		}; 
	}

	public static LikeCriterion like(String propertyName, String pattern) {
		return new LikeCriterion(propertyName, pattern);
	}

	public static Disjunction or(Criterion... criterions) {
		return new Disjunction(criterions);
	}

	public static Disjunction disjunction() {
		return new Disjunction();
	}

	public static Conjunction and(Criterion... criterions) {
		return new Conjunction(criterions);
	}
	
	public static Conjunction conjunction() {
		return new Conjunction();
	}
}
