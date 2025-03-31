package org.forrisco.risk;

public enum RiskOrganizationalLevel {
	OPERATIONAL(5, "Operacional"),
	TACTICAL(10, "Tático"),
	STRATEGIC(15, "Estratégico");

	private final int id;
	private final String label;
	
	private RiskOrganizationalLevel(int id, String label) {
		this.id = id;
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public static RiskOrganizationalLevel getById(int id) {
		for (RiskOrganizationalLevel organizationalLevel : values()) {
			if (organizationalLevel.id == id) {
				return organizationalLevel;
			}
		}
		
		throw new IllegalArgumentException("Unrecognized RiskOrganizationalLevel id: " + id);
	}
}
