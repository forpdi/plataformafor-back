package org.forpdi.security.auth;

import org.forpdi.core.user.User;
import org.forpdi.core.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private UserSession userSession;

	@Test
	@DisplayName("Quando o nível de acesso for obtido, então deve retornar o nível de acesso correto do usuário autenticado")
	void givenAuthenticatedUser_whenGetAccessLevel_thenShouldReturnAccessLevel() {
		AuthData authData = mock(AuthData.class);
		when(authData.getAccessLevel()).thenReturn(1);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(authentication.getPrincipal()).thenReturn(authData);

		int accessLevel = userSession.getAccessLevel();

		assertEquals(1, accessLevel, "O nível de acesso deve ser o correto.");
	}

	@Test
	@DisplayName("Quando o usuário for obtido, então deve retornar o usuário correto do repositório")
	void givenAuthenticatedUser_whenGetUser_thenShouldReturnUser() {
		long userId = 123L;
		AuthData authData = mock(AuthData.class);
		User user = mock(User.class);
		when(authData.getId()).thenReturn(userId);
		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(authentication.getPrincipal()).thenReturn(authData);

		User retrievedUser = userSession.getUser();

		assertEquals(user, retrievedUser, "O usuário retornado deve ser o correto.");
		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("Quando as permissões forem obtidas, então deve retornar uma lista vazia de permissões")
	void givenNoPermissions_whenGetPermissions_thenShouldReturnEmptyList() {
		AuthData authData = mock(AuthData.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		List<String> permissions = userSession.getPermissions();

		assertTrue(permissions.isEmpty(), "A lista de permissões deve estar vazia.");
	}

	@Test
	@DisplayName("Quando o usuário não estiver autenticado, então deve retornar false para isLogged")
	void whenUserIsNotAuthenticated_thenIsLoggedShouldReturnFalse() {
		SecurityContextHolder.getContext().setAuthentication(null);

		boolean logged = userSession.isLogged();

		assertFalse(logged, "O usuário não autenticado deve retornar false.");
	}

	@Test
	@DisplayName("Quando o usuário estiver autenticado, então deve retornar true para isLogged")
	void whenUserIsAuthenticated_thenIsLoggedShouldReturnTrue() {
		AuthData authData = mock(AuthData.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(authentication.getPrincipal()).thenReturn(authData);

		boolean logged = userSession.isLogged();

		assertTrue(logged, "O usuário autenticado deve retornar true.");
	}

	@Test
	@DisplayName("Quando os dados de autenticação forem obtidos, então deve lançar exceção se o usuário não estiver autenticado")
	void whenUserIsNotAuthenticated_thenGetAuthDataShouldThrowException() {
		SecurityContextHolder.getContext().setAuthentication(null);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> userSession.getAuthData());
		assertEquals("User not authenticated", thrown.getMessage(), "Deve lançar exceção indicando que o usuário não está autenticado.");
	}

	@Test
	@DisplayName("Quando os dados de autenticação forem obtidos, então deve retornar os dados de autenticação corretos")
	void whenUserIsAuthenticated_thenGetAuthDataShouldReturnAuthData() {
		AuthData authData = mock(AuthData.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(authentication.getPrincipal()).thenReturn(authData);

		AuthData retrievedAuthData = userSession.getAuthData();

		assertEquals(authData, retrievedAuthData, "Os dados de autenticação devem ser os corretos.");
	}
}
