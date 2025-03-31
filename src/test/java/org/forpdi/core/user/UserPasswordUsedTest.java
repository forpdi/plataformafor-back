package org.forpdi.core.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import java.util.Date;

class UserPasswordUsedTest {


    @Test
    void test_create_user_password_used_with_valid_data() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setName("Test User");
    
        UserPasswordUsed userPasswordUsed = new UserPasswordUsed();
        userPasswordUsed.setUser(user);
        userPasswordUsed.setPassword("validPassword123");
    
        assertNotNull(userPasswordUsed.getUser());
        assertEquals("validPassword123", userPasswordUsed.getPassword());
        assertNotNull(userPasswordUsed.getCreation());
        assertFalse(userPasswordUsed.isDeleted());
    }

    @Test
    void test_set_and_get_password() {
        UserPasswordUsed userPasswordUsed = new UserPasswordUsed();
        String testPassword = "testPassword123";
        userPasswordUsed.setPassword(testPassword);

        assertEquals(testPassword, userPasswordUsed.getPassword());
    }

	@Test
	public void test_set_valid_creation_date() {
		UserPasswordUsed userPassword = new UserPasswordUsed();
		Date testDate = new Date();

		userPassword.setCreation(testDate);

		assertEquals(testDate, userPassword.getCreation());
	}
}