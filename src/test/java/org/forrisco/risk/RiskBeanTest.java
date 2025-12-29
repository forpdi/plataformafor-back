package org.forrisco.risk;

import java.util.Date;

import org.forpdi.core.common.PaginatedList;
import org.forrisco.risk.links.RiskStrategyBean;
import org.forrisco.risk.monitor.MonitoringState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskBeanTest {
	@Test
	@DisplayName("Teste do construtor com parâmetros")
	void testConstructorWithParameters() {
		long id = 1L;
		long userId = 2L;
		Long managerId = 3L;
		long unitId = 4L;
		RiskLevel riskLevel = new RiskLevel();
		String name = "Test Risk";
		String probability = "High";
		String impact = "Critical";
		String periodicity = "Monthly";
		String tipology = "Financial";
		String otherTipologies = "Operational";
		String type = "Strategic";
		Date begin = new Date();
		Integer response = 1;

		RiskBean riskBean = new RiskBean(id, userId, managerId, unitId, riskLevel, name, probability, impact,
			periodicity, tipology, otherTipologies, type, begin, response);

		assertEquals(id, riskBean.getId(), "ID incorreto.");
		assertEquals(userId, riskBean.getUserId(), "User ID incorreto.");
		assertEquals(managerId, riskBean.getManagerId(), "Manager ID incorreto.");
		assertEquals(unitId, riskBean.getUnitId(), "Unit ID incorreto.");
		assertEquals(riskLevel, riskBean.getRiskLevel(), "Nível de risco incorreto.");
		assertEquals(name, riskBean.getName(), "Nome incorreto.");
		assertEquals(probability, riskBean.getProbability(), "Probabilidade incorreta.");
		assertEquals(impact, riskBean.getImpact(), "Impacto incorreto.");
		assertEquals(periodicity, riskBean.getPeriodicity(), "Periodicidade incorreta.");
		assertEquals(tipology, riskBean.getTipology(), "Tipologia incorreta.");
		assertEquals(otherTipologies, riskBean.getOtherTipologies(), "Outras tipologias incorretas.");
		assertEquals(type, riskBean.getType(), "Tipo incorreto.");
		assertEquals(begin, riskBean.getBegin(), "Data de início incorreta.");
		assertEquals(response, riskBean.getResponse(), "Resposta incorreta.");
	}

	@Test
	@DisplayName("Teste do construtor vazio e setters")
	void testConstructorAndSetters() {
		RiskBean riskBean = new RiskBean();

		riskBean.setId(1L);
		riskBean.setUserId(2L);
		riskBean.setManagerId(3L);
		riskBean.setUnitId(4L);
		riskBean.setRiskLevel(new RiskLevel());
		riskBean.setName("Test Risk");
		riskBean.setProbability("Low");
		riskBean.setImpact("Moderate");
		riskBean.setPeriodicity("Quarterly");
		riskBean.setTipology("Technical");
		riskBean.setOtherTipologies("Compliance");
		riskBean.setType("Tactical");
		riskBean.setBegin(new Date());
		riskBean.setResponse(2);
		riskBean.setMonitoringState(MonitoringState.UP_TO_DATE.getOrder());

		assertEquals(1L, riskBean.getId(), "ID incorreto.");
		assertEquals(2L, riskBean.getUserId(), "User ID incorreto.");
		assertEquals(3L, riskBean.getManagerId(), "Manager ID incorreto.");
		assertEquals(4L, riskBean.getUnitId(), "Unit ID incorreto.");
		assertNotNull(riskBean.getRiskLevel(), "Nível de risco não deveria ser nulo.");
		assertEquals("Test Risk", riskBean.getName(), "Nome incorreto.");
		assertEquals("Low", riskBean.getProbability(), "Probabilidade incorreta.");
		assertEquals("Moderate", riskBean.getImpact(), "Impacto incorreto.");
		assertEquals("Quarterly", riskBean.getPeriodicity(), "Periodicidade incorreta.");
		assertEquals("Technical", riskBean.getTipology(), "Tipologia incorreta.");
		assertEquals("Compliance", riskBean.getOtherTipologies(), "Outras tipologias incorretas.");
		assertEquals("Tactical", riskBean.getType(), "Tipo incorreto.");
		assertNotNull(riskBean.getBegin(), "Data de início não deve ser nula.");
		assertEquals(1, riskBean.getMonitoringState(), "O estado de monitoramento não está correto.");
		assertEquals(2, riskBean.getResponse(), "Resposta incorreta.");
	}

	@Test
	@DisplayName("Teste do atributo strategies")
	void testStrategies() {
		RiskBean riskBean = new RiskBean();

		PaginatedList<RiskStrategyBean> strategies = new PaginatedList<>();
		riskBean.setStrategies(strategies);

		assertEquals(strategies, riskBean.getStrategies(), "O atributo strategies não foi configurado corretamente.");
	}
}