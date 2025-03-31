package org.forpdi.security.auth;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private final static String AUTH_INFO_CLAIM = "auth-info";
	private final static String AUTH_SUBJECT = "auth-info";

	@Value("${security.iss}")
	private String issuer;
	
	@Value("${security.jwt.key}")
	private String accessKey;
	
	@Value("${security.jwt.refresh-key}")
	private String refreshKey;
	
	@Value("${security.jwt.expiration-sec}")
	private Integer accessExpiration;
	
	@Value("${security.jwt.refresh-expiration-sec}")
	private Integer refreshExpiration;

	public String generateAccessToken(AuthData authData) {
		return generateToken(authData, accessKey, accessExpiration);
	}
	
	public String generateRefreshToken(AuthData authData) {
		return generateToken(authData, refreshKey, refreshExpiration);
	}

	public AuthData validateAccessToken(String token) {
		return validateToken(token, accessKey);
	}
	
	public AuthData validateRefreshToken(String token) {
		return validateToken(token, refreshKey);
	}
	
	public Claims parseRefreshToken(String refreshToken) {
		return parseToken(refreshToken, refreshKey);
	}

	private String generateToken(AuthData authData, String key, Integer expiration) {
		LocalDateTime now = LocalDateTime.now();
		Date nowDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		Date exp = Date.from(
			now.plusSeconds(expiration)
				.atZone(ZoneId.systemDefault())
				.toInstant()
		);

		return Jwts.builder()
			.claim(AUTH_INFO_CLAIM, authData)
			.expiration(exp)
			.issuedAt(nowDate)
			.issuer(issuer)
			.notBefore(nowDate)
			.subject(AUTH_SUBJECT)
			.signWith(getSecretKey(key))
			.compact();
	}

	@SuppressWarnings("unchecked")
	private AuthData validateToken(String token, String key) {
		Claims claims = parseToken(token, key);
		LinkedHashMap<String, Object> authDataMap = claims.get(AUTH_INFO_CLAIM, LinkedHashMap.class);
		AuthData authData = new AuthData(authDataMap);
		return authData;
	}
	
	private Claims parseToken(String token, String key) {
		return Jwts.parser()
				.verifyWith(getSecretKey(key)).build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSecretKey(String key) {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
	}
}
