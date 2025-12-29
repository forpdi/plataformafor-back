package org.forrisco.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CloseToMaturityPeriodTest {

	@DisplayName("CloseToMaturityPeriod criação do Enum DIARIO.")
	@Test
	void testCloseToMaturityPeriodCreationDIARIO() {
		CloseToMaturityPeriod enumDiario = CloseToMaturityPeriod.DIARIO;

		double returnedValue = enumDiario.getValue();

		assertEquals(7, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum SEMANAL.")
	@Test
	void testCloseToMaturityPeriodCreationSEMANAL() {
		CloseToMaturityPeriod enumSEMANAL = CloseToMaturityPeriod.SEMANAL;

		double returnedValue = enumSEMANAL.getValue();

		assertEquals(2, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum QUINZENAL.")
	@Test
	void testCloseToMaturityPeriodCreationQUINZENAL() {
		CloseToMaturityPeriod enumQUINZENAL = CloseToMaturityPeriod.QUINZENAL;

		double returnedValue = enumQUINZENAL.getValue();

		assertEquals(7, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum MENSAL.")
	@Test
	void testCloseToMaturityPeriodCreationMENSAL() {
		CloseToMaturityPeriod enumMENSAL = CloseToMaturityPeriod.DIARIO;

		double returnedValue = enumMENSAL.getValue();

		assertEquals(7, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum BIMESTRAL.")
	@Test
	void testCloseToMaturityPeriodCreationBIMESTRAL() {
		CloseToMaturityPeriod enumBIMESTRAL = CloseToMaturityPeriod.BIMESTRAL;

		double returnedValue = enumBIMESTRAL.getValue();

		assertEquals(21, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum TRIMESTRAL.")
	@Test
	void testCloseToMaturityPeriodCreationTRIMESTRAL() {
		CloseToMaturityPeriod enumTRIMESTRAL = CloseToMaturityPeriod.TRIMESTRAL;

		double returnedValue = enumTRIMESTRAL.getValue();

		assertEquals(21, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum SEMESTRAL.")
	@Test
	void testCloseToMaturityPeriodCreationSEMESTRAL() {
		CloseToMaturityPeriod enumSEMESTRAL = CloseToMaturityPeriod.SEMESTRAL;

		double returnedValue = enumSEMESTRAL.getValue();

		assertEquals(30, returnedValue, "O valor retornado não corresponde ao esperado.");
	}

	@DisplayName("CloseToMaturityPeriod criação do Enum ANUAL.")
	@Test
	void testCloseToMaturityPeriodCreationANUAL() {
		CloseToMaturityPeriod enumANUAL = CloseToMaturityPeriod.ANUAL;

		double returnedValue = enumANUAL.getValue();

		assertEquals(30, returnedValue, "O valor retornado não corresponde ao esperado.");
	}
}