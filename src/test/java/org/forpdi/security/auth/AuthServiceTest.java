package org.forpdi.security.auth;

import io.jsonwebtoken.Claims;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Teste da classe AuthService")
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private JwtService jwtService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HibernateDAO dao;

	@Mock
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Teste de logout com token ausente")
	void testLogoutMissingToken() {
		when(request.getHeader("Refresh-token")).thenReturn(null);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.logout());
		assertEquals("Authorization header is missing", exception.getMessage());
	}

	@Test
	@DisplayName("Teste de criação de dados de autenticação")
	void testCreateAuthData() {
		User user = new User();
		user.setId(1L);

		when(jwtService.generateAccessToken(any(AuthData.class))).thenReturn("access_token");
		when(jwtService.generateRefreshToken(any(AuthData.class))).thenReturn("refresh_token");

		SessionInfo sessionInfo = authService.createAuthData(user);

		assertNotNull(sessionInfo);
		assertEquals("access_token", sessionInfo.getToken());
		assertEquals("refresh_token", sessionInfo.getRefreshToken());
	}
}
