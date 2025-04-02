package org.forpdi.core.jobs;

import java.util.LinkedList;
import java.util.Queue;

import org.forpdi.core.notification.NotificationEmail;
import org.forpdi.system.EmailUtilsPlugin;
import org.forpdi.system.jobsetup.JobsSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tarefa que é executada a cada minuto, para verificar se existe algum e-mail
 * pendente para ser enviado.
 * 
 * @author Pedro Mutter
 *
 * 
 */
@Configuration
public class EmailSenderTask {

	private static final Logger LOG = LoggerFactory.getLogger(EmailSenderTask.class);
	
	@Autowired
	private EmailUtilsPlugin emailPlugin;
	
	private Queue<NotificationEmail> queue;

	public EmailSenderTask() {
		this.queue = new LinkedList<>();
	}

	/**
	 * Retorna a fila de e-mails pendentes à ser enviados.
	 * 
	 * @return queue, fila de e-mails.
	 */
	public Queue<NotificationEmail> getQueue() {
		return queue;
	}

	/**
	 * Adiciona um e-mail à fila, para ser enviado na próxima vez que a tarefa
	 * for executada.
	 * 
	 * @param email
	 */
	public synchronized void add(NotificationEmail email) {
		queue.add(email);
	}

	/**
	 * Método que é executado a cada minuto, verificando se existe e-mail na
	 * fila e os enviando.
	 */
	@Scheduled(fixedRate = JobsSetup.EMAIL_SENDER_FIXED_RATE)
	public void execute() {
		if (emailPlugin.emailSenderIsEnabled()) {
			if (!this.queue.isEmpty()) {
				LOG.info("Executando envio de {} emails...", this.queue.size());
			}
			while (!this.queue.isEmpty()) {
				NotificationEmail email = this.queue.poll();
				try {
					emailPlugin.sendHtmlEmail(email.getEmail(), email.getName(), email.getSubject(), email.getBody(), email.getAttach());
				} catch (Throwable e) {
					LOG.error("Falha ao enviar e-mail: " + email.getNotificationType(), e);
				}
			}
		}
	}

}
