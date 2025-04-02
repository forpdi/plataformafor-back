package org.forrisco.risk;

import java.util.Date;

public class RiskStateChecker {

	private RiskStateChecker() {
	}
	
	/**
	 * Retorna o estado de cada risco a partir da data
	 * 
	 * @param String
	 *            periodicidade
	 * 
	 * @param Data
	 *            Data para comparação da peridicidade
	 * 
	 * @return int estado atual do monitoramento
	 */
	public static int riskState(String periodicity, Date date) {
		Date now = new Date();

		double diffInSec = (now.getTime() - date.getTime()) / 1000.0;
		double diffDays = diffInSec / (60 * 60 * 24);
		return evaluateRiskState(periodicity, diffDays);
	}
	
	private static int evaluateRiskState(String periodicity, double diffDays) {
		switch (periodicity.toLowerCase()) {
		case "diária":
			return evaluateDailyPeriodicity(diffDays);
		case "semanal":
			return  evaluateWeeklyPeriodicity(diffDays);
		case "quinzenal":
			return  evaluateBiweeklyPeriodicity(diffDays);
		case "mensal":
			return  evaluateMonthlyPeriodicity(diffDays);
		case "bimestral":
			return  evaluateBimonthlyPeriodicity(diffDays);
		case "trimestral":
			return  evaluateQuaterlyPeriodicity(diffDays);
		case "semestral":
			return  evaluateSemiannualPeriodicity(diffDays);
		case "anual":
			return  evaluateAnnualPeriodicity(diffDays);
		default:
			return RiskState.UP_TO_DATE.getId();
		}
	}

	private static int evaluateAnnualPeriodicity(double diffDays) {
		if (diffDays > 360) {
			return RiskState.LATE.getId();
		} else if (diffDays > CloseToMaturityPeriod.ANUAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateSemiannualPeriodicity(double diffDays) {
		if (diffDays > 180) {
			return RiskState.LATE.getId();
		} else if (diffDays > 180 - CloseToMaturityPeriod.SEMESTRAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateQuaterlyPeriodicity(double diffDays) {
		if (diffDays > 90) {
			return RiskState.LATE.getId();
		} else if (diffDays > 90 - CloseToMaturityPeriod.TRIMESTRAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateBimonthlyPeriodicity(double diffDays) {
		if (diffDays > 60) {
			return RiskState.LATE.getId();
		} else if (diffDays > 60 - CloseToMaturityPeriod.BIMESTRAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateMonthlyPeriodicity(double diffDays) {
		if (diffDays > 30) {
			return RiskState.LATE.getId();
		} else if (diffDays > 30 - CloseToMaturityPeriod.MENSAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateBiweeklyPeriodicity(double diffDays) {
		if (diffDays > 15) {
			return RiskState.LATE.getId();
		} else if (diffDays > 15 - CloseToMaturityPeriod.QUINZENAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateWeeklyPeriodicity(double diffDays) {
		if (diffDays > 7) {
			return RiskState.LATE.getId();
		} else if (diffDays > 7 - CloseToMaturityPeriod.SEMANAL.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}

	private static int evaluateDailyPeriodicity(double diffDays) {
		if (diffDays > 1) {
			return RiskState.LATE.getId();
		} else if (diffDays * 24 > CloseToMaturityPeriod.DIARIO.getValue()) {
			return RiskState.CLOSE_TO_EXPIRE.getId();
		}
		return RiskState.UP_TO_DATE.getId();
	}
}
