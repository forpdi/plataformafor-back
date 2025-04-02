package org.forpdi.planning.attribute;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.hibernate.criterion.Criterion;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AttributeHelperTest {

	@Mock
	private HibernateDAO dao;

	@InjectMocks
	private AttributeHelper attributeHelper;

	@Mock
	private Criteria criteria;

	@Mock
	private Criterion criterion;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Testa a recuperação de uma instância de atributo por nível e atributo")
	void testRetrieveAttributeInstance() {
		StructureLevelInstance levelInstance = mock(StructureLevelInstance.class);
		Attribute attribute = mock(Attribute.class);
		AttributeInstance mockInstance = mock(AttributeInstance.class);

		when(dao.newCriteria(AttributeInstance.class)).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria); 
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(mockInstance);

		AttributeInstance result = attributeHelper.retrieveAttributeInstance(levelInstance, attribute);

		assertNotNull(result);
		verify(criteria, times(2)).add(any());
	}

	@Test
	@DisplayName("Testa a recuperação de uma instância formatada por ID")
	void testRetrieveFormatAttributeInstanceById() {
		Long levelInstanceId = 1L;
		AttributeInstance mockInstance = mock(AttributeInstance.class);

		when(dao.newCriteria(AttributeInstance.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), eq(JoinType.INNER_JOIN))).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(mockInstance);

		AttributeInstance result = attributeHelper.retrieveFormatAttributeInstance(levelInstanceId);

		assertNotNull(result);
		verify(criteria, times(2)).add(any());
	}

	@Test
	@DisplayName("Testa a recuperação de uma instância formatada por nível")
	void testRetrieveFormatAttributeInstanceByLevelInstance() {
		StructureLevelInstance levelInstance = mock(StructureLevelInstance.class);
		when(levelInstance.getId()).thenReturn(1L);

		AttributeInstance mockInstance = mock(AttributeInstance.class);
		when(dao.newCriteria(AttributeInstance.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), eq(JoinType.INNER_JOIN))).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(mockInstance);

		AttributeInstance result = attributeHelper.retrieveFormatAttributeInstance(levelInstance);

		assertNotNull(result);
		verify(levelInstance).getId();
	}

	@Test
	@DisplayName("Testa a recuperação de uma instância de polaridade por ID")
	void testRetrievePolarityAttributeInstanceById() {
		Long levelInstanceId = 1L;
		AttributeInstance mockInstance = mock(AttributeInstance.class);

		when(dao.newCriteria(AttributeInstance.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), eq(JoinType.INNER_JOIN))).thenReturn(criteria);
		when(criteria.add(any())).thenReturn(criteria);
		when(criteria.setMaxResults(1)).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(mockInstance);

		AttributeInstance result = attributeHelper.retrievePolarityAttributeInstance(levelInstanceId);

		assertNotNull(result);
		verify(criteria, times(2)).add(any());
	}

	@Test
	void test_retrieve_finish_date_attribute_with_null_instance() {
		AttributeHelper attributeHelper = new AttributeHelper();

		assertThrows(NullPointerException.class, () -> {
			attributeHelper.retrieveFinishDateFieldAttribute(null);
		});
	}

	@Test
	void test_retrieve_reached_field_attribute_with_null_level_instance() {
		AttributeHelper attributeHelper = new AttributeHelper();

		assertThrows(NullPointerException.class, () -> {
			attributeHelper.retrieveReachedFieldAttribute(null);
		});
	}

}