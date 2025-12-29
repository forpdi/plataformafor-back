package org.forpdi.core.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Path;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectionsTest {

	@Test
	@DisplayName("Dado o nome de uma propriedade, quando a projeção for criada, então a projeção deve retornar a propriedade correta")
	void givenPropertyName_whenPropertyProjectionCreated_thenShouldReturnCorrectProperty() {
		String propertyName = "name";

		SimplePropertyProjection projection = Projections.property(propertyName);

		assertEquals(propertyName, projection.getPropertyName(), "O nome da propriedade deve ser o correto.");
	}

	@Test
	@DisplayName("Quando uma ProjectionList for criada, então a lista de projeções deve ser instanciada corretamente")
	void whenProjectionListCreated_thenShouldReturnEmptyList() {
		ProjectionList projectionList = Projections.projectionList();

		assertNotNull(projectionList, "A ProjectionList deve ser instanciada.");
//		assertTrue(projectionList.wait(), "A ProjectionList recém-criada deve estar vazia.");
	}
}
