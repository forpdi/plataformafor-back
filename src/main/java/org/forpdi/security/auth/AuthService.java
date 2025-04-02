package org.forpdi.security.auth;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;

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
        return new SessionInfo(user, refreshToken, token, List.of());

	}

	public void logout() {
		String refreshToken = getRefreshTokenFromHeader();
		Claims claims = jwtService.parseRefreshToken(refreshToken);
		long exp = (long) claims.get("exp");
		RefreshTokenBlacklist rtb = new RefreshTokenBlacklist(refreshToken, new Date(exp * 1000));
		dao.merge(rtb);
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
