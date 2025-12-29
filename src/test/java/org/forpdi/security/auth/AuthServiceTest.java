package org.forpdi.security.auth;

import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

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
}
