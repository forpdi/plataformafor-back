package org.forpdi.system.accesslog;

import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class AccessLogBSTest {

	@Mock
	private UserAccessLogRepository userAccessLogRepository;

	@Mock
	private AccessLogHistoryRepository accessLogHistoryRepository;

	@Mock
	private CompanyDomainContext domain;

	@InjectMocks
	private AccessLogBS accessLogBS;

	private User user;
	private UserAccessLog userAccessLog;
	private AccessLogHistory accessLogHistory;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Criando mocks para as dependências
		user = mock(User.class);
		userAccessLog = new UserAccessLog();
		accessLogHistory = new AccessLogHistory();

		when(domain.get()).thenReturn(mock(CompanyDomain.class));
		when(user.getId()).thenReturn(1L);
	}

	@Test
	@DisplayName("Testa o método fpdiAccess() para um usuário SYSTEM_ADMIN que não deve ser logado")
	void testFpdiAccessForSystemAdmin() {
		when(user.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());

		accessLogBS.fpdiAccess(user);

		verify(userAccessLogRepository, never()).save(any());
	}

	@Test
	@DisplayName("Testa o método friscoAccess() para um usuário SYSTEM_ADMIN que não deve ser logado")
	void testFriscoAccessForSystemAdmin() {
		when(user.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());

		accessLogBS.friscoAccess(user);

		verify(userAccessLogRepository, never()).save(any());
	}

	@Test
	@DisplayName("Testa o método listAccessLogHistory()")
	void testListAccessLogHistory() {
		when(accessLogHistoryRepository.findAll()).thenReturn(List.of(accessLogHistory));

		List<AccessLogHistory> result = accessLogBS.listAccessLogHistory();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Testa o método listAccessLogHistoryByPeriod()")
	void testListAccessLogHistoryByPeriod() {
		LocalDate begin = LocalDate.now().minusDays(1);
		LocalDate end = LocalDate.now();

		when(accessLogHistoryRepository.findById_DateBetweenOrderById_DateAsc(begin, end))
			.thenReturn(List.of(accessLogHistory));

		List<AccessLogHistory> result = accessLogBS.listAccessLogHistoryByPeriod(begin, end);

		assertNotNull(result);
		assertEquals(1, result.size());
	}
}
