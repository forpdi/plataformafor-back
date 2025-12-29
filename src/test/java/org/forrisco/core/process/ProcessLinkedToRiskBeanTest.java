package org.forrisco.core.process;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessLinkedToRiskBeanTest {

	@DisplayName("Deve criar um bean com o construtor padrão e setters")
	@Test
	void testCreationWithDefaultConstructor() {
		ProcessLinkedToRiskBean bean = new ProcessLinkedToRiskBean();

		bean.setId(1L);
		bean.setRiskId(2L);
		bean.setUnitId(3L);
		bean.setName("Name");

		assertEquals(1L, bean.getId());
		assertEquals(2L, bean.getRiskId());
		assertEquals(3L, bean.getUnitId());
		assertEquals("Name", bean.getName());
	}

	@DisplayName("Deve criar um bean com o construtor que recebe todos os parâmetros")
	@Test
	void testCreationWithAllArgsConstructor() {
		ProcessLinkedToRiskBean bean = new ProcessLinkedToRiskBean(10L, "Constructor Test", 20L, 30L);

		assertEquals(10L, bean.getId());
		assertEquals("Constructor Test", bean.getName());
		assertEquals(20L, bean.getRiskId());
		assertEquals(30L, bean.getUnitId());
	}

	@DisplayName("equals: Deve retornar true para objetos com mesmo id e riskId")
	@Test
	void equals_shouldReturnTrueForSameIdAndRiskId() {
		ProcessLinkedToRiskBean bean1 = new ProcessLinkedToRiskBean(1L, "Process A", 100L, 1000L);
		ProcessLinkedToRiskBean bean2 = new ProcessLinkedToRiskBean(1L, "Process B", 100L, 2000L);

		assertTrue(bean1.equals(bean2), "Objetos com mesmo id e riskId devem ser iguais.");
	}

	@DisplayName("equals: Deve retornar false para objetos com riskId diferente")
	@Test
	void equals_shouldReturnFalseForDifferentRiskId() {
		ProcessLinkedToRiskBean bean1 = new ProcessLinkedToRiskBean(1L, "Process A", 100L, 1000L);
		ProcessLinkedToRiskBean bean2 = new ProcessLinkedToRiskBean(1L, "Process A", 999L, 1000L);

		assertFalse(bean1.equals(bean2), "Objetos com riskId diferente não devem ser iguais.");
	}

	@DisplayName("equals: Deve retornar false quando comparado com nulo ou outro tipo de objeto")
	@Test
	void equals_shouldReturnFalseForNullOrDifferentType() {
		ProcessLinkedToRiskBean bean1 = new ProcessLinkedToRiskBean(1L, "Process A", 100L, 1000L);
		Object otherObject = new Object();

		assertFalse(bean1.equals(null), "Comparar com null deve retornar false.");
		assertFalse(bean1.equals(otherObject), "Comparar com um tipo de objeto diferente deve retornar false.");
	}

	@DisplayName("hashCode: Deve retornar o mesmo hash para objetos que são iguais")
	@Test
	void hashCode_shouldReturnSameHashForEqualObjects() {
		ProcessLinkedToRiskBean bean1 = new ProcessLinkedToRiskBean(1L, "Process A", 100L, 1000L);
		ProcessLinkedToRiskBean bean2 = new ProcessLinkedToRiskBean(1L, "Process B", 100L, 2000L);

		assertEquals(bean1.hashCode(), bean2.hashCode(), "Objetos iguais devem ter o mesmo hashCode.");
	}
}