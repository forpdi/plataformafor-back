package org.forpdi.system.accesslog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAccessLogTest {

	private UserAccessLog userAccessLog;

	@BeforeEach
	void setUp() {
		userAccessLog = new UserAccessLog();
	}

	@Test
	@DisplayName("Verificar o comportamento do getter e setter de userId")
	void testUserId() {
		Long userId = 123L;

		userAccessLog.setUserId(userId);

		assertEquals(userId, userAccessLog.getUserId(), "O ID do usuário deve ser igual ao valor setado");
	}

	@Test
	@DisplayName("Verificar o comportamento do getter e setter de fpdiLastAccess")
	void testFpdiLastAccess() {
		LocalDateTime fpdiLastAccess = LocalDateTime.now();

		userAccessLog.setFpdiLastAccess(fpdiLastAccess);

		assertEquals(fpdiLastAccess, userAccessLog.getFpdiLastAccess(), "O valor de fpdiLastAccess deve ser igual ao valor setado");
	}

	@Test
	@DisplayName("Verificar o comportamento do getter e setter de friscoLastAccess")
	void testFriscoLastAccess() {
		LocalDateTime friscoLastAccess = LocalDateTime.now();

		userAccessLog.setFriscoLastAccess(friscoLastAccess);

		assertEquals(friscoLastAccess, userAccessLog.getFriscoLastAccess(), "O valor de friscoLastAccess deve ser igual ao valor setado");
	}
}
