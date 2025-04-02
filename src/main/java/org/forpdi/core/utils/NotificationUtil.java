package org.forpdi.core.utils;

import org.forpdi.core.notification.Notification;
import org.forpdi.core.notification.NotificationType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

public class NotificationUtil {

    private static final Map<NotificationType, BiFunction<String, String, String>> NOTIFICATION_DESCRIPTIONS = new EnumMap<>(NotificationType.class);

    static {
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.WELCOME, (text, aux) -> "Bem vindo ao ForPDI, seu cadastro foi realizado com sucesso.");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.ACCESSLEVEL_CHANGED, (text, aux) -> "<b>Seu tipo de conta foi alterado. Agora você é um </b>\"" + text + "\"<b>.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.PERMISSION_CHANGED, (text, aux) -> "<b>Suas permissões foram alteradas. Agora você pode: </b>\"" + text + "\"<b>. Faça o login novamente para aplicar as alterações.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.PLAN_MACRO_CREATED, (text, aux) -> "<b>O plano </b>\"" + text + "\"<b> foi criado.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.PLAN_CREATED, (text, aux) -> "<b>O plano de metas </b>\"" + text + "\"<b> foi criado no plano </b>\"" + aux + "\"<b>.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.ATTRIBUTED_RESPONSIBLE, (text, aux) -> "<b>Você foi atribuído como responsável em: </b>\"" + aux + "\" - \"" + text + "\"<b>.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.GOAL_CLOSED, (text, aux) -> "<b>A meta </b>\"" + text + "\"<b> foi concluída.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.GOAL_OPENED, (text, aux) -> "<b>A meta </b>\"" + text + "\"<b> foi reaberta.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.PLAN_CLOSE_TO_MATURITY, (text, aux) -> "<b>O plano de metas </b>\"" + text + "\"<b> do plano </b>\"" + aux + "\"<b> está próximo da data de término.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.GOAL_CLOSE_TO_MATURITY, (text, aux) -> {
            if (!aux.isEmpty()) {
                aux = "<b> do indicador </b>\"" + aux + "\"";
            }
            return "<b>A meta </b>\"" + text + "\"" + aux + "<b> está próxima da data de vencimento.</b>";
        });
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.LATE_GOAL, (text, aux) -> {
            if (!aux.isEmpty()) {
                aux = "<b> do indicador </b>\"" + aux + "\"";
            }
            return "<b>A meta </b>\"" + text + "\"" + aux + "<b> está atrasada.</b>";
        });
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.ACTION_PLAN_CLOSED, (text, aux) -> "<b>A ação </b>\"" + text + "\"<b> do indicador </b>\"" + aux + "\"<b> foi concluída.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.LATE_ACTION_PLAN, (text, aux) -> "<b>A ação </b>\"" + text + "\"<b> do indicador </b>\"" + aux + "\"<b> está atrasada.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.ACTION_PLAN_CLOSE_TO_MATURITY, (text, aux) -> "<b>A ação </b>\"" + text + "\"<b> do indicador </b>\"" + aux + "\"<b> está próxima do vencimento.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.GOAL_ATTRIBUTE_UPDATED, (text, aux) -> "<b>A meta </b>\"" + text + "\" do indicador \"" + aux + "<b> foi alterada. Verifique as alterações.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.FORRISCO_PROCESS_CREATED, (text, aux) -> "<b>" + text + "</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.FORRISCO_RISK_CLOSE_TO_MATURITY, (text, aux) -> "<b>O monitoramento do risco</b> \"" + text + "\" <b>no ForRisco está próximo a vencer. Crie um novo monitoramento no sistema para atualizar o risco.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.FORRISCO_USER_LINKED_TO_RISK, (text, aux) -> "<b>" + text + "</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.FORRISCO_MANAGER_RISK_UPDATED, (text, aux) -> "<b>" + text + "</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM, (text, aux) -> "<b>Você foi vinculado como gestor responsável por " + text + " do risco " + aux + " no ForRisco</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED, (text, aux) -> "<b>" + text + " do risco " + aux + " foi alterado.</b>");
        NOTIFICATION_DESCRIPTIONS.put(NotificationType.USER_LINKED_TO_ACTION_PLAN, (text, aux) -> "Você foi vinculado como responsável para o plano de ação \"" + text + "\" do indicador \"" + aux + "\"");
    }

    /**
     * Setar a descrição da notificação.
     *
     * @param notification Tipo da notificação para setar a descrição.
     * @param type         Tipo da notificação.
     * @param text         Texto da descrição.
     * @param aux          Nome do Plano de metas na onde ocorreu a notificação.
     */
    public static void setDescriptionForNotification(Notification notification, NotificationType type, String text, String aux) {
        String description = NOTIFICATION_DESCRIPTIONS.getOrDefault(type, (t, a) -> "").apply(text, aux);
        notification.setDescription(description);
    }
}