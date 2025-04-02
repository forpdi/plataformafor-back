package org.forpdi.dashboard.goalsinfo;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.jobsetup.JobLockService;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("Teste da classe GoalsInfoCalculatorTask")
@ExtendWith(MockitoExtension.class)
class GoalsInfoCalculatorTaskTest {
	@Spy
	@InjectMocks
	private GoalsInfoCalculatorTask task;

	@Mock
	private HibernateDAO dao;

	@Mock
	private GoalsInfoBS bs;

	@Mock
	private JobLockService jobLockService;
	@Mock
	Criteria criteria;

	@Test
	@DisplayName("Teste de execução da tarefa com sucesso")
	void testExecuteSuccess() {
		when(jobLockService.lockJob(eq(GoalsInfoCalculatorTask.class), any(LocalDateTime.class))).thenReturn(true);

		PlanMacro planMacro1 = new PlanMacro();
		PlanMacro planMacro2 = new PlanMacro();
		planMacro1.setId(2L);
		planMacro2.setId(3L);
		List<PlanMacro> planMacros = Arrays.asList(planMacro1, planMacro2);
		doReturn(planMacros).when(task).listPlanMacros();

		StructureLevelInstance goal1 = mock(StructureLevelInstance.class);
		StructureLevelInstance goal2 = mock(StructureLevelInstance.class);
		List<StructureLevelInstance> goals1 = Arrays.asList(goal1, goal2);
		List<StructureLevelInstance> goals2 = Collections.singletonList(goal1);

		doReturn(goals1).when(task).listGoalsByPlanMacro(planMacro1);
		doReturn(goals2).when(task).listGoalsByPlanMacro(planMacro2);

		GoalsInfo newInfo = new GoalsInfo();
		GoalsInfo existentInfo = new GoalsInfo();
		existentInfo.setId(2L);

		when(bs.calculateAdminGoalsInfo(goals1)).thenReturn(newInfo);
		when(bs.retrieveGoalsInfoByPlanMacro(planMacro1.getId())).thenReturn(existentInfo);
		when(bs.calculateAdminGoalsInfo(goals2)).thenReturn(newInfo);
		when(bs.retrieveGoalsInfoByPlanMacro(planMacro2.getId())).thenReturn(null);

		task.execute();

		verify(dao, times(1)).merge(existentInfo);
		verify(dao, times(1)).merge(newInfo);
		verify(jobLockService, times(1))
			.lockJob(eq(GoalsInfoCalculatorTask.class), any(LocalDateTime.class));
	}

	@Test
	@DisplayName("Teste de execução da tarefa sem bloquear o job")
	void testExecuteWithoutLock() {
		when(jobLockService.lockJob(eq(GoalsInfoCalculatorTask.class), any(LocalDateTime.class))).thenReturn(false);

		task.execute();

		verify(jobLockService, times(1))
			.lockJob(eq(GoalsInfoCalculatorTask.class), any(LocalDateTime.class));
		verifyNoInteractions(dao, bs);
	}

	@Test
	@DisplayName("Teste de listagem de metas por plano macro")
	void testListGoalsByPlanMacro() {
		PlanMacro planMacro = mock(PlanMacro.class);
		List<StructureLevelInstance> mockGoals = Arrays.asList(mock(StructureLevelInstance.class), mock(StructureLevelInstance.class));

		when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(StructureLevelInstance.class))).thenReturn(mockGoals);

		List<StructureLevelInstance> goals = task.listGoalsByPlanMacro(planMacro);

		assertEquals(2, goals.size());
		verify(dao, times(1)).findByCriteria(any(Criteria.class), eq(StructureLevelInstance.class));
	}

	@Test
	@DisplayName("Teste de listagem de planos macro")
	void testListPlanMacros() {
		PlanMacro planMacro1 = mock(PlanMacro.class);
		PlanMacro planMacro2 = mock(PlanMacro.class);
		List<PlanMacro> mockPlanMacros = Arrays.asList(planMacro1, planMacro2);

		when(dao.newCriteria(PlanMacro.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(PlanMacro.class))).thenReturn(mockPlanMacros);

		List<PlanMacro> planMacros = task.listPlanMacros();

		assertEquals(2, planMacros.size());
		verify(dao, times(1)).findByCriteria(any(Criteria.class), eq(PlanMacro.class));
	}
}