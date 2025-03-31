package org.forpdi.core.utils;

import org.forpdi.core.notification.Notification;
import org.forpdi.core.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste da classe NotificationUtil")
class NotificationUtilTest {

	@Test
	@DisplayName("Teste de descrição de notificações para cada tipo definido")
	void testSetDescriptionForNotification() {
		Notification notification = new Notification();
		String text = "Texto de Teste";
		String aux = "Auxiliar";

		for (NotificationType type : NotificationType.values()) {
			NotificationUtil.setDescriptionForNotification(notification, type, text, aux);
			String description = notification.getDescription();

			switch (type) {
				case WELCOME:
					assertEquals("Bem vindo ao ForPDI, seu cadastro foi realizado com sucesso.", description);
					break;

				case ACCESSLEVEL_CHANGED:
					assertEquals("<b>Seu tipo de conta foi alterado. Agora você é um </b>\"Texto de Teste\"<b>.</b>", description);
					break;

				case PERMISSION_CHANGED:
					assertEquals("<b>Suas permissões foram alteradas. Agora você pode: </b>\"Texto de Teste\"<b>. Faça o login novamente para aplicar as alterações.</b>", description);
					break;

				case PLAN_MACRO_CREATED:
					assertEquals("<b>O plano </b>\"Texto de Teste\"<b> foi criado.</b>", description);
					break;

				case PLAN_CREATED:
					assertEquals("<b>O plano de metas </b>\"Texto de Teste\"<b> foi criado no plano </b>\"Auxiliar\"<b>.</b>", description);
					break;

				case ATTRIBUTED_RESPONSIBLE:
					assertEquals("<b>Você foi atribuído como responsável em: </b>\"Auxiliar\" - \"Texto de Teste\"<b>.</b>", description);
					break;

				case GOAL_CLOSED:
					assertEquals("<b>A meta </b>\"Texto de Teste\"<b> foi concluída.</b>", description);
					break;

				case GOAL_OPENED:
					assertEquals("<b>A meta </b>\"Texto de Teste\"<b> foi reaberta.</b>", description);
					break;

				case PLAN_CLOSE_TO_MATURITY:
					assertEquals("<b>O plano de metas </b>\"Texto de Teste\"<b> do plano </b>\"Auxiliar\"<b> está próximo da data de término.</b>", description);
					break;

				case GOAL_CLOSE_TO_MATURITY:
					assertEquals("<b>A meta </b>\"Texto de Teste\"<b> do indicador </b>\"Auxiliar\"<b> está próxima da data de vencimento.</b>", description);
					break;

				case LATE_GOAL:
					assertEquals("<b>A meta </b>\"Texto de Teste\"<b> do indicador </b>\"Auxiliar\"<b> está atrasada.</b>", description);
					break;

				case ACTION_PLAN_CLOSED:
					assertEquals("<b>A ação </b>\"Texto de Teste\"<b> do indicador </b>\"Auxiliar\"<b> foi concluída.</b>", description);
					break;

				case LATE_ACTION_PLAN:
					assertEquals("<b>A ação </b>\"Texto de Teste\"<b> do indicador </b>\"Auxiliar\"<b> está atrasada.</b>", description);
					break;

				case ACTION_PLAN_CLOSE_TO_MATURITY:
					assertEquals("<b>A ação </b>\"Texto de Teste\"<b> do indicador </b>\"Auxiliar\"<b> está próxima do vencimento.</b>", description);
					break;

				case GOAL_ATTRIBUTE_UPDATED:
					assertEquals("<b>A meta </b>\"Texto de Teste\" do indicador \"Auxiliar<b> foi alterada. Verifique as alterações.</b>", description);
					break;

				case FORRISCO_PROCESS_CREATED:
				case FORRISCO_USER_LINKED_TO_RISK:
				case FORRISCO_MANAGER_RISK_UPDATED:
					assertEquals("<b>Texto de Teste</b>", description);
					break;

				case FORRISCO_RISK_CLOSE_TO_MATURITY:
					assertEquals("<b>O monitoramento do risco</b> \"Texto de Teste\" <b>no ForRisco está próximo a vencer. Crie um novo monitoramento no sistema para atualizar o risco.</b>", description);
					break;

				case FORRISCO_MANAGER_LINKED_TO_RISK_ITEM:
					assertEquals("<b>Você foi vinculado como gestor responsável por Texto de Teste do risco Auxiliar no ForRisco</b>", description);
					break;

				case FORRISCO_MANAGER_RISK_ITEM_UPDATED:
					assertEquals("<b>Texto de Teste do risco Auxiliar foi alterado.</b>", description);
					break;

				case USER_LINKED_TO_ACTION_PLAN:
					assertEquals("Você foi vinculado como responsável para o plano de ação \"Texto de Teste\" do indicador \"Auxiliar\"", description);
					break;

				default:
					assertEquals("", description, "Para notificações não mapeadas, a descrição deve ser uma string vazia.");
			}
		}
	}
}
