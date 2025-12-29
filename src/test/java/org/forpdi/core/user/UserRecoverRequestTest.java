package org.forpdi.core.user;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class UserRecoverRequestTest {

    @Test
    void test_create_valid_recover_request() {
        String token = "validToken123";
        User user = new User();
        user.setEmail("test@test.com");
        user.setName("Test User");

        UserRecoverRequest request = new UserRecoverRequest();
        request.setToken(token);
        request.setUser(user);
        request.setCreation(new Date());
        request.setCreationIp("127.0.0.1");
        request.setExpiration(new Date());

        assertNotNull(request);
        assertEquals(token, request.getToken());
        assertEquals(user, request.getUser());
        assertFalse(request.isUsed());
        assertNotNull(request.getCreation());
        assertNotNull(request.getExpiration());
        assertEquals("127.0.0.1", request.getCreationIp());
    }

	@Test
	void test_set_used_flag_to_true() {
		UserRecoverRequest request = new UserRecoverRequest();

		request.setUsed(true);

		assertTrue(request.isUsed());
	}

	@Test
	void test_get_recover_returns_set_date() {
		UserRecoverRequest request = new UserRecoverRequest();
		Date recoverDate = new Date();
		request.setRecover(recoverDate);

		Date result = request.getRecover();

		assertEquals(recoverDate, result);
	}

	@Test
	void test_set_valid_recover_date() {
		UserRecoverRequest request = new UserRecoverRequest();
		Date recoverDate = new Date();

		request.setRecover(recoverDate);

		assertEquals(recoverDate, request.getRecover());
	}

	@Test
	void test_returns_stored_ip_address() {
		UserRecoverRequest request = new UserRecoverRequest();
		String expectedIp = "192.168.1.1";
		request.setRecoverIp(expectedIp);

		String actualIp = request.getRecoverIp();

		assertEquals(expectedIp, actualIp);
	}

	@Test
	void test_set_valid_ip_address() {
		UserRecoverRequest request = new UserRecoverRequest();
		String validIp = "192.168.1.1";

		request.setRecoverIp(validIp);

		assertEquals(validIp, request.getRecoverIp());
	}
}