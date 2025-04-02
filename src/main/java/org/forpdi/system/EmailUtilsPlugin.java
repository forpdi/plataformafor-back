package org.forpdi.system;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.properties.SystemConfigs;
import org.forpdi.core.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailUtilsPlugin {
	
	@Value("${smtp.from.name}")
	private String fromName;
	@Value("${smtp.from.email}")
	private String fromEmail;
	@Value("${smtp.host}")
	private String host;
	@Value("${smtp.port}")
	private Integer port;
	@Value("${smtp.user}")
	private String user;
	@Value("${smtp.password}")
	private String password;
	@Value("${smtp.ssl}")
	private boolean ssl;
	@Value("${smtp.tls}")
	private boolean tls;
	
	@Value("${store.files}")
	private String storeFiles;
	
	@Value("${sys.frontendUrl}")
	private String frontendUrl;
	
	
	public boolean emailSenderIsEnabled() {
		return !GeneralUtils.isEmpty(host);
	}
	
	/**
	 * Eviar email com imagens e outros contéudos
	 * @param toEmail
	 * 			Email
	 * @param toName
	 * 			Nome
	 * @param subject
	 * 			Assunto
	 * @param msg
	 * 			Mensagem
	 * @return
	 * @throws EmailException
	 */
	public String sendHtmlEmail(String toEmail, String toName, String subject, String msg, String attach) throws EmailException {
		MultiPartEmail email = EmailUtils.getHtmlEmail();
		email.setAuthentication(user, password);
		email.setFrom(fromEmail, fromName);
		email.setHostName(host);
		email.setSmtpPort(port);
		email.setSslSmtpPort(port.toString());
		email.addTo(toEmail, toName);
		email.setSubject(subject);
		email.setMsg(msg);
		email.setSSLOnConnect(ssl);
		email.setStartTLSEnabled(tls);
		//email.setStartTLSRequired(true);

		// Create the attachment
		if(attach !=null) {
			EmailAttachment attachment = new EmailAttachment();
			attachment.setPath(storeFiles + attach);
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.setDescription(attach);
			attachment.setName(attach);
			email.attach(attachment);
		}
		
		return email.send();
	}
	
	public static class EmailBuilder {

		private String subject;
		private String body;

		public String getSubject() {
			return this.subject;
		}

		public String getBody() {
			return this.body;
		}

		/**
		 * Enviar email de notificação para o usuário.
		 * 
		 * @param type
		 *            Tipo da notificação.
		 * @param extras
		 *            Campo com as informações para gerar a mensagem do e-mail da
		 *            notificação.
		 */
		public EmailBuilder(NotificationType type, String... extras) {
			this.subject = this.getSubjectByType(type);
			switch (type) {
			case WELCOME:
				this.body = this.mountWelcomeEmail(extras[0]);
				break;
			case ACCESSLEVEL_CHANGED:
				this.body = this.mountAccessLevelChanged(extras[0], extras[1]);
				break;
			case INVITE_USER:
				this.body = this.mountInviteUserEmail(extras[0], extras[1]);
				break;
			case RECOVER_PASSWORD:
				this.body = this.mountRecoverEmail(extras[0], extras[1]);
				break;
			case FORRISCO_PROCESS_CREATED:
			case FORRISCO_RISK_CLOSE_TO_MATURITY:
			case FORRISCO_USER_LINKED_TO_RISK:
			case FORRISCO_MANAGER_LINKED_TO_RISK_ITEM:
			case FORRISCO_MANAGER_RISK_ITEM_UPDATED:
			case FORRISCO_MANAGER_RISK_UPDATED:
				this.body = this.mountForriscoNotificationEmail(extras[0], extras[1]);
				break;
			default:
				this.body = this.mountNotificationEmail(extras[0], extras[1]);
				break;
			}
		}

		/**
		 * Retorna o nome do tipo da notificação.
		 * 
		 * @param type
		 *            Tipo da notificação.
		 * @return String Nome do tipo da notificação.
		 */
		private String getSubjectByType(NotificationType type) {
			switch (type) {
			case WELCOME:
				return "Bem-vindo a PlataformaFor";
			case ACCESSLEVEL_CHANGED:
				return "Seu nível de acesso na PlataformaFor foi alterada";
			case PERMISSION_CHANGED:
				return "Suas permissões no ForPDI foram alteradas";
			case PLAN_MACRO_CREATED:
				return "Um novo plano foi criado na sua instituição do ForPDI";
			case PLAN_CREATED:
				return "Um novo plano de metas foi criado na sua instituição do ForPDI";
			case ATTRIBUTED_RESPONSIBLE:
				return "Você foi atribuido como responsável por um nível no ForPDI";
			case GOAL_CLOSED:
				return "Uma meta foi concluída no ForPDI";
			case GOAL_OPENED:
				return "Uma meta foi reaberta no ForPDI";
			case PLAN_CLOSE_TO_MATURITY:
				return "Um plano de metas está próximo a data de finalização no ForPDI";
			case GOAL_CLOSE_TO_MATURITY:
				return "Uma meta está próxima a data de vencimento no ForPDI";
			case LATE_GOAL:
				return "Uma meta está atrasada no ForPDI";
			case INVITE_USER:
				return "Complete seu cadastro na Plataforma For";
			case RECOVER_PASSWORD:
				return "Recuperação de Senha de Acesso à Plataforma For";
			case ACTION_PLAN_CLOSED:
				return "Plano de ação foi concluído no ForPDI";
			case LATE_ACTION_PLAN:
				return "Um plano de ação está atrasado no ForPDI";
			case ACTION_PLAN_CLOSE_TO_MATURITY:
				return "Um plano de ação está próximo do vencimento no ForPDI";
			case GOAL_ATTRIBUTE_UPDATED:
				return "Ocorreu uma alteração em uma meta";
			case FORRISCO_PROCESS_CREATED:
				return "ForRisco - A sua unidade foi relacionada a um processo";
			case FORRISCO_RISK_CLOSE_TO_MATURITY:
				return "ForRisco - O risco está com monitoramento vencido";
			case FORRISCO_USER_LINKED_TO_RISK:
				return "ForRisco - Você foi vinculado a um risco";
			case FORRISCO_MANAGER_LINKED_TO_RISK_ITEM:
				return "ForRisco - Você foi vinculado como gestor de um item do risco";
			case FORRISCO_MANAGER_RISK_UPDATED:
				return "ForRisco - Ocorreu uma alteração em um risco";
			case FORRISCO_MANAGER_RISK_ITEM_UPDATED:
				return "ForRisco - Ocorreu uma alteração em um item de um risco";
			case USER_LINKED_TO_ACTION_PLAN:
				return "Você foi atribuido como responsável por um plano de ação no ForPDI";
			default:
				throw new IllegalArgumentException("NotificationType doesnt have an email subject");
			}
		}

		/**
		 * Mensagem base do email para os vários tipos de notificação.
		 * 
		 * @param msg
		 *            Mensagem da notificação.
		 * @return Mensagem do e-mail para os vários tipos de notificação.
		 */
		private String mountNotificationEmail(String msg, String url) {
			return ("<meta charset='utf-8'>" + "<div>" + "<div>"
					+ "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: " + url
					+ "</p>" + "<center style='margin-top: 50px'>"
					+ "<img src='" + getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
					+ "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
					+ "Plataforma Aberta para Gest&atilde;o e Acompanhamento do <br>Plano de Desenvolvimento Institucional - PDI"
					+ "</h3>"
					+ "<center style='margin: 25px;width: 600px;height: 350px;border-top: 4px solid #0383D9;border-right: 1px solid #CCC;"
					+ "border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
					+ "<h1 style='color: #0A4068;font-family: sans-serif;'>Notifica&ccedil;&atilde;o</h1>"
					+ "<p style='margin-bottom: 80px;margin-top: 90px;font-family: sans-serif;color: #9C9C9C;width: 90%;'>"
					+ msg + "</p>"
					+ "<p><a style='text-decoration: none;background-color: #1C486D;color: #FFF;padding-top: 8px;padding-bottom: 8px;"
					+ "padding-left: 20px;padding-right: 20px;border-radius: 7px;font-family: sans-serif;' href='" + url
					+ "'>" + "Acesse o ForPDI" + "</a></p>" + "</center>"
					+ "<center style='margin-top: 30px;font-family: sans-serif; font-size: 12px;'>"
					//+ "<p style='color: #1C486D;font-weight: 600;'>ForPDI - Todos os direitos reservados</p>"
					//+ "<a style='color: #9C9C9C;text-decoration: none;' href='www.forpdi.org'>www.forpdi.org</a>"
					+ "</center>" + "</center>" + "</div>" + "</div>" + "</div>");
		}

		/**
		 * Mensagem base do email para os vários tipos de notificação.
		 * 
		 * Pela plataforma ForRisco
		 * 
		 * @param msg
		 *            Mensagem da notificação.
		 * @return Mensagem do e-mail para os vários tipos de notificação.
		 */
		private String mountForriscoNotificationEmail(String msg, String url) {
			return ("<meta charset='utf-8'>" + "<div>" + "<div>"
					+ "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: " + url
					+ "</p>" + "<center style='margin-top: 50px'>"
					+ "<img src='" + getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
					+ "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
					+ "Plataforma Aberta para Gestão e Acompanhamento de Riscos <br>Advindos dos Processos Desenvolvidos pelas Instituições"
					+ "</h3>"
					+ "<center style='margin: 25px;width: 600px;height: 350px;border-top: 4px solid #0383D9;border-right: 1px solid #CCC;"
					+ "border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
					+ "<h1 style='color: #0A4068;font-family: sans-serif;'>Notifica&ccedil;&atilde;o</h1>"
					+ "<p style='margin-bottom: 80px;margin-top: 90px;font-family: sans-serif;color: #9C9C9C;width: 90%;'>"
					+ msg + "</p>"
					+ "<p><a style='text-decoration: none;background-color: #7d7acc;color: #FFF;padding-top: 8px;padding-bottom: 8px;"
					+ "padding-left: 20px;padding-right: 20px;border-radius: 7px;font-family: sans-serif;' href='" + url
					+ "'>" + "Acesse o ForRisco" + "</a></p>" + "</center>"
					+ "<center style='margin-top: 30px;font-family: sans-serif; font-size: 12px;'>"
					+ "<p style='color: #1C486D;font-weight: 600;'>ForRisco - Todos os direitos reservados</p>"
					+ "<a style='color: #9C9C9C;text-decoration: none;' href='www.forpdi.org'>www.forrisco.org/</a>"
					+ "</center>" + "</center>" + "</div>" + "</div>" + "</div>");
		}

		/**
		 * Email de convite do usuário.
		 * 
		 * @param user
		 *            Nome do usuário convidado.
		 * @param url
		 *            Url para usuário completar cadastro no sistema.
		 * @return String Mensagem do e-mail de convite do usuário.
		 */
		private String mountInviteUserEmail(String user, String url) {
			return ("<meta charset='utf-8'>" + "<div>" + "<div>"
					+ "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: " + url
					+ "</p>" + "<center style='margin-top: 50px'>"
					+ "<img src='" + getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
					+ "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
					+ "Plataforma Aberta para Gest&atilde;o e Acompanhamento do <br>Plano de Desenvolvimento Institucional e Riscos"
					+ "</h3>"
					+ "<center style='margin: 25px;width: 600px;border-top: 4px solid #0383D9;border-right: 1px solid"
					+ " #CCC;border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
					+ "<h1 style='color: #0A4068;font-family: sans-serif;'>" + "Completar Cadastro" + "</h1>"
					+ "<p style='margin-top: 20px;font-family: sans-serif;color: #9C9C9C;margin-bottom: 30px;width: 90%;'>"
					+ "Ol&aacute; " + user
					+ ", <br>voc&ecirc; foi convidado para acessar a Plataforma For<br> Para completar o cadastro "
					+ "clique no bot&atilde;o abaixo:" + "</p>"
					+ "<p><a style='text-decoration: none;background-color: #1C486D;color: #FFF;padding-top: 8px;padding-bottom: 8px;padding-left: 20px;"
					+ "padding-right: 20px;border-radius: 7px;font-family: sans-serif;' href='" + url + "'>"
					+ "Complete seu cadastro" + "</a></p>"
					+ "<p style='font-family: sans-serif;color: #9C9C9C;margin-top: 50px'>"
					+ "Ou cole em seu navegador de internet o link:" + "</p>"
					+ "<a style='font-family: sans-serif;color: #1C486D;font-size: 12px;word-wrap: break-word;'>" + url
					+ "</a>" + "<p style='font-family: sans-serif;color: #9C9C9C;'>"
					+ "Atenciosamente<br> Equipe da Plataforma For" + "</p>" + "</center>"
					+ "<center style='margin-top: 30px;font-family: sans-serif; font-size: 12px;'>"
					//+ "<p style='color: #1C486D;font-weight: 600;'>" + "ForPDI - Todos os direitos reservados" + "</p>"
					//+ "<a style='color: #9C9C9C;text-decoration: none;' href='www.forpdi.org'>" + "www.forpdi.org"
					+ "</a>" + "</center>" + "</center>" + "</div>" + "</div>" + "</div>");
		}

		/**
		 * Mensagem de e-mail de notificação do tipo Bem-Vindo.
		 * 
		 * @return String Mensagem do e-mail de notificação do tipo Bem-Vindo.
		 */
		private String mountWelcomeEmail(String domainBaseUrl) {
			return ("<meta charset='utf-8'>"
					+ "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: "
					+ domainBaseUrl + "</p>" + "<center style='margin-top: 50px'>"
					+ "<img src='" + getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
					+ "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
					+ "Plataforma Aberta para Gest&atilde;o e Acompanhamento do <br>Plano de Desenvolvimento Institucional e Riscos"
					+ "</h3>"
					+ "<center style='margin: 25px;width: 600px;height: 230px;border-top: 4px solid #0383D9;border-right: 1px solid #CCC;"
					+ "border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
					+ "<h1 style='color: #0A4068;font-family: sans-serif;'>Seja bem-vindo(a) a Plataforma FOR</h1>"
					+ "<p style='margin-top: 20px;font-family: sans-serif;color: #9C9C9C;margin-bottom: 30px;font-size: 14px'>"
					+ "A Plataforma For &eacute; aberta para gest&atilde;o e acompanhamento de desenvolvimento<br>institucional (FORPDI) e gerenciamento de riscos (ForRisco) de Universidades Federais<br>e outras institui&ccedil;&otilde;es p&uacute;blicas."
					+ "</p>"
					+ "<p><a style='text-decoration: none;background-color: #1C486D;color: #FFF;padding-top: 8px;padding-bottom: 8px;padding-left: 20px;padding-right: 20px;border-radius: 7px;font-family: sans-serif;' href='"
					+ domainBaseUrl + "'>" + "Acesse a Plataforma FOR" + "</a></p>" + "</center>"
					+ "</center>" + "</div>");
		}
		
		/**
		 * Mensagem de notificação de e-mail de recuperação de senha.
		 * 
		 * @param user
		 *            Usuário que solicitou a recuperação da senha.
		 * @param url
		 *            Url para o usuário recuperar sua senha.
		 * @return
		 */
		private String mountRecoverEmail(String user, String url) {
			return ("<meta charset='utf-8'>" + "<div>" + "<div>"
					+ "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: " + url
					+ "</p>" + "<center style='margin-top: 50px'>"
					+ "<img src='" + getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
					+ "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
					+ "Plataforma Aberta para Gest&atilde;o e Acompanhamento do <br>Plano de Desenvolvimento Institucional e Riscos"
					+ "</h3>"
					+ "<center style='margin: 25px;width: 600px;border-top: 4px solid #0383D9;border-right: 1px solid #CCC;"
					+ "border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
					+ "<h2 style='color: #0A4068;font-family: sans-serif;'>Recupera&ccedil;&atilde;o de Senha de Acesso à Plataforma For</h2>"
					+ "<p style='margin-top: 60px;font-family: sans-serif;color: #9C9C9C;margin-bottom: 80px;font-size: 14px;width: 90%;'>"
					+ "Ol&aacute; " + user
					+ ",<br> voc&ecirc; solicitou a recuperação de seus dados de acesso à Plataforma For.<br> Para alterar sua senha acesse:<br> <a href='"
					+ url + "'>" + url + "</a> <br><br><br>Atenciosamente, Equipe da Plataforma For." + "</p>"
					+ "<p><a style='background-color: #1C486D;color: #FFF;padding-top: 8px;padding-bottom: 8px;padding-left: 20px;padding-right: 20px;border-radius: 7px;font-family: sans-serif;text-decoration: none' href='"
					+ url + "'>" + "Acesse a Plataforma For" + "</a></p>" + "</center>"
					+ "<center style='margin-top: 30px;font-family: sans-serif; font-size: 12px;'>"
					//+ "<p style='color: #1C486D;font-weight: 600;'>ForPDI - Todos os direitos reservados</p>"
					//+ "<a style='color: #9C9C9C;text-decoration: none;' href='www.forpdi.org'>www.forpdi.org</a>"
					+ "</center>" + "</center>" + "</div>" + "</div>" + "</div>");
		}

		/**
		 * 
		 * @param msg
		 *            Mensagem da notificação.
		 * @return Mensagem do e-mail para a notificação de nível de acesso alterado
		 */
		private String mountAccessLevelChanged(String msg, String url) {
			return ("<meta charset='utf-8'>" + "<div>" + "<div>"
					+ "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: " + url
					+ "</p>" + "<center style='margin-top: 50px'>"
					+ "<img src='" + getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
					+ "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
					+ "Plataforma Aberta para Gest&atilde;o e Acompanhamento do <br>Plano de Desenvolvimento Institucional e Riscos"
					+ "</h3>"
					+ "<center style='margin: 25px;width: 600px;height: 350px;border-top: 4px solid #0383D9;border-right: 1px solid #CCC;"
					+ "border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
					+ "<h1 style='color: #0A4068;font-family: sans-serif;'>Notifica&ccedil;&atilde;o</h1>"
					+ "<p style='margin-bottom: 80px;margin-top: 90px;font-family: sans-serif;color: #9C9C9C;width: 90%;'>"
					+ msg + "</p>"
					+ "<p><a style='text-decoration: none;background-color: #1C486D;color: #FFF;padding-top: 8px;padding-bottom: 8px;"
					+ "padding-left: 20px;padding-right: 20px;border-radius: 7px;font-family: sans-serif;' href='" + url
					+ "'>" + "Acesse o ForPDI" + "</a></p>" + "</center>"
					+ "<center style='margin-top: 30px;font-family: sans-serif; font-size: 12px;'>"
					+ "</center>" + "</center>" + "</div>" + "</div>" + "</div>");
		}
		
		private String getEmailLogoUrl() {
			return SystemConfigs.getEmailLogoUrl();
		}

	}
}
