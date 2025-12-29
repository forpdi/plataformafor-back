package org.forpdi.planning.structure;

import org.forpdi.planning.attribute.AttributeInstance;

public enum Polarity {
	BIGGER_BETTER("Maior-melhor", ">"),
	SMALLER_BETTER("Menor-melhor", "<");
	
	private String value;
	private String operator;

	private Polarity(String value, String operator) {
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getOperator() {
		return operator;
	}
	
	public String getReverseOperator() {
		return operator.equals(">") ? "<" : ">";
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public static Polarity getByValue(String value) {
		for (Polarity polarity : values()) {
			if (polarity.value.equals(value)) {
				return polarity;
			}
		}
		
		throw new IllegalArgumentException("Invalid polarity: " + value);
	}
	

	public static boolean polarityComparison(AttributeInstance polarity, Double x, Double y) {
		if (x == null || y == null)
			return false;

		if (polarity == null || polarity.getValue().equals(Polarity.BIGGER_BETTER.getValue())) {
			return x > y;
		} else if (polarity.getValue().equals(Polarity.SMALLER_BETTER.getValue())) {
			return x < y;
		}

		throw new IllegalArgumentException("Polaridade inválida: " + polarity.getValue());
	}
}
