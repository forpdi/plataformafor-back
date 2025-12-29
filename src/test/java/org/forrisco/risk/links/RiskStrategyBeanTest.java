package org.forrisco.risk.links;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RiskStrategyBeanTest {

	@DisplayName("RiskStrategyBean Criação do objeto pelo construtor completo.")
	@Test
	public void testRiskStrategyBeanConstructorWithParameters() {
		long expectedId = 1L;
		String expectedName = "Strategy A";
		long expectedStructureId = 100L;

		RiskStrategyBean bean = new RiskStrategyBean(expectedId, expectedName, expectedStructureId);

		assertEquals(expectedId, bean.getId());
		assertEquals(expectedName, bean.getName());
		assertEquals(expectedStructureId, bean.getStructureId());
	}

	@DisplayName("RiskStrategyBean Criação do objeto pelo construtor vazio.")
	@Test
	public void testRiskStrategyBeanConstructorWithoutParameters() {
		RiskStrategyBean bean = new RiskStrategyBean();

		assertEquals(0L, bean.getId());
		assertNull(bean.getName());
		assertEquals(0L, bean.getStructureId());
	}

	@DisplayName("RiskStrategyBean Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testSettersAndGetters() {
		RiskStrategyBean bean = new RiskStrategyBean();

		bean.setId(2L);
		bean.setName("Strategy B");
		bean.setStructureId(200L);

		assertEquals(2L, bean.getId());
		assertEquals("Strategy B", bean.getName());
		assertEquals(200L, bean.getStructureId());
	}
}