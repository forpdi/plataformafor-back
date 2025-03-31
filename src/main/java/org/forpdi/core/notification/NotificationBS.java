package org.forpdi.core.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.exception.InvalidTenantAccess;
import org.forpdi.core.jobs.EmailSenderTask;
import org.forpdi.core.properties.SystemConfigs;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.user.authz.UserPermission;
import org.forpdi.core.user.authz.permission.ManageUsersPermission;
import org.forpdi.core.user.authz.permission.ViewUsersPermission;
import org.forpdi.core.utils.NotificationUtil;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.planning.permissions.ManageDocumentPermission;
import org.forpdi.planning.permissions.ManagePlanMacroPermission;
import org.forpdi.planning.permissions.ManagePlanPermission;
import org.forpdi.planning.permissions.UpdateGoalPermission;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.EmailUtilsPlugin.EmailBuilder;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Rodrigo de Freitas Santos
 */
@Service
public class NotificationBS extends HibernateBusiness {

	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private UserSession userSession;
	@Autowired
	private EmailSenderTask emailTask;
	@Autowired
	@Lazy
	private UserBS userBS;
	
	public Notification retrieveById(Long id) {
		return dao.exists(id, Notification.class);
	}

	/**
	 * Salvar todas as notificações do sistema.
	 * 
	 * @param type
	 *            Tipo da notificação.
	 * @param text
	 *            Texto da notificação.
	 * @param aux
	 *            Nome do Plano de metas onde ocorreu a notificação.
	 * @param userId
	 *            Id do usuário para receber a notificação.
	 */
	public void sendNotification(NotificationType type, String text, String aux, Long userId, String url) {
		PaginatedList<User> users = new PaginatedList<User>();
		users.setList(new ArrayList<User>());
		if (userId != null) {
			User user = this.userBS.existsByUser(userId);
			users.getList().add(user);
		} else {
			users = this.userBS.listUsersByCompany();
		}
		url = url.replace("//#", "/#");
		Notification notification = new Notification();
		notification.setPicture(type.getImageUrl());
		notification.setCompany(domain.get().getCompany());
		notification.setType(type.getValue());
		notification.setOnlyEmail(type.isOnlyEmail());
		notification.setUrl(url);
		NotificationUtil.setDescriptionForNotification(notification, type, text, aux);
		for (User user : users.getList()) {
			Notification not = new Notification();
			not.setPicture(notification.getPicture());
			not.setCompany(notification.getCompany());
			not.setType(notification.getType());
			not.setOnlyEmail(notification.isOnlyEmail());
			not.setDescription(notification.getDescription());
			not.setUrl(notification.getUrl());
			not.setUser(user);
			this.persist(not);
		}
	}

	/**
	 * Enviar notificação do sistema para o email do usuário.
	 * 
	 * @param type
	 *            Tipo da notificação.
	 * @param text
	 *            Texto da notificação.
	 * @param aux
	 *            Nome do Plano de metas onde ocorreu a notificação.
	 * @param user
	 *            Id do usuário para receber a notificação.
	 * @throws EmailException
	 */
	public void sendNotificationEmail(NotificationType type, String text, String aux, User user, String url)
			throws EmailException {
		if (url == null) {
			url = domain.get().getBaseUrl();
		}
		url = url.replace("//#", "/#");
		EmailBuilder builder;
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setCompany(domain.get().getCompany());
		notification.setCreation(new Date());
		notification.setOnlyEmail(true);
		notification.setPicture(type.getImageUrl());
		notification.setVizualized(false);
		notification.setType(type.getValue());
		notification.setUrl(url);
		NotificationUtil.setDescriptionForNotification(notification, type, text, aux);
		if (type == NotificationType.WELCOME) {
			builder = new EmailBuilder(NotificationType.WELCOME, domain.get().getBaseUrl());
		} else if (type == NotificationType.INVITE_USER) {
			builder = new EmailBuilder(NotificationType.INVITE_USER, user.getName(), text);
		} else if (type == NotificationType.RECOVER_PASSWORD) {
			builder = new EmailBuilder(NotificationType.RECOVER_PASSWORD, user.getName(), text);
		} else {
			builder = new EmailBuilder(type, notification.getDescription(), url);
		}
		this.persist(notification);
		this.emailTask.add(
				new NotificationEmail(user.getEmail(), user.getName(), builder.getSubject(), builder.getBody(), null, type));
	}

	/**
	 * Enviar notificação do sistema para o email do usuário com arquivo anexado.
	 * 
	 * @param type
	 *            Tipo da notificação.
	 * @param text
	 *            Texto da notificação.
	 * @param aux
	 *            Nome do Plano de metas onde ocorreu a notificação.
	 * @param user
	 *            Id do usuário para receber a notificação.
	 * @throws EmailException
	 */
	public void sendAttachedNotificationEmail(NotificationType type, String text, String aux, User user, String url,
			Archive attachment) throws EmailException {
		if (url == null) {
			url = domain.get().getBaseUrl();
		}
		url = url.replace("//#", "/#");
		EmailBuilder builder;
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setCompany(domain.get().getCompany());
		notification.setCreation(new Date());
		notification.setOnlyEmail(true);
		notification.setPicture(type.getImageUrl());
		notification.setVizualized(false);
		notification.setType(type.getValue());
		notification.setUrl(url);
		NotificationUtil.setDescriptionForNotification(notification, type, text, aux);
		if (type == NotificationType.WELCOME) {
			builder = new EmailBuilder(NotificationType.WELCOME, domain.get().getBaseUrl());
		} else if (type == NotificationType.INVITE_USER) {
			builder = new EmailBuilder(NotificationType.INVITE_USER, user.getName(), text);
		} else if (type == NotificationType.RECOVER_PASSWORD) {
			builder = new EmailBuilder(NotificationType.RECOVER_PASSWORD, user.getName(), text);
		} else {
			builder = new EmailBuilder(type, notification.getDescription(), url);
		}
		this.persist(notification);
		this.emailTask.add(new NotificationEmail(user.getEmail(), user.getName(), builder.getSubject(),
				builder.getBody(), attachment.getName(), type));
	}


	/**
	 * Listar as permissões do usuário em uma instituição.
	 * 
	 * @param user
	 *            Usuário para listar as notifições.
	 * @param limit
	 *            Número maximo de notificações listadas.
	 * @param page
	 *            Número da pagina para listar as notificações.
	 * @return
	 */
	public PaginatedList<Notification> listNotificationByUserCompany(User user, Boolean visualized, DefaultParams params) {
		if (domain == null || domain.get().getCompany() == null) {
			return new PaginatedList<>(new ArrayList<>(0), 0L);
		}
		PaginatedList<Notification> results = new PaginatedList<Notification>();
		Criteria criteria = this.dao.newCriteria(Notification.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		criteria.createAlias("company", "company", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
        criteria.add(Restrictions.eq("onlyEmail", false));
        
		if (visualized != null) {
	        criteria.add(Restrictions.eq("vizualized", visualized));
		}
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("vizualized"));
			criteria.addOrder(Order.desc("creation"));
		}
		
		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((int) ((page - 1) * pageSize));
		criteria.setMaxResults(pageSize);
		List<Notification> notificationUser = this.dao.findByCriteria(criteria, Notification.class);

		Criteria counting = this.dao.newCriteria(Notification.class);
		counting.setProjection(Projections.countDistinct("id"));
		counting.add(Restrictions.eq("user", user));
		counting.add(Restrictions.eq("company", domain.get().getCompany()));
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("onlyEmail", false));

		results.setList(notificationUser);
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}
	
	public PaginatedList<Notification> listAllNotificationByUserCompany(User user) {
		if (domain == null || domain.get().getCompany() == null) {
			return new PaginatedList<>(new ArrayList<>(0), 0L);
		}
		PaginatedList<Notification> results = new PaginatedList<Notification>();
		Criteria criteria = this.dao.newCriteria(Notification.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("onlyEmail", false));
		criteria.addOrder(Order.desc("creation"));
		List<Notification> notificationUser = this.dao.findByCriteria(criteria, Notification.class);

		Criteria counting = this.dao.newCriteria(Notification.class);
		counting.setProjection(Projections.countDistinct("id"));
		counting.add(Restrictions.eq("user", user));
		counting.add(Restrictions.eq("company", domain.get().getCompany()));
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("onlyEmail", false));

		results.setList(notificationUser);
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	/**
	 * Retornar o nome do nível de acesso do usuário no sistema.
	 * 
	 * @param companyUser
	 *            Nível de acesso do usuário.
	 * @return String Nome do nível de acesso do usuário.
	 */
	public String getAccessLevelText(CompanyUser companyUser) {
		String text = "";
		if (companyUser.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel()) {
			text = "Administrador do Sistema";
		} else if (companyUser.getAccessLevel() == AccessLevels.COMPANY_ADMIN.getLevel()) {
			text = "Administrador da Instituição: " + companyUser.getCompany().getName();
		} else if (companyUser.getAccessLevel() == AccessLevels.MANAGER.getLevel()) {
			text = "Gerente da Instituição: " + companyUser.getCompany().getName();
		} else if (companyUser.getAccessLevel() == AccessLevels.COLABORATOR.getLevel()) {
			text = "Colaborador da Instituição: " + companyUser.getCompany().getName();
		} else if (companyUser.getAccessLevel() == AccessLevels.AUDITOR.getLevel()) {
			text = "Auditor da Instituição: " + companyUser.getCompany().getName();
		} else if (companyUser.getAccessLevel() == AccessLevels.AUTHENTICATED.getLevel()) {
			text = "Usuário da Instituição: " + companyUser.getCompany().getName();
		}
		return text;
	}

	/**
	 * Retornar o nome da permissão do usuário no sistema.
	 * 
	 * @param permission
	 *            Permissão do usuário.
	 * @return String Nome da permissão do usuário.
	 */
	public String getPermissionText(UserPermission permission) {
		String text = "";
		if (permission.getPermission().equals(ManageUsersPermission.class.getCanonicalName())) {
			text = "Gerenciar Usuários na Instituição: " + permission.getCompany().getName();
		} else if (permission.getPermission().equals(ViewUsersPermission.class.getCanonicalName())) {
			text = "Visualizar Usuários na Instituição: " + permission.getCompany().getName();
		} else if (permission.getPermission().equals(ManagePlanMacroPermission.class.getCanonicalName())) {
			text = "Gerenciar Planos na Instituição: " + permission.getCompany().getName();
		} else if (permission.getPermission().equals(ManageDocumentPermission.class.getCanonicalName())) {
			text = "Gerenciar Documentos na Instituição: " + permission.getCompany().getName();
		} else if (permission.getPermission().equals(ManagePlanPermission.class.getCanonicalName())) {
			text = "Gerenciar Planos de Metas na Instituição: " + permission.getCompany().getName();
		} else if (permission.getPermission().equals(UpdateGoalPermission.class.getCanonicalName())) {
			text = "Atualizar Metas na Instituição: " + permission.getCompany().getName();
		}
		return text;
	}

	/**
	 * Enviar uma mensagem.
	 * 
	 * @param messageHistory
	 *            Mensagem que será enviada.
	 * 
	 * @return messageHistory Mensagem que foi salva.
	 */
	public MessageHistory sendMessage(String subject, String message, Long userId) {
		if (message.length() > 255 * 3) {
			throw new IllegalStateException("Mensagem muito longa");
		}
		User user = this.userBS.existsByUser(userId);
		CompanyUser companyUser = userBS.retrieveCompanyUser(user, domain.getCompany());
		if (companyUser == null) {
			throw new InvalidTenantAccess("Não é possível enviar mensagens para usuários de outras instituições");
		}

		MessageHistory messageHistory = new MessageHistory();
		messageHistory.setSubject(SanitizeUtil.sanitize(subject));
		List<String> allowedTags = Arrays.asList("p", "strong", "em", "u", "br");
		messageHistory.setMessage(SanitizeUtil.sanitize(message, allowedTags));
		messageHistory.setUserSender(this.userSession.getUser());
		messageHistory.setUserReceiver(user);
		messageHistory.setCompany(domain.get().getCompany());

		Notification notification = createUserMessageNotification(messageHistory);
		messageHistory.setNotification(notification);

		this.persist(messageHistory);
		
		sendUserMessageEmail(messageHistory);
		
		return messageHistory;
	}

	private void sendUserMessageEmail(MessageHistory messageHistory) {
		String url = domain.get().getBaseUrl() + "#/users/profile/notifications";
    String msg = "<meta charset='utf-8'>" + "<div>" + "<div>"
            + "<div><p style='margin-bottom: 30px'>Se não for possível ver esse email, acesse o link: " + url
            + "<center style='margin-top: 50px'>"
            + "<img src='" + SystemConfigs.getEmailLogoUrl() + "' alt='PlataformaFor Logo' width='200'>"
            + "<h3 style='font-family:sans-serif;font-size:1rem;color:#293a59;margin-top: 25px;'>"
            + "Plataforma Aberta para Gest&atilde;o e Acompanhamento do <br>Plano de Desenvolvimento Institucional e Riscos"
            + "</h3>"
            + "<center style='margin: 25px;width: 600px;height: 350px;border-top: 4px solid #0383D9;border-right: 1px solid #CCC;"
            + "border-bottom: 1px solid #CCC;border-left: 1px solid #CCC;'>"
            + "<h1 style='color: #0A4068;font-family: sans-serif;'>Notifica&ccedil;&atilde;o</h1>"
            + "<p style='margin-top: 90px;font-family: sans-serif;color: #9C9C9C;width: 90%;'>"
            + "Você recebeu uma mensagem de " + this.userSession.getUser().getName()+ ":" + messageHistory.getMessage() + "</p>"
            + "<p><a style='text-decoration: none;background-color: #1C486D;color: #FFF;padding-top: 8px;padding-bottom: 8px;"
						+ "padding-left: 20px;padding-right: 20px;border-radius: 7px;font-family: sans-serif; margin-bottom:80px;' href='" + url
						+ "'>" + "Acesse a Mensagem" + "</a></p>" + "</center>"
						+ "<center style='margin-top: 30px;font-family: sans-serif; font-size: 12px;'>"
						+ "</center>" + "</center>" + "</div>" + "</div>" + "</div>";

    NotificationEmail ne = new NotificationEmail(messageHistory.getUserReceiver().getEmail(),
            messageHistory.getUserReceiver().getName(), messageHistory.getSubject(), msg,
            null, NotificationType.SEND_MESSAGE);
    
    this.emailTask.add(ne);
}

	private Notification createUserMessageNotification(MessageHistory messageHistory) {
		Notification notification = new Notification();
		notification.setUser(messageHistory.getUserReceiver());
		notification.setCompany(domain.get().getCompany());
		notification.setCreation(new Date());
		notification.setOnlyEmail(false);
		notification.setVizualized(false);
		notification.setType(NotificationType.SEND_MESSAGE.getValue());
		notification.setResponded(false);
		notification.setDescription("<b>Mensagem de " + messageHistory.getUserSender().getName() + ".</b> "
				+ messageHistory.getSubject() + ": " + messageHistory.getMessage());
		
		return this.dao.merge(notification);
	}

	/**
	 * Listar as mensagens enviadas para um usuário
	 * 
	 * @param user
	 *            Usuário que recebeu as mensagens.
	 * 
	 */
	public PaginatedList<MessageHistory> listMessageHistory(User user, DefaultParams params) {

		PaginatedList<MessageHistory> results = new PaginatedList<MessageHistory>();

		Criteria criteria = this.dao.newCriteria(MessageHistory.class);
		criteria.add(Restrictions.eq("userSender", this.userSession.getUser()));
		criteria.add(Restrictions.eq("userReceiver", user));
		criteria.add(Restrictions.eq("company", domain.get().getCompany()));
		criteria.add(Restrictions.eq("deleted", false));

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.desc("creation"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((int) ((page - 1) * pageSize));
		criteria.setMaxResults(pageSize);

		List<MessageHistory> messageHistoryList = this.dao.findByCriteria(criteria, MessageHistory.class);

		Criteria counting = this.dao.newCriteria(MessageHistory.class);
		counting.setProjection(Projections.countDistinct("id"));
		counting.add(Restrictions.eq("userSender", this.userSession.getUser()));
		counting.add(Restrictions.eq("userReceiver", user));
		counting.add(Restrictions.eq("company", domain.get().getCompany()));
		counting.add(Restrictions.eq("deleted", false));

		results.setList(messageHistoryList);
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	public MessageHistory getMessageByNotificationId(Long notificationId) {
		Criteria criteria = this.dao.newCriteria(MessageHistory.class)
				.add(Restrictions.eq("notification.id", notificationId));

		return (MessageHistory) criteria.uniqueResult();
	}

}
