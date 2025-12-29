package org.forpdi.core.utils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.forpdi.core.common.GeneralUtils;

public class DateUtil {

	private static Map<Integer, String> MONTH_NAMES_ABB;
	public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	public static String getMonthNameAbbreviated(int monthValue) {
		if (MONTH_NAMES_ABB == null) {
			MONTH_NAMES_ABB = new HashMap<>();
			MONTH_NAMES_ABB.put(1, "jan");
			MONTH_NAMES_ABB.put(2, "fev");
			MONTH_NAMES_ABB.put(3, "mar");
			MONTH_NAMES_ABB.put(4, "abr");
			MONTH_NAMES_ABB.put(5, "mai");
			MONTH_NAMES_ABB.put(6, "jun");
			MONTH_NAMES_ABB.put(7, "jul");
			MONTH_NAMES_ABB.put(8, "ago");
			MONTH_NAMES_ABB.put(9, "set");
			MONTH_NAMES_ABB.put(10, "out");
			MONTH_NAMES_ABB.put(11, "nov");
			MONTH_NAMES_ABB.put(12, "dez");
		}
		return MONTH_NAMES_ABB.get(monthValue);
	}
	
	public static String dateToString(Date date) {
		return GeneralUtils.DATE_FORMAT.format(date);
	}

	public static String dateToStringYYYY(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date);
	}
	
    public static boolean isSameYear(Date date, Integer year) {
    	
    	if(date == null) {
    		return false;
    	}
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	
    	return (calendar.get(Calendar.YEAR) == year);
    }

	public static String formatSearchDate(String term) {
		String iTerm = new StringBuilder(term).reverse().toString();

		String[] parts = iTerm.split("/");
        String convertedTerm="", convertedAux="";
		int j=0;

		for (String part : parts) {
			convertedAux += new StringBuilder(part).reverse().toString();
		}

		for (int i=0; i<iTerm.length(); i++) {
			convertedTerm += ((iTerm.charAt(i)=='/')?('-'):(convertedAux.charAt(j)));
			if(iTerm.charAt(i) != '/') {j++;}
		}

		return convertedTerm;
	}
	
    public static boolean isAfterOrEqual(Date date1, Date date2) {
        return date1.after(date2) || date1.equals(date2);
    }
    
    public static boolean isBeforeOrEqual(Date date1, Date date2) {
        return date1.before(date2) || date1.equals(date2);
    }
    
	public static boolean isBetween(Date date, Date startDate, Date endDate) {
		return isAfterOrEqual(date, startDate) && isBeforeOrEqual(date, endDate);
    }

    public static String formatYearMonth(YearMonth yearMonth) {
    	return yearMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }
}
