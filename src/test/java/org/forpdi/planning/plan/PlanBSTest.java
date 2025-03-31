package org.forpdi.planning.plan;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.company.Company;
import org.forpdi.planning.document.bootstrap.ForpladHelper;
import org.hibernate.criterion.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanBSTest {

	@InjectMocks
	PlanBS planBs;

	@Mock
	HibernateDAO dao;

	@Mock
	Criteria criteria;

	@BeforeEach
	void setup() {
	}

	@DisplayName("PlanBS Salvar Plano Macro.")
	@Test
	void testSavePlanMacro() {
		PlanMacro planMacroToPersist = new PlanMacro();

		doNothing().when(dao).persist(planMacroToPersist);
		ForpladHelper documentCreator = spy(new ForpladHelper(dao));
		when(dao.newCriteria(any())).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);

		planBs.savePlanMacro(planMacroToPersist);

		verify(dao).persist(planMacroToPersist);
		assertNotNull(documentCreator, "A instância de ForpladHelper não deveria ser nula.");
		assertNotNull(documentCreator.initializeDocument(planMacroToPersist),
			"O Documento retornado não deveria ser nulo.");

	}

	@DisplayName("PlanBS Listar todos os Planos Macros por Companhia")
	@Test
	void testListAllMacros() {

		Company expectedCompany = new Company();
		expectedCompany.setId(1L);
		PlanMacro planA = new PlanMacro();
		planA.setCompany(expectedCompany);
		PlanMacro planB = new PlanMacro();
		planB.setCompany(expectedCompany);
		List<PlanMacro> expectedListOfCompany = Arrays.asList(planA, planB);

		when(dao.newCriteria(PlanMacro.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PlanMacro.class)).thenReturn(expectedListOfCompany);

		List<PlanMacro> listReturned = planBs.listAllMacros(expectedCompany);

		verify(dao).newCriteria(PlanMacro.class);
		verify(criteria).add(any(Criterion.class));
		verify(dao).findByCriteria(criteria, PlanMacro.class);
		assertEquals(expectedListOfCompany.size(), listReturned.size(),
			"O tamanho entre as listas retornadas não são iguais.");
		assertTrue(listReturned.stream()
			.allMatch(
				planMacro -> planMacro.getCompany().getId().equals(expectedCompany.getId())
			), "Todos os planos macros retornados devem ser vinculados a companhia esperada.");
	}

	@DisplayName("PlanBS Listagem de todos os Planos por Planos Macro lista não vazia.")
	@Test
	void listAllPlansForPlansMacroListNotEmptyCase() {
		PlanMacro planMacroA = new PlanMacro();
		PlanMacro planMacroB = new PlanMacro();
		PlanMacro planMacroC = new PlanMacro();
		List<PlanMacro> listOfPlanMacro = Arrays.asList(planMacroA, planMacroB, planMacroC);
		List<Plan> listExpectedOfPlan = new ArrayList<>();

		when(dao.newCriteria(Plan.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Plan.class)).thenReturn(listExpectedOfPlan);

		List<Plan> listReturned = planBs.listAllPlansForPlansMacro(listOfPlanMacro);

		assertFalse(listOfPlanMacro.isEmpty());
		verify(dao).newCriteria(Plan.class);
		verify(criteria).add(any(Criterion.class));
		verify(dao).findByCriteria(criteria, Plan.class);
		assertEquals(listExpectedOfPlan.size(), listReturned.size());
	}

	@DisplayName("PlanBS Listagem de todos os Planos por Planos Macro lista vazia.")
	@Test
	void listAllPlansForPlansMacroListEmptyCase() {
		List<PlanMacro> listOfPlanMacro = new ArrayList<>();

		List<Plan> listReturned = planBs.listAllPlansForPlansMacro(listOfPlanMacro);

		assertTrue(listOfPlanMacro.isEmpty());
		assertTrue(listReturned.isEmpty());
	}

	@DisplayName("PlanBs Listar Planos pelo Plano Macro")
	@Test
	void testListPlansForPlanMacro() {
		PlanMacro planMacro = new PlanMacro();

		when(dao.newCriteria(Plan.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Plan.class)).thenReturn(new ArrayList<Plan>());

		planBs.listPlansForPlanMacro(planMacro);

		verify(dao).newCriteria(Plan.class);
		verify(criteria, times(3)).add(any(Criterion.class));
		verify(criteria, times(3)).addOrder(any(Order.class));

	}

	@DisplayName("PlanBS Listar Planos pelo Id da Estrutura")
	@Test
	void testListPlansByStructureId() {
		long strucuteId = 1L;
		when(dao.newCriteria(Plan.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Plan.class)).thenReturn(new ArrayList<>());

		planBs.listPlansByStructureId(strucuteId);

		verify(dao).newCriteria(Plan.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(dao).findByCriteria(criteria, Plan.class);
	}

	@DisplayName("PlanBS Retornar Plano de Metas pelo Id.")
	@Test
	void testRetrieveById() {
		Long planId = 2L;
		Plan planExpected = new Plan();
		planExpected.setId(planId);

		when(dao.newCriteria(Plan.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(planExpected);

		Plan planReturned = planBs.retrieveById(planId);

		verify(dao).newCriteria(Plan.class);
		verify(criteria).add(any(Criterion.class));
		verify(criteria).uniqueResult();
		assertEquals(planExpected.getId(), planReturned.getId(),
			"O id do plano retornado não corresponde ao esperado.");
	}

	@DisplayName("PlanBS Listar Planos Macros por uma lista não vazia de Id's ")
	@Test
	void testListPlanMacrosByIdsListCaseListNotEmpty() {
		List<Long> idsPlans = Arrays.asList(2L, 3L, 5L);
		when(dao.newCriteria(PlanMacro.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PlanMacro.class)).thenReturn(new ArrayList<>());

		List<PlanMacro> returnedList = planBs.listPlanMacrosByIds(idsPlans);

		verify(dao).newCriteria(PlanMacro.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(dao).findByCriteria(criteria, PlanMacro.class);
		assertNotNull(returnedList, "A lista retornada não deveria ser nula.");
	}

	@DisplayName("PlanBS Listar Planos Macros caso da lista vazia ")
	@Test
	void testListPlanMacrosByIdsListCaseListEmpty() {
		List<Long> idsEmptyList = new ArrayList<>();

		List<PlanMacro> listReturned = planBs.listPlanMacrosByIds(idsEmptyList);

		assertTrue(idsEmptyList.isEmpty());
		assertTrue(listReturned.isEmpty());
	}

	@DisplayName("PlanBS Recuperar o Plano Macro por Id.")
	@Test
	void testRetrievePlanMacroById() {
		Long planMacroId = 2L;
		PlanMacro planMacroExpected = new PlanMacro();
		planMacroExpected.setId(planMacroId);

		when(dao.newCriteria(PlanMacro.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(planMacroExpected);

		PlanMacro planReturned = planBs.retrievePlanMacroById(planMacroId);

		verify(dao).newCriteria(PlanMacro.class);
		verify(criteria).add(any(Criterion.class));
		verify(criteria).uniqueResult();
		assertEquals(planMacroExpected.getId(), planReturned.getId(),
			"O id do Plano Macro retornado é o esperado.");
	}

	@DisplayName("PlanBS Duplicar o PlanMacro")
	@Test
	void testDuplicatePlanMacroWithoutException() {
		PlanMacro planMacroToDupiclate = new PlanMacro();
		doNothing().when(dao).persist(planMacroToDupiclate);

		assertDoesNotThrow(() -> {
			planBs.duplicatePlanMacro(planMacroToDupiclate);
		}, "Não era esperado a ocorrência de uma exceção.");

		verify(dao).persist(planMacroToDupiclate);
	}

	@DisplayName("PlanBS Listar o Plano detalhadamente pelo Plano de Metas.")
	@Test
	void testListPlansDetailed() {
		Plan planToList = new Plan();
		planToList.setId(1L);

		when(dao.newCriteria(PlanDetailed.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PlanDetailed.class)).thenReturn(new ArrayList<>());

		List<PlanDetailed> listReturned = planBs.listPlansDetailed(planToList);

		verify(dao).newCriteria(PlanDetailed.class);
		verify(criteria, times(3)).add(any(Criterion.class));
		verify(dao).findByCriteria(criteria, PlanDetailed.class);
		assertNotNull(listReturned);
	}

	@DisplayName("PlanBS Lista todos os planos detalhadamente pelo Plano de Metas.")
	@Test
	void testListAllPlansDetailedListNotEmptyCase() {
		Plan planA = new Plan();
		Plan planB = new Plan();
		Plan planC = new Plan();
		List<Plan> listOfPlanMacro = Arrays.asList(planA, planB, planC);
		List<PlanDetailed> listExpectedOfPlans = new ArrayList<>();

		when(dao.newCriteria(PlanDetailed.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, PlanDetailed.class)).thenReturn(listExpectedOfPlans);

		List<PlanDetailed> listReturned = planBs.listAllPlansDetailed(listOfPlanMacro);

		assertFalse(listOfPlanMacro.isEmpty());
		verify(dao).newCriteria(PlanDetailed.class);
		verify(criteria).add(any(Criterion.class));
		verify(dao).findByCriteria(criteria, PlanDetailed.class);
		assertEquals(listExpectedOfPlans.size(), listReturned.size(), "O tamanho das listas devem ser iguais.");
	}

	@DisplayName("PlanBS Lista todos os planos detalhadamente pelo Plano de Metas. Lista Vazia")
	@Test
	void testListAllPlansDetailedListEmptyCase() {
		List<Plan> listOfPlanMacro = new ArrayList<>();

		List<PlanDetailed> listReturned = planBs.listAllPlansDetailed(listOfPlanMacro);

		assertTrue(listReturned.isEmpty(), "A lista esperada seria vazia.");
	}
}