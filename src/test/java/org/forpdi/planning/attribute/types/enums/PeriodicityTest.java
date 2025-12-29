package org.forpdi.planning.attribute.types.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PeriodicityTest {

	@Test
	@DisplayName("Dado um nome de periodicidade válido, quando obtido através de getPeriodicityByName, então a periodicidade correta é retornada")
	void testGetPeriodicityByName_ValidName() {
		String dailyName = "Diária";
		String weeklyName = "Semanal";
		String monthlyName = "Mensal";

		Periodicity daily = Periodicity.getPeriodicityByName(dailyName);
		Periodicity weekly = Periodicity.getPeriodicityByName(weeklyName);
		Periodicity monthly = Periodicity.getPeriodicityByName(monthlyName);

		assertEquals(Periodicity.DAILY, daily, "A periodicidade retornada deve ser DAILY.");
		assertEquals(Periodicity.WEEKLY, weekly, "A periodicidade retornada deve ser WEEKLY.");
		assertEquals(Periodicity.MONTHLY, monthly, "A periodicidade retornada deve ser MONTHLY.");
	}

	@Test
	@DisplayName("Dado um nome de periodicidade inválido, quando chamado o método getPeriodicityByName, então uma exceção IllegalArgumentException deve ser lançada")
	void testGetPeriodicityByName_InvalidName() {
		String invalidName = "Inexistente";

		assertThrows(IllegalArgumentException.class, () -> {
			Periodicity.getPeriodicityByName(invalidName);
		}, "Deve lançar IllegalArgumentException para um nome inválido.");
	}

	@Test
	@DisplayName("Dado uma periodicidade, quando o método getValue é chamado, então o valor numérico da periodicidade é retornado corretamente")
	void testGetValue() {
		Periodicity daily = Periodicity.DAILY;
		Periodicity weekly = Periodicity.WEEKLY;
		Periodicity annual = Periodicity.ANNUAL;

		Integer dailyValue = daily.getValue();
		Integer weeklyValue = weekly.getValue();
		Integer annualValue = annual.getValue();

		assertEquals(1, dailyValue, "O valor para DAILY deve ser 1.");
		assertEquals(2, weeklyValue, "O valor para WEEKLY deve ser 2.");
		assertEquals(8, annualValue, "O valor para ANNUAL deve ser 8.");
	}

	@Test
	@DisplayName("Dado uma periodicidade, quando o método getName é chamado, então o nome da periodicidade é retornado corretamente")
	void testGetName() {
		Periodicity daily = Periodicity.DAILY;
		Periodicity monthly = Periodicity.MONTHLY;
		Periodicity annual = Periodicity.ANNUAL;

		String dailyName = daily.getName();
		String monthlyName = monthly.getName();
		String annualName = annual.getName();

		assertEquals("Diária", dailyName, "O nome para DAILY deve ser 'Diária'.");
		assertEquals("Mensal", monthlyName, "O nome para MONTHLY deve ser 'Mensal'.");
		assertEquals("Anual", annualName, "O nome para ANNUAL deve ser 'Anual'.");
	}
}
