package org.forrisco.risk;

import java.util.ArrayList;
import java.util.List;

public enum RiskResponse {
	ACCEPT(5, "Aceitar"),
	MITIGATE(10, "Mitigar"),
	SHARE(15, "Compartilhar"),
	AVOID(20, "Evitar");

	private final int id;
	private final String label;
	
	private RiskResponse(int id, String label) {
		this.id = id;
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public static RiskResponse GetById(int id) {
		for (RiskResponse riskResponse : values()) {
			if (riskResponse.id == id) {
				return riskResponse;
			}
		}
		
		throw new IllegalArgumentException("Unrecognized RiskResponse id: " + id);
	}

	public static List<Integer> getIdsByName(String label) {
		List<Integer> responses = new ArrayList<>();

		if(label == null) {
			throw new IllegalArgumentException("Empty RiskResponse label: " + label);
		}

		for (RiskResponse riskResponse : values()) {
			if (riskResponse.label.toLowerCase().contains(label.toLowerCase())) {
				responses.add(riskResponse.id);
			}
		}

		return responses;
	} 
}
