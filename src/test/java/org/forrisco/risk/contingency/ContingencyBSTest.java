package org.forrisco.risk.contingency;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.*;
import org.forrisco.risk.Risk;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContingencyBSTest {

	@Mock
	HibernateDAO dao;

	@Mock
	EntityManager entityManager;

	@Mock
	Criteria criteria;

	@Mock
	CriteriaBuilder criteriaBuilder;

	@Mock
	CriteriaQuery<Contingency> criteriaQuery;

	@Mock
	Root<Contingency> root;

	@Mock
	TypedQuery<Contingency> typedQuery;

	@InjectMocks
	ContingencyBS contingencyBS;

	@DisplayName("ContingencyBS Salvar Contingência")
	@Test
	void testSaveContingency() {
		Contingency contingencyToSave = new Contingency();

		contingencyBS.saveContingency(contingencyToSave);

		assertFalse(contingencyToSave.isDeleted());
		verify(dao, atMostOnce()).persist(contingencyToSave);
	}

	@DisplayName("ContingencyBS Deletar Contingência.")
	@Test
	void testDelete() {
		Contingency contingencyToDelete = new Contingency();

		contingencyBS.delete(contingencyToDelete);

		assertTrue(contingencyToDelete.isDeleted());
		verify(dao, atMostOnce()).persist(contingencyToDelete);
	}

	@DisplayName("ContingencyBS Retornar os contingenciamentos a partir de um risco.")
	@Test
	void testListContingenciesByRisk() {
		Risk risk = new Risk();

		PaginatedList<Contingency> expectedList = new PaginatedList<>();
		expectedList.setList(List.of(new Contingency()));
		expectedList.setTotal(1L);

		ContingencyBS contingencyBsLocalMock = mock(ContingencyBS.class);
		doCallRealMethod().when(contingencyBsLocalMock).listContingenciesByRisk(risk);
		when(contingencyBsLocalMock.listContingenciesByRisk(risk, -1)).thenReturn(expectedList);

		PaginatedList<Contingency> returnedList = contingencyBsLocalMock.listContingenciesByRisk(risk);

		verify(contingencyBsLocalMock, times(1)).listContingenciesByRisk(risk, -1);
		assertNotNull(returnedList.getList());
		assertEquals(expectedList.getTotal(), returnedList.getTotal());
	}

	@DisplayName("ContingencyBS Listar as Contingências vinculadas a um riso, quando ano válido.")
	@Test
	void testListContingenciesByRiskAndValidSelectedYear() {
		Risk riskOfContingencies = new Risk();

		Contingency contingencyA = new Contingency();
		contingencyA.setRisk(riskOfContingencies);
		Contingency contingencyB = new Contingency();
		contingencyB.setRisk(riskOfContingencies);
		PaginatedList<Contingency> expectedList = new PaginatedList<>();
		expectedList.setList(Arrays.asList(contingencyA, contingencyB));
		expectedList.setTotal(2L);

		Integer selectedYear = 2023;

		when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(Contingency.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(Contingency.class)).thenReturn(root);
		when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(expectedList.getList());

		PaginatedList<Contingency> returnedList = contingencyBS.listContingenciesByRisk(riskOfContingencies, selectedYear);

		verify(entityManager, atMostOnce()).getCriteriaBuilder();
		verify(criteriaBuilder, atMostOnce()).createQuery(Contingency.class);
		verify(criteriaQuery, atMostOnce()).from(Contingency.class);
		verify(entityManager, atMostOnce()).createQuery(criteriaQuery);
		verify(criteriaBuilder, atMostOnce()).function("year", Integer.class, root.get("begin"));
		verify(criteriaBuilder, atMostOnce()).equal(any(Expression.class), any(Integer.class));
		verify(typedQuery, atMostOnce()).getResultList();
		assertEquals(expectedList.getTotal(), returnedList.getTotal());

	}

	@DisplayName("ContingencyBS Listar as Contingências vinculadas a um riso, quando ano(-1).")
	@Test
	void testListContingenciesByRiskAndInvalidSelectedYear() {
		Risk riskOfContingencies = new Risk();

		Contingency contingencyA = new Contingency();
		contingencyA.setRisk(riskOfContingencies);
		Contingency contingencyB = new Contingency();
		contingencyB.setRisk(riskOfContingencies);
		PaginatedList<Contingency> expectedList = new PaginatedList<>();
		expectedList.setList(Arrays.asList(contingencyA, contingencyB));
		expectedList.setTotal(2L);

		Integer selectedYear = -1;

		when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
		when(criteriaBuilder.createQuery(Contingency.class)).thenReturn(criteriaQuery);
		when(criteriaQuery.from(Contingency.class)).thenReturn(root);
		when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(expectedList.getList());

		PaginatedList<Contingency> returnedList = contingencyBS.listContingenciesByRisk(riskOfContingencies, selectedYear);

		verify(entityManager, atMostOnce()).getCriteriaBuilder();
		verify(criteriaBuilder, atMostOnce()).createQuery(Contingency.class);
		verify(criteriaQuery, atMostOnce()).from(Contingency.class);
		verify(entityManager, atMostOnce()).createQuery(criteriaQuery);
		verify(criteriaBuilder, times(0))
			.function("year", Integer.class, root.get("begin"));
		verify(criteriaBuilder, times(0))
			.equal(any(Expression.class), any(Integer.class));
		verify(typedQuery, atMostOnce()).getResultList();
		assertEquals(expectedList.getTotal(), returnedList.getTotal());

	}

	@DisplayName("ContingencyBS Listar as Contingências vinculadas a um risco com filtragem completa.")
	@Test
	void testListContingenciesByRiskAndAllDefaultParams() {
		Risk risk = new Risk();
		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();
		paramsFilter.setTerm("de");
		paramsFilter.setSortedBy(new String[]{"name", "desc"});

		Contingency contingencyA = new Contingency();
		contingencyA.setRisk(risk);
		contingencyA.setAction("Ação de contingência.");

		List<Contingency> listExpected = List.of(contingencyA);

		when(dao.newCriteria(Contingency.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class)))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.addOrder(any(Order.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Contingency.class)))
			.thenReturn(listExpected);
		when(criteria.uniqueResult())
			.thenReturn(1L);

		PaginatedList<Contingency> result = contingencyBS.listContingenciesByRisk(risk, paramsFilter);

		verify(dao, times(2))
			.newCriteria(Contingency.class);
		verify(criteria, times(4))
			.createAlias(anyString(), anyString(), any(JoinType.class));
		verify(criteria)
			.setProjection(any(Projection.class));
		verify(criteria, atMostOnce())
			.addOrder(paramsFilter.getSortOrder());
		verify(criteria, times(6))
			.add(any(Criterion.class));
		verify(criteria, atMostOnce())
			.setFirstResult(anyInt());
		verify(criteria, atMostOnce())
			.setMaxResults(anyInt());
		assertEquals(1L, result.getList().size(),
			"O tamanho da lista retornada não é o esperado.");
		assertEquals(contingencyA.getAction(), result.getList().get(0).getAction(),
			"A ação de contingência não corresponde a esperada.");
	}

	@DisplayName("ContingencyBS Listar as Contingências vinculadas a um risco com filtragem sem termo.")
	@Test
	void testListContingenciesByRiskWithDefaultParamsWithoutTerm() {
		Risk risk = new Risk();
		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();
		paramsFilter.setTerm(null);
		paramsFilter.setSortedBy(new String[]{"name", "desc"});

		Contingency contingencyA = new Contingency();
		contingencyA.setRisk(risk);
		contingencyA.setAction("Ação de contingência.");

		List<Contingency> listExpected = List.of(contingencyA);

		when(dao.newCriteria(Contingency.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class)))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.addOrder(any(Order.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Contingency.class)))
			.thenReturn(listExpected);
		when(criteria.uniqueResult())
			.thenReturn(1L);

		PaginatedList<Contingency> result = contingencyBS.listContingenciesByRisk(risk, paramsFilter);

		verify(dao, times(2))
			.newCriteria(Contingency.class);
		verify(criteria, times(4))
			.createAlias(anyString(), anyString(), any(JoinType.class));
		verify(criteria)
			.setProjection(any(Projection.class));
		verify(criteria, atMostOnce())
			.addOrder(paramsFilter.getSortOrder());
		verify(criteria, times(4))
			.add(any(Criterion.class));
		verify(criteria, atMostOnce())
			.setFirstResult(anyInt());
		verify(criteria, atMostOnce())
			.setMaxResults(anyInt());
		assertEquals(1L, result.getList().size(),
			"O tamanho da lista retornada não é o esperado.");
		assertEquals(contingencyA.getAction(), result.getList().get(0).getAction(),
			"A ação de contingência não corresponde a esperada.");
	}

	@DisplayName("ContingencyBS Listar as Contingências vinculadas a um risco com filtragem sem classificação.")
	@Test
	void testListContingenciesByRiskWithDefaultParamsWithoutSorting() {
		Risk risk = new Risk();
		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();
		paramsFilter.setTerm("Valor");

		Contingency contingencyA = new Contingency();
		contingencyA.setRisk(risk);
		contingencyA.setAction("Ação de contingência.");

		List<Contingency> listExpected = List.of(contingencyA);

		when(dao.newCriteria(Contingency.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class)))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Contingency.class)))
			.thenReturn(listExpected);
		when(criteria.uniqueResult())
			.thenReturn(1L);

		PaginatedList<Contingency> result = contingencyBS.listContingenciesByRisk(risk, paramsFilter);

		verify(dao, times(2))
			.newCriteria(Contingency.class);
		verify(criteria, times(4))
			.createAlias(anyString(), anyString(), any(JoinType.class));
		verify(criteria)
			.setProjection(any(Projection.class));
		verify(criteria, times(0))
			.addOrder(any(Order.class));
		verify(criteria, times(6))
			.add(any(Criterion.class));
		verify(criteria, atMostOnce())
			.setFirstResult(anyInt());
		verify(criteria, atMostOnce())
			.setMaxResults(anyInt());
		assertEquals(1L, result.getList().size(),
			"O tamanho da lista retornada não é o esperado.");
		assertEquals(contingencyA.getAction(), result.getList().get(0).getAction(),
			"A ação de contingência não corresponde a esperada.");
	}

	@DisplayName("ContingencyBS Buscar Contingência pelo Id.")
	@Test
	void testFindByContingencyId() {
		Contingency expectedContingency = new Contingency();
		expectedContingency.setId(3L);
		long findValue = 3L;

		when(dao.newCriteria(Contingency.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(expectedContingency);

		Contingency returnedContingency = contingencyBS.findByContingencyId(findValue);

		verify(dao, atMostOnce()).newCriteria(Contingency.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(criteria, atMostOnce()).uniqueResult();
		assertEquals(expectedContingency.getId(), returnedContingency.getId(),
			"O id da contingência retornada não corresponde ao esperado.");
	}
}