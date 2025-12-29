package org.forrisco.risk;

import java.util.Calendar;
import java.util.Date;

public class RiskStateCheckerTestDateGenerator {

	/**
	 * Gera uma data que corresponde ao estado de risco fornecido para uma determinada periodicidade.
	 *
	 * @param periodicity String - periodicidade válida entre: "diária", "semanal", "quinzenal",
	 *                    "mensal", "bimestral", "trimestral", "semestral", "anual".
	 * @param riskStateId int - 0 para UP_TO_DATE, 1 para CLOSE_TO_EXPIRE, 2 para LATE.
	 * @return Date - a data correspondente ao estado de risco.
	 */
	public static Date getRiskStateDate(String periodicity, int riskStateId) {
		double daysDifference;

		switch (periodicity.toLowerCase()) {
			case "diária":
				if (riskStateId == 1) {
					daysDifference =
						calculateDaysDifference(1, riskStateId, CloseToMaturityPeriod.DIARIO.getValue() / 7);
				} else {
					daysDifference =
						calculateDaysDifference(1, riskStateId, CloseToMaturityPeriod.DIARIO.getValue());
				}
				break;
			case "semanal":
				daysDifference =
					calculateDaysDifference(7, riskStateId, CloseToMaturityPeriod.SEMANAL.getValue());
				break;
			case "quinzenal":
				daysDifference =
					calculateDaysDifference(15, riskStateId, CloseToMaturityPeriod.QUINZENAL.getValue());
				break;
			case "mensal":
				daysDifference =
					calculateDaysDifference(30, riskStateId, CloseToMaturityPeriod.MENSAL.getValue());
				break;
			case "bimestral":
				daysDifference =
					calculateDaysDifference(60, riskStateId, CloseToMaturityPeriod.BIMESTRAL.getValue());
				break;
			case "trimestral":
				daysDifference =
					calculateDaysDifference(90, riskStateId, CloseToMaturityPeriod.TRIMESTRAL.getValue());
				break;
			case "semestral":
				daysDifference =
					calculateDaysDifference(180, riskStateId, CloseToMaturityPeriod.SEMESTRAL.getValue());
				break;
			case "anual":
				daysDifference =
					calculateDaysDifference(360, riskStateId, 360 - CloseToMaturityPeriod.ANUAL.getValue());
				break;
			default:
				throw new IllegalArgumentException("Periodicidade inválida: " + periodicity);
		}

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -(int) daysDifference);

		calendar.set(Calendar.HOUR_OF_DAY, 5);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Calcula a diferença de dias com base no estado de risco.
	 *
	 * @param maxDays       double - o número máximo de dias para a periodicidade.
	 * @param riskStateId   int - o estado do risco (0, 1, 2).
	 * @param closeToExpire double - o valor de "próximo do vencimento" para a periodicidade.
	 * @return double - a diferença em dias.
	 */
	private static double calculateDaysDifference(double maxDays, int riskStateId, double closeToExpire) {
		return switch (riskStateId) {
			case 0 -> // UP_TO_DATE
				maxDays - closeToExpire - 1;
			case 1 -> // CLOSE_TO_EXPIRE
				maxDays - closeToExpire / 2;
			case 2 -> // LATE
				maxDays + 1;
			default -> throw new IllegalArgumentException("Estado de risco inválido: " + riskStateId);
		};
	}
}
