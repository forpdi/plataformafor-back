package org.forpdi.core.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class UserBSTest {

	@DisplayName("UserBS Salvar Usuário, caso ainda não existente no sistema.")
	@Test
	void test_save_user_with_valid_data() {
		User user = new User();
		user.setEmail("test@test.com");
		user.setPassword("password123");

		UserBS userBSMock = mock(UserBS.class);

		PasswordEncoder passwordEncoderMock = mock(PasswordEncoder.class);
		ReflectionTestUtils.setField(userBSMock, "passwordEncoder", passwordEncoderMock);

		when(passwordEncoderMock.encode(user.getPassword()))
			.thenReturn("$2a$12$ZJ8kPYpsUh1Glgj7iJrtHuELNnhc6hj7D3uDZydENCw0nvk6DIzwG\n");
		when(userBSMock.existsByEmail(user.getEmail())).thenReturn(null);
		doCallRealMethod().when(userBSMock).save(user);

		userBSMock.save(user);

		verify(userBSMock).existsByEmail(user.getEmail());
		verify(passwordEncoderMock).encode("password123");
		verify(userBSMock).persist(user);
		assertTrue(user.isActive());
		assertFalse(user.isDeleted());
		assertNotNull(user.getCreation());
	}

	@DisplayName("UserBS Salvar Usuário, caso onde o e-mail já está cadastrado no sistema.")
	@Test
	void test_save_user_already_on_system() {
		User user = new User();
		user.setEmail("test@test.com");
		user.setPassword("password123");

		UserBS userBSMock = mock(UserBS.class);

		PasswordEncoder passwordEncoderMock = mock(PasswordEncoder.class);
		ReflectionTestUtils.setField(userBSMock, "passwordEncoder", passwordEncoderMock);

		when(passwordEncoderMock.encode(user.getPassword()))
			.thenReturn("$2a$12$ZJ8kPYpsUh1Glgj7iJrtHuELNnhc6hj7D3uDZydENCw0nvk6DIzwG\n");
		when(userBSMock.existsByEmail(user.getEmail())).thenReturn(user);
		doCallRealMethod().when(userBSMock).save(user);

		Exception exception = assertThrows(IllegalArgumentException.class,
			() -> userBSMock.save(user),
			"Era esperado a geração de IllegalArgumentException devido o email já existir em um usuário.");
		assertEquals("Já existe um usuários cadastrado com este e-mail.", exception.getMessage(),
			"A mensagem da exception não é a esperada.");
	}

}