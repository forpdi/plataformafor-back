package org.forpdi.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DateUtilsTest {

	@DisplayName("Testa converter Date para String")
	@Test
	public void testDateToString() {
		String expected = "02/12/2000";
		Date date = new Date(975797534595L);
		assertEquals(expected, DateUtil.dateToStringYYYY(date));
	}
}
