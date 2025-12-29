package org.forpdi.security.authz;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthzConfigTest {
	@Test
	void test_hierarchy_string_generation() {
		AuthzConfig config = new AuthzConfig();

		String hierarchy = config.getHierarchyAsString();

		assertEquals("ROLE_SYSTEM_ADMIN > ROLE_COMPANY_ADMIN > ROLE_MANAGER > ROLE_COLABORATOR > ROLE_AUDITOR > ROLE_AUTHENTICATED > ROLE_NONE", hierarchy);
	}

	@Test
	void test_empty_access_levels() {
		AuthzConfig config = new AuthzConfig();

		List<AccessLevels> emptyList = Collections.emptyList();
		try (MockedStatic<AccessLevels> mockedStatic = mockStatic(AccessLevels.class)) {
			mockedStatic.when(AccessLevels::getOrderedByLevel).thenReturn(emptyList);

			String hierarchy = config.getHierarchyAsString();

			assertEquals("", hierarchy);
		}
	}

	@Test
	void test_role_hierarchy_respects_access_level_ordering() {
		AuthzConfig config = new AuthzConfig();

		String hierarchy = config.getHierarchyAsString();

		assertEquals("ROLE_SYSTEM_ADMIN > ROLE_COMPANY_ADMIN > ROLE_MANAGER > ROLE_COLABORATOR > ROLE_AUDITOR > ROLE_AUTHENTICATED > ROLE_NONE", hierarchy);
	}

	@Test
	void test_access_levels_order() {
		AuthzConfig config = new AuthzConfig();

		String hierarchy = config.getHierarchyAsString();

		assertEquals("ROLE_SYSTEM_ADMIN > ROLE_COMPANY_ADMIN > ROLE_MANAGER > ROLE_COLABORATOR > ROLE_AUDITOR > ROLE_AUTHENTICATED > ROLE_NONE", hierarchy);
	}

	@Test
	void test_role_hierarchy_handles_empty_hierarchy() {
		AuthzConfig authzConfig = spy(new AuthzConfig());
		String emptyHierarchy = "";
		doReturn(emptyHierarchy).when(authzConfig).getHierarchyAsString();

		RoleHierarchy roleHierarchy = authzConfig.roleHierarchy();

		assertNotNull(roleHierarchy);
		assertTrue(roleHierarchy instanceof RoleHierarchyImpl);

	}
}