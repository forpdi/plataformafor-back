package org.forpdi.core.common;

import java.io.Serializable;

public class Order implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean ascending;
	private boolean ignoreCase;
	private String propertyName;

	public static Order asc(String propertyName) {
		return new Order( propertyName, true );
	}

	public static Order desc(String propertyName) {
		return new Order( propertyName, false );
	}

	protected Order(String propertyName, boolean ascending) {
		this.propertyName = propertyName;
		this.ascending = ascending;
	}

	public Order ignoreCase() {
		ignoreCase = true;
		return this;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public boolean isAscending() {
		return ascending;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}
}
