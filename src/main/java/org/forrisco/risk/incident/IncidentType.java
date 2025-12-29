package org.forrisco.risk.incident;

public enum IncidentType {
	AMEACA (1, "Ameaça"),
	OPORTUNIDADE (2, "Oportunidade");
	
	private int typeId;
	private String label;
	
	public int getTypeId() {
		return typeId;
	}

	public String getLabel() {
		return label;
	}

	private IncidentType(int id, String label) {
		this.typeId = id;
		this.label = label;
	}
	
	public static IncidentType getById(int id) {
		for (IncidentType type : IncidentType.values()) {
			if (type.typeId == id) {
				return type;
			}
		}
		return null;
	}
}
