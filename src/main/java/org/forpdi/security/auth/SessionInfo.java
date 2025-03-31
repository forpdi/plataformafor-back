package org.forpdi.security.auth;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.forpdi.core.user.User;

public class SessionInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private User user;
	private String refreshToken;
	private String token;
	private List<String> permissions;
	private int accessLevel;
	private Date termsAcceptance;
	
	public SessionInfo(User user, String refreshToken, String token, List<String> permissions) {
		this.user = user;
		this.refreshToken = refreshToken;
		this.token = token;
		this.permissions = permissions;
		this.accessLevel = user.getAccessLevel();
		this.termsAcceptance = user.getTermsAcceptance();
	}

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

	public int getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
	}

	public Date getTermsAcceptance() {
		return termsAcceptance;
	}
	
	
}
