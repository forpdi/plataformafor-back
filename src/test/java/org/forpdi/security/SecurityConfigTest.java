package org.forpdi.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Consumer; // Importe Consumer para lambdas
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.forpdi.core.CustomAccessDeniedHandler;
import org.forpdi.security.auth.AuthContextFilter;
import org.forpdi.security.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor; // Importe ArgumentCaptor
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext; // Necessário para simular Beans
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy; // Adicione este import
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer; // Adicione este import
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer; // Importe para capturar a lambda
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // Para verificar regras
import org.springframework.security.web.util.matcher.RegexRequestMatcher; // Para verificar regras


class SecurityConfigTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Mock
    private AuthContextFilter authContextFilter;
    @Mock
    private RoleHierarchy roleHierarchy; 
    @Mock
    private AuthenticationConfiguration authenticationConfiguration; 

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

    }

      @Test
    @DisplayName("Teste de AuthenticationManager")
    void testAuthenticationManager() throws Exception {
        AuthenticationManager mockAuthenticationManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthenticationManager);
        
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);
        
        assertNotNull(result);
        assertEquals(mockAuthenticationManager, result);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
}
}