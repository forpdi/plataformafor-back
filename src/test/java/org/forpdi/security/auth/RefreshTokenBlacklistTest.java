package org.forpdi.security.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

class RefreshTokenBlacklistTest {

	@Test
	@DisplayName("Deve criar instância de RefreshTokenBlacklist corretamente")
	void testConstructor() {
		String token = "token_test";
		Date expiration = new Date();

		RefreshTokenBlacklist refreshTokenBlacklist = new RefreshTokenBlacklist(token, expiration);

		assertEquals(token, refreshTokenBlacklist.getToken());
		assertEquals(expiration, refreshTokenBlacklist.getExpiration());
	}

	@Test
	@DisplayName("Deve permitir modificar o token e a data de expiração")
	void testSetters() {
		String token = "token_test";
		Date expiration = new Date();
		RefreshTokenBlacklist refreshTokenBlacklist = new RefreshTokenBlacklist(token, expiration);

		String newToken = "new_token_test";
		Date newExpiration = new Date(System.currentTimeMillis() + 100000);

		refreshTokenBlacklist.setToken(newToken);
		refreshTokenBlacklist.setExpiration(newExpiration);

		assertEquals(newToken, refreshTokenBlacklist.getToken());
		assertEquals(newExpiration, refreshTokenBlacklist.getExpiration());
	}

	@Test
	@DisplayName("Deve ter valor nulo no token quando o setter for chamado com nulo")
	void testSetTokenWithNull() {
		String token = null;
		Date expiration = new Date();
		RefreshTokenBlacklist refreshTokenBlacklist = new RefreshTokenBlacklist(token, expiration);

		assertNull(refreshTokenBlacklist.getToken());
	}

	@Test
	@DisplayName("Deve retornar null se a data de expiração não for definida")
	void testSetExpirationWithNull() {
		String token = "token_test";
		Date expiration = null;
		RefreshTokenBlacklist refreshTokenBlacklist = new RefreshTokenBlacklist(token, expiration);

		assertNull(refreshTokenBlacklist.getExpiration());
	}
}
