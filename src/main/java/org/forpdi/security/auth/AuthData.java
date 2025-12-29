package org.forpdi.security.auth;

import java.util.Map;

import org.forpdi.core.user.User;

public class AuthData {
	private long id;
	private String name;
	private String email;
	private int accessLevel;
	private int tokenVersion; 

	public AuthData(final Map<String, Object> authDataMap) {
		this.id = Long.valueOf(authDataMap.get("id").toString());
		this.name = authDataMap.get("name").toString();
		this.email = authDataMap.get("email").toString();
		this.accessLevel = Integer.valueOf(authDataMap.get("accessLevel").toString());
		Object tokenVersionObj = authDataMap.get("tokenVersion");
		if (tokenVersionObj != null) {
			this.tokenVersion = Integer.parseInt(tokenVersionObj.toString());
		} else {
			this.tokenVersion = 0;
    }
	}
	
	public AuthData(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getUsername();
		this.accessLevel = user.getAccessLevel();
		this.tokenVersion = user.getTokenVersion();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
	}
	public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }
}
