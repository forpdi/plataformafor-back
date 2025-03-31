package org.forpdi.core.common;

public abstract class AggregateProjection extends PropertyProjection {

	private final Class<?> resultClass;
	
	public AggregateProjection(String propertyName) {
		this(propertyName, Object.class);
	}

	public AggregateProjection(String propertyName, Class<?> resultClass) {
		super(propertyName);
		this.resultClass = resultClass;
	}

	public final Class<?> getResultClass() {
		return resultClass;
	}
}
