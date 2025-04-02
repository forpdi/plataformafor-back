package org.forpdi.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

class CustomAccessDeniedHandlerTest {

    @Test
    @DisplayName("Deve retornar erro não autorizado quando o acesso for negado")
    void test_handle_access_denied_with_unauthorized_response() throws IOException, ServletException {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        handler.handle(request, response, exception);

        verify(response).sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
    }

	@Test
	public void test_handle_null_request_parameter() throws Exception {
		CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();

		HttpServletResponse response = mock(HttpServletResponse.class);
		AccessDeniedException exception = new AccessDeniedException("Access Denied");

		handler.handle(null, response, exception);

		verify(response).sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
	}
}
