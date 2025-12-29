package org.forrisco.core.item;

public enum FieldType {

    NO_TYPE(0, "SEM INFORMAÇÃO"),
	FILE(1, "ARQUIVO"),
	TEXT(2, "TEXTO");

	private Integer value;
	private String name;

	private FieldType(Integer value, String name){
		this.value = value;
		this.name = name;
	}
	
	static public FieldType getPeriodicityByName(String name) {
		for (FieldType periodicity : values()) {
			if (periodicity.getName().equals(name)) {
				return periodicity;
			}
		}
		throw new IllegalArgumentException("Invalid file type name: " + name);
	}
	
	public Integer getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
}
