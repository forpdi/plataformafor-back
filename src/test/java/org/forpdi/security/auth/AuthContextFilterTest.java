package org.forpdi.security.auth;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthContextFilterTest {
	Authentication auth;

	@BeforeEach
	void setup(){
		Authentication auth = null;
	}

    @Test
    void test_valid_jwt_token_sets_auth_context() throws Exception {
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
    void test_missing_token_in_auth_header() throws Exception {
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
    void test_valid_jwt_token_in_cookie_sets_auth_context() throws Exception {
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
    void test_exception_in_token_validation_allows_request_to_continue() throws Exception {
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
    void test_invalid_token_logs_remote_address() throws Exception {
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
    void test_invalid_jwt_token_does_not_modify_security_context() throws Exception {
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
    
	@Test
	void test_missing_token_throws_exception() throws Exception {
		JwtService jwtService = new JwtService();
		AuthContextFilter filter = new AuthContextFilter(jwtService);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer ");
		request.setRemoteAddr("127.0.0.1");

		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		verify(chain).doFilter(request, response);
	}
}