package org.forrisco.risk.incident;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IncidentBeanTest {
	private IncidentBean incident;

	@BeforeEach
	void setUp() {
		incident = new IncidentBean();
	}

	@Test
	@DisplayName("Validação da classe pelos métodos set e get.")
	void testIncidentBeanAttributes() {

		long id = 1L;
		long riskId = 100L;
		long unitId = 200L;
		String description = "Descrição de incidente";
		int type = 2;
		Date begin = new Date();

		incident.setId(id);
		incident.setRiskId(riskId);
		incident.setUnitId(unitId);
		incident.setDescription(description);
		incident.setType(type);
		incident.setBegin(begin);

		assertEquals(id, incident.getId(), "O valor de 'id' está incorreto.");
		assertEquals(riskId, incident.getRiskId(), "O valor de 'riskId' está incorreto.");
		assertEquals(unitId, incident.getUnitId(), "O valor de 'unitId' está incorreto.");
		assertEquals(description, incident.getDescription(), "O valor de 'description' está incorreto.");
		assertEquals(type, incident.getType(), "O valor de 'type' está incorreto.");
		assertEquals(begin, incident.getBegin(), "O valor de 'begin' está incorreto.");
	}

	@Test
	@DisplayName("Validação da criação da classe pelo seu construtor vazio")
	void testIncidentBeanEmptyConstructor() {
		IncidentBean incidentBean = new IncidentBean();

		assertNotNull(incidentBean, "O objeto não deveria ser nulo.");
	}

	@Test
	@DisplayName("Validação da criação da classe pelo construtor parametrizado.")
	void testIncidentBeanParameterizedConstructor() {

		long id = 1L;
		long riskId = 100L;
		long unitId = 200L;
		String description = "Descrição de incidente";
		int type = 2;
		Date begin = new Date();

		IncidentBean incident = new IncidentBean(id, riskId, unitId, description, type, begin);

		assertEquals(id, incident.getId(), "O valor de 'id' no construtor está incorreto.");
		assertEquals(riskId, incident.getRiskId(), "O valor de 'riskId' no construtor está incorreto.");
		assertEquals(unitId, incident.getUnitId(), "O valor de 'unitId' no construtor está incorreto.");
		assertEquals(description, incident.getDescription(), "O valor de 'description' no construtor está incorreto.");
		assertEquals(type, incident.getType(), "O valor de 'type' no construtor está incorreto.");
		assertEquals(begin, incident.getBegin(), "O valor de 'begin' no construtor está incorreto.");
	}
}