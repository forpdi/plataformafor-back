package org.forrisco.risk.typology;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskTypologyTypeTest {
	@Test
	@DisplayName("Teste da inicialização das Enum.")
	void testEnumValues() {

		assertEquals(1, RiskTypologyType.OPERATIONAL.getId());
		assertEquals("Risco Operacional", RiskTypologyType.OPERATIONAL.getValue());
		assertEquals("#ed3737", RiskTypologyType.OPERATIONAL.getColorHex());

		assertEquals(2, RiskTypologyType.IMAGE_REPUTATION.getId());
		assertEquals("Risco de imagem/reputação de órgão", RiskTypologyType.IMAGE_REPUTATION.getValue());
		assertEquals("#ef8a49", RiskTypologyType.IMAGE_REPUTATION.getColorHex());

		assertEquals(3, RiskTypologyType.LEGAL.getId());
		assertEquals("Risco legal", RiskTypologyType.LEGAL.getValue());
		assertEquals("#f6cd2b", RiskTypologyType.LEGAL.getColorHex());

		assertEquals(4, RiskTypologyType.BUDGET_FINANCIAL.getId());
		assertEquals("Risco financeiro/orçamentário", RiskTypologyType.BUDGET_FINANCIAL.getValue());
		assertEquals("#cce655", RiskTypologyType.BUDGET_FINANCIAL.getColorHex());

		assertEquals(5, RiskTypologyType.INTEGRITY.getId());
		assertEquals("Risco de Integridade", RiskTypologyType.INTEGRITY.getValue());
		assertEquals("#79cbc1", RiskTypologyType.INTEGRITY.getColorHex());

		assertEquals(6, RiskTypologyType.OTHER.getId());
		assertEquals("Outras", RiskTypologyType.OTHER.getValue());
		assertEquals("#9ec3f4", RiskTypologyType.OTHER.getColorHex());
	}

	@Test
	@DisplayName("Teste do método getRiskTypologyTypeByValue.")
	void testGetRiskTypologyTypeByValue() {

		assertEquals(RiskTypologyType.OPERATIONAL, RiskTypologyType.getRiskTypologyTypeByValue("Risco Operacional"));
		assertEquals(RiskTypologyType.IMAGE_REPUTATION, RiskTypologyType.getRiskTypologyTypeByValue("Risco de imagem/reputação de órgão"));
		assertEquals(RiskTypologyType.LEGAL, RiskTypologyType.getRiskTypologyTypeByValue("Risco legal"));

		assertNull(RiskTypologyType.getRiskTypologyTypeByValue("Valor Inexistente"));
	}

	@Test
	@DisplayName("Teste do método getRiskTypologyTypesByValue.")
	void testGetRiskTypologyTypesByValue() {

		String value = "Risco Operacional;Risco legal;Risco de Integridade";
		List<RiskTypologyType> typologies = RiskTypologyType.getRiskTypologyTypesByValue(value);

		assertEquals(3, typologies.size());
		assertTrue(typologies.contains(RiskTypologyType.OPERATIONAL));
		assertTrue(typologies.contains(RiskTypologyType.LEGAL));
		assertTrue(typologies.contains(RiskTypologyType.INTEGRITY));

		assertTrue(RiskTypologyType.getRiskTypologyTypesByValue(null).isEmpty());
		assertTrue(RiskTypologyType.getRiskTypologyTypesByValue("").contains(null));
	}

	@Test
	@DisplayName("Utilização do comparador.")
	void testComparator() {
		RiskTypologyType typology1 = RiskTypologyType.LEGAL;
		RiskTypologyType typology2 = RiskTypologyType.OPERATIONAL;

		assertTrue(typology1.compare(typology1, typology2) > 0);
		assertTrue(typology2.compare(typology2, typology1) < 0);
		assertEquals(0, typology1.compare(typology1, typology1));
	}
}