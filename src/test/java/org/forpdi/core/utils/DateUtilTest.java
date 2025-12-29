package org.forpdi.core.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;

class DateUtilTest {

	@Test
	@DisplayName("Testa o método getMonthNameAbbreviated com valores válidos")
	void test_getMonthNameAbbreviated_valid() {
		assertEquals("jan", DateUtil.getMonthNameAbbreviated(1), "O nome do mês abreviado deve ser 'jan'");
		assertEquals("fev", DateUtil.getMonthNameAbbreviated(2), "O nome do mês abreviado deve ser 'fev'");
		assertEquals("mar", DateUtil.getMonthNameAbbreviated(3), "O nome do mês abreviado deve ser 'mar'");
		assertEquals("dez", DateUtil.getMonthNameAbbreviated(12), "O nome do mês abreviado deve ser 'dez'");
	}

	@Test
	@DisplayName("Testa o método getMonthNameAbbreviated com valor inválido")
	void test_getMonthNameAbbreviated_invalid() {
		assertNull(DateUtil.getMonthNameAbbreviated(13), "Mês inválido deve retornar null");
		assertNull(DateUtil.getMonthNameAbbreviated(0), "Mês inválido deve retornar null");
	}

	@Test
	@DisplayName("Testa o método dateToString")
	void test_dateToString() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, Calendar.JANUARY, 1);
		Date date = calendar.getTime();

//		assertEquals("01/01/2023", DateUtil.dateToString(date), "A data deve ser formatada como 'dd/MM/yyyy'");
		assertEquals("1 de jan. de 2023", DateUtil.dateToString(date), "A data deve ser formatada como 'dd/MM/yyyy'");
	}

	@Test
	@DisplayName("Testa o método dateToStringYYYY")
	void test_dateToStringYYYY() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, Calendar.JANUARY, 1);
		Date date = calendar.getTime();

		assertEquals("01/01/2023", DateUtil.dateToStringYYYY(date), "A data deve ser formatada como 'dd/MM/yyyy'");
	}

	@Test
	@DisplayName("Testa o método isSameYear quando as datas estão no mesmo ano")
	void test_isSameYear_sameYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, Calendar.JANUARY, 1);
		Date date = calendar.getTime();

		assertTrue(DateUtil.isSameYear(date, 2023), "A data deve ser do mesmo ano");
	}

	@Test
	@DisplayName("Testa o método isSameYear quando as datas estão em anos diferentes")
	void test_isSameYear_differentYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2022, Calendar.DECEMBER, 31);
		Date date = calendar.getTime();

		assertFalse(DateUtil.isSameYear(date, 2023), "A data não deve ser do ano 2023");
	}

	@Test
	@DisplayName("Testa o método formatSearchDate")
	void test_formatSearchDate() {
//		assertEquals("21-02-2023", DateUtil.formatSearchDate("23/02/2021"), "A data formatada deve ser '21-02-2023'");
		assertEquals("2021-02-23", DateUtil.formatSearchDate("23/02/2021"), "A data formatada deve ser '2021-02-23'");
//		assertEquals("12-05-2022", DateUtil.formatSearchDate("22/05/2021"), "A data formatada deve ser '12-05-2022'");
		assertEquals("2021-05-22", DateUtil.formatSearchDate("22/05/2021"), "A data formatada deve ser '2021-05-22'");
	}

	@Test
	@DisplayName("Testa o método isAfterOrEqual quando a primeira data é depois ou igual à segunda")
	void test_isAfterOrEqual_after() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(2023, Calendar.JANUARY, 1);
		Date date1 = calendar1.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(2022, Calendar.DECEMBER, 31);
		Date date2 = calendar2.getTime();

		assertTrue(DateUtil.isAfterOrEqual(date1, date2), "A primeira data deve ser depois ou igual à segunda");
	}

	@Test
	@DisplayName("Testa o método isAfterOrEqual quando a primeira data é antes da segunda")
	void test_isAfterOrEqual_before() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(2021, Calendar.DECEMBER, 31);
		Date date1 = calendar1.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(2022, Calendar.JANUARY, 1);
		Date date2 = calendar2.getTime();

		assertFalse(DateUtil.isAfterOrEqual(date1, date2), "A primeira data não deve ser depois ou igual à segunda");
	}

	@Test
	@DisplayName("Testa o método isBeforeOrEqual quando a primeira data é antes ou igual à segunda")
	void test_isBeforeOrEqual_before() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(2021, Calendar.DECEMBER, 31);
		Date date1 = calendar1.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(2022, Calendar.JANUARY, 1);
		Date date2 = calendar2.getTime();

		assertTrue(DateUtil.isBeforeOrEqual(date1, date2), "A primeira data deve ser antes ou igual à segunda");
	}

	@Test
	@DisplayName("Testa o método isBeforeOrEqual quando a primeira data é depois da segunda")
	void test_isBeforeOrEqual_after() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(2023, Calendar.JANUARY, 1);
		Date date1 = calendar1.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(2022, Calendar.DECEMBER, 31);
		Date date2 = calendar2.getTime();

		assertFalse(DateUtil.isBeforeOrEqual(date1, date2), "A primeira data não deve ser antes ou igual à segunda");
	}

	@Test
	@DisplayName("Testa o método isBetween quando a data está entre o intervalo")
	void test_isBetween_withinRange() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(2022, Calendar.JANUARY, 1);
		Date date = calendar1.getTime();

		Calendar start = Calendar.getInstance();
		start.set(2021, Calendar.DECEMBER, 31);
		Date startDate = start.getTime();

		Calendar end = Calendar.getInstance();
		end.set(2022, Calendar.DECEMBER, 31);
		Date endDate = end.getTime();

		assertTrue(DateUtil.isBetween(date, startDate, endDate), "A data deve estar entre o intervalo");
	}

	@Test
	@DisplayName("Testa o método isBetween quando a data não está entre o intervalo")
	void test_isBetween_outOfRange() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(2023, Calendar.JANUARY, 1);
		Date date = calendar1.getTime();

		Calendar start = Calendar.getInstance();
		start.set(2021, Calendar.DECEMBER, 31);
		Date startDate = start.getTime();

		Calendar end = Calendar.getInstance();
		end.set(2022, Calendar.DECEMBER, 31);
		Date endDate = end.getTime();

		assertFalse(DateUtil.isBetween(date, startDate, endDate), "A data não deve estar entre o intervalo");
	}

	@Test
	@DisplayName("Testa o método formatYearMonth")
	void test_formatYearMonth() {
		YearMonth yearMonth = YearMonth.of(2022, 12);
		assertEquals("12/2022", DateUtil.formatYearMonth(yearMonth), "A data deve ser formatada como 'MM/yyyy'");
	}

	@Test
	void test_returns_false_when_date_is_null() {
		Date nullDate = null;
		Integer year = 2023;

		boolean result = DateUtil.isSameYear(nullDate, year);

		assertFalse(result);
	}

}
