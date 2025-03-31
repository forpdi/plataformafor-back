package org.forrisco.core.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ItemToSelect;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projection;
import org.forpdi.core.company.Company;
import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.Item;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.RiskLevel;
import org.hibernate.criterion.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyBSTest {
	@Mock
	HibernateDAO dao;

	@Mock
	Criteria criteria;

	@InjectMocks
	PolicyBS policyBS;

	@DisplayName("PolicyBS Salva no banco de dados uma nova política.")
	@Test
	void testSavePolicy() {
		Policy policyToSave = new Policy();

		policyBS.save(policyToSave);

		assertFalse(policyToSave.isDeleted(), "A polícita salva não deve ter estar como deletada.");
		verify(dao).persist(policyToSave);
	}

	@DisplayName("PolicyBS Salva no banco de dados um item.")
	@Test
	void testSavePolicyItem() {
		Item itemToSave = new Item();

		policyBS.save(itemToSave);

		verify(dao).persist(itemToSave);
	}

	@DisplayName("Salva no banco de dados um campo de item.")
	@Test
	void testSavePolicyFieldItem() {
		FieldItem fItemToSave = new FieldItem();

		policyBS.save(fItemToSave);

		verify(dao).persist(fItemToSave);
	}

	@DisplayName("PolicyBS Deleta uma política.")
	@Test
	void testDeletePolicy() {
		Policy policyToDelete = new Policy();

		policyBS.delete(policyToDelete);

		assertTrue(policyToDelete.isDeleted(), "A política excluída deveria estar como deletada.");
		verify(dao).persist(policyToDelete);
	}

	@DisplayName("PolicyBS ")
	@Test
	void testListPoliciesWithAllParams() {
		List<Policy> policies = new ArrayList<>();
		long totalPolicies = 2;

		Company company = new Company();
		company.setId(1L);

		Policy policy1 = new Policy();
		policy1.setCompany(company);

		Policy policy2 = new Policy();
		policy2.setCompany(company);

		Boolean archived = false;

		DefaultParams defaultParams = new DefaultParams();
		defaultParams.setTerm("Pesq");
		String[] sortedby = {"name", "asc"};
		defaultParams.setSortedBy(sortedby);

		when(dao.newCriteria(Policy.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Policy.class)).thenReturn(policies);
		when(criteria.uniqueResult()).thenReturn(totalPolicies);

		policyBS.listPolicies(company, archived, defaultParams);
	}

	@DisplayName("PolicyBS ")
	@Test
	void testListPoliciesWithAllDefaultParamsEmpty() {
		List<Policy> policies = new ArrayList<>();
		long totalPolicies = 2;

		Company company = new Company();
		company.setId(1L);

		Policy policy1 = new Policy();
		policy1.setCompany(company);

		Policy policy2 = new Policy();
		policy2.setCompany(company);

		Boolean archived = false;

		DefaultParams defaultParams = new DefaultParams();

		when(dao.newCriteria(Policy.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Policy.class)).thenReturn(policies);
		when(criteria.uniqueResult()).thenReturn(totalPolicies);

		policyBS.listPolicies(company, archived, defaultParams);
	}

	@DisplayName("PolicyBS Lista as políticas da Companhia.")
	@Test
	void testListPoliciesToSelect() {
		Company company = new Company();
		company.setId(1L);
		List<ItemToSelect> expectedList = new ArrayList<>();
		ItemToSelect item1 = new ItemToSelect();
		ItemToSelect item2 = new ItemToSelect();
		item1.setId(1L);
		item2.setId(2L);
		expectedList.add(item1);
		expectedList.add(item2);

		when(dao.newCriteria(Policy.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, ItemToSelect.class)).thenReturn(expectedList);

		List<ItemToSelect> returnedList = policyBS.listPoliciesToSelect(company);

		assertEquals(expectedList, returnedList, "A lista de políticas da companhia informada não é válida.");
	}

	@DisplayName("PolicyBS ")
	@Test
	void testListPoliciesHasPlans() {
		PaginatedList<Policy> expectedPolicies = new PaginatedList<>();

		Policy policy = new Policy();
		policy.setId(1L);

		Policy policy2 = new Policy();
		policy2.setId(2L);

		expectedPolicies.setList(List.of(policy, policy2));
		expectedPolicies.setTotal(2L);

		PolicyBS localMock = mock(PolicyBS.class);
		when(localMock.hasLinkedPlans(policy)).thenReturn(true);
		when(localMock.hasLinkedPlans(policy2)).thenReturn(false);
		doCallRealMethod().when(localMock).listPoliciesHasPlans(expectedPolicies);

		PaginatedList<Policy> returnedList = localMock.listPoliciesHasPlans(expectedPolicies);

		assertNotNull(returnedList, "A lista retornada não deveria ser nula");
		assertTrue(returnedList.getList().contains(policy));
		assertTrue(returnedList.getList().contains(policy2));
		assertFalse(returnedList.getList().stream().noneMatch(Policy::getHasLinkedPlans),
			"A primeira política possui planos linkados.");
	}

	@DisplayName("PolicyBS ")
	@Test
	void testListPlanByPolicy() {
		Policy policy = new Policy();
		policy.setId(1L);

		PlanRisk planRisk1 = new PlanRisk();
		PlanRisk planRisk2 = new PlanRisk();

		List<PlanRisk> expectedList = new ArrayList<>();
		expectedList.add(planRisk1);
		expectedList.add(planRisk2);
		long expectedTotal = 2L;

		when(dao.newCriteria(PlanRisk.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PlanRisk.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn(expectedTotal);

		PaginatedList<PlanRisk> returnedList = policyBS.listPlanbyPolicy(policy);

		assertEquals(expectedTotal, returnedList.getTotal(),
			" O tamanho da lista não corresponde ao esperado.");
		assertEquals(expectedList, returnedList.getList(),
			" A lista retornada não é a esperada.");
	}

	@DisplayName("PolicyBS ")
	@Test
	void testHasLinkedPlans() {
		Boolean hasLinkedPlans = true;

		Policy policy = new Policy();
		policy.setId(1L);

		when(dao.newCriteria(PlanRisk.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(policy);

		Boolean resultHasLinkedPlans = policyBS.hasLinkedPlans(policy);

		assertEquals(hasLinkedPlans, resultHasLinkedPlans, "Era esperado que a política seja linkada a planos.");
	}

	@DisplayName("PolicyBS ")
	@Test
	void testValidateDatesWithAllNullDates() {
		PolicyBS policyBS = new PolicyBS();
		Policy policy = new Policy();
		policy.setValidityBegin(null);
		policy.setValidityEnd(null);

		policyBS.validateDates(policy);
	}

	@DisplayName("PolicyBS ")
	@Test
	void testValidateDatesWithOnlyBeginDate() {
		PolicyBS policyBS = new PolicyBS();
		Policy policy = new Policy();
		policy.setValidityBegin(new Date());
		policy.setValidityEnd(null);

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			policyBS.validateDates(policy);
		});
		assertEquals("Não é permitido preencher somente uma das datas do prazo de vigência", exception.getMessage(),
			"O retorno da exception deveria ser igual.");
	}

	@DisplayName("PolicyBS ")
	@Test
	void testValidateDatesWithBeginDateGreaterThanEndDate() {
		Policy policy = new Policy();
		Calendar calendar = Calendar.getInstance();

		calendar.set(2024, Calendar.DECEMBER, 31);
		policy.setValidityBegin(calendar.getTime());

		calendar.set(2024, Calendar.DECEMBER, 1);
		policy.setValidityEnd(calendar.getTime());

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			policyBS.validateDates(policy);
		});
		assertEquals("A data de início do prazo de vigência não deve ser superior à data de término", exception.getMessage(),
			"O retorno da exception deveria ser igual.");
	}

	@DisplayName("PolicyBS ")
	@Test
	void testGetRiskLevelsMap() {
		Policy policy = new Policy();
		policy.setId(2L);

		List<RiskLevel> expectedList = new ArrayList<>();
		RiskLevel risk1 = new RiskLevel();
		risk1.setLevel("High");
		risk1.setId(1L);
		risk1.setPolicy(policy);
		RiskLevel risk2 = new RiskLevel();
		risk2.setLevel("Low");
		risk2.setId(2L);
		risk2.setPolicy(policy);
		RiskLevel risk3 = new RiskLevel();
		risk3.setLevel("Medium");
		risk3.setId(3L);
		risk3.setPolicy(policy);

		expectedList.add(risk1);
		expectedList.add(risk2);
		expectedList.add(risk3);

		long expectedTotal = 3L;

		PaginatedList<RiskLevel> paginatedList = new PaginatedList<>();
		paginatedList.setList(expectedList);
		paginatedList.setTotal(expectedTotal);

		PolicyBS mockBs = mock(PolicyBS.class);
		when(mockBs.listRiskLevelbyPolicy(policy)).thenReturn(paginatedList);
		doCallRealMethod().when(mockBs).getRiskLevelsMap(policy);

		Map<String, RiskLevel> returnedMap = mockBs.getRiskLevelsMap(policy);

		assertNotNull(returnedMap, "A estrutura retornada não deveria ser nula.");
		assertEquals(3, returnedMap.size(), "O tamanho da estrutura retornada deveria ser 3");
		assertEquals(risk1, returnedMap.get("High"));
		assertEquals(risk2, returnedMap.get("Low"));
		assertEquals(risk3, returnedMap.get("Medium"));
	}

	@DisplayName("PolicyBS ")
	@Test
	void testListRiskLevelByPolicy() {
		Policy policy = new Policy();
		policy.setId(2L);

		List<RiskLevel> expectedList = new ArrayList<>();
		RiskLevel risk1 = new RiskLevel();
		risk1.setId(1L);
		risk1.setPolicy(policy);
		RiskLevel risk2 = new RiskLevel();
		risk2.setId(2L);
		risk2.setPolicy(policy);
		RiskLevel risk3 = new RiskLevel();
		risk3.setId(3L);
		risk3.setPolicy(policy);
		long expectedTotal = 3L;

		when(dao.newCriteria(RiskLevel.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, RiskLevel.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn(expectedTotal);

		PaginatedList<RiskLevel> returnedList = policyBS.listRiskLevelbyPolicy(policy);

		assertEquals(expectedList, returnedList.getList(),
			"A lista de RiskLeves retornada deveria ser igual a esperada.");
		assertEquals(expectedTotal, returnedList.getTotal(),
			"O total de riscos deveria ser igual.");

	}
}