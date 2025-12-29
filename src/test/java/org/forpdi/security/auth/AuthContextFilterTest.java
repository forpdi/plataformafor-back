package org.forpdi.security.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthContextFilterTest {
	Authentication auth;

	@BeforeEach
	void setup(){
		Authentication auth = null;
	}

    @Test
    public void test_valid_jwt_token_sets_auth_context() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AuthContextFilter filter = new AuthContextFilter(jwtService);
    
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
    
        AuthData authData = new AuthData(Map.of(
            "id", 1L,
            "name", "Test User",
            "email", "test@test.com", 
            "accessLevel", AccessLevels.MANAGER.getLevel()
        ));
    
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.validateAccessToken("valid-token")).thenReturn(authData);
    
        filter.doFilterInternal(request, response, chain);
    
		auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(authData, auth.getPrincipal());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().iterator().next().getAuthority().equals("ROLE_MANAGER"));
    
        verify(chain).doFilter(request, response);
    }

    @Test
    public void test_missing_token_in_auth_header() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AuthContextFilter filter = new AuthContextFilter(jwtService);
    
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
    
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    
        filter.doFilterInternal(request, response, chain);

        assertNull(auth);
        verify(chain).doFilter(request, response);
    }

    @Test
    public void test_valid_jwt_token_in_cookie_sets_auth_context() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AuthContextFilter filter = new AuthContextFilter(jwtService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        AuthData authData = new AuthData(Map.of(
            "id", 1L,
            "name", "Test User",
            "email", "test@test.com", 
            "accessLevel", AccessLevels.MANAGER.getLevel()
        ));

        Cookie jwtCookie = new Cookie("jwt-token", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtService.validateAccessToken("valid-token")).thenReturn(authData);

        filter.doFilterInternal(request, response, chain);

		auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(authData, auth.getPrincipal());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().iterator().next().getAuthority().equals("ROLE_MANAGER"));

        verify(chain).doFilter(request, response);
    }

    @Test
    public void test_exception_in_token_validation_allows_request_to_continue() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AuthContextFilter filter = new AuthContextFilter(jwtService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.validateAccessToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        filter.doFilterInternal(request, response, chain);

        assertNull(auth);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void test_invalid_token_logs_remote_address() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AuthContextFilter filter = new AuthContextFilter(jwtService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(jwtService.validateAccessToken("invalid-token")).thenThrow(new IllegalArgumentException("Invalid token"));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void test_invalid_jwt_token_does_not_modify_security_context() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        AuthContextFilter filter = new AuthContextFilter(jwtService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.validateAccessToken("invalid-token")).thenThrow(new IllegalArgumentException("Invalid token"));

        filter.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        verify(chain).doFilter(request, response);
    }
}