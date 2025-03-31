package org.forpdi.system;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriteriaCompanyFilterTest {

	@Mock
	CompanyDomainContext company;

	@Mock
	HibernateDAO dao;

	@InjectMocks
	CriteriaCompanyFilter filter;

	Company oneCompany;

	@BeforeEach
	void setUp() {
		oneCompany = new Company();
		oneCompany.setId(1L);
		oneCompany.setName("SINERJI");
		when(company.get()).thenReturn(mock(CompanyDomain.class));
		when(company.get().getCompany()).thenReturn(oneCompany);
	}

	@Test
	@DisplayName("Deve filtrar e listar quando a companhia não for nula")
	void testFilterAndListWithCompanyNotNull() {
		Criteria criteria = mock(Criteria.class);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Company.class)).thenReturn(List.of(oneCompany));

		List<Company> returnedList = filter.filterAndList(criteria, Company.class);

		assertNotNull(returnedList, "A lista não deveria ser nula");
		assertTrue(returnedList.get(0).getName().contains(oneCompany.getName()), "O nome da companhia não é o esperado.");
		assertEquals(oneCompany.getId(), returnedList.get(0).getId(), "O id da companhia não é o esperado.");
	}

	@Test
	@DisplayName("Deve retornar lista vazia quando a companhia for nula")
	void testFilterAndListWithCompanyNull() {
		Criteria criteria = mock(Criteria.class);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		company = null;

		List<Company> returnedList = filter.filterAndList(criteria, Company.class);

		assertTrue(returnedList.isEmpty(), "No caso do domínio ser nulo, o retorno deveria ser vazio.");
	}

	@Test
	@DisplayName("Deve filtrar e listar com alias quando a companhia não for nula")
	void testFilterAndListWithCompanyNotNullAndAlias() {
		String alias = "oneAlias";
		Criteria criteria = mock(Criteria.class);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Company.class)).thenReturn(List.of(oneCompany));

		List<Company> returnedList = filter.filterAndList(criteria, Company.class, alias);

		assertNotNull(returnedList, "A lista não deveria ser nula");
		assertTrue(returnedList.get(0).getName().contains(oneCompany.getName()), "O nome da companhia não é o esperado.");
		assertEquals(oneCompany.getId(), returnedList.get(0).getId(), "O id da companhia não é o esperado.");
	}

	@Test
	@DisplayName("Deve retornar lista vazia com alias quando a companhia for nula")
	void testFilterAndListWithCompanyNullAndAlias() {
		String alias = "oneAlias";
		Criteria criteria = mock(Criteria.class);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		company = null;

		List<Company> returnedList = filter.filterAndList(criteria, Company.class, alias);

		assertTrue(returnedList.isEmpty(), "No caso do domínio ser nulo, o retorno deveria ser vazio.");
	}

	@Test
	@DisplayName("Deve filtrar e encontrar uma companhia quando ela não for nula")
	void testFilterAndFindWithCompanyNotNull() {
		Criteria criteria = mock(Criteria.class);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(oneCompany);

		Company returnedCompany = filter.filterAndFind(criteria);

		assertNotNull(returnedCompany, "A lista não deveria ser nula");
		assertTrue(returnedCompany.getName().contains(oneCompany.getName()), "O nome da companhia não é o esperado.");
		assertEquals(oneCompany.getId(), returnedCompany.getId(), "O id da companhia não é o esperado.");
	}

	@Test
	@DisplayName("Deve retornar nulo quando a companhia for nula ao tentar filtrar e encontrar")
	void testFilterAndFindWithCompanyNull() {
		Criteria criteria = mock(Criteria.class);
		company = null;

		Company returnedCompany = filter.filterAndFind(criteria);

		assertNull(returnedCompany, "No caso do domínio ser nulo, o retorno deveria ser vazio.");
	}

	@Test
	@DisplayName("Deve filtrar e encontrar uma companhia com alias quando ela não for nula")
	void testFilterAndFindWithCompanyNotNullAndAlias() {
		String alias = "oneAlias";
		Criteria criteria = mock(Criteria.class);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(oneCompany);

		Company returnedCompany = filter.filterAndFind(criteria, alias);

		assertNotNull(returnedCompany, "A lista não deveria ser nula");
		assertTrue(returnedCompany.getName().contains(oneCompany.getName()), "O nome da companhia não é o esperado.");
		assertEquals(oneCompany.getId(), returnedCompany.getId(), "O id da companhia não é o esperado.");
	}

	@Test
	@DisplayName("Deve retornar nulo com alias quando a companhia for nula ao tentar filtrar e encontrar")
	void testFilterAndFindWithCompanyNullAndAlias() {
		String alias = "oneAlias";
		Criteria criteria = mock(Criteria.class);
		company = null;

		Company returnedCompany = filter.filterAndFind(criteria, alias);

		assertNull(returnedCompany, "No caso do domínio ser nulo, o retorno deveria ser vazio.");
	}
}