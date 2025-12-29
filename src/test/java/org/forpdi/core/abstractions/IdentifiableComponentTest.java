package org.forpdi.core.abstractions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentifiableComponentTest {

	static class TestIdentifiableComponent extends IdentifiableComponent {
		@Override
		public String getDisplayName() {
			return "Test Component";
		}
	}

	@Test
	@DisplayName("Deve gerar ID baseado no nome canônico da classe")
	void testGenerateId() {
		Class<?> clazz = TestIdentifiableComponent.class;

		String generatedId = IdentifiableComponent.generateId(clazz);

		assertNotNull(generatedId);
		assertEquals(clazz.getCanonicalName(), generatedId);
	}

	@Test
	@DisplayName("Deve inicializar com UUID se ID gerado for vazio")
	void testConstructorGeneratesUUID() {
		class EmptyIdComponent extends IdentifiableComponent {
			@Override
			public String getDisplayName() {
				return "Empty ID Component";
			}
		}

		EmptyIdComponent component = new EmptyIdComponent();

		assertNotNull(component.getId());
		assertDoesNotThrow(() -> UUID.fromString(component.getId())); 
	}

	@Test
	@DisplayName("Deve retornar ID único do componente")
	void testGetId() {
		TestIdentifiableComponent component = new TestIdentifiableComponent();

		String id = component.getId();

		assertNotNull(id);
		assertEquals(TestIdentifiableComponent.class.getCanonicalName(), id);
	}

/*
	@Test
	@DisplayName("Deve lançar exceção se ID for vazio no equals")
	void testEqualsThrowsExceptionForEmptyId() {
		class EmptyIdComponent extends IdentifiableComponent {
			@Override
			public String getDisplayName() {
				return "Empty ID Component";
			}

			@Override
			public String getId() {
				return "";
			}
		}
		EmptyIdComponent component = new EmptyIdComponent();

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			component.equals(new TestIdentifiableComponent());
		});
		assertEquals("A identifiable component must have a non-empty ID.", exception.getMessage());
	}
 */
	@Test
	@DisplayName("Deve verificar igualdade corretamente com IDs iguais")
	void testEqualsWithMatchingIds() {
		TestIdentifiableComponent component1 = new TestIdentifiableComponent();
		TestIdentifiableComponent component2 = new TestIdentifiableComponent();

		boolean areEqual = component1.equals(component2);

		assertTrue(areEqual);
	}

	@Test
	@DisplayName("Deve retornar hashCode baseado no ID")
	void testHashCode() {
		TestIdentifiableComponent component = new TestIdentifiableComponent();

		int hashCode = component.hashCode();

		assertEquals(component.getId().hashCode(), hashCode);
	}
}
