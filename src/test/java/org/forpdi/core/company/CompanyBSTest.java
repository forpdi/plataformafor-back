package org.forpdi.core.company;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.*;
import org.hibernate.criterion.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyBSTest {
	@InjectMocks
	@Spy
	private CompanyBS bs;

	@Mock
	private HibernateDAO dao;

	@Mock
	private Criteria criteria;

	@Mock
	private HttpServletRequest request;

	private CompanyDomain currentDomain;

	@BeforeEach
	void setUp() {
		currentDomain = new CompanyDomain();
		Company company = new Company();
		company.setId(1L);
		company.setDeleted(false);

		currentDomain.setHost("teste.tst.apps.tst");
		currentDomain.setId(1L);
		currentDomain.setBaseUrl("https://teste.tst.apps.tst/");
		currentDomain.setCompany(company);
	}

	@Test
	@DisplayName("Recupera a instância do objeto CompanyDomain que está ativo no momento.")
	void testCurrentDomainWithNoException() {

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(request.getHeader("Host")).thenReturn("teste.tst.apps.tst");
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(currentDomain);

		CompanyDomain returnedDomain = bs.currentDomain();

		assertEquals(currentDomain, returnedDomain, "O domínio que foi retornado não foi o esperado.");
	}

	@Test
	@DisplayName("Recupera a instância do objeto CompanyDomain que está ativo no momento. Caso de retorno null.")
	void testCurrentDomainWithNullReturn() {

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);

		CompanyDomain returnedDomain = bs.currentDomain();

		assertNull(returnedDomain, "O domínio retornado deveria ser nulo.");
	}

	@Test
	@DisplayName("Recupera uma instância do objeto CompanyDomain utilizando um host específico")
	void testRetrieveByHost() {

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(currentDomain);

		CompanyDomain returnedDomain = bs.retrieveByHost(currentDomain.getHost());

		assertEquals(currentDomain, returnedDomain, "O domínio que foi retornado não foi o esperado.");
	}

	@Test
	@DisplayName("Recupera os domínios de uma companhia específica.")
	void testRetrieveCompanyByDomain() {

		List<CompanyDomain> expectedList = new ArrayList<>(List.of(currentDomain));

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, CompanyDomain.class)).thenReturn(expectedList);

		List<CompanyDomain> returnedList = bs.retrieveCompanyByDomain(currentDomain.getCompany());

		assertEquals(expectedList, returnedList, "A lista de domínios da companhia não é a esperada.");
	}

	@Test
	@DisplayName("Recupera uma instância do objeto CompanyDomain utilizando um id.")
	void testRetrieveCompanyById() {

		when(dao.newCriteria(Company.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(currentDomain.getCompany());

		Company returnedCompany = bs.retrieveCompanyById(currentDomain.getCompany().getId());

		assertEquals(currentDomain.getCompany(), returnedCompany, "A companhia retornada não é a esperada.");
	}

	@Test
	@DisplayName("Recupera as mensagens de um companhia.")
	void testRetrieveMessages() {

		List<CompanyMessage> expectedMessagesList = new ArrayList<>();

		when(dao.newCriteria(CompanyMessage.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, CompanyMessage.class)).thenReturn(expectedMessagesList);

		List<CompanyMessage> returnedMessagesList = bs.retrieveMessages(currentDomain.getCompany());

		assertNotNull(returnedMessagesList, "A lista de mensagens da companhia não deveria ser nula.");
	}

	@Test
	@DisplayName("Recupera os overlays com mensagens.")
	void testRetrieveMessagesOverlayWithMessages() {
		Company mockCompany = new Company();
		List<CompanyMessage> mockMessages = new ArrayList<>();

		CompanyMessage message1 = new CompanyMessage();
		message1.setMessageKey("key1");
		message1.setMessageValue("value1");

		CompanyMessage message2 = new CompanyMessage();
		message2.setMessageKey("key2");
		message2.setMessageValue("value2");

		mockMessages.add(message1);
		mockMessages.add(message2);

		when(dao.newCriteria(CompanyMessage.class)).thenReturn(criteria);
		when(dao.findByCriteria(criteria, CompanyMessage.class)).thenReturn(mockMessages);

		Map<String, String> result = bs.retrieveMessagesOverlay(mockCompany);

		assertEquals(2, result.size());
		assertEquals("value1", result.get("key1"));
		assertEquals("value2", result.get("key2"));
		verify(dao, times(1)).findByCriteria(any(), eq(CompanyMessage.class));
	}

	@Test
	@DisplayName("Recupera os overlays com mensagens. Caso não haja mensagem.")
	void testRetrieveMessagesOverlayWithoutMessages() {
		Company mockCompany = new Company();
		List<CompanyMessage> mockMessages = new ArrayList<>();

		when(dao.newCriteria(CompanyMessage.class)).thenReturn(criteria);
		when(dao.findByCriteria(any(), eq(CompanyMessage.class))).thenReturn(mockMessages);

		Map<String, String> result = bs.retrieveMessagesOverlay(mockCompany);

		assertEquals(0, result.size());

		verify(dao, times(1)).findByCriteria(any(), eq(CompanyMessage.class));
	}

	@Test
	@DisplayName("Atualiza o overlay da mensagem.")
	void testUpdateMessageOverlayMessageExists() {
		Company mockCompany = new Company();
		mockCompany.setId(1L);

		String key = "key1";
		String value = "newValue";

		CompanyMessage existingMessage = new CompanyMessage();
		existingMessage.setMessageKey(key);
		existingMessage.setMessageValue("oldValue");

		when(dao.newCriteria(CompanyMessage.class)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(existingMessage);

		bs.updateMessageOverlay(mockCompany, key, value);

		verify(dao, times(1)).newCriteria(CompanyMessage.class);
		verify(dao, times(1)).persist(existingMessage);
		assert existingMessage.getMessageValue().equals(value);
	}

	@Test
	@DisplayName("Atualiza o overlay da mensagem. Caso mensagem não exista.")
	void testUpdateMessageOverlayMessageDoesNotExist() {
		Company mockCompany = new Company();
		mockCompany.setId(1L);

		String key = "key2";
		String value = "value2";

		when(dao.newCriteria(CompanyMessage.class)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(null);

		bs.updateMessageOverlay(mockCompany, key, value);

		verify(dao, times(1)).newCriteria(CompanyMessage.class);
		verify(dao, times(1)).persist(any(CompanyMessage.class));
	}

	@Test
	@DisplayName("Salvar companhia.")
	void testSave() {
		Company company = new Company();
		company.setName("Test Company");

		bs.save(company);

		verify(dao, times(1)).persist(company);
	}

	@Test
	@DisplayName("Lista companhias.")
	void testListAll() {
		List<Company> mockCompanies = new ArrayList<>();
		Company company1 = new Company();
		company1.setName("Company 1");
		Company company2 = new Company();
		company2.setName("Company 2");

		mockCompanies.add(company1);
		mockCompanies.add(company2);

		PaginatedList<Company> listCompanies = new PaginatedList<>();
		listCompanies.setList(mockCompanies);
		listCompanies.setTotal(2L);
		CompanyBS mockBs = mock(CompanyBS.class);

		doReturn(listCompanies).when(mockBs).list(anyInt(), anyInt());
		doCallRealMethod().when(mockBs).listAll();

		List<Company> result = mockBs.listAll();

		assertEquals(2, result.size());
		assertEquals("Company 1", result.get(0).getName());
		assertEquals("Company 2", result.get(1).getName());
		verify(mockBs, times(1)).list(anyInt(), anyInt());
	}

	@Test
	@DisplayName("Lista as companhias limitados a uma dada página")
	void testListWithPagination() {
		List<Company> mockCompanies = new ArrayList<>();
		Company company1 = new Company();
		company1.setName("Company 1");
		Company company2 = new Company();
		company2.setName("Company 2");

		mockCompanies.add(company1);
		mockCompanies.add(company2);

		when(dao.newCriteria(Company.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);

		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Company.class))).thenReturn(mockCompanies);
		when(criteria.uniqueResult()).thenReturn(10L);

		PaginatedList<Company> result = bs.list(1, 5);

		assertNotNull(result);
		assertEquals(2, result.getList().size());
		assertEquals(10L, result.getTotal());
		verify(dao, times(1)).findByCriteria(any(Criteria.class), eq(Company.class));
	}

	@Test
	@DisplayName("Lista as companhias limitados a uma dada página. Caso sem paginação.")
	void testListWithoutPagination() {
		List<Company> mockCompanies = new ArrayList<>();
		Company company1 = new Company();
		company1.setName("Company 1");
		Company company2 = new Company();
		company2.setName("Company 2");

		mockCompanies.add(company1);
		mockCompanies.add(company2);

		when(dao.newCriteria(Company.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);

		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Company.class))).thenReturn(mockCompanies);
		when(criteria.uniqueResult()).thenReturn(10L);

		PaginatedList<Company> result = bs.list(0, null);

		assertNotNull(result);
		assertEquals(2, result.getList().size());
		assertEquals(10L, result.getTotal());
		verify(dao, times(1)).findByCriteria(any(Criteria.class), eq(Company.class));
	}

	@Test
	@DisplayName("Procura uma companhia de acordo com os parâmetros enviados.")
	void testSearchWithDefaultParamsAndTerm() {
		Company company1 = new Company();
		company1.setName("Company 1");
		Company company2 = new Company();
		company2.setName("Company 2");

		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();
		paramsFilter.setTerm("1");
		paramsFilter.setSortedBy(new String[]{"name", "desc"});

		when(dao.newCriteria(Company.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString()))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.addOrder(any(Order.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Company.class)))
			.thenReturn(List.of(company1));
		when(criteria.uniqueResult())
			.thenReturn(1L);

		PaginatedList<Company> returnedList = bs.search(paramsFilter);

		verify(dao, times(2))
			.newCriteria(Company.class);
		verify(criteria, times(4))
			.createAlias(anyString(), anyString());
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
		assertEquals(1L, returnedList.getList().size(),
			"O tamanho da lista retornada não é o esperado.");
		assertEquals(company1, returnedList.getList().get(0),
			"A companhia não corresponde a esperada.");
	}

	@Test
	@DisplayName("Procura uma companhia de acordo com os parâmetros enviados. Caso sem termo e ordenação.")
	void testSearchWithDefaultParamsAndNoTerm() {
		List<Company> mockCompanies = new ArrayList<>();
		Company company1 = new Company();
		company1.setName("Company 1");
		Company company2 = new Company();
		company2.setName("Company 2");

		mockCompanies.add(company1);
		mockCompanies.add(company2);

		DefaultParams paramsFilter = DefaultParams.createWithMaxPageSize();

		when(dao.newCriteria(Company.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString()))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.addOrder(any(Order.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Company.class)))
			.thenReturn(List.of(company1, company2));
		when(criteria.uniqueResult())
			.thenReturn(10L);

		PaginatedList<Company> returnedList = bs.search(paramsFilter);

		verify(dao, times(2))
			.newCriteria(Company.class);
		verify(criteria, times(4))
			.createAlias(anyString(), anyString());
		verify(criteria)
			.setProjection(any(Projection.class));
		verify(criteria, times(2))
			.add(any(Criterion.class));
		verify(criteria, atMostOnce())
			.setFirstResult(anyInt());
		verify(criteria, atMostOnce())
			.setMaxResults(anyInt());
		assertEquals(mockCompanies.size(), returnedList.getList().size(),
			"O tamanho da lista retornada não é o esperado.");
		assertEquals(company1, returnedList.getList().get(0),
			"A companhia não corresponde a esperada.");
	}

	@Test
	@DisplayName("Procura uma um domínio de acordo com os parâmetros enviados.")
	void testSearchDomainWithDefaultParamsAndTerm() {
		DefaultParams params = DefaultParams.createWithMaxPageSize();
		params.setTerm("1");
		params.setSortedBy(new String[]{"name", "desc"});

		List<Company> expectedCompaniesList = new ArrayList<>();
		Company company1 = new Company();
		company1.setName("Company 1");
		expectedCompaniesList.add(company1);

		List<CompanyDomain> expectedDomainsList = new ArrayList<>();
		CompanyDomain domain1 = new CompanyDomain();
		domain1.setCompany(company1);
		expectedDomainsList.add(domain1);

		PaginatedList<Company> expectedCompaniesPaginated = new PaginatedList<>();
		expectedCompaniesPaginated.setList(expectedCompaniesList);
		expectedCompaniesPaginated.setTotal(1L);

		PaginatedList<CompanyDomain> mockPaginatedDomainList = new PaginatedList<>();
		mockPaginatedDomainList.setList(expectedDomainsList);
		mockPaginatedDomainList.setTotal(1L);

		doReturn(expectedCompaniesPaginated).when(bs).search(any(DefaultParams.class));
		when(dao.newCriteria(Company.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.addOrder(any(Order.class)))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Company.class)))
			.thenReturn(expectedCompaniesList);

		when(dao.newCriteria(CompanyDomain.class))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(criteria.uniqueResult())
			.thenReturn(1L);

		when(dao.findByCriteria(criteria, CompanyDomain.class)).thenReturn(expectedDomainsList);

		PaginatedList<CompanyDomain> result = bs.searchDomain(params);

		assertEquals(mockPaginatedDomainList.getList().get(0), result.getList().get(0),
			"O elemento na lista retornada não é o esperado.");
	}

	@Test
	@DisplayName("Procura uma domínio de acordo com os parâmetros enviados. Caso sem termo e ordenação.")
	void testSearchDomainWithDefaultParamsAndNoTerm() {

		DefaultParams params = DefaultParams.createWithMaxPageSize();
		params.setTerm(null);

		List<Company> allCompanies = new ArrayList<>();
		Company company1 = new Company();
		company1.setName("Company 1");
		allCompanies.add(company1);

		List<CompanyDomain> allDomains = new ArrayList<>();
		CompanyDomain domain1 = new CompanyDomain();
		domain1.setCompany(company1);
		allDomains.add(domain1);

		PaginatedList<Company> paginatedCompanyList = new PaginatedList<>();
		paginatedCompanyList.setList(allCompanies);
		paginatedCompanyList.setTotal(1L);

		PaginatedList<CompanyDomain> mockPaginatedDomainList = new PaginatedList<>();
		mockPaginatedDomainList.setList(allDomains);
		mockPaginatedDomainList.setTotal(1L);

		doReturn(paginatedCompanyList).when(bs).search(any(DefaultParams.class));
		when(dao.newCriteria(Company.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class)))
			.thenReturn(criteria);
		when(criteria.addOrder(any(Order.class)))
			.thenReturn(criteria);
		when(dao.findByCriteria(any(Criteria.class), eq(Company.class)))
			.thenReturn(allCompanies);

		when(dao.newCriteria(CompanyDomain.class))
			.thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class)))
			.thenReturn(criteria);
		when(criteria.setFirstResult(anyInt()))
			.thenReturn(criteria);
		when(criteria.setMaxResults(anyInt()))
			.thenReturn(criteria);
		when(criteria.uniqueResult())
			.thenReturn(1L);
		when(dao.findByCriteria(criteria, CompanyDomain.class)).thenReturn(allDomains);

		PaginatedList<CompanyDomain> result = bs.searchDomain(params);

		assertEquals(mockPaginatedDomainList.getList().get(0), result.getList().get(0),
			"O elemento na lista retornada não é o esperado.");
	}

	@Test
	@DisplayName("Verifica a existência da url base do domínio.")
	void testAlreadyExistsURLWithExistingBaseUrl() {
		CompanyDomain company = new CompanyDomain();
		company.setBaseUrl("http://example.com");

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(new CompanyDomain());

		Boolean result = bs.alreadyExistsURL(company);

		assertTrue(result);
		verify(criteria, times(1)).setMaxResults(1);
		verify(criteria, times(1)).uniqueResult();
	}

	@Test
	@DisplayName("Verifica a existência da url base do domínio.")
	void testAlreadyExistsURLWithNoExistingBaseUrl() {

		CompanyDomain company = new CompanyDomain();
		company.setBaseUrl("http://example.com");


		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(null);


		Boolean result = bs.alreadyExistsURL(company);


		assertFalse(result);
		verify(criteria, times(1)).setMaxResults(1);
		verify(criteria, times(1)).uniqueResult();
	}

	@Test
	@DisplayName("Verifica a existência da url base do domínio.")
	void testAlreadyExistsURLWithId() {
		CompanyDomain company = new CompanyDomain();
		company.setBaseUrl("http://example.com");
		company.setId(1L);

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(new CompanyDomain());

		Boolean result = bs.alreadyExistsURL(company);

		assertTrue(result);
		verify(criteria, times(1)).setMaxResults(1);
		verify(criteria, times(1)).uniqueResult();
	}

	@Test
	@DisplayName("Verifica a existência da url base do domínio.")
	void testAlreadyExistsURLWithIdAndNoExistingBaseUrl() {
		CompanyDomain company = new CompanyDomain();
		company.setBaseUrl("http://example.com");
		company.setId(1L);

		when(dao.newCriteria(CompanyDomain.class)).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(null);

		Boolean result = bs.alreadyExistsURL(company);
		assertFalse(result);
		verify(criteria, times(1)).setMaxResults(1);
		verify(criteria, times(1)).uniqueResult();
	}
}