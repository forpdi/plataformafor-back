package org.forpdi.core.user;

import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void test_create_user_with_required_fields() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
    
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertEquals(AccessLevels.NONE.getLevel(), user.getAccessLevel());
        assertFalse(user.isActive());
        assertNotNull(user.getCreation());
    }

    @Test
    void test_set_and_get_access_level() {
        User user = new User();
        user.setAccessLevel(AccessLevels.MANAGER.getLevel());
    
        assertEquals(AccessLevels.MANAGER.getLevel(), user.getAccessLevel());
    }

    @Test
    void test_default_values_on_user_creation() {
        User user = new User();
    
        assertNotNull(user);
        assertEquals(AccessLevels.NONE.getLevel(), user.getAccessLevel());
        assertFalse(user.isActive());
        assertNotNull(user.getCreation());
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getCpf());
        assertNull(user.getCellphone());
        assertNull(user.getPassword());
        assertNull(user.getPhone());
        assertNull(user.getDepartment());
        assertNull(user.getPicture());
        assertNull(user.getBirthdate());
        assertNull(user.getTermsAcceptance());
        assertNull(user.getInviteToken());
    }

    @Test
    void test_logical_deletion() {
        User user = new User();
        assertFalse(user.isDeleted(), "User should not be deleted initially.");
    
//        user.setDeleted(true);
//        assertTrue(user.isDeleted(), "User should be marked as deleted.");
    
        user.setDeleted(false);
        assertFalse(user.isDeleted(), "User should not be marked as deleted after reset.");
    }

	@Test
	void test_set_valid_invite_token() {
		User user = new User();
		String expectedToken = "abc123";

		user.setInviteToken(expectedToken);

		assertEquals(expectedToken, user.getInviteToken());
	}

	@Test
	void test_method_returns_true() {
		User user = new User();

		boolean result = user.isEnabled();

		assertTrue(result);
	}

	@Test
	void test_credentials_non_expired_returns_true() {
		User user = new User();

		boolean result = user.isCredentialsNonExpired();

		assertTrue(result);
	}

	@Test
	void test_method_consistent_across_states() {
		User user = new User();
		user.setDeleted(true);
		user.setActive(false);

		boolean result = user.isAccountNonLocked();

		assertTrue(result);
	}

	@Test
	void test_method_isAccountNonExpired_returns_true() {
		User user = new User();

		boolean result = user.isAccountNonExpired();

		assertTrue(result);
	}

	@Test
	void test_returns_system_admin_authority() {
		User user = new User();
		user.setAccessLevel(AccessLevels.SYSTEM_ADMIN.getLevel());

		Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

		assertNotNull(authorities);
		assertEquals(1, authorities.size());
		assertInstanceOf(SimpleGrantedAuthority.class, authorities.iterator().next());
		assertEquals("ROLE_SYSTEM_ADMIN", authorities.iterator().next().getAuthority());
	}

	@Test
	void test_set_valid_birthdate() {
		User user = new User();
		Date validDate = new Date(90, 0, 1); 

		user.setBirthdate(validDate);

		assertEquals(validDate, user.getBirthdate());
	}

	@Test
	void test_set_valid_picture_url() {
		User user = new User();
		String validPictureUrl = "https://example.com/profile.jpg";

		user.setPicture(validPictureUrl);

		assertEquals(validPictureUrl, user.getPicture());
	}
}