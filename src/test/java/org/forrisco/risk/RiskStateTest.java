package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskStateTest {

	@DisplayName("RiskState Criação do Enum para UP_TO_DATE.")
	@Test
	public void testRiskStateUpToDateCaseCreation() {
		RiskState state = RiskState.UP_TO_DATE;

		int id = state.getId();

		assertEquals(0, id);
	}

	@DisplayName("RiskState Criação do Enum para CLOSE_TO_EXPIRE.")
	@Test
	public void testRiskStateCloseToExpireCreation() {
		RiskState state = RiskState.CLOSE_TO_EXPIRE;

		int id = state.getId();

		assertEquals(1, id);
	}

	@DisplayName("RiskState Criação do Enum para LATE.")
	@Test
	public void testRiskStateLateCreation() {
		RiskState state = RiskState.LATE;

		int id = state.getId();

		assertEquals(2, id);
	}

	@DisplayName("RiskState Retornar o Enum por um Id válido.")
	@Test
	void testRiskStategetByIdWithValidId() {
		RiskState expectedState = RiskState.UP_TO_DATE;

		RiskState returnedState = RiskState.getById(expectedState.getId());

		assertEquals(expectedState, returnedState);
	}

	@DisplayName("RiskState Retornar o Enum por um Id inválido.")
	@Test
	void testRiskStategetByIdWithInvalidId() {
		{
			IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> RiskState.getById(-1)
			);
			assertEquals("Unrecognized RiskState id: -1", exception.getMessage());
		}
	}

}