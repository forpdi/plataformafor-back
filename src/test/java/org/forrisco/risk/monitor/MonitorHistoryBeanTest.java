package org.forrisco.risk.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MonitorHistoryBeanTest {
	private MonitorHistoryBean monitorHistory;

	@BeforeEach
	@DisplayName("Inicializa o objeto MonitorHistoryBean antes de cada teste")
	void setUp() {
		monitorHistory = new MonitorHistoryBean();
	}

	@Test
	@DisplayName("Teste dos métodos get e set de todos os atributos")
	void testGettersAndSetters() {
		long id = 1L;
		long unitId = 100L;
		String estado = "Em andamento";
		int month = 5;
		int year = 2024;
		int quantity = 10;

		monitorHistory.setId(id);
		monitorHistory.setUnitId(unitId);
		monitorHistory.setEstado(estado);
		monitorHistory.setMonth(month);
		monitorHistory.setYear(year);
		monitorHistory.setQuantity(quantity);

		assertEquals(id, monitorHistory.getId(), "O valor de 'id' está incorreto.");
		assertEquals(unitId, monitorHistory.getUnitId(), "O valor de 'unitId' está incorreto.");
		assertEquals(estado, monitorHistory.getEstado(), "O valor de 'estado' está incorreto.");
		assertEquals(month, monitorHistory.getMonth(), "O valor de 'month' está incorreto.");
		assertEquals(year, monitorHistory.getYear(), "O valor de 'year' está incorreto.");
		assertEquals(quantity, monitorHistory.getQuantity(), "O valor de 'quantity' está incorreto.");
	}

	@Test
	@DisplayName("Teste do construtor parametrizado")
	void testParameterizedConstructor() {
		long id = 2L;
		long unitId = 200L;
		String estado = "Concluído";
		int month = 6;
		int year = 2025;
		int quantity = 15;

		MonitorHistoryBean monitorHistory = new MonitorHistoryBean(id, unitId, estado, month, year, quantity);

		assertEquals(id, monitorHistory.getId(), "O valor de 'id' no construtor está incorreto.");
		assertEquals(unitId, monitorHistory.getUnitId(), "O valor de 'unitId' no construtor está incorreto.");
		assertEquals(estado, monitorHistory.getEstado(), "O valor de 'estado' no construtor está incorreto.");
		assertEquals(month, monitorHistory.getMonth(), "O valor de 'month' no construtor está incorreto.");
		assertEquals(year, monitorHistory.getYear(), "O valor de 'year' no construtor está incorreto.");
		assertEquals(quantity, monitorHistory.getQuantity(), "O valor de 'quantity' no construtor está incorreto.");
	}

	@Test
	@DisplayName("Teste do construtor padrão")
	void testDefaultConstructor() {
		MonitorHistoryBean monitorHistory = new MonitorHistoryBean();

		assertEquals(0L, monitorHistory.getId(), "O valor padrão de 'id' deve ser 0.");
		assertEquals(0L, monitorHistory.getUnitId(), "O valor padrão de 'unitId' deve ser 0.");
		assertNull(monitorHistory.getEstado(), "O valor padrão de 'estado' deve ser null.");
		assertEquals(0, monitorHistory.getMonth(), "O valor padrão de 'month' deve ser 0.");
		assertEquals(0, monitorHistory.getYear(), "O valor padrão de 'year' deve ser 0.");
		assertEquals(0, monitorHistory.getQuantity(), "O valor padrão de 'quantity' deve ser 0.");
	}
}