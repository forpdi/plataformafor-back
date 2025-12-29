package org.forpdi.security.auth;

import org.forpdi.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class AuthConfigTest {

	@InjectMocks
	private AuthConfig authConfig;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticationConfiguration authenticationConfiguration;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Verificar se o UserDetailsService é configurado corretamente")
	void testUserDetailsService() {
		when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.empty());

		UserDetailsService userDetailsService = authConfig.userDetailsService();
		assertNotNull(userDetailsService, "O UserDetailsService deve ser configurado corretamente");
	}

	@Test
	@DisplayName("Verificar se o AuthenticationProvider é configurado corretamente")
	void testAuthenticationProvider() {
		AuthenticationProvider authProvider = authConfig.authenticationProvider();
		assertNotNull(authProvider, "O AuthenticationProvider deve ser configurado corretamente");
	}

	@Test
	@DisplayName("Verificar se o AuthenticationManager é configurado corretamente")
	void testAuthenticationManager() throws Exception {
		when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

		AuthenticationManager manager = authConfig.authenticationManager(authenticationConfiguration);
		assertNotNull(manager, "O AuthenticationManager deve ser configurado corretamente");
	}

	@Test
	@DisplayName("Verificar se o PasswordEncoder é configurado corretamente")
	void testPasswordEncoder() {
		PasswordEncoder encoder = authConfig.passwordEncoder();
		assertNotNull(encoder, "O PasswordEncoder deve ser configurado corretamente");
	}
}
