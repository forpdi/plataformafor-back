package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskOrganizationalLevelTest {

	@DisplayName("RiskOrganizationalLevel Criação da Enum OPERATIONAL.")
	@Test
	public void testRiskOrganizationalLevelOPERATIONALCreation() {
		RiskOrganizationalLevel operational = RiskOrganizationalLevel.OPERATIONAL;

		int consultedValue = operational.getId();
		String consultedName = operational.getLabel();

		assertEquals(5, consultedValue);
		assertEquals("Operacional", consultedName);
	}

	@DisplayName("RiskOrganizationalLevel Criação da Enum TACTICAL.")
	@Test
	public void testRiskOrganizationalLevelTACTICALCreation() {
		RiskOrganizationalLevel tatico = RiskOrganizationalLevel.TACTICAL;

		int consultedValue = tatico.getId();
		String consultedName = tatico.getLabel();

		assertEquals(10, consultedValue);
		assertEquals("Tático", consultedName);
	}

	@DisplayName("RiskOrganizationalLevel Criação da Enum STRATEGIC.")
	@Test
	public void testRiskOrganizationalLevelSTRATEGICCreation() {
		RiskOrganizationalLevel estrategico = RiskOrganizationalLevel.STRATEGIC;

		int consultedValue = estrategico.getId();
		String consultedName = estrategico.getLabel();

		assertEquals(15, consultedValue);
		assertEquals("Estratégico", consultedName);
	}

	@DisplayName("RiskOrganizationalLevel Obtendo a Enum pelo seu id. Caso sem exceção.")
	@Test
	void testRiskOrganizationalLevelGetByIdValidCase() {
		int consultedId = 10;

		RiskOrganizationalLevel returnedEnum = RiskOrganizationalLevel.getById(consultedId);

		assertNotNull(returnedEnum);
		assertEquals(RiskOrganizationalLevel.TACTICAL, returnedEnum);
	}

	@DisplayName("RiskOrganizationalLevel Obtendo a Enum pelo seu id. Caso com exceção.")
	@Test
	void testRiskOrganizationalLevelGetByIdInvalidCase() {
		int invalidId = 50;

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> RiskOrganizationalLevel.getById(invalidId)
		);
		assertEquals("Unrecognized RiskOrganizationalLevel id: " + invalidId, exception.getMessage());
	}
}