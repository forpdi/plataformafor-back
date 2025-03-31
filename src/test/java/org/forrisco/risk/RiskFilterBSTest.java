package org.forrisco.risk;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Projection;
import org.forrisco.risk.links.*;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RiskFilterBSTest {

	@InjectMocks
	@Spy
	RiskFilterBS bs;

	@Mock
	HibernateDAO dao;

	@Mock
	Criteria criteria;

	@Mock
	Criteria count;

	@Test
	@DisplayName("Aplicar filtros ao filtrar por unidades")
	void testApplyFiltersFilteringByUnits() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);
		when(filterParams.filteringByUnits()).thenReturn(true);

		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por termo")
	void testApplyFiltersFilteringByTerm() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);
		when(filterParams.filteringByTerm()).thenReturn(true);
		when(filterParams.getTerm()).thenReturn("Aceitar");

		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por processos")
	void testApplyFiltersFilteringByProcesses() {
		List<Long> processesId = new ArrayList<>(List.of(1L, 3L, 4L));
		RiskFilterParams filterParams = mock(RiskFilterParams.class);
		when(filterParams.filteringByProcesses()).thenReturn(true);
		when(filterParams.getProcessesId()).thenReturn(processesId);
		doReturn(List.of(1L, 2L)).when(bs).listRiskIdsLinked(processesId, "process", RiskActivity.class);
		doReturn(List.of(7L, 12L)).when(bs).listRiskIdsLinkedToProcessObjectives(processesId);

		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por PDIs vinculados")
	void testApplyFiltersFilteringByLinkedPdis() {
		List<Long> linkedPdiIds = new ArrayList<>(List.of(2L, 9L, 12L));
		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByLinkedPdis()).thenReturn(true);
		when(filterParams.getLinkedPdiIds()).thenReturn(linkedPdiIds);
		doReturn(List.of(3L, 6L)).when(bs).listRiskIdsLinked(linkedPdiIds, "structure", RiskAxis.class);
		doReturn(List.of(45L, 22L)).when(bs).listRiskIdsLinked(linkedPdiIds, "structure", RiskStrategy.class);
		doReturn(List.of(19L, 25L)).when(bs).listRiskIdsLinked(linkedPdiIds, "structure", RiskIndicator.class);
		doReturn(List.of(11L, 23L)).when(bs).listRiskIdsLinked(linkedPdiIds, "structure", RiskGoal.class);

		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por nome ou código")
	void testApplyFiltersFilteringByNameOrCode() {
		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByNameOrCode()).thenReturn(true);

		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por tipo")
	void testApplyFiltersFilteringByType() {
		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByType()).thenReturn(true);

		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por tipologias")
	void testApplyFiltersFilteringByTypologies() {
		List<String> typologies = new ArrayList<>(List.of("Typo 1", "Typo 2"));
		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByTypologies()).thenReturn(true);
		when(filterParams.getTypologies()).thenReturn(typologies);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por respostas com nenhuma resposta")
	void testApplyFiltersFilteringByResponses() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByResponses()).thenReturn(true);
		when(filterParams.filteringByNoneResponse()).thenReturn(true);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por respostas sem caso de nenhuma resposta")
	void testApplyFiltersFilteringByResponsesNoneCase() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByResponses()).thenReturn(true);
		when(filterParams.filteringByNoneResponse()).thenReturn(false);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por níveis com nenhum nível")
	void testApplyFiltersFilteringByLevels() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByLevels()).thenReturn(true);
		when(filterParams.filteringByNoneLevels()).thenReturn(true);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por níveis sem casos de nenhum nível")
	void testApplyFiltersFilteringByLevelsNoneCase() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByLevels()).thenReturn(true);
		when(filterParams.filteringByNoneLevels()).thenReturn(false);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por início de criação")
	void testApplyFiltersFilteringByStartCreation() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByStartCreation()).thenReturn(true);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por fim de criação")
	void testApplyFiltersFilteringByEndCreation() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByEndCreation()).thenReturn(true);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Aplicar filtros ao filtrar por arquivados")
	void testApplyFiltersFilteringByArchivedCreation() {

		RiskFilterParams filterParams = mock(RiskFilterParams.class);

		when(filterParams.filteringByArchived()).thenReturn(true);
		bs.applyFilters(filterParams, criteria, count);

		verify(bs).applyFilters(filterParams, criteria, count);
	}

	@Test
	@DisplayName("Listar IDs de riscos vinculados a uma entidade")
	void testListRiskIdsLinked() {
		List<Long> linkedPdiIds = new ArrayList<>(List.of(2L));

		when(dao.newCriteria(isA(Class.class))).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Long.class)).thenReturn(List.of(1L, 5L, 7L));

		List<Long> returnedIdList = bs.listRiskIdsLinked(linkedPdiIds, "Column", RiskActivity.class);

		assertTrue(returnedIdList.contains(1L));
		assertTrue(returnedIdList.contains(5L));
		assertTrue(returnedIdList.contains(7L));
	}

	@Test
	@DisplayName("Listar IDs de riscos vinculados aos objetivos do processo")
	void testListRiskIdsLinkedToProcessObjectives() {
		List<Long> linkedIds = new ArrayList<>(List.of(2L));

		when(dao.newCriteria(RiskProcessObjective.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Long.class)).thenReturn(List.of(1L, 5L, 7L));

		List<Long> returnedIdList = bs.listRiskIdsLinkedToProcessObjectives(linkedIds);

		assertTrue(returnedIdList.contains(1L));
		assertTrue(returnedIdList.contains(5L));
		assertTrue(returnedIdList.contains(7L));
	}
}