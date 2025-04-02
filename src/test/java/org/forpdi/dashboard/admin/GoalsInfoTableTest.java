package org.forpdi.dashboard.admin;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalsInfoTableTest {

	@Test
	@DisplayName("Teste de configuração e obtenção do nome do plano")
	void testSetAndGetPlanName() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setPlanName("Plano Estratégico 2025");

		assertEquals("Plano Estratégico 2025", goalInfo.getPlanName(), "O nome do plano deve ser 'Plano Estratégico 2025'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção da expectativa de atingimento da meta")
	void testSetAndGetExpected() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setExpected(75.5);

		assertEquals(75.5, goalInfo.getExpected(), "O valor esperado deve ser 75.5");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do valor alcançado da meta")
	void testSetAndGetReached() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setReached(80.0);

		assertEquals(80.0, goalInfo.getReached(), "O valor alcançado deve ser 80.0");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do status de progresso da meta")
	void testSetAndGetProgressStatus() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setProgressStatus(2);

		assertEquals(2, goalInfo.getProgressStatus(), "O status de progresso deve ser 2");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção da data de término da meta")
	void testSetAndGetFinishDate() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		Date finishDate = new Date();
		goalInfo.setFinishDate(finishDate);

		assertEquals(finishDate, goalInfo.getFinishDate(), "A data de término deve ser igual a data definida");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do status de deadline da meta")
	void testSetAndGetDeadLineStatus() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setDeadLineStatus(1);

		assertEquals(1, goalInfo.getDeadLineStatus(), "O status de deadline deve ser 1");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do nome do eixo estratégico")
	void testSetAndGetStrategicAxisName() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setStrategicAxisName("Eixo Estratégico 1");

		assertEquals("Eixo Estratégico 1", goalInfo.getStrategicAxisName(), "O nome do eixo estratégico deve ser 'Eixo Estratégico 1'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do nome do objetivo")
	void testSetAndGetObjectiveName() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setObjectiveName("Aumentar a eficiência");

		assertEquals("Aumentar a eficiência", goalInfo.getObjectiveName(), "O nome do objetivo deve ser 'Aumentar a eficiência'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do nome do indicador")
	void testSetAndGetIndicatorName() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setIndicatorName("Taxa de Retorno");

		assertEquals("Taxa de Retorno", goalInfo.getIndicatorName(), "O nome do indicador deve ser 'Taxa de Retorno'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do nome da meta")
	void testSetAndGetGoalName() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setGoalName("Meta de Crescimento");

		assertEquals("Meta de Crescimento", goalInfo.getGoalName(), "O nome da meta deve ser 'Meta de Crescimento'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do responsável pela meta")
	void testSetAndGetResponsible() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setResponsible("João Silva");

		assertEquals("João Silva", goalInfo.getResponsible(), "O responsável pela meta deve ser 'João Silva'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção do status da meta")
	void testSetAndGetGoalStatus() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setGoalStatus("Em progresso");

		assertEquals("Em progresso", goalInfo.getGoalStatus(), "O status da meta deve ser 'Em progresso'");
	}

	@Test
	@DisplayName("Teste de configuração e obtenção da última modificação")
	void testSetAndGetLastModification() {
		GoalsInfoTable goalInfo = new GoalsInfoTable();
		goalInfo.setLastModification("2024-12-18");

		assertEquals("2024-12-18", goalInfo.getLastModification(), "A última modificação deve ser '2024-12-18'");
	}

}
