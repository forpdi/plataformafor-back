package org.forpdi.core.utils;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.forpdi.core.common.GeneralUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmailUtils {
	private static final Logger LOG = LoggerFactory.getLogger(EmailUtils.class);

	private static String SMTP_HOST = "localhost";
	private static Integer SMTP_PORT = 25;
	private static String SMTP_USER = null;
	private static String SMTP_PASSWORD = null;
	private static Boolean SMTP_SSL = false;

	private static String DEFAULT_FROM_EMAIL = null;
	private static String DEFAULT_FROM_NAME = null;

	public static void setSmtpSettings(
			String host,
			Integer port,
			String user,
			String password,
			Boolean ssl) {
		SMTP_HOST = host;
		SMTP_PORT = port;
		SMTP_USER = user;
		SMTP_PASSWORD = password;
		SMTP_SSL = ssl;
	}

	public static void setDefaultFrom(String email, String name) {
		DEFAULT_FROM_EMAIL = email;
		DEFAULT_FROM_NAME = name;
	}

	public static final Pattern ENDL_REGEX = Pattern.compile("\\v");
	public static final Pattern DUPE_WS_REGEX = Pattern.compile("\\h+");

	public static String sendEmail(
			String to,
			String subject,
			String body,
			boolean html) throws EmailException {
		return sendEmail(DEFAULT_FROM_EMAIL, DEFAULT_FROM_NAME, to, subject, body, html);
	}

	public static String sendEmail(
			String fromMail,
			String fromName,
			String to,
			String subject,
			String body,
			boolean html) throws EmailException {
		Email email;
		String msgId = null;

		subject = Util.htmlToText(subject);

		if (html) {
			HtmlEmail htmlEmail = EmailUtils.getHtmlEmail();

			List<String> allowedHtmlTags = List.of("p", "br", "strong", "em", "ul", "li", "a", "b", "i", "u", "ol", "span");

			body = SanitizeUtil.sanitize(body, allowedHtmlTags);

			email = htmlEmail;
		} else {
			SimpleEmail simpleEmail = EmailUtils.getSimpleEmail();

			body = Util.htmlToText(body);

			email = simpleEmail;
		}

		email.setFrom(fromMail, fromName);
		email.addTo(to);
		email.setSubject(subject);
		email.setMsg(body);

		msgId = email.send();
		LOG.info("Sent e-mail with ID: {}", msgId);

		return msgId;
	}

	public static SimpleEmail getSimpleEmail() {
		SimpleEmail email = new SimpleEmail();
		EmailUtils.configureConnection(email);
		return email;
	}

	public static HtmlEmail getHtmlEmail() {
		HtmlEmail email = new HtmlEmail();
		EmailUtils.configureConnection(email);
		return email;
	}

	private static void configureConnection(Email email) {
		try {
			email.setSmtpPort(SMTP_PORT);
			email.setHostName(SMTP_HOST);
			email.setCharset("UTF-8");
			if (!GeneralUtils.isEmpty(SMTP_USER)) {
				email.setAuthentication(
						SMTP_USER,
						SMTP_PASSWORD);
			}
			email.setSSLOnConnect(SMTP_SSL);
			email.setSSLCheckServerIdentity(true);
		} catch (Throwable ex) {
			LOG.error("Erro ao configurar o email.", ex);
			throw new RuntimeException("Error configuring smtp connection.", ex);
		}
	}

	public static String clearHtmlPitfalls(String pre) {
		return DUPE_WS_REGEX.matcher(
				ENDL_REGEX.matcher(pre).replaceAll("")).replaceAll(" ");
	}
}
