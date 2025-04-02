package org.forpdi.core.company;

public enum CompanyType {
	UNIVERSITY(5, "Universidade"),
	INSTITUTE(10, "Instituto Federal"),
	OTHER(15, "Outros");

	private final int id;
	private final String label;
	
	private CompanyType(int id, String label) {
		this.id = id;
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
}
