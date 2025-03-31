package org.forpdi.security.auth;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.forpdi.security.authz.AccessLevels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthContextFilter extends OncePerRequestFilter {
	private static final Logger LOG = LoggerFactory.getLogger(AuthContextFilter.class);
	
	private JwtService jwtService;

	public AuthContextFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			String token = this.getTokenFromHeader(request);
			AuthData authData = jwtService.validateAccessToken(token);
			AccessLevels accessLevel = AccessLevels.getByLevel(authData.getAccessLevel());
			
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				authData,
				null,
				List.of(new SimpleGrantedAuthority(accessLevel.asRoleName()))
			);
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception e) {
			LOG.warn("Invalid token provided by: {} Caused by: {}", request.getRemoteAddr(), e.getMessage());
		}

		chain.doFilter(request, response);
	}

	private String getTokenFromHeader(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			String token = authHeader.split(" ", 2)[1];
			if (token != null && !token.isBlank()) {
				return token;
			}
			throw new IllegalArgumentException("Token is missing");
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("jwt-token")) {
						return cookie.getValue();
					}
				}
			}
		}
		throw new IllegalArgumentException("Authorization header is missing");
	}
}
