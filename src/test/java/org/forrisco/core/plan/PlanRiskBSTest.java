package org.forrisco.core.plan;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ItemToSelect;
import org.forpdi.core.common.*;
import org.forpdi.core.company.Company;
import org.forrisco.core.policy.Policy;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanRiskBSTest {

	@Mock
	HibernateDAO dao;

	@Mock
	Criteria criteria;

	@InjectMocks
	PlanRiskBS planRiskBS;

	@DisplayName("PlanRiskBS Retornar Plano de Risco pelo Id.")
	@Test
	void testRetrieveById() {
		long planRiskId = 1L;
		PlanRisk planExpected = new PlanRisk();
		planExpected.setId(planRiskId);

		when(dao.newCriteria(PlanRisk.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(planExpected);

		PlanRisk planRiskReturned = planRiskBS.retrieveById(planRiskId);

		verify(dao).newCriteria(PlanRisk.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(criteria).uniqueResult();
		assertEquals(planExpected.getId(), planRiskReturned.getId(),
			"Eram esperados valores iguais para os ids");
	}

	@DisplayName("PlanRiskBS Salvar um novo Plano de Risco.")
	@Test
	void testSave() {
		PlanRisk planRiskToSave = new PlanRisk();

		planRiskBS.save(planRiskToSave);

		assertNull(planRiskToSave.getId());
		assertFalse(planRiskToSave.isDeleted());
		verify(dao).persist(planRiskToSave);
	}

	@DisplayName("PlanRiskBS Listar todos os planos de risco com filtragem. Todos os parâmetros fornecidos. ")
	@Test
	void testListPlanRiskWithAllParamsFilters() {

		Company company = new Company();
		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();
		paramsFilter.setTerm("Val");
		paramsFilter.setSortedBy(new String[]{"name", "asc"});

		PlanRisk planA = new PlanRisk();
		planA.setName("Validar riscos ocupacionais");

		List<PlanRisk> listExpected = List.of(planA);

		when(dao.findByCriteria(any(Criteria.class), eq(PlanRisk.class)))
			.thenReturn(listExpected);
		when(dao.newCriteria(PlanRisk.class))
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
		when(criteria.uniqueResult())
			.thenReturn(1L);

		PaginatedList<PlanRisk> result = planRiskBS.listPlanRisk(company, paramsFilter);

		verify(dao, times(2))
			.newCriteria(PlanRisk.class);
		verify(criteria, times(2))
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
		assertEquals(planA.getName(), result.getList().get(0).getName(),
			"O plano de risco retornado não corresponde ao esperado.");
	}

	@DisplayName("PlanRiskBS Listar todos os planos de risco com filtragem. Termo não fornecido.")
	@Test
	void testListPlanRiskWithoutTerm() {

		Company company = new Company();
		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();

		PlanRisk planA = new PlanRisk();
		planA.setName("Validar riscos ocupacionais");
		planA.setId(4L);

		PlanRisk planB = new PlanRisk();
		planB.setName("Riscos ambientais");
		planB.setId(3L);

		PlanRisk planC = new PlanRisk();
		planC.setName("Plano de Riscos Reforma Setor X");
		planC.setId(1L);

		List<PlanRisk> allPlans = Arrays.asList(planA, planB, planC);
		List<PlanRisk> listExpectedSorted = new ArrayList<>(allPlans);
		listExpectedSorted.sort(Comparator.comparingLong(PlanRisk::getId));

		when(dao.findByCriteria(any(Criteria.class), eq(PlanRisk.class)))
			.thenReturn(listExpectedSorted);
		when(dao.newCriteria(PlanRisk.class))
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
		when(criteria.uniqueResult())
			.thenReturn(3L);

		PaginatedList<PlanRisk> result = planRiskBS.listPlanRisk(company, paramsFilter);

		verify(dao, times(2))
			.newCriteria(PlanRisk.class);
		verify(criteria, times(2))
			.createAlias(anyString(), anyString(), any(JoinType.class));
		verify(criteria)
			.setProjection(any(Projection.class));
		verify(criteria, atMostOnce())
			.addOrder(Order.asc("id"));
		verify(criteria, times(4))
			.add(any(Criterion.class));
		verify(criteria, atMostOnce())
			.setFirstResult(anyInt());
		verify(criteria, atMostOnce())
			.setMaxResults(anyInt());
		assertEquals(allPlans.size(), result.getList().size(),
			"O tamanho da lista retornada não é o esperado.");
		assertTrue(listExpectedSorted.equals(result.getList()),
			"A lista retornada era para estar ordenada pelo Id do Plano de Risco.");
		assertFalse(allPlans.equals(result.getList()), "A lista retornada não deveria estar desordenada.");
	}

	@DisplayName("PlanRiskBS Listar Planos de Risco por Company e policyI.")
	@Test
	void listPlanRisksToSelectWithCompanyWithAllParams() {

		Long policyId = 3L;

		Company company = new Company();
		company.setId(1L);

		List<ItemToSelect> itemsWithoutPolicyId = List.of(
			new ItemToSelect(1L, "Plano de Risco 1"),
			new ItemToSelect(2L, "Plano de Risco 2")
		);

		when(dao.newCriteria(PlanRisk.class)).thenReturn(criteria);
		when(criteria.createAlias("policy", "policy")).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(ProjectionList.class))).thenReturn(criteria);
		when(criteria.setResultTransformer(ItemToSelect.class)).thenReturn(criteria);
		when(dao.findByCriteria(criteria, ItemToSelect.class)).thenReturn(itemsWithoutPolicyId);

		List<ItemToSelect> resultWithoutPolicyId = planRiskBS.listPlanRisksToSelect(company, policyId);

		verify(dao).newCriteria(PlanRisk.class);
		verify(criteria).createAlias("policy", "policy");
		verify(criteria, times(4)).add(any(Criterion.class));
		verify(criteria).addOrder(any(Order.class));
		verify(criteria).setProjection(any(ProjectionList.class));
		verify(criteria).setResultTransformer(ItemToSelect.class);
		assertEquals(itemsWithoutPolicyId.size(), resultWithoutPolicyId.size(),
			"O número de itens retornados não corresponde ao esperado.");
		assertEquals(itemsWithoutPolicyId.get(0).getName(), resultWithoutPolicyId.get(0).getName(),
			"Os nomes dos itens retornados não correspondem.");
	}

	@DisplayName("PlanRiskBS Listar Planos de Risco por Company e policyId(null).")
	@Test
	void listPlanRisksToSelectWithCompanyAndNullPolicyId() {
		Company company = new Company();
		company.setId(1L);

		List<ItemToSelect> itemsWithoutPolicyId = List.of(
			new ItemToSelect(1L, "Plano de Risco 1"),
			new ItemToSelect(2L, "Plano de Risco 2")
		);

		when(dao.newCriteria(PlanRisk.class)).thenReturn(criteria);
		when(criteria.createAlias("policy", "policy")).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(ProjectionList.class))).thenReturn(criteria);
		when(criteria.setResultTransformer(ItemToSelect.class)).thenReturn(criteria);
		when(dao.findByCriteria(criteria, ItemToSelect.class)).thenReturn(itemsWithoutPolicyId);

		List<ItemToSelect> resultWithoutPolicyId = planRiskBS.listPlanRisksToSelect(company, null);

		verify(dao).newCriteria(PlanRisk.class);
		verify(criteria).createAlias("policy", "policy");
		verify(criteria, times(3)).add(any(Criterion.class));
		verify(criteria).addOrder(any(Order.class));
		verify(criteria).setProjection(any(ProjectionList.class));
		verify(criteria).setResultTransformer(ItemToSelect.class);
		assertEquals(itemsWithoutPolicyId.size(), resultWithoutPolicyId.size(),
			"O número de itens retornados não corresponde ao esperado.");
		assertEquals(itemsWithoutPolicyId.get(0).getName(), resultWithoutPolicyId.get(0).getName(),
			"Os nomes dos itens retornados não correspondem.");
	}

	@DisplayName("PlanRiskBS Listar Política por Plano de Risco.")
	@Test
	void testListPolicybyPlanRisk() {
		PlanRisk planRisk = new PlanRisk();
		Policy policyExpected = new Policy();
		policyExpected.setId(3L);
		planRisk.setPolicy(policyExpected);

		when(dao.newCriteria(Policy.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(policyExpected);

		Policy policyReturned = planRiskBS.listPolicybyPlanRisk(planRisk);

		verify(dao).newCriteria(Policy.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(criteria, atMostOnce()).uniqueResult();
		assertEquals(policyExpected.getId(), policyReturned.getId(),
			"O id entre as duas políticas devem ser iguais.");
	}

	@DisplayName("PlanRiskBs Deletar um Plano de Risco.")
	@Test
	void testDelete() {
		PlanRisk planRiskToDelete = new PlanRisk();

		planRiskBS.delete(planRiskToDelete);

		assertTrue(planRiskToDelete.isDeleted());
		verify(dao).persist(planRiskToDelete);

	}
}