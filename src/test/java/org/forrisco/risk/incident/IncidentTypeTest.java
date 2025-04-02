package org.forrisco.risk.incident;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IncidentTypeTest {

	@DisplayName("IncidentType Criação do Enum para AMEACA")
	@Test
	void testIncidetTypeAMEACACreation() {
		IncidentType enumAMEACA = IncidentType.AMEACA;

		int returnedTypeId = enumAMEACA.getTypeId();
		String returnedLabel = enumAMEACA.getLabel();

		assertEquals(1, returnedTypeId, "O typeId esperado não corresponde ao retornado.");
		assertEquals("Ameaça", returnedLabel, "A Label esperada não corresponde a retornada.");
	}

	@DisplayName("IncidentType Criação do Enum para OPORTUNIDADE")
	@Test
	void testIncidetTypeOPORTUNIDADECreation() {
		IncidentType enumAMEACA = IncidentType.OPORTUNIDADE;

		int returnedTypeId = enumAMEACA.getTypeId();
		String returnedLabel = enumAMEACA.getLabel();

		assertEquals(2, returnedTypeId, "O typeId esperado não corresponde ao retornado.");
		assertEquals("Oportunidade", returnedLabel, "A Label esperada não corresponde a retornada.");
	}

	@DisplayName("IncidentType Obtendo uma Enum pelo seu typeId existente")
	@Test
	void testIncidetTypeGetByIdSuccesfulCase() {
		IncidentType expectedEnum = IncidentType.AMEACA;
		int idIncidentTypeToObtains = 1;

		IncidentType returnedIncident = IncidentType.getById(idIncidentTypeToObtains);

		assertNotNull(returnedIncident, "Era esperado um retorno não nulo para o typeId" + idIncidentTypeToObtains);
		assertEquals(expectedEnum, returnedIncident, "Era esperado enums iguais.");
	}

	@DisplayName("IncidentType Obtendo uma Enum pelo seu typeId não existente")
	@Test
	void testIncidetTypeGetByIdNullCase() {
		int idIncidentTypeToObtains = 0;

		IncidentType returnedIncident = IncidentType.getById(idIncidentTypeToObtains);

		assertNull(returnedIncident, "Era esperado um retorno nulo para o typeId" + idIncidentTypeToObtains);
	}

}