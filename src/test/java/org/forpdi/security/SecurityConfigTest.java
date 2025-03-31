package org.forpdi.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.forpdi.core.CustomAccessDeniedHandler;
import org.forpdi.security.SecurityConfig;
import org.forpdi.security.auth.AuthContextFilter;
import org.forpdi.security.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry;

class SecurityConfigTest {

	@Mock
	private JwtService jwtService;

	@Mock
	private AuthenticationProvider authenticationProvider;

	@Mock
	private CustomAccessDeniedHandler customAccessDeniedHandler;

	@InjectMocks
	private SecurityConfig securityConfig;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Teste de configuração do filtro de segurança")
	void testFilterChainConfiguration() throws Exception {
		HttpSecurity httpSecurity = mock(HttpSecurity.class);

		// Mocks para cors() e csrf()
		CorsConfigurer<HttpSecurity> corsConfigurer = mock(CorsConfigurer.class);
		CsrfConfigurer<HttpSecurity> csrfConfigurer = mock(CsrfConfigurer.class);

		when(httpSecurity.cors()).thenReturn(corsConfigurer);
		when(httpSecurity.csrf()).thenReturn(csrfConfigurer);

		// Encadeamento para garantir que chamadas subsequentes ao configurador retornem o próprio objeto
		when(corsConfigurer.and()).thenReturn(httpSecurity);
		when(csrfConfigurer.disable()).thenReturn(httpSecurity);

		// Acessando o método privado "configureCorsAndCsrf" via Reflection
		Method configureCorsAndCsrf = SecurityConfig.class.getDeclaredMethod("configureCorsAndCsrf", HttpSecurity.class);
		configureCorsAndCsrf.setAccessible(true);
		configureCorsAndCsrf.invoke(securityConfig, httpSecurity);

		// Verifica se os métodos foram chamados
		verify(httpSecurity, times(1)).cors();
		verify(httpSecurity, times(1)).csrf();
	}

	@Test
	@DisplayName("Teste de instância de expressão de segurança com hierarquia de papéis")
	void testWebExpressionHandler() throws Exception {
		// Acessando o método privado "webExpressionHandler" via Reflection
		Method webExpressionHandler = SecurityConfig.class.getDeclaredMethod("webExpressionHandler");
		webExpressionHandler.setAccessible(true);

		var handler = webExpressionHandler.invoke(securityConfig);

		assertNotNull(handler, "Handler de expressão de segurança não deveria ser nulo.");
	}

	// Método auxiliar para obter campos privados usando Reflection
	private List<?> getFieldValue(String fieldName) throws Exception {
		var field = SecurityConfig.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (List<?>) field.get(null);
	}

}
