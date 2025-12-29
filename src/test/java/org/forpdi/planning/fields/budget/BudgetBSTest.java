package org.forpdi.planning.fields.budget;

import java.util.Arrays;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.planning.structure.StructureLevelInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetBSTest {

	@Mock
	HibernateDAO hibernateDAO;

	@Mock
	Criteria criteria;

	@InjectMocks
	BudgetBS budgetBS;

	@DisplayName("BudgetBS Salvar Elemento Orçamentário.")
	@Test
	void testSaveBudgetElement() {

		BudgetElement budgetToSave = new BudgetElement();
		budgetToSave.setDeleted(false);
		doNothing().when(hibernateDAO).persist(any(BudgetElement.class));

		budgetBS.saveBudgetElement(budgetToSave);

		assertFalse(budgetToSave.isDeleted());
		verify(hibernateDAO).persist(budgetToSave);
	}

	@DisplayName("BudgetBS Lista Elementos Orçamentário.")
	@Test
	void testListBudgetElement() {

		Company company = new Company();
		company.setId(1L);
		List<BudgetElement> budgetElementsOfOneCompany = Arrays.asList(new BudgetElement(), new BudgetElement());
		budgetElementsOfOneCompany.forEach(budgetElement -> budgetElement.setCompany(company));
		when(hibernateDAO.newCriteria(BudgetElement.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(hibernateDAO.findByCriteria(criteria, BudgetElement.class)).thenReturn(budgetElementsOfOneCompany);

		PaginatedList<BudgetElement> result = budgetBS.listBudgetElement(company);

		verify(hibernateDAO,atMostOnce()).newCriteria(BudgetElement.class);
		verify(hibernateDAO, atMostOnce()).findByCriteria(criteria, BudgetElement.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		assertNotNull(result,
			"A lista de elementos orçamentários não deveria ser nula.");
		assertEquals(2, result.getList().size(),
			"A lista de elementos orçamentários deveria ter tamanho 2");
		assertTrue(
			result.getList().stream().allMatch(be -> be.getCompany().getId().equals(1L)),
			"Todos os elementos orçamentários devem estar associados à Company com id 1L."
		);
	}

	@DisplayName("BudgetBS Lista Elementos Orçamentários por Companhia.")
	@Test
	void testListAllBudgetElementsByCompany() {

		Company companyA = new Company();
		Company companyB = new Company();
		companyA.setId(1L);
		companyB.setId(2L);

		BudgetElement budgetOfCompanyA = new BudgetElement();
		budgetOfCompanyA.setCompany(companyA);

		BudgetElement budgetOfCompanyB = new BudgetElement();
		budgetOfCompanyB.setCompany(companyB);

		List<BudgetElement> budgetsFiltered = List.of(budgetOfCompanyA);

		when(hibernateDAO.newCriteria(BudgetElement.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(hibernateDAO.findByCriteria(criteria, BudgetElement.class)).thenReturn(budgetsFiltered);


		List<BudgetElement> returnedList = budgetBS.listAllBudgetElementsByCompany(companyA);

		verify(hibernateDAO).newCriteria(BudgetElement.class);
		verify(hibernateDAO).findByCriteria(criteria, BudgetElement.class);
		verify(criteria).add(any(Criterion.class));
		assertNotNull(returnedList, "A lista de elementos orçamentários não deveria ser nula.");
		assertEquals(1, returnedList.size(),
			"A lista de elementos orçamentários filtrados por companhia deveria ter tamanho 1.");
		assertTrue(
			returnedList.stream().allMatch(be -> be.getCompany().getId().equals(companyA.getId())),
			"Todos os elementos orçamentários devem estar associados à Company com id " + companyA.getId()
		);
	}

	@DisplayName("BudgetBS Elemento Orçamentário existe por id.")
	@Test
	void test_retrieve_budget_element_by_valid_id() {

		Long validId = 1L;
		BudgetElement expectedElement = new BudgetElement();
		expectedElement.setId(validId);
		expectedElement.setDeleted(false);

		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(expectedElement);
		when(hibernateDAO.newCriteria(BudgetElement.class)).thenReturn(criteria);

		BudgetElement result = budgetBS.budgetElementExistsById(validId);

		verify(hibernateDAO).newCriteria(BudgetElement.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		assertNotNull(result, "O elemento orçamentário não deveria ser nulo.");
		assertEquals(expectedElement, result,"O elemento retornado não é o esperado.");
		assertFalse(result.isDeleted(),"O elemento não deveria ter o status de deletado.");
	}

	@DisplayName("BudgetBS Elemento orçamentário existe por sub-ação e companhia.")
	@Test
	void budgetElementExistsBySubActionAndCompany() {

		String subAction = "subAction1";
		Company company = new Company();
		company.setName("Test Company");

		BudgetElement expectedElement = new BudgetElement();
		expectedElement.setSubAction(subAction);
		expectedElement.setCompany(company);

		when(hibernateDAO.newCriteria(BudgetElement.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(expectedElement);

		BudgetElement returnedBudget = budgetBS.budgetElementExistsBySubActionAndCompany(subAction, company);

		verify(hibernateDAO).newCriteria(BudgetElement.class);
		verify(criteria, times(3)).add(any(Criterion.class));
		assertEquals(expectedElement.getId(), returnedBudget.getId(),
			"O id do orçamento retornado não é o esperado.");
	}

	@DisplayName("BudgetBS Atualizar elemento orçamentário.")
	@Test
	void testUpdate() {
		BudgetElement elementToUpdate = new BudgetElement();
		elementToUpdate.setDeleted(false);
		doNothing().when(hibernateDAO).persist(elementToUpdate);

		budgetBS.update(elementToUpdate);

		verify(hibernateDAO).persist(elementToUpdate);
		assertFalse(elementToUpdate.isDeleted(), "O elemento não deveria possuir status válido de deletado.");
	}

	@DisplayName("BudgetBS Deletar elemento orçamentário.")
	@Test
	void testDeleteBudget() {

		BudgetElement elementToDelete = new BudgetElement();
		elementToDelete.setDeleted(true);
		doNothing().when(hibernateDAO).persist(elementToDelete);

		budgetBS.deleteBudget(elementToDelete);

		verify(hibernateDAO).persist(elementToDelete);
		assertTrue(elementToDelete.isDeleted(), "O elemento deletado deve possuir status válido de deletado.");
	}

	@DisplayName("BudgetBS Lista Orçamentos por Elemento Orçamentário.")
	@Test
	void testListBudgetsByBudgetElement() {

		BudgetElement budgetElement = new BudgetElement();
		budgetElement.setId(1L);

		Budget budget1 = new Budget();
		budget1.setBudgetElement(budgetElement);

		Budget budget2 = new Budget();
		budget2.setBudgetElement(budgetElement);

		List<Budget> budgets = List.of(budget1, budget2);

		when(hibernateDAO.newCriteria(Budget.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(hibernateDAO.findByCriteria(criteria, Budget.class)).thenReturn(budgets);

		PaginatedList<Budget> result = budgetBS.listBudgetsByBudgetElement(budgetElement);

		verify(hibernateDAO).newCriteria(Budget.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(hibernateDAO).findByCriteria(criteria, Budget.class);

		assertNotNull(result, "O resultado não deveria ser nulo.");
		assertEquals(2, result.getList().size(), "A lista deveria conter 2 orçamentos.");
		assertTrue(
			result.getList().stream().allMatch(b -> b.getBudgetElement().equals(budgetElement)),
			"Todos os orçamentos devem estar associados ao elemento orçamentário informado."
		);
	}

	@DisplayName("BudgetBS Lista Orçamentos não deletados por Instância de Nível Estrutural")
	@Test
	void testListBudgetNotDeletedByLevelInstance() {


		StructureLevelInstance levelInstance = new StructureLevelInstance();
		levelInstance.setId(1L);

		Budget budget1 = new Budget();
		budget1.setLevelInstance(levelInstance);

		Budget budget2 = new Budget();
		budget2.setLevelInstance(levelInstance);

		List<Budget> budgets = List.of(budget1, budget2);

		when(hibernateDAO.newCriteria(Budget.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(hibernateDAO.findByCriteria(criteria, Budget.class)).thenReturn(budgets);

		List<Budget> result = budgetBS.listBudgetByLevelInstance(levelInstance);

		verify(hibernateDAO).newCriteria(Budget.class);
		verify(criteria, times(2)).add(any(Criterion.class));

		verify(hibernateDAO).findByCriteria(criteria, Budget.class);

		assertNotNull(result, "A lista de orçamentos não deveria ser nula.");
		assertEquals(2, result.size(), "A lista deveria conter 2 orçamentos.");
		assertTrue(
			result.stream().allMatch(b -> b.getLevelInstance().equals(levelInstance)),
			"Todos os orçamentos devem estar associados à instância de nível estrutural fornecida."
		);
	}

	@DisplayName("BudgetBS Lista todos Orçamentos por Instância de Nível Estrutural")
	@Test
	void testListAllBudgetByLevelInstance() {

		StructureLevelInstance levelInstance = new StructureLevelInstance();
		levelInstance.setId(1L);

		Budget budget1 = new Budget();
		budget1.setLevelInstance(levelInstance);

		Budget budget2 = new Budget();
		budget2.setLevelInstance(levelInstance);

		List<Budget> budgets = List.of(budget1, budget2);

		when(hibernateDAO.newCriteria(Budget.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(hibernateDAO.findByCriteria(criteria, Budget.class)).thenReturn(budgets);

		List<Budget> result = budgetBS.listAllBudgetByLevelInstance(levelInstance);

		verify(hibernateDAO).newCriteria(Budget.class);
		verify(criteria, atMostOnce()).add(any(Criterion.class));
		verify(hibernateDAO).findByCriteria(criteria, Budget.class);

		assertNotNull(result, "A lista de orçamentos não deveria ser nula.");
		assertEquals(2, result.size(), "A lista deveria conter 2 orçamentos.");
		assertTrue(
			result.stream().allMatch(b -> b.getLevelInstance().equals(levelInstance)),
			"Todos os orçamentos devem estar associados à instância de nível estrutural fornecida."
		);

	}
}
