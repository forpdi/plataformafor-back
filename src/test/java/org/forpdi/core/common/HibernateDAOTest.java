package org.forpdi.core.common;

import org.forpdi.core.user.User;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HibernateDAOTest {
	@Mock
	private EntityManager entityManager;
	@InjectMocks
	private HibernateDAO hibernateDAO;

	@Test
	@DisplayName("Dada uma entidade, ela deverá ser persistida.")
	void testPersist() {
		User entity = mock(User.class);
		hibernateDAO.persist(entity);
		verify(entityManager, times(1)).persist(entity);
	}

	@Test
	@DisplayName("Dada uma entidade, ela deverá ser atualizada.")
	void testMerge() {
		User entity = mock(User.class);
		when(entityManager.merge(entity)).thenReturn(entity);

		User result = hibernateDAO.merge(entity);

		verify(entityManager, times(1)).merge(entity);
		assert result == entity;
	}

	@Test
	@DisplayName("Dada uma entidade, ela deverá ser removida da base de dados.")
	void testDelete() {
		User entity = mock(User.class);
		hibernateDAO.delete(entity);
		verify(entityManager, times(1)).remove(entity);
	}

	@Test
	@DisplayName("Dada um id e uma classe de entidade, sua existência poderá ser verificada.")
	void testExistsWithNotNullId() {
		Long id = 1L;
		Class<User> clazz = User.class;

		when(entityManager.find(clazz, id)).thenReturn(mock(User.class));

		User result = hibernateDAO.exists(id, clazz);

		verify(entityManager, times(1)).find(clazz, id);
		assert result != null;
	}

	@Test
	@DisplayName("Dada um id, a entidade de uma classe poderá ser consultada pelo seu id e retornada.")
	void testExistsWithNullId() {
		Long id = null;
		Class<User> clazz = User.class;

		User result = hibernateDAO.exists(id, clazz);

		assertNull(result);
	}

	@Test
	@DisplayName("Dado uma string, poderá ser criada uma query sql.")
	void testNewSQLQuery() {
		String sql = "SELECT * FROM table";
		Query queryMock = mock(Query.class);
		when(entityManager.createNativeQuery(sql)).thenReturn(queryMock);

		Query result = hibernateDAO.newSQLQuery(sql);

		verify(entityManager, times(1)).createNativeQuery(sql);
		assert result == queryMock;
	}

	@Test
	@DisplayName("Dada a classe de entidades, poderá ser criada uma nova instância de consulta criteria para ela.")
	void testCreateNewCriteria() {
		Class<User> clazz = User.class;

		Criteria criteria = hibernateDAO.newCriteria(clazz);

		assertNotNull(criteria);
	}

	@Test
	@DisplayName("Dada a instância de critéria, poderá ser realizada a consulta.")
	void testfindByCriteria() {
		Class<User> clazz = User.class;
		User user1 = new User();
		User user2 = new User();
		List<User> usersList = List.of(user1, user2);
		Criteria criteria = mock(Criteria.class);

		when(criteria.list()).thenReturn(usersList);

		List<User> returnedList = hibernateDAO.findByCriteria(criteria, clazz);

		assertFalse(returnedList.isEmpty());
		assertTrue(returnedList.contains(user1));
		assertTrue(returnedList.contains(user2));
	}

	@Test
	@DisplayName("Execução da operação transacional.")
	void testExecuteTransactionalOperation() {
		HibernateDAO.TransactionalOperation operation = mock(HibernateDAO.TransactionalOperation.class);

		hibernateDAO.execute(operation);

		try {
			verify(operation, times(1)).execute(hibernateDAO);
		} catch (HibernateException e) {
			assert false : "Unexpected HibernateException during test";
		}
	}
}