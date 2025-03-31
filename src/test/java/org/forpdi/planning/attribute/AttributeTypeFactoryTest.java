package org.forpdi.planning.attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeTypeFactoryTest {

	private AttributeTypeFactory factory;

	@BeforeEach
	void setUp() {
		factory = AttributeTypeFactory.getInstance();
	}

	@Test
	@DisplayName("Dado a instância da fábrica, quando a obtemos, então é a mesma instância singleton")
	void testSingletonInstance() {
		AttributeTypeFactory firstInstance = AttributeTypeFactory.getInstance();
		AttributeTypeFactory secondInstance = AttributeTypeFactory.getInstance();

		assertSame(firstInstance, secondInstance, "A instância deve ser a mesma para o padrão singleton.");
	}

	@Test
	@DisplayName("Dado a fábrica, quando registramos os tipos de atributo, então os tipos devem ser registrados corretamente.")
	void testRegisterAttributeTypes() {

		/*
			Se o intuíto é validar o registo dos tipos, por meio de 'get' é possível recuperar um componente pelo seu ID
		 	que é seu nome canônico.
			Logo toda vez que uma fábrica for criada, haverá os registros por meio do método privado e se o mesmo
			acontecer, haverá um retorno na função get.
		*/

		// Verifica se o retorno da consulta a um tipo não é nulo(Foi registrado).
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.TextArea"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.TextField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.DateField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.DateTimeField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.Currency"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.NumberField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.Percentage"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.BudgetField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.ActionPlanField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.ScheduleField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.TableField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.TotalField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.SelectField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.SelectPlan"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.ResponsibleField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.AttachmentField"));
		assertNotNull(factory.get("org.forpdi.planning.attribute.types.ManagerField"));

		// Valida o total esperado de registros.
		assertEquals(17, factory.size());
	}

}
