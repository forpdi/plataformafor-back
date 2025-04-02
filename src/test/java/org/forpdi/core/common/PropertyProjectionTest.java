package org.forpdi.core.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PropertyProjectionTest {

	@Mock
	private CriteriaBuilder mockCriteriaBuilder;

	@Mock
	private CriteriaPropertiesContext mockPropertiesContext;

	@Mock
	private static Selection<?> mockSelection1;

	@Mock
	private static Selection<?> mockSelection2;

	private static class ConcretePropertyProjection extends PropertyProjection {
		public ConcretePropertyProjection(String propertyName) {
			super(propertyName);
		}

		@Override
		public Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
			return new Selection<?>[]{mockSelection1, mockSelection2};
		}
	}

	@Test
	@DisplayName("Deve retornar o nome da propriedade corretamente")
	void testGetPropertyName() {
		String expectedPropertyName = "nomeDaPropriedade";
		PropertyProjection projection = new ConcretePropertyProjection(expectedPropertyName);

		String propertyName = projection.getPropertyName();

		assertEquals(expectedPropertyName, propertyName, "O nome da propriedade deve ser retornado corretamente");
	}

	@Test
	@DisplayName("Deve retornar uma seleção.")
	public void testGetSingleSelection() {
		String expectedPropertyName = "nomeDaPropriedade";
		PropertyProjection projection = new ConcretePropertyProjection(expectedPropertyName);

		Selection<?> result = projection.getSingleSelection(mockCriteriaBuilder, mockPropertiesContext);

		assertNotNull(result, "A seleção retornada não deve ser nula.");
	}
}
