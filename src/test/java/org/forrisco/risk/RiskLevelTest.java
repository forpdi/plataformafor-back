package org.forrisco.risk;

import org.forrisco.core.policy.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RiskLevelTest {

	@DisplayName("RiskLevel Criação do objeto.")
	@Test
	public void testRiskLevelCreationWithGettersAndSetters() {
		RiskLevel riskLevel = new RiskLevel();

		String testLevel = "High";
		int testColor = 255;
		Policy mockPolicy = mock(Policy.class);

		riskLevel.setLevel(testLevel);
		riskLevel.setColor(testColor);
		riskLevel.setPolicy(mockPolicy);

		assertEquals(testLevel, riskLevel.getLevel(), "O nível deveria ser igual ao definido.");
		assertEquals(testColor, riskLevel.getColor(), "A cor deveria ser igual à definida.");
		assertEquals(mockPolicy, riskLevel.getPolicy(), "A política deveria ser igual à mockada.");
	}

	@DisplayName("RiskLevel Criação do objeto. Construtor vazio.")
	@Test
	public void testRiskLevelCreationEmptyConstructor() {
		RiskLevel object = new RiskLevel();

		assertNotNull(object, "O RiskLevel criado não deveria ser nulo.");
	}

	@DisplayName("RiskLevel Criação do objeto. Construtor com todos os campos requeridos.")
	@Test
	public void testRiskLevelCreationWithFullConstructor() {
		Policy mockPolicy = mock(Policy.class);
		int testColor = 255;
		String testLevel = "High";

		RiskLevel riskLevel = new RiskLevel(mockPolicy, testColor, testLevel);

		assertNotNull(riskLevel, "O objeto RiskLevel não deveria ser nulo.");
		assertEquals(testLevel, riskLevel.getLevel(), "O nível deveria ser igual ao definido.");
		assertEquals(testColor, riskLevel.getColor(), "A cor deveria ser igual à definida.");
		assertEquals(mockPolicy, riskLevel.getPolicy(), "A política deveria ser igual à mockada.");
	}

}