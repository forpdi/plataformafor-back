package org.forpdi.security.auth;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity(name = RefreshTokenBlacklist.TABLE)
@Table(name = RefreshTokenBlacklist.TABLE)
public class RefreshTokenBlacklist implements Serializable {
	public static final String TABLE = "fpdi_refresh_token_blacklist";
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length = 510)
	private String token;
	
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiration;
	
	public RefreshTokenBlacklist() {
	}

	public RefreshTokenBlacklist(String token, Date expiration) {
		this.token = token;
		this.expiration = expiration;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
}
