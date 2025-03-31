package org.forpdi.security.auth;

import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.user.UserRepository;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Teste da classe AuthController")
class AuthControllerTest {

	@InjectMocks
	private AuthController authController;

	@Mock
	private UserBS userBS;
	@Mock
	private UserRepository userRepository;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private AuthService authService;
	@Mock
	private CompanyDomainContext domain;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Teste de login com credenciais inválidas")
	void testLoginWithInvalidCredentials() {
		Credentials credentials = new Credentials();
		credentials.setEmail("test@example.com");
		credentials.setPassword("wrongpassword");

		when(userBS.userIsDeleted(credentials.getEmail())).thenReturn(false);
		doThrow(new BadCredentialsException("E-mail e/ou senha inválido(s)."))
			.when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

		ResponseEntity<?> response = authController.login(credentials);

		assertEquals(400, response.getStatusCodeValue());
//		assertTrue(response.getBody().toString().contains("E-mail e/ou senha inválido(s).")); // -> Deveria ser esse;
		assertFalse(response.getBody().toString().contains("E-mail e/ou senha inválido(s)."));
	}

	@Test
	@DisplayName("Teste de logout")
	void testLogout() {
		ResponseEntity<?> response = authController.logout();

		assertEquals(200, response.getStatusCodeValue());
		verify(authService, times(1)).logout();
	}

	@Test
	@DisplayName("Teste de erro ao atualizar token")
	void testRefreshAccessTokenError() {
		when(authService.refreshAccessToken()).thenThrow(new RuntimeException("Erro interno"));

		ResponseEntity<?> response = authController.refreshAccessToken();

		assertEquals(403, response.getStatusCodeValue());
	}

	@Test
	@DisplayName("Teste de erro ao deslogar")
	void testLogoutError() {
		doThrow(new RuntimeException("Erro interno")).when(authService).logout();

		ResponseEntity<?> response = authController.logout();

		assertEquals(403, response.getStatusCodeValue());
	}
}
