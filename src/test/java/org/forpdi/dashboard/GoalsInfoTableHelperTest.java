package org.forpdi.dashboard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.dashboard.goalsinfo.GoalsInfoBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GoalsInfoTableHelperTest {

	@InjectMocks
	private GoalsInfoTableHelper helper;

	@Mock
	private HibernateDAO dao;

	@Mock
	private GoalsInfoBS goalsInfoBS;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Testa setIndicators com lista de metas válida")
	void testSetIndicatorsValidGoals() {
		List<StructureLevelInstance> goals = new ArrayList<>();
		StructureLevelInstance goal1 = mock(StructureLevelInstance.class);
		StructureLevelInstance goal2 = mock(StructureLevelInstance.class);
		when(goal1.getParent()).thenReturn(1L);
		when(goal2.getParent()).thenReturn(2L);
		goals.add(goal1);
		goals.add(goal2);

		StructureLevelInstance parent1 = mock(StructureLevelInstance.class);
		StructureLevelInstance parent2 = mock(StructureLevelInstance.class);
		when(parent1.getId()).thenReturn(1L);
		when(parent2.getId()).thenReturn(2L);

		Criteria mockCriteria = mock(Criteria.class);
		when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(mockCriteria);
		when(mockCriteria.add(any())).thenReturn(mockCriteria);
		when(dao.findByCriteria(mockCriteria, StructureLevelInstance.class))
			.thenReturn(List.of(parent1, parent2));

		helper.setIndicators(goals);

		verify(goal1).setLevelParent(parent1);
		verify(goal2).setLevelParent(parent2);
	}

	@Test
	@DisplayName("Testa setIndicators com lista de metas vazia")
	void testSetIndicatorsEmptyGoals() {
		List<StructureLevelInstance> goals = new ArrayList<>();
		helper.setIndicators(goals);

		verifyNoInteractions(dao);
	}

	@Test
	@DisplayName("Testa setObjectives com lista de metas válida")
	void testSetObjectivesValidGoals() {
		List<StructureLevelInstance> goals = new ArrayList<>();
		StructureLevelInstance goal = mock(StructureLevelInstance.class);
		StructureLevelInstance levelParent = mock(StructureLevelInstance.class);
		when(goal.getLevelParent()).thenReturn(levelParent);
		when(levelParent.getParent()).thenReturn(1L);
		goals.add(goal);

		StructureLevelInstance objectiveParent = mock(StructureLevelInstance.class);
		when(objectiveParent.getId()).thenReturn(1L);

		Criteria mockCriteria = mock(Criteria.class);
		when(dao.newCriteria(StructureLevelInstance.class)).thenReturn(mockCriteria);
		when(mockCriteria.add(any())).thenReturn(mockCriteria);
		when(dao.findByCriteria(mockCriteria, StructureLevelInstance.class))
			.thenReturn(List.of(objectiveParent));

		helper.setObjectives(goals);

		verify(levelParent).setLevelParent(objectiveParent);
	}

	@Test
	@DisplayName("Testa setObjectives com lista de metas vazia")
	void testSetObjectivesEmptyGoals() {
		List<StructureLevelInstance> goals = new ArrayList<>();
		helper.setObjectives(goals);

		verifyNoInteractions(dao);
	}

	@Test
	@DisplayName("Testa generateGoalsInfo com lista de metas válida")
	void testGenerateGoalsInfo() {
		List<StructureLevelInstance> goals = new ArrayList<>();
		StructureLevelInstance goal1 = mock(StructureLevelInstance.class);
		StructureLevelInstance goal2 = mock(StructureLevelInstance.class);
		goals.add(goal1);
		goals.add(goal2);

		GoalsInfoTable goalInfo1 = mock(GoalsInfoTable.class);
		GoalsInfoTable goalInfo2 = mock(GoalsInfoTable.class);
		when(goalsInfoBS.generateGoalInfo(goal1)).thenReturn(goalInfo1);
		when(goalsInfoBS.generateGoalInfo(goal2)).thenReturn(goalInfo2);

		List<GoalsInfoTable> result = helper.generateGoalsInfo(goals);

		assertEquals(2, result.size());
		assertTrue(result.contains(goalInfo1));
		assertTrue(result.contains(goalInfo2));
	}

	@Test
	@DisplayName("Testa generateGoalsInfo com lista de metas vazia")
	void testGenerateGoalsInfoEmptyGoals() {
		List<StructureLevelInstance> goals = new ArrayList<>();
		List<GoalsInfoTable> result = helper.generateGoalsInfo(goals);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
}
