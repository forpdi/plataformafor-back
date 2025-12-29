package org.forpdi.core.abstractions;

import org.forpdi.planning.attribute.types.DateField;
import org.forpdi.planning.attribute.types.TextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComponentFactoryTest {

	static class TestComponentFactory extends ComponentFactory<IdentifiableComponent> {
	}

	private TestComponentFactory componentFactory;

	@BeforeEach
	void setUp() {
		componentFactory = new TestComponentFactory();
	}

	@DisplayName("ComponentFactory Regista um novo componente a fábrica. Não deverá retorna erro.")
	@Test
	void testComponentFactoryRegisterComponentSuccessfulCase() {
		int returnedIndiceA = componentFactory.register(new TextArea());
		int returnedIndiceB = componentFactory.register(new DateField());

		assertEquals(0, returnedIndiceA, "O primeiro índice esperado seria o de posição 0.");
		assertEquals(1, returnedIndiceB, "O segundo índice esperado seria o de posição 1");

	}

	@DisplayName("ComponentFactory Regista um novo componente a fábrica. Caso do retorno de erro devido ao registro de componentes duplicados.")
	@Test
	void testComponentFactoryRegisterComponentElementAlreadyInList() {
		int returnedIndiceA = componentFactory.register(new TextArea());

		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> {
				int returnedIndiceB = componentFactory.register(new TextArea());
			}
		);
		assertEquals("Duplicate component registering: org.forpdi.planning.attribute.types.TextArea", exception.getMessage());
	}

	@DisplayName("ComponentFactory Regista um novo componente a fábrica. Retornará erro devido ao registro receber um componente nulo.")
	@Test
	void testComponentFactoryRegisterComponentElementNull() {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> {
				int returnedIndiceB = componentFactory.register(null);
			}
		);
		assertEquals("Null component object passed.", exception.getMessage());
	}

	@DisplayName("ComponentFactory Busca um componente já registrado, pelo seu id inteiro.")
	@Test
	void testComponentFactoryGetByNumberId() {
		TextArea componentToRegisterAndFinds = new TextArea();

		int index = componentFactory.register(componentToRegisterAndFinds);

		assertNotNull(componentFactory.get(index), "O componente não deveria ser nulo.");
		assertEquals(componentToRegisterAndFinds, componentFactory.get(index),
			"Os componentes deveriam corresponder.");
	}

	@DisplayName("ComponentFactory Busca um componente já registrado, pelo seu id textual.")
	@Test
	void testComponentFactoryByCanocialName() {
		TextArea componentToRegisterAndFinds = new TextArea();
		String canonicalName = "org.forpdi.planning.attribute.types.TextArea";
		int index = componentFactory.register(componentToRegisterAndFinds);

		assertEquals(componentToRegisterAndFinds, componentFactory.get(canonicalName),
			"Os componentes deveriam corresponder.");
	}

	@DisplayName("ComponentFactory Busca um componente já registrado, pelo seu id textual. Caso retorno nulo.")
	@Test
	void testComponentFactoryGetByCanocialNameNullCase() {
		String canonicalName = "org.forpdi.planning.attribute.types.InvalidadName";

		assertNull(componentFactory.get(canonicalName));
	}

	@DisplayName("ComponentFactory Retorna o tamanho da lista.")
	@Test
	void testComponentFactoryNumberOfRegisteredsComponentes() {
		componentFactory.register(new TextArea());
		componentFactory.register(new DateField());

		assertEquals(2, componentFactory.size(), "O número de componentes cadastrados não é o correto.");
	}

	@DisplayName("ComponentFactory Executa o consumer para da componente cadastrado na fábrica.")
	@Test
	void testComponentFactoryEach() {
		componentFactory.register(new TextArea());
		componentFactory.register(new DateField());
		Consumer<IdentifiableComponent> mockConsumer = spy(Consumer.class);

		componentFactory.each(mockConsumer);

		verify(mockConsumer, times(2)).accept(any());
	}

	@DisplayName("ComponentFactory Retorna uma string formatada em JSON com dados dos componentes cadastrados.")
	@Test
	void testToJSONWithMultipleComponents() {
		componentFactory.register(new TextArea());
		componentFactory.register(new DateField());
		var component1 = componentFactory.get(0);
		var componet2 = componentFactory.get(1);

		String expectedJson = "[{\"label\":\"" + component1.getDisplayName() + "\",\"id\":\"" + component1.getId()
			+ "\"},{\"label\":\"" + componet2.getDisplayName() + "\",\"id\":\"" + componet2.getId() + "\"}]";

		String actualJson = componentFactory.toJSON();

		assertEquals(expectedJson, actualJson, "O JSON gerado não corresponde ao esperado para múltiplos componentes.");
	}
}
