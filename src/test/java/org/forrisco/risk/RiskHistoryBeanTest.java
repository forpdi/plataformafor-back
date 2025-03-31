package org.forrisco.risk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RiskHistoryBeanTest {
	private RiskHistoryBean riskHistory;

	@BeforeEach
	@DisplayName("Inicializa o objeto RiskHistoryBean antes de cada teste")
	void setUp() {
		riskHistory = new RiskHistoryBean();
	}

	@Test
	@DisplayName("Teste dos métodos get e set de todos os atributos")
	void testGettersAndSetters() {
		long id = 1L;
		long riskLevelId = 101L;
		long unitId = 1001L;
		int month = 7;
		int year = 2023;
		boolean threat = true;
		int quantity = 5;

		riskHistory.setId(id);
		riskHistory.setRiskLevelId(riskLevelId);
		riskHistory.setUnitId(unitId);
		riskHistory.setMonth(month);
		riskHistory.setYear(year);
		riskHistory.setThreat(threat);
		riskHistory.setQuantity(quantity);

		assertEquals(id, riskHistory.getId(), "O valor de 'id' está incorreto.");
		assertEquals(riskLevelId, riskHistory.getRiskLevelId(), "O valor de 'riskLevelId' está incorreto.");
		assertEquals(unitId, riskHistory.getUnitId(), "O valor de 'unitId' está incorreto.");
		assertEquals(month, riskHistory.getMonth(), "O valor de 'month' está incorreto.");
		assertEquals(year, riskHistory.getYear(), "O valor de 'year' está incorreto.");
		assertEquals(threat, riskHistory.isThreat(), "O valor de 'threat' está incorreto.");
		assertEquals(quantity, riskHistory.getQuantity(), "O valor de 'quantity' está incorreto.");
	}

	@Test
	@DisplayName("Teste do construtor parametrizado")
	void testParameterizedConstructor() {

		long id = 2L;
		long riskLevelId = 102L;
		long unitId = 1002L;
		int month = 8;
		int year = 2024;
		boolean threat = false;
		int quantity = 10;

		RiskHistoryBean riskHistory = new RiskHistoryBean(id, riskLevelId, unitId, month, year, threat, quantity);

		assertEquals(id, riskHistory.getId(), "O valor de 'id' no construtor está incorreto.");
		assertEquals(riskLevelId, riskHistory.getRiskLevelId(), "O valor de 'riskLevelId' no construtor está incorreto.");
		assertEquals(unitId, riskHistory.getUnitId(), "O valor de 'unitId' no construtor está incorreto.");
		assertEquals(month, riskHistory.getMonth(), "O valor de 'month' no construtor está incorreto.");
		assertEquals(year, riskHistory.getYear(), "O valor de 'year' no construtor está incorreto.");
		assertEquals(threat, riskHistory.isThreat(), "O valor de 'threat' no construtor está incorreto.");
		assertEquals(quantity, riskHistory.getQuantity(), "O valor de 'quantity' no construtor está incorreto.");
	}

	@Test
	@DisplayName("Teste do construtor padrão")
	void testDefaultConstructor() {
		RiskHistoryBean riskHistory = new RiskHistoryBean();

		assertEquals(0L, riskHistory.getId(), "O valor padrão de 'id' deve ser 0.");
		assertEquals(0L, riskHistory.getRiskLevelId(), "O valor padrão de 'riskLevelId' deve ser 0.");
		assertEquals(0L, riskHistory.getUnitId(), "O valor padrão de 'unitId' deve ser 0.");
		assertEquals(0, riskHistory.getMonth(), "O valor padrão de 'month' deve ser 0.");
		assertEquals(0, riskHistory.getYear(), "O valor padrão de 'year' deve ser 0.");
		assertFalse(riskHistory.isThreat(), "O valor padrão de 'threat' deve ser false.");
		assertEquals(0, riskHistory.getQuantity(), "O valor padrão de 'quantity' deve ser 0.");
	}
}