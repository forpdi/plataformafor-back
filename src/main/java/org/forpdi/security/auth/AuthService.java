package org.forpdi.security.auth;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HibernateDAO dao;
	
	@Autowired
	private EntityManager entityManager;

	@Autowired
    private HttpServletResponse response;
	
	public SessionInfo refreshAccessToken() {
		String refreshToken = getRefreshTokenFromHeader();
		if (!refreshTokenIsValid(refreshToken)) {
			throw new IllegalArgumentException("Invalid refresh token");
		}
		
		AuthData authData = jwtService.validateRefreshToken(refreshToken);
		User user = userRepository.findById(authData.getId()).get();
		
		return createAuthData(user);
	}

	public SessionInfo createAuthData(User user) {
		AuthData authData = new AuthData(user);
		String refreshToken = jwtService.generateRefreshToken(authData);
		String token = jwtService.generateAccessToken(authData);

		ResponseCookie jwtCookie = ResponseCookie.from("jwt-token", token)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.sameSite("Lax")
				.maxAge(Duration.ofMinutes(70))
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

		return new SessionInfo(user, refreshToken, token, List.of());
	}

	public void logout() {
		String refreshToken = getRefreshTokenFromHeader();

		Claims claims = jwtService.parseRefreshToken(refreshToken);
		long exp = (long) claims.get("exp");
		RefreshTokenBlacklist rtb =
				new RefreshTokenBlacklist(refreshToken, new Date(exp * 1000));
		dao.merge(rtb);

		AuthData authData = jwtService.validateRefreshToken(refreshToken);
		User user = userRepository.findById(authData.getId())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		user.incrementTokenVersion();
		userRepository.save(user);

		boolean isHttps = request.isSecure() ||
				"https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

		ResponseCookie jwtCookie = ResponseCookie.from("jwt-token", "")
				.httpOnly(true)
				.secure(isHttps)
				.path("/")
				.sameSite("Lax")
				.maxAge(0)
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
	}

	
	private String getRefreshTokenFromHeader() {
		String authHeader = request.getHeader("Refresh-token");
		if (authHeader != null) {
			String split[] = authHeader.split(" ", 2);
			if (split.length != 2) {
				throw new IllegalArgumentException("Invalid refresh token");
			}
			String token = authHeader.split(" ", 2)[1];
			if (token != null && !token.isBlank()) {
				return token;
			}
			throw new IllegalArgumentException("Token is missing");
		}
		throw new IllegalArgumentException("Authorization header is missing");
	}
	
	private boolean refreshTokenIsValid(String refreshToken) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<RefreshTokenBlacklist> root = cq.from(RefreshTokenBlacklist.class);
		
		cq.select(cb.count(root.get("token")));
		
		cq.where(
				cb.equal(root.get("token"), refreshToken),
				cb.greaterThan(root.get("expiration"), new Date()));
		
		return entityManager.createQuery(cq).getSingleResult() == 0;
	}
}
