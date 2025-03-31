package org.forpdi.core.notification;


import java.util.List;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.notification.dto.NotificationIdDto;
import org.forpdi.core.notification.dto.SendMessageDto;
import org.forpdi.core.notification.dto.VisualizeAllNotificationsDto;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Rafael S. Lima
 */
@RestController
public class NotificationController extends AbstractController {
	
	@Autowired
	private NotificationBS bs;
	@Autowired
	private UserBS userBS;
	

	/**
	 * Recuperação das notificações do usuário 
	 * @param limit
	 * 		limite de notificações por página
	 * @param page
	 * 		número da página a ser listada
     * @param visualized
     *      busca limitada para o display na dropdown
	 * @return list
	 * 		lista de notificações
	 * 
	 */
	@GetMapping("/api/notification/notifications")
	public ResponseEntity<?> listNotifications(
			@RequestParam(required = false) Boolean visualized,
			@ModelAttribute DefaultParams params) {

		try {
			User existent = this.userSession.getUser();
			PaginatedList<Notification> list = this.bs.listNotificationByUserCompany(existent, visualized, params);

			return this.success(list);
		} catch (Throwable ex){
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	/**
	 * Enviar uma mensagem para um usuário
	 * 
	 * @param subject
	 *            Assunto da mensagem.
	 * @param message
	 * 			  Corpo da mensagem.
	 * @param userId
	 * 			  Id do usuário que vai receber a mensagem.
	 *            
	 */
	@PostMapping("/api/structure/sendmessage")
	public ResponseEntity<?> sendMessage(@RequestBody SendMessageDto dto) {
		try {
			MessageHistory messageHistory = this.bs.sendMessage(dto.subject(), dto.message(), dto.userId());
			return this.success(messageHistory);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	/**
	 * Atualiza status da notificacao de mensagem para respondida
	 * 
	 * @param notificationId
	 *            Id da notificacao.
	 *            
	 */
	@PostMapping("/api/structure/setresponded")
	public ResponseEntity<?> setNotificationResponded(@RequestBody NotificationIdDto dto) {
		try {
			Notification notification = this.bs.retrieveById(dto.notificationId());
			if (!notification.getUser().getId().equals(userSession.getUser().getId())) {
				return this.fail(ErrorMessages.UNAUTHORIZED_ACCESS_ERROR);
			}
			
			notification.setVizualized(true);
			notification.setResponded(true);
			
			this.bs.persist(notification);
			return this.success(notification);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	/**
	 * Recuperação de mensagem pelo id da notificacao gerada 
	 * @param notificationId
	 * 		Id da notificacao relacionada
	 */
	@GetMapping("/api/structure/get-message-by-notification-id/{notificationId}")
	public ResponseEntity<?> getMessage(@PathVariable Long notificationId) {
		try {
			Notification notification = this.bs.retrieveById(notificationId);
			if (!notification.getUser().getId().equals(userSession.getUser().getId())) {
				return this.fail(ErrorMessages.UNAUTHORIZED_ACCESS_ERROR);
			}

            notification.setVizualized(true);
            this.bs.persist(notification);

			MessageHistory messageHistory = bs.getMessageByNotificationId(notificationId);
			return this.success(messageHistory);
		} catch (Throwable ex){
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Listar as mensagens enviadas para um usuário
	 * 
	 * @param userId
	 * 			  Id do usuário que recebeu as mensagens.
	 *            
	 */
	@GetMapping(BASEPATH + "/structure/listmessages")
	public ResponseEntity<?> listMessages(@RequestParam(required = false) Long userId,
				@ModelAttribute DefaultParams params) {

		try {
			User user = this.userBS.existsByUser(userId);
			PaginatedList<MessageHistory> messageHistoryList = new PaginatedList<MessageHistory>();

			messageHistoryList = this.bs.listMessageHistory(user, params);
			return this.success(messageHistoryList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
   /**
     * Apaga uma notificação do usuário 
     * 
     */
    @DeleteMapping("/api/notification/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        try {
            Notification notification = this.bs.retrieveById(notificationId);
            if (!notification.getUser().getId().equals(userSession.getUser().getId())) {
                return this.fail(ErrorMessages.UNAUTHORIZED_ACCESS_ERROR);
            }
            notification.setDeleted(true);
            this.bs.persist(notification);
            return this.success(notification);
        } catch (Throwable ex) {
            LOGGER.error("Unexpected runtime error", ex);
            return this.fail(ex.getMessage());
        }
    }
    
    /**
     * Marca lista de notificações como visualizada 
     * @param maxResults
     *      limite de notificações por página
     * @param page
     *      número da página a ser listada
     * 
     */
    @PostMapping("/api/notification/setlistvisualized")
    public ResponseEntity<?> visualizeAll(@RequestBody VisualizeAllNotificationsDto dto) {
        try {
        	int page = dto.page() == null ? 1 : dto.page();
        	int maxResults = dto.maxResults() == null ? 7 : dto.maxResults();
        	
            User user = this.userSession.getUser();
            DefaultParams params = DefaultParams.create(page, maxResults);
            
            PaginatedList<Notification> list = this.bs.listNotificationByUserCompany(user, false, params);

            for (Notification notification : list.getList()) {
                notification.setVizualized(true);
                this.bs.persist(notification);
            }

            return this.success();
        } catch (Throwable ex) {
            LOGGER.error("Unexpected runtime error", ex);
            return this.fail(ex.getMessage());
        }
    }

	/**
	 * Apaga as notificações do usuário 
	 * @return list
	 * 		lista de notificações
	 * 
	 */
	@GetMapping("/api/notification/deleteNotifications")
	public ResponseEntity<?> deleteNotifications() {
		try {
			
			User existent = this.bs.exists(this.userSession.getUser().getId(), User.class);
			PaginatedList<Notification> list = this.bs.listAllNotificationByUserCompany(existent);
			for (Notification notification : list.getList()) {
					notification.setDeleted(true);
					this.bs.persist(notification);
			}

			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}

}
