package org.forpdi.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class GeneralUtils {

	public static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT,
		new Locale("pt", "BR"));
	public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.DEFAULT,
		new Locale("pt", "BR"));
	

	static {
		DATE_FORMAT.setLenient(false);
		DATETIME_FORMAT.setLenient(false);
	}

	private GeneralUtils() {
	}

	/**
	 * Retorna uma data em GMT.
	 * 
	 * @param milliSeconds
	 * @return
	 */
	public static String getGMTTimeString(long milliSeconds) {
		SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
		return sdf.format(new Date(milliSeconds));
	}

	public static Date getGMTTimeDate(String str) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
		return sdf.parse(str);
	}

	public static void streamingPipe(InputStream is, OutputStream os) throws IOException {
		byte[] bytes = new byte[2048];
		int readlen = -1;
		while ((readlen = is.read(bytes)) != -1) {
			os.write(bytes, 0, readlen);
			os.flush(); // Let us flush after bulk write
		}
	}

	/**
	 * Verifica se uma string � uma string vazia.
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isEmpty(String string) {
		return (string == null || string.isEmpty());
	}

	/**
	 * Verifica se a lista � uma lista vazia.
	 * 
	 * @param list
	 * @return
	 */
	public static boolean isEmpty(List<?> list) {
		return (list == null || list.isEmpty());
	}

	public static boolean isInvalidId(Long id) {
		return ((id == null) || (id <= 0L));
	}

	public static boolean isInvalid(SimpleIdentifiable entity) {
		return ((entity == null) || (entity.getId() == null) || (entity.getId() <= 0L));
	}

	public static Throwable getRootCause(Throwable ex) {
		if (ex == null)
			return null;
		while (ex.getCause() != null) {
			ex = ex.getCause();
		}
		return ex;
	}

	public static Date parseDate(String dateTime) {
		return parseDate(dateTime, DATE_FORMAT);
	}
	
	public static Date parseDate(String dateTime, DateFormat format) {
		Date dt;
		try {
			dt = format.parse(dateTime);
		} catch (ParseException ex) {
			throw new RuntimeException("Data inválida");
		}
		return dt;
	}

	
	public static String parseDateToString(Date dateTime) {
		String dt = "";

		if(dateTime==null) {
			return dt;
		}

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

		LocalDateTime localDateTime = LocalDateTime.parse(dateTime.toString(), inputFormatter);

		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		dt = localDateTime.format(outputFormatter);
		return dt;
	}

	public static Date parseDateTime(String dateTime) {
		Date dt;
		try {
			dt = DATETIME_FORMAT.parse(dateTime);
		} catch (ParseException ex) {
			throw new RuntimeException("Data inválida");
		}
		return dt;
	}

	/**
	 * Transforma um string que representa um Locale. A lingua padr�o � pt
	 * universal.
	 * 
	 * @param locale
	 *            String que representa o locale, Ex: pt_BR
	 * @return
	 */
	public static Locale parseLocaleString(String locale) {
		String lang = GeneralUtils.isEmpty(locale) ? "pt" : locale.substring(0, 2);
		String country = GeneralUtils.isEmpty(locale) ? "" : locale.substring(3, 5);
		return new Locale(lang, country);
	}

	public static String formatCpf(String userName) {
		if (GeneralUtils.isEmpty(userName))
			return userName;
		if (userName.matches("[0-9.]{11}")) {
			String userNamePart1 = userName.substring(0, 3);
			userNamePart1 += '.';
			String userNamePart2 = userName.substring(3, 6);
			userNamePart2 += '.';
			String userNamePart3 = userName.substring(6, 9);
			userNamePart3 += '-';
			String userNamePart4 = userName.substring(9, 11);
			String newUserName = userNamePart1 + userNamePart2 + userNamePart3 + userNamePart4;
			return newUserName;
		}
		return userName;
	}

	public static boolean validateCpf(String cpf) {
		if (GeneralUtils.isEmpty(cpf)) {
			return false;
		}
		cpf = cpf.replace(".", "");
		cpf = cpf.replace("-", "");
		if (cpf.length() != 11 || cpf.equals("00000000000") || cpf.equals("11111111111") || cpf.equals("22222222222")
				|| cpf.equals("33333333333") || cpf.equals("44444444444") || cpf.equals("55555555555")
				|| cpf.equals("66666666666") || cpf.equals("77777777777") || cpf.equals("88888888888")
				|| cpf.equals("99999999999"))
			return false;
		int i = 0, add = 0, rev = 0;
		for (i = 0; i < 9; i++)
			add += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
		rev = 11 - (add % 11);
		if (rev == 10 || rev == 11)
			rev = 0;
		if (rev != Character.getNumericValue(cpf.charAt(9)))
			return false;
		add = 0;
		for (i = 0; i < 10; i++)
			add += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
		rev = 11 - (add % 11);
		if (rev == 10 || rev == 11)
			rev = 0;
		if (rev != Character.getNumericValue(cpf.charAt(10)))
			return false;
		return true;
	}
}
