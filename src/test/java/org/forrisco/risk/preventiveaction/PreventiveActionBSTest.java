package org.forrisco.risk.preventiveaction;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.*;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forrisco.core.plan.PlanRisk;
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
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreventiveActionBSTest {
	@InjectMocks
	PreventiveActionBS bs;
	@Mock
	ArchiveBS archiveBS;
	@Mock
	Criteria criteria;
	@Mock
	EntityManager entityManager;
	@Mock
	HibernateDAO dao;
	
	@DisplayName("Deve salvar uma ação de prevenção anexada um arquivo.")
	@Test
	void testSaveActionWithFileNotNull() {
		PreventiveAction action = new PreventiveAction();
		String fileLink = "12.png";
		action.setFileLink(fileLink);
		Archive archive = new Archive();

		when(archiveBS.getByFileLink(fileLink)).thenReturn(archive);

		bs.saveAction(action);

		assertFalse(action.isDeleted(), "A ação de prevenção não deveria estar como deletada.");
		assertNotNull(action.getFile(), "A ação deveria possuir um arquivo.");
	}

	@DisplayName("Deve salvar uma ação de prevenção sem um arquivo.")
	@Test
	void testSaveActionWithFileLinkNull() {
		PreventiveAction action = new PreventiveAction();

		bs.saveAction(action);

		assertFalse(action.isDeleted(), "A ação de prevenção não deveria estar como deletada.");
		assertNull(action.getFile(), "A ação deveria não possuir um arquivo");
	}

	@DisplayName("Deve deletar uma ação de prevenção")
	@Test
	void testDelete() {
		PreventiveAction actionToDelete = new PreventiveAction();

		bs.delete(actionToDelete);

		assertTrue(actionToDelete.isDeleted(), "A ação deletada deve possuir status deletada.");
	}

	@DisplayName("Deve retornar as ações preventivas a partir de um risco")
	@Test
	void testListActionByRisk() {

		Risk risk = new Risk();
		risk.setId(1L);
		risk.setName("Test Risk");

		PreventiveAction action1 = new PreventiveAction();
		action1.setRisk(risk);
		action1.setAction("Action 1");

		PreventiveAction action2 = new PreventiveAction();
		action2.setRisk(risk);
		action2.setAction("Action 2");

		List<PreventiveAction> listActions = Arrays.asList(action1, action2);
		PaginatedList<PreventiveAction> expectedList = new PaginatedList<>();
		expectedList.setList(listActions);
		expectedList.setTotal(2L);

		PreventiveActionBS preventiveActionBS = mock(PreventiveActionBS.class);
		when(preventiveActionBS.listActionByRisk(risk, -1)).thenReturn(expectedList);
		doCallRealMethod().when(preventiveActionBS).listActionByRisk(risk);

		PaginatedList<PreventiveAction> returnedList = preventiveActionBS.listActionByRisk(risk);

		assertEquals(expectedList.getList(), returnedList.getList(),
			"A lista retornada não corresponde a esperada.");
		assertEquals(expectedList.getTotal(), returnedList.getTotal(),
			"O total não corresponde ao esperada.");
	}

	@DisplayName("Deve retornar as ações preventivas a partir de um risco")
	@Test
	void testListActionByRiskAndSelectedYear() {

		Risk risk = new Risk();
		risk.setId(1L);
		risk.setName("Test Risk");

		PreventiveAction action1 = new PreventiveAction();
		action1.setRisk(risk);
		action1.setAction("Action 1");

		PreventiveAction action2 = new PreventiveAction();
		action2.setRisk(risk);
		action2.setAction("Action 2");

		List<PreventiveAction> expectedActions = Arrays.asList(action1, action2);

		CriteriaBuilder builder = mock(CriteriaBuilder.class);
		CriteriaQuery<PreventiveAction> cq = mock(CriteriaQuery.class);
		Root<PreventiveAction> root = mock(Root.class);
		TypedQuery<PreventiveAction> qr = mock(TypedQuery.class);
		when(entityManager.getCriteriaBuilder()).thenReturn(builder);
		when(builder.createQuery(PreventiveAction.class)).thenReturn(cq);
		when(cq.from(PreventiveAction.class)).thenReturn(root);

		when(entityManager.createQuery(cq)).thenReturn(qr);
		when(qr.getResultList()).thenReturn(expectedActions);

		PaginatedList<PreventiveAction> result = bs.listActionByRisk(risk, -1);

		assertEquals(2, result.getTotal(), "O total não corresponde ao esperado.");
		assertEquals(expectedActions, result.getList(), "A lista retornada não é a esperada.");
	}

	@DisplayName("Deve retornar as ações preventivas a partir de um risco")
	@Test
	void testListActionByRiskAndSelectedAndNotSelectedYear() {

		PreventiveActionBS actionBS = new PreventiveActionBS();

		Risk risk = new Risk();
		risk.setId(1L);
		risk.setName("Test Risk");

		PreventiveAction action1 = new PreventiveAction();
		action1.setRisk(risk);
		action1.setAction("Action 1");

		PreventiveAction action2 = new PreventiveAction();
		action2.setRisk(risk);
		action2.setAction("Action 2");

		List<PreventiveAction> expectedActions = Arrays.asList(action1, action2);

		CriteriaBuilder builder = mock(CriteriaBuilder.class);
		CriteriaQuery<PreventiveAction> cq = mock(CriteriaQuery.class);
		Root<PreventiveAction> root = mock(Root.class);
		TypedQuery<PreventiveAction> qr = mock(TypedQuery.class);
		when(entityManager.getCriteriaBuilder()).thenReturn(builder);
		when(builder.createQuery(PreventiveAction.class)).thenReturn(cq);
		when(cq.from(PreventiveAction.class)).thenReturn(root);

		when(entityManager.createQuery(cq)).thenReturn(qr);
		when(qr.getResultList()).thenReturn(expectedActions);

		PaginatedList<PreventiveAction> result = bs.listActionByRisk(risk, 2024);

		assertEquals(2, result.getTotal(), "O total não corresponde ao esperado.");
		assertEquals(expectedActions, result.getList(), "A lista retornada não é a esperada.");
	}

	@DisplayName("Deve retorna as ações preventivas a partir do plano de risco")
	@Test
	void testListActionsByPlanRisk() {
		PlanRisk planRisk = new PlanRisk();

		PreventiveActionBean action1 = new PreventiveActionBean();
		action1.setAction("Action 1");

		PreventiveActionBean action2 = new PreventiveActionBean();
		action2.setAction("Action 2");

		List<PreventiveActionBean> listActionBean = Arrays.asList(action1, action2);

		when(dao.newCriteria(PreventiveAction.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(ProjectionList.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PreventiveActionBean.class)).thenReturn(listActionBean);

		List<PreventiveActionBean> returnedList = bs.listActionsByPlanRisk(planRisk);

		assertEquals(listActionBean, returnedList, "A lista de Ações preventivas deveria ser igual.");
	}

	@DisplayName("Deve retornar as ações preventivas a partir de um risco. Com paginação, busca por termo e ordenação.")
	@Test
	void testListActionByRiskAndParamsWithTermAndOrder() {
		Risk riskToListActions = new Risk();
		riskToListActions.setId(1L);

		DefaultParams params = DefaultParams.createWithMaxPageSize();
		params.setTerm("Ação");
		params.setSortedBy(new String[]{"name", "asc"});

		PreventiveAction action1 = new PreventiveAction();
		action1.setRisk(riskToListActions);
		action1.setAction("Ação de prevenção 1");
		action1.setId(1L);

		PreventiveAction action2 = new PreventiveAction();
		action2.setRisk(riskToListActions);
		action1.setAction("Ação de prevenção 2");
		action2.setId(2L);

		List<PreventiveAction> expectedList =
			new ArrayList<>(List.of(action1, action2));

		when(dao.newCriteria(PreventiveAction.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PreventiveAction.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<PreventiveAction> returnedList = bs.listActionByRisk(riskToListActions, params);

		assertEquals(expectedList, returnedList.getList(),
			"A lista retornada de ações preventivas não corresponde à esperada.");
		assertEquals(expectedList.size(), returnedList.getTotal(), "O número de ações não é o esperado.");
		verify(criteria, times(6)).add(any());
		verify(criteria).addOrder(any());
	}

	@DisplayName("Deve retornar as ações preventivas a partir de um risco com paginação. " +
		"Caso onde não há termo nem ordenação.")
	@Test
	void testListActionByRiskAndParamsWithNoTerm() {
		Risk riskToListActions = new Risk();
		riskToListActions.setId(1L);

		DefaultParams params = DefaultParams.createWithMaxPageSize();

		PreventiveAction action1 = new PreventiveAction();
		action1.setRisk(riskToListActions);
		action1.setAction("Ação de prevenção 1");
		action1.setId(1L);

		PreventiveAction action2 = new PreventiveAction();
		action2.setRisk(riskToListActions);
		action1.setAction("Ação de prevenção 2");
		action2.setId(2L);

		List<PreventiveAction> expectedList =
			new ArrayList<>(List.of(action1, action2));

		when(dao.newCriteria(PreventiveAction.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PreventiveAction.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<PreventiveAction> returnedList = bs.listActionByRisk(riskToListActions, params);

		assertEquals(expectedList, returnedList.getList(),
			"A lista retornada de ações preventivas não corresponde à esperada.");
		assertEquals(expectedList.size(), returnedList.getTotal(), "O número de ações não é o esperado.");
		verify(criteria, times(4)).add(any());
		verify(criteria, times(0)).addOrder(any());
	}

	@DisplayName("Deve retornar uma ação de prevenção pelo seu id.")
	@Test
	void testFindByActionId() {
		Long actionId = 3L;
		PreventiveAction expectedAction = new PreventiveAction();
		expectedAction.setId(actionId);

		when(dao.newCriteria(PreventiveAction.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(expectedAction);

		PreventiveAction returnedAction = bs.findByActionId(actionId);

		assertEquals(expectedAction, returnedAction, "A ação retornada deveria ser igual a esperada.");
	}

	@DisplayName("Deve retornar as ações preventivas que são vinculadas a um rico X")
	@Test
	void testListPreventiveActionByRisk() {
		Risk riskToListActions = new Risk();
		riskToListActions.setId(7L);

		PreventiveAction action1 = new PreventiveAction();
		action1.setRisk(riskToListActions);
		action1.setAction("Ação de prevenção 1");
		action1.setId(5L);

		PreventiveAction action2 = new PreventiveAction();
		action2.setRisk(riskToListActions);
		action1.setAction("Ação de prevenção 2");
		action2.setId(12L);

		List<PreventiveAction> expectedList =
			new ArrayList<>(List.of(action1, action2));

		when(dao.newCriteria(PreventiveAction.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PreventiveAction.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<PreventiveAction> returnedPaginatedList = bs.listPreventiveActionByRisk(riskToListActions);

		assertEquals(expectedList, returnedPaginatedList.getList(),
			"A lista de ações de prevenção deveria corresponder à esperada.");
		assertEquals(expectedList.size(), returnedPaginatedList.getTotal(),
			"O total de elementos da lista retornada não corresponde ao esperado..");
	}

}