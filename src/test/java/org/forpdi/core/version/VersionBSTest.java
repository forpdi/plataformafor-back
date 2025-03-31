package org.forpdi.core.version;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.PaginatedList;
import org.hibernate.criterion.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionBSTest {

	@Mock
	private HibernateDAO daoMock;
	@Mock
	Criteria criteria;
	@InjectMocks
	private VersionBS versionBS;

	@Test
	@DisplayName("Dado uma versão nova, quando saveNewVersion é chamado, então a versão é salva corretamente")
	void testSaveNewVersion() {
		VersionHistory versionHistory = new VersionHistory();
		versionHistory.setDeleted(false);

		VersionHistory savedVersion = versionBS.saveNewVersion(versionHistory);

		assertNotNull(savedVersion, "A versão não pode ser nula após a chamada de persist.");
		verify(daoMock, times(1)).persist(versionHistory);
	}

	@Test
	@DisplayName("Dado um ID válido, quando retrieveVersionById é chamado, então a versão é recuperada corretamente")
	void testRetrieveVersionById() {
		Long versionId = 1L;
		VersionHistory versionHistory = new VersionHistory();
		versionHistory.setId(versionId);
		versionHistory.setDeleted(false);

		when(daoMock.newCriteria(VersionHistory.class))
			.thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(versionHistory);

		VersionHistory retrievedVersion = versionBS.retrieveVersionById(versionId);

		verify(daoMock).newCriteria(VersionHistory.class);
		verify(criteria, times(2)).add(any(Criterion.class));
		verify(criteria).uniqueResult();
		assertEquals(versionHistory, retrievedVersion, "A versão recuperada deve ser igual à versão criada.");
	}

	@Test
	@DisplayName("Quando listVersions é chamado, então a lista de versões é retornada corretamente")
	void testListVersions() {
		List<VersionHistory> versionList = new ArrayList<>();
		VersionHistory version1 = new VersionHistory();
		version1.setDeleted(false);
		versionList.add(version1);

		VersionHistory version2 = new VersionHistory();
		version2.setDeleted(false);
		versionList.add(version2);

		PaginatedList<VersionHistory> paginatedList = new PaginatedList<>();
		paginatedList.setList(versionList);
		paginatedList.setTotal((long) versionList.size());

		when(daoMock.newCriteria(VersionHistory.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(daoMock.findByCriteria(criteria, VersionHistory.class)).thenReturn(versionList);

		PaginatedList<VersionHistory> result = versionBS.listVersions();

		verify(daoMock).newCriteria(VersionHistory.class);
		verify(criteria).add(any(Criterion.class));
		verify(criteria).addOrder(any(Order.class));
		verify(daoMock).findByCriteria(criteria, VersionHistory.class);
		assertEquals(versionList, result.getList(), "A lista de versões retornada deve ser igual à lista configurada.");
		assertEquals(2, result.getTotal(), "O total de versões retornado deve ser 2.");
	}
}
