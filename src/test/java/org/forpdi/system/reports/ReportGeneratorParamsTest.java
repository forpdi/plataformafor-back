package org.forpdi.system.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorParamsTest {

	private static class ValidParams implements ReportGeneratorParams {
	}

	private static class InvalidParams implements ReportGeneratorParams {
	}

	@Test
	void testExtractParamsSuccessfulCase() {

		ValidParams params = new ValidParams();
		ValidParams result = ReportGeneratorParams.extractParams(params, ValidParams.class);
		assertNotNull(result);
		assertSame(params, result);
	}

	@Test
	void testExtractParamsUnsuccessfulCase() {
		InvalidParams params = new InvalidParams();
		assertThrows(IllegalArgumentException.class, () -> {
			ReportGeneratorParams.extractParams(params, ValidParams.class);
		});
	}

}