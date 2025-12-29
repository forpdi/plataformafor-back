package org.forrisco.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FriscoController.class)
@AutoConfigureMockMvc
class FriscoControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("Teste de endpoint GET /frisco sem autenticação")
	@WithAnonymousUser
	void testFriscoEndpointWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/frisco"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "testUser")
	@DisplayName("Teste de endpoint GET /frisco com usuário autenticado")
	void testFriscoEndpointWithMockUser() throws Exception {
		mockMvc.perform(get("/frisco"))
			.andExpect(status().isOk())
			.andExpect(content().string("frisco"));
	}
}