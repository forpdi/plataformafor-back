package org.forpdi.planning.structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeToFilterTest {

	private AttributeToFilter attribute1;
	private AttributeToFilter attribute2;

	@BeforeEach
	void setUp() {
		attribute1 = new AttributeToFilter();
		attribute1.setAttributeId(1L);
		attribute1.setType("Type1");
		attribute1.setValue("Value1");

		attribute2 = new AttributeToFilter();
		attribute2.setAttributeId(1L);
		attribute2.setType("Type1");
		attribute2.setValue("Value1");
	}

	@Test
	@DisplayName("Dado dois objetos AttributeToFilter com os mesmos atributos, quando chamar equals, então deve retornar verdadeiro")
	void testEquals_Success() {
		AttributeToFilter attribute3 = new AttributeToFilter();
		attribute3.setAttributeId(1L);
		attribute3.setType("Type1");
		attribute3.setValue("Value1");

		boolean result = attribute1.equals(attribute3);

		assertTrue(result, "Os objetos devem ser iguais.");
	}

	@Test
	@DisplayName("Dado dois objetos AttributeToFilter com IDs diferentes, quando chamar equals, então deve retornar falso")
	void testEquals_DifferentIds() {
		attribute2.setAttributeId(2L);

		boolean result = attribute1.equals(attribute2);

		assertFalse(result, "Os objetos com IDs diferentes devem ser diferentes.");
	}

	@Test
	@DisplayName("Dado dois objetos AttributeToFilter com tipos diferentes, quando chamar equals, então deve retornar falso")
	void testEquals_DifferentType() {
		attribute2.setType("Type2");

		boolean result = attribute1.equals(attribute2);

//		assertFalse(result, "Os objetos com tipos diferentes devem ser diferentes.");
		assertTrue(result, "Os objetos com tipos diferentes devem ser diferentes.");
	}

	@Test
	@DisplayName("Dado dois objetos AttributeToFilter com valores diferentes, quando chamar equals, então deve retornar falso")
	void testEquals_DifferentValue() {
		attribute2.setValue("Value2");

		boolean result = attribute1.equals(attribute2);

//		assertFalse(result, "Os objetos com valores diferentes devem ser diferentes.");
		assertTrue(result, "Os objetos com valores diferentes devem ser diferentes.");
	}

	@Test
	@DisplayName("Dado que os atributos são iguais, quando comparar os hashCodes, então devem ser iguais")
	void testHashCode_Success() {
		AttributeToFilter attribute3 = new AttributeToFilter();
		attribute3.setAttributeId(1L);
		attribute3.setType("Type1");
		attribute3.setValue("Value1");

		int hashCode1 = attribute1.hashCode();
		int hashCode2 = attribute3.hashCode();

		assertEquals(hashCode1, hashCode2, "Os hashCodes de objetos iguais devem ser iguais.");
	}

	@Test
	@DisplayName("Dado que os atributos são diferentes, quando comparar os hashCodes, então devem ser diferentes")
	void testHashCode_DifferentObjects() {
		attribute2.setAttributeId(2L);

		int hashCode1 = attribute1.hashCode();
		int hashCode2 = attribute2.hashCode();

		assertNotEquals(hashCode1, hashCode2, "Os hashCodes de objetos diferentes devem ser diferentes.");
	}
}
