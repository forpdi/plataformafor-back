package org.forpdi.security.authz;

import java.util.Arrays;
import java.util.List;

public enum AccessLevels {

	SYSTEM_ADMIN(100),
	COMPANY_ADMIN(50),
	MANAGER(30),
	COLABORATOR(15),
	AUDITOR(10),
	AUTHENTICATED(5),
	NONE(0);
	
	public static final String HAS_ROLE_SYSTEM_ADMIN = "hasRole('SYSTEM_ADMIN')";
	public static final String HAS_ROLE_COMPANY_ADMIN = "hasRole('COMPANY_ADMIN')";
	public static final String HAS_ROLE_MANAGER = "hasRole('MANAGER')";
	public static final String HAS_ROLE_COLABORATOR = "hasRole('COLABORATOR')";
	
	private final int level;
	
	private AccessLevels(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public static AccessLevels getByLevel(int level) {
		for (AccessLevels accessLevel : AccessLevels.values()) {
			if (level == accessLevel.level) {
				return accessLevel;
			}
		}

		throw new IllegalArgumentException("Role id not exists: " + level);
	}

	public static List<AccessLevels> getOrderedByLevel() {
		List<AccessLevels> roles = Arrays.asList(AccessLevels.values());
		roles.sort((a, b) -> {
			if (a.level < b.level) {
				return 1;
			}
			if (a.level > b.level) {
				return -1;
			}
			return 0;
		});
		return roles;
	}
	
	public String asRoleName() {
		return "ROLE_" + toString();
	}
}
