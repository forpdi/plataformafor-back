package org.forpdi.security.auth;

import java.util.Collections;
import java.util.List;

import org.forpdi.core.user.User;
import org.forpdi.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserSession {

	@Autowired
	private UserRepository userRepository;
	
	public int getAccessLevel() {
		return getAuthData().getAccessLevel();
	}

	public User getUser() {
		long id = getAuthData().getId();
		return userRepository.findById(id).get();
	}
	
	public List<String> getPermissions() {
		// As permissões não estão sendoutilizadas
		return Collections.emptyList();
	}
	
	public boolean isLogged() {
		Authentication authentication = getAuthentication();
		if (authentication == null) {
			return false;
		}
		
		return authentication.getPrincipal() instanceof AuthData;
	}
	
	public AuthData getAuthData() {
		if (getAuthentication() == null) {
			throw new IllegalStateException("User not authenticated");
		}

		return (AuthData) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}
		
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
}
