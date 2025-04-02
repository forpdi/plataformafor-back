package org.forpdi.core.notification;

public class NotificationEmail {
	private String email;
	private String name;
	private String subject;
	private String body;
	private String attach;
	private NotificationType notificationType;
	
	public NotificationEmail(String email, String name, String subject, String body, String attach, NotificationType notificationType) {
		super();
		this.email = email;
		this.name = name;
		this.subject = subject;
		this.body = body;
		this.attach= attach;
		this.notificationType = notificationType;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getAttach() {
		return attach;
	}
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public NotificationType getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}
}
