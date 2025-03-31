package org.forpdi.dashboard.indicators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompanyAccessHistoryTest {

	private CompanyAccessHistory companyAccessHistory;
	private Map<String, Integer[]> history;

	@BeforeEach
	void setUp() {
		history = new HashMap<>();
		history.put("2024-12-01", new Integer[]{1, 2});
		companyAccessHistory = new CompanyAccessHistory(123L, history);
	}

	@Test
	@DisplayName("Dado um CompanyAccessHistory, quando o companyId é obtido, então o companyId deve ser retornado corretamente")
	void testGetCompanyId() {
		Long companyId = companyAccessHistory.getCompanyId();

		assertEquals(123L, companyId, "O companyId deve ser corretamente retornado");
	}

	@Test
	@DisplayName("Dado um CompanyAccessHistory, quando o histórico é obtido, então o histórico deve ser retornado corretamente")
	void testGetHistory() {
		Map<String, Integer[]> retrievedHistory = companyAccessHistory.getHistory();

		assertNotNull(retrievedHistory, "O histórico não deve ser nulo");
		assertTrue(retrievedHistory.containsKey("2024-12-01"), "O histórico deve conter a chave 2024-12-01");
		assertArrayEquals(new Integer[]{1, 2}, retrievedHistory.get("2024-12-01"), "Os valores no histórico devem corresponder");
	}

	@Test
	@DisplayName("Dado um CompanyAccessHistory, quando o companyId é alterado, então o novo companyId deve ser refletido")
	void testSetCompanyId() {
		companyAccessHistory.setCompanyId(456L);

		assertEquals(456L, companyAccessHistory.getCompanyId(), "O companyId deve ser atualizado corretamente");
	}

	@Test
	@DisplayName("Dado um CompanyAccessHistory, quando o histórico é alterado, então o novo histórico deve ser refletido")
	void testSetHistory() {
		Map<String, Integer[]> newHistory = new HashMap<>();
		newHistory.put("2024-12-02", new Integer[]{3, 4});

		companyAccessHistory.setHistory(newHistory);

		assertNotNull(companyAccessHistory.getHistory(), "O histórico não deve ser nulo após atualização");
		assertTrue(companyAccessHistory.getHistory().containsKey("2024-12-02"), "O novo histórico deve conter a chave 2024-12-02");
		assertArrayEquals(new Integer[]{3, 4}, companyAccessHistory.getHistory().get("2024-12-02"), "Os valores no novo histórico devem corresponder");
	}
}
