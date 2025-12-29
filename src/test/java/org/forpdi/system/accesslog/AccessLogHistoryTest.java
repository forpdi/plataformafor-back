package org.forpdi.system.accesslog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccessLogHistoryTest {

	@Test
	@DisplayName("Deve criar um objeto AccessLogHistory com os valores corretos")
	void testAccessLogHistoryConstructor() {
		Long companyId = 1L;
		LocalDate date = LocalDate.now();
		int fpdiAccessCount = 10;
		int friscoAccessCount = 5;

		AccessLogHistory accessLogHistory = new AccessLogHistory(companyId, date, fpdiAccessCount, friscoAccessCount);

		assertEquals(companyId, accessLogHistory.getCompanyId(), "O companyId deve ser igual ao informado.");
		assertEquals(date, accessLogHistory.getDate(), "A data deve ser igual à informada.");
		assertEquals(fpdiAccessCount, accessLogHistory.getFpdiAccessCount(), "O contador de acessos FPDI deve ser igual ao informado.");
		assertEquals(friscoAccessCount, accessLogHistory.getFriscoAccessCount(), "O contador de acessos Frisco deve ser igual ao informado.");
	}

	@Test
	@DisplayName("Deve incrementar corretamente o contador de acessos FPDI")
	void testIncrementFpdiAccess() {
		Long companyId = 1L;
		LocalDate date = LocalDate.now();
		int initialFpdiAccessCount = 10;
		int friscoAccessCount = 5;
		AccessLogHistory accessLogHistory = new AccessLogHistory(companyId, date, initialFpdiAccessCount, friscoAccessCount);

		accessLogHistory.incrementFpdiAccess();

		assertEquals(initialFpdiAccessCount + 1, accessLogHistory.getFpdiAccessCount(), "O contador de acessos FPDI deve ser incrementado em 1.");
	}

	@Test
	@DisplayName("Deve incrementar corretamente o contador de acessos Frisco")
	void testIncrementFriscoAccess() {
		Long companyId = 1L;
		LocalDate date = LocalDate.now();
		int fpdiAccessCount = 10;
		int initialFriscoAccessCount = 5;
		AccessLogHistory accessLogHistory = new AccessLogHistory(companyId, date, fpdiAccessCount, initialFriscoAccessCount);

		accessLogHistory.incrementFriscoAccess();

		assertEquals(initialFriscoAccessCount + 1, accessLogHistory.getFriscoAccessCount(), "O contador de acessos Frisco deve ser incrementado em 1.");
	}

	@Test
	@DisplayName("Deve criar um AccessLogHistoryId corretamente com companyId e date")
	void testAccessLogHistoryIdConstructor() throws NoSuchFieldException, IllegalAccessException {
		Long companyId = 1L;
		LocalDate date = LocalDate.now();

		AccessLogHistory.AccessLogHistoryId accessLogHistoryId = new AccessLogHistory.AccessLogHistoryId(companyId, date);

		Field field = AccessLogHistory.AccessLogHistoryId.class.getDeclaredField("companyId");
		field.setAccessible(true);
		Long fieldCompanyId = (Long) field.get(accessLogHistoryId);

		assertEquals(companyId, fieldCompanyId, "O companyId deve ser igual ao informado.");
	}

	@Test
	@DisplayName("Deve ser possível alterar o contador de acessos FPDI")
	void testSetFpdiAccessCount() {
		Long companyId = 1L;
		LocalDate date = LocalDate.now();
		int initialFpdiAccessCount = 10;
		int friscoAccessCount = 5;
		AccessLogHistory accessLogHistory = new AccessLogHistory(companyId, date, initialFpdiAccessCount, friscoAccessCount);

		accessLogHistory.setFpdiAccessCount(20);

		assertEquals(20, accessLogHistory.getFpdiAccessCount(), "O contador de acessos FPDI deve ser atualizado para 20.");
	}

	@Test
	@DisplayName("Deve ser possível alterar o contador de acessos Frisco")
	void testSetFriscoAccessCount() {
		Long companyId = 1L;
		LocalDate date = LocalDate.now();
		int fpdiAccessCount = 10;
		int initialFriscoAccessCount = 5;
		AccessLogHistory accessLogHistory = new AccessLogHistory(companyId, date, fpdiAccessCount, initialFriscoAccessCount);

		accessLogHistory.setFriscoAccessCount(10);

		assertEquals(10, accessLogHistory.getFriscoAccessCount(), "O contador de acessos Frisco deve ser atualizado para 10.");
	}
}
