package org.forpdi.planning.structure;


public enum GoalProgressStatus {
	MINIMUM_BELOW(1, "Abaixo do mínimo"),
	MINIMUM(2, "Abaixo do esperado"),
	ENOUGH_UP(3, "Suficiente"),
	MAXIMUM_UP(4, "Acima do máximo"),
	NOT_STARTED(5, "Não iniciado"),
	NOT_FILLED(6, "Não preenchido");

	private int id;
	private String label;

	private GoalProgressStatus(int id, String label) {
		this.id = id;
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public static GoalProgressStatus getById(int id) {
		for (GoalProgressStatus goalProgressStatus : values()) {
			if (goalProgressStatus.getId() == id) {
				return goalProgressStatus;
			}
		}
		
		throw new IllegalArgumentException("Status de progresso inválido: " + id);
	}
}
