package org.forrisco.risk;

public enum RiskState {
	UP_TO_DATE(0),
	CLOSE_TO_EXPIRE(1),
	LATE(2);
	
	private int id;
	
	private RiskState(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public static RiskState getById(int id) {
		for (RiskState riskState : values()) {
			if (riskState.id == id) {
				return riskState;
			}
		}
		throw new IllegalArgumentException("Unrecognized RiskState id: " + id);
	}
}
