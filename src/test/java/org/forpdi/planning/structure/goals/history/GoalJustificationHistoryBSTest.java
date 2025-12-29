package org.forpdi.planning.structure.goals.history;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.user.User;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.planning.structure.goals.history.GoalJustificationHistory.JustificationAndReachedValue;
import org.forpdi.security.auth.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GoalJustificationHistoryBSTest {

	@Mock
	private UserSession userSession;

	@Mock
	private StructureLevelInstance levelInstance;

	@Mock
	private JustificationAndReachedValue oldJustificationAndReachedValue;

	@Mock
	private JustificationAndReachedValue newJustificationAndReachedValue;

	@Mock
	Criteria criteria;

	@Mock
	HibernateDAO dao;

	@InjectMocks
	private GoalJustificationHistoryBS goalJustificationHistoryBS;

	@Test
	@DisplayName("Deve salvar corretamente o histórico da justificativa e valor atingido")
	void testSaveHistory() {
		when(oldJustificationAndReachedValue.getJustification()).thenReturn("Justificação antiga");
		when(oldJustificationAndReachedValue.getReachedValue()).thenReturn(100.0);
		when(userSession.getUser()).thenReturn(new User());  // Simula o usuário autenticado
		doNothing().when(dao).persist(any(GoalJustificationHistory.class));

		goalJustificationHistoryBS.saveHistory(oldJustificationAndReachedValue, newJustificationAndReachedValue, levelInstance);

		verify(dao, times(1)).persist(any(GoalJustificationHistory.class));

		ArgumentCaptor<GoalJustificationHistory> captor = ArgumentCaptor.forClass(GoalJustificationHistory.class);
		verify(dao).persist(captor.capture());

		GoalJustificationHistory savedHistory = captor.getValue();
		assertEquals("Justificação antiga", savedHistory.getJustification());
		assertEquals(100.0, savedHistory.getReachedValue());
	}

	@Test
	@DisplayName("Deve listar histórico de justificativas por nível de instância")
	void testListByLevelInstance() {
		List<GoalJustificationHistory> expectedList = mock(List.class);
		when(dao.newCriteria(GoalJustificationHistory.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(any(), eq(GoalJustificationHistory.class)))
			.thenReturn(expectedList);

		List<GoalJustificationHistory> result = goalJustificationHistoryBS.listByLevelInstance(1L);

		assertNotNull(result);
		assertEquals(expectedList, result);
	}
}
