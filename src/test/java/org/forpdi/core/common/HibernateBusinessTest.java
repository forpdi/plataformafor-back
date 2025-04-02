package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

class HibernateBusinessTest {

	@Mock
	private HibernateDAO dao;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private TestHibernateBusiness business;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testExists() {
		Serializable id = 1L;
		Class<Serializable> clazz = Serializable.class;
		Serializable expectedEntity = mock(Serializable.class);

		when(dao.exists(id, clazz)).thenReturn(expectedEntity);

		Serializable result = business.exists(id, clazz);

		verify(dao, times(1)).exists(id, clazz);
		assertSame(expectedEntity, result);
	}

	@Test
	void testPersist() {
		Serializable entity = mock(Serializable.class);

		business.persist(entity);

		verify(dao, times(1)).persist(entity);
	}

	@Test
	void testRemove() {
		Serializable entity = mock(Serializable.class);

		business.remove(entity);

		verify(dao, times(1)).delete(entity);
	}

	private static class TestHibernateBusiness extends HibernateBusiness {
	}
}