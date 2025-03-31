package org.forpdi.planning.attribute;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolarityMapHelperTest {
	@Mock
	Criteria criteria;
	@Mock
	HibernateDAO dao;
	@InjectMocks
	PolarityMapHelper polarityMapHelper;

	@DisplayName("PolarityMapHelper Construção da classe.")
	@Test
	void testPolarityMapHelperCreation() {
		PolarityMapHelper polarityMapHelper = new PolarityMapHelper();

		assertNotNull(polarityMapHelper, "O objeto instânciado não deveria ser nulo.");
	}

	@DisplayName("PolarityMapHelper Geração do Map que representa Goal e Polarity. Goal possui Parent.")
	@Test
	void testPolarityMapHelperGeneratePolarityMapWithAllGoalsParentsNotNull() {

		PolarityMapHelper polarityMock = mock(PolarityMapHelper.class);
		StructureLevelInstance goal1 = new StructureLevelInstance();
		goal1.setId(1L);
		goal1.setParent(10L);

		StructureLevelInstance goal2 = new StructureLevelInstance();
		goal2.setId(2L);
		goal2.setParent(20L);

		List<StructureLevelInstance> goals = List.of(goal1, goal2);

		AttributeInstance polarity1 = new AttributeInstance();
		StructureLevelInstance parent1 = new StructureLevelInstance();
		parent1.setId(10L);
		polarity1.setLevelInstance(parent1);

		AttributeInstance polarity2 = new AttributeInstance();
		StructureLevelInstance parent2 = new StructureLevelInstance();
		parent2.setId(20L);
		polarity2.setLevelInstance(parent2);

		doCallRealMethod().when(polarityMock).generatePolarityMap(goals);
		when(polarityMock.retrievePolaritiesByLevelInstanceIds(List.of(10L, 20L)))
			.thenReturn(Arrays.asList(polarity1, polarity2));

		Map<Long, AttributeInstance> result = polarityMock.generatePolarityMap(goals);

		assertEquals(2, result.size(), "A quantidade de ocorrências no map não é a esperada.");
		assertEquals(polarity1, result.get(1L), "A polaridade retornada não é a esperada.");
		assertEquals(polarity2, result.get(2L), "A polaridade retornada não é a esperada.");
		verify(polarityMock).retrievePolaritiesByLevelInstanceIds(any());
	}

	@DisplayName("PolarityMapHelper Geração do Map que representa Goal e Polarity. Goal pode não possuir Parent. ")
	@Test
	void testPolarityMapHelperGeneratePolarityMapWithGoalParentNull() {

		PolarityMapHelper polarityMock = mock(PolarityMapHelper.class);
		StructureLevelInstance goal1 = new StructureLevelInstance();
		goal1.setId(1L);
		goal1.setParent(10L);

		StructureLevelInstance goal2 = new StructureLevelInstance();
		goal2.setId(2L);
		goal2.setParent(20L);

		StructureLevelInstance goal3 = new StructureLevelInstance();
		goal3.setId(5L);
		goal3.setParent(null);

		StructureLevelInstance goal4 = new StructureLevelInstance();
		goal4.setId(6L);
		goal4.setParent(30L);

		List<StructureLevelInstance> goals = List.of(goal1, goal2, goal3, goal4);

		AttributeInstance polarity1 = new AttributeInstance();
		StructureLevelInstance parent1 = new StructureLevelInstance();
		parent1.setId(10L);
		polarity1.setLevelInstance(parent1);

		AttributeInstance polarity2 = new AttributeInstance();
		StructureLevelInstance parent2 = new StructureLevelInstance();
		parent2.setId(20L);
		polarity2.setLevelInstance(parent2);

		AttributeInstance polarity4 = new AttributeInstance();
		StructureLevelInstance parent4 = new StructureLevelInstance();
		parent4.setId(30L);
		polarity4.setLevelInstance(parent4);

		doCallRealMethod().when(polarityMock).generatePolarityMap(goals);
		when(polarityMock.retrievePolaritiesByLevelInstanceIds(List.of(10L, 20L, 30L)))
			.thenReturn(Arrays.asList(polarity1, polarity2, polarity4));

		Map<Long, AttributeInstance> result = polarityMock.generatePolarityMap(goals);

		assertEquals(3, result.size(), "A quantidade de ocorrências no map não é a esperada.");
		assertEquals(polarity1, result.get(1L), "A polaridade retornada não é a esperada.");
		assertEquals(polarity2, result.get(2L), "A polaridade retornada não é a esperada.");
		assertEquals(polarity4, result.get(6L), "A polaridade retornada não é a esperada.");
		verify(polarityMock).retrievePolaritiesByLevelInstanceIds(List.of(10L, 20L, 30L));
	}

	@DisplayName("PolarityMapHelper Busca de instâncias com os id passados. Caso lista vazia.")
	@Test
	void testRetrievePolaritiesByLevelInstanceIdsWithEmptyLevelInstanceIds() {
		List<Long> lId = new ArrayList<>();

		List<AttributeInstance> returnedList = polarityMapHelper.retrievePolaritiesByLevelInstanceIds(lId);

		assertTrue(returnedList.isEmpty());
	}

	@DisplayName("PolarityMapHelper Busca de instâncias com os id passados. Caso lista com ids não vazios.")
	@Test
	void testRetrievePolaritiesByLevelInstanceIdsWithNotEmptyLevelInstanceIds() {
		List<Long> levelInstanceIds = Arrays.asList(1L, 2L);

		when(dao.newCriteria(AttributeInstance.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);

		List<AttributeInstance> expected = Arrays.asList(new AttributeInstance(), new AttributeInstance());
		when(dao.findByCriteria(criteria, AttributeInstance.class)).thenReturn(expected);

		List<AttributeInstance> result = polarityMapHelper.retrievePolaritiesByLevelInstanceIds(levelInstanceIds);

		assertEquals(expected, result);
		verify(criteria).createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		verify(criteria, times(2)).add(any(Criterion.class));
	}
}