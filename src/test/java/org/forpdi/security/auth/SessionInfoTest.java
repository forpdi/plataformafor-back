package org.forpdi.security.auth;

import org.forpdi.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionInfoTest {

	private SessionInfo sessionInfo;
	private User user;

	@BeforeEach
	void setUp() {
		user = new User(); 
		user.setId(1L);  
		user.setAccessLevel(10);  
		user.setTermsAcceptance(new Date());  

		sessionInfo = new SessionInfo(user, "refreshToken123", "token123", Arrays.asList("PERMISSION_A", "PERMISSION_B"));
	}

	@Test
	@DisplayName("Dado que o usuário é atribuído, quando recuperar o usuário, então o usuário deve ser retornado corretamente")
	void testGetUser() {
		User expectedUser = user;

		sessionInfo.setUser(expectedUser);
		User actualUser = sessionInfo.getUser();

		assertEquals(expectedUser, actualUser, "O usuário atribuído deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Dado que o refreshToken é atribuído, quando recuperar o refreshToken, então o refreshToken deve ser retornado corretamente")
	void testGetRefreshToken() {
		String expectedRefreshToken = "refreshToken123";

		sessionInfo.setRefreshToken("refreshToken123");
		String actualRefreshToken = sessionInfo.getRefreshToken();

		assertEquals(expectedRefreshToken, actualRefreshToken, "O refreshToken atribuído deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Dado que o token é atribuído, quando recuperar o token, então o token deve ser retornado corretamente")
	void testGetToken() {
		String expectedToken = "token123";

		sessionInfo.setToken("token123");
		String actualToken = sessionInfo.getToken();

		assertEquals(expectedToken, actualToken, "O token atribuído deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Dado que as permissões são atribuídas, quando recuperar as permissões, então as permissões devem ser retornadas corretamente")
	void testGetPermissions() {
		var expectedPermissions = Arrays.asList("PERMISSION_A", "PERMISSION_B");

		sessionInfo.setPermissions(expectedPermissions);
		var actualPermissions = sessionInfo.getPermissions();

		assertEquals(expectedPermissions, actualPermissions, "As permissões atribuídas devem ser retornadas corretamente.");
	}

	@Test
	@DisplayName("Dado que o nível de acesso é atribuído, quando recuperar o nível de acesso, então o nível de acesso deve ser retornado corretamente")
	void testGetAccessLevel() {
		int expectedAccessLevel = 10;

		sessionInfo.setAccessLevel(user.getAccessLevel());
		int actualAccessLevel = sessionInfo.getAccessLevel();

		assertEquals(expectedAccessLevel, actualAccessLevel, "O nível de acesso atribuído deve ser retornado corretamente.");
	}

	@Test
	@DisplayName("Dado que a data de aceitação dos termos é atribuída, quando recuperar a data de aceitação dos termos, então ela deve ser retornada corretamente")
	void testGetTermsAcceptance() {
		Date expectedTermsAcceptance = user.getTermsAcceptance();

		Date actualTermsAcceptance = sessionInfo.getTermsAcceptance();

		assertEquals(expectedTermsAcceptance, actualTermsAcceptance, "A data de aceitação dos termos atribuída deve ser retornada corretamente.");
	}
}
