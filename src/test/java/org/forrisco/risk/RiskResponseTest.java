package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskResponseTest {
	@DisplayName("RiskResponse ACCEPT enum creation.")
	@Test
	void testRiskResponseACCEPTEnumCreation() {
		RiskResponse enumAccept = RiskResponse.ACCEPT;

		int idReturned = enumAccept.getId();
		String labelReturned = enumAccept.getLabel();

		assertNotNull(enumAccept, "O objeto não deveria ser nulo.");
		assertEquals(5, idReturned, "O id retornado não corresponde ao esperado.");
		assertEquals("Aceitar", labelReturned, "A label retornada não corresponde à esperada.");
	}

	@DisplayName("RiskResponse MITIGATE enum creation.")
	@Test
	void testRiskResponseMITIGATEEnumCreation() {
		RiskResponse enumAccept = RiskResponse.MITIGATE;

		int idReturned = enumAccept.getId();
		String labelReturned = enumAccept.getLabel();

		assertNotNull(enumAccept, "O objeto não deveria ser nulo.");
		assertEquals(10, idReturned, "O id retornado não corresponde ao esperado.");
		assertEquals("Mitigar", labelReturned, "A label retornada não corresponde à esperada.");
	}

	@DisplayName("RiskResponse SHARE enum creation.")
	@Test
	void testRiskResponseSHAREEnumCreation() {
		RiskResponse enumAccept = RiskResponse.SHARE;

		int idReturned = enumAccept.getId();
		String labelReturned = enumAccept.getLabel();

		assertNotNull(enumAccept, "O objeto não deveria ser nulo.");
		assertEquals(15, idReturned, "O id retornado não corresponde ao esperado.");
		assertEquals("Compartilhar", labelReturned, "A label retornada não corresponde à esperada.");
	}

	@DisplayName("RiskResponse AVOID enum creation.")
	@Test
	void testRiskResponseAVOIDEnumCreation() {
		RiskResponse enumAccept = RiskResponse.AVOID;

		int idReturned = enumAccept.getId();
		String labelReturned = enumAccept.getLabel();

		assertNotNull(enumAccept, "O objeto não deveria ser nulo.");
		assertEquals(20, idReturned, "O id retornado não corresponde ao esperado.");
		assertEquals("Evitar", labelReturned, "A label retornada não corresponde à esperada.");
	}

	@DisplayName("RiskResponse Obtendo o Enum por um Id válido.")
	@Test
	void testRiskResponseGebByIdValidCase() {
		RiskResponse response = RiskResponse.GetById(5);

		assertEquals(RiskResponse.ACCEPT, response, "O Enum retornado não corresponde ao esperado.");
		assertEquals(5, response.getId(), "O id do Enum não é o esperado.");
		assertEquals("Aceitar", response.getLabel(), "A label do Enum não é a esperada.");
	}

	@DisplayName("RiskResponse Obtendo o Enum por um Id inválido.")
	@Test
	void testRiskResponseGebByIdInvalidCase() {
		int idToFinds = 7;
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RiskResponse.GetById(idToFinds));
		assertEquals("Unrecognized RiskResponse id: " + idToFinds, exception.getMessage());
	}

	@DisplayName("RiskResponse Obtendo o Enum por uma label válida.")
	@Test
	void testRiskResponsegetIdsByNameValidCase() {
		String label = "Aceitar";
		List<Integer> expectedIds = List.of(5);

		List<Integer> actualIds = RiskResponse.getIdsByName(label);

		assertEquals(expectedIds, actualIds, "O id retornado não é o esperado.");
	}

	@DisplayName("RiskResponse Obtendo o Enum por uma label inválida.")
	@Test
	void testRiskResponseGetIdsByNameLabelNullCase() {
		String label = null;

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RiskResponse.getIdsByName(label));

		assertEquals("Empty RiskResponse label: " + label, exception.getMessage());
	}
}