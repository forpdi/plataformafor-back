package org.forpdi.core.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectionListTest {

	@Test
	@DisplayName("Deve adicionar uma projeção com um alias")
	void testAddProjection() {
		ProjectionList projectionList = new ProjectionList();
		PropertyProjection mockProjection = mock(PropertyProjection.class);
		String alias = "propertyAlias";

		projectionList.add(mockProjection, alias);

		Collection<PropertyProjection> properties = projectionList.listProperties();
		assertNotNull(properties);
		assertEquals(1, properties.size());
		assertTrue(properties.contains(mockProjection));
	}

	@Test
	@DisplayName("Deve retornar uma coleção vazia quando nenhuma projeção foi adicionada")
	void testListPropertiesInitiallyEmpty() {
		ProjectionList projectionList = new ProjectionList();

		Collection<PropertyProjection> properties = projectionList.listProperties();

		assertNotNull(properties);
		assertTrue(properties.isEmpty());
	}

/*
	@Test
	@DisplayName("Deve gerar seleções baseadas nas projeções adicionadas")
	void testGetSelection() {
		ProjectionList projectionList = new ProjectionList();
		PropertyProjection mockProjection1 = mock(PropertyProjection.class);
		PropertyProjection mockProjection2 = mock(PropertyProjection.class);
		CriteriaBuilder mockBuilder = mock(CriteriaBuilder.class);
		CriteriaPropertiesContext mockContext = mock(CriteriaPropertiesContext.class);

		@SuppressWarnings("unchecked")
		Selection<Object> mockSelection1 = mock(Selection.class);
		@SuppressWarnings("unchecked")
		Selection<Object> mockSelection2 = mock(Selection.class);

		when(mockProjection1.getSingleSelection(mockBuilder, mockContext)).thenReturn(mockSelection1);
		when(mockProjection2.getSingleSelection(mockBuilder, mockContext)).thenReturn(mockSelection2);

		projectionList.add(mockProjection1, "alias1");
		projectionList.add(mockProjection2, "alias2");

		Selection<?>[] selections = projectionList.getSelection(mockBuilder, mockContext);

		assertNotNull(selections);
		assertEquals(2, selections.length);
		assertEquals(mockSelection1, selections[0]);
		assertEquals(mockSelection2, selections[1]);

		verify(mockProjection1, times(1)).getSingleSelection(mockBuilder, mockContext);
		verify(mockProjection2, times(1)).getSingleSelection(mockBuilder, mockContext);
	}
*/
}

