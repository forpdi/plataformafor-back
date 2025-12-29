package org.forpdi.dashboard.indicators;

import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FPDI_ACCESS_IDX;
import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FRISCO_ACCESS_IDX;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.company.Company;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.system.accesslog.AccessLogHistory;

public class AccessHistoryCalculator {
	private List<AccessLogHistory> accessLogHistoryList;

	public AccessHistoryCalculator(List<AccessLogHistory> accessLogHistoryList) {
		this.accessLogHistoryList = accessLogHistoryList;
	}

	public AccessHistoryResult getAccessHistoryResult() {
		Map<Long, List<AccessLogHistory>> companyAccessLogMap = Util.generateGroupedMap(accessLogHistoryList, h -> h.getCompanyId());
		List<CompanyAccessHistory> companiesAccessHistory = new ArrayList<>(companyAccessLogMap.size());

		for (var entry : companyAccessLogMap.entrySet()) {
			List<AccessLogHistory> companyAccessLogHistoryList = entry.getValue();

			Map<String, Integer[]> history = new TreeMap<>();
			List<LocalDate> dates = new ArrayList<>(companyAccessLogHistoryList.size());
			companyAccessLogHistoryList.forEach(alh -> {
				Integer[] accesses = new Integer[2];
				accesses[CompanyAccessHistory.FPDI_ACCESS_IDX] = alh.getFpdiAccessCount();
				accesses[CompanyAccessHistory.FRISCO_ACCESS_IDX] = alh.getFriscoAccessCount();

				LocalDate date = alh.getDate();
				dates.add(date);
				history.put(date.toString(), accesses);
			});

			fillAccessHistoryMissingDates(history, companyAccessLogHistoryList);

			companiesAccessHistory.add(new CompanyAccessHistory(entry.getKey(), history));
		}

		return new AccessHistoryResult(companiesAccessHistory);
	}

	public AccessHistoryResult getAccessHistoryResultOneByMonth() {
		Map<Long, List<AccessLogHistory>> companyAccessLogMap = Util.generateGroupedMap(accessLogHistoryList, h -> h.getCompanyId());
		List<CompanyAccessHistory> companiesAccessHistory = new ArrayList<>(companyAccessLogMap.size());

		for (var entry : companyAccessLogMap.entrySet()) {
			List<AccessLogHistory> companyAccessLogHistoryList = entry.getValue();

			Map<String, Integer[]> history = new TreeMap<>();

			Map<String, Boolean> fpdiMonthRecorded = new HashMap<>();
			Map<String, Boolean> friscoMonthRecorded = new HashMap<>();

			for (AccessLogHistory alh : companyAccessLogHistoryList) {
				LocalDate date = alh.getDate();
				String monthKey = date.getYear() + "-" + date.getMonthValue();

				Integer[] accesses = history.getOrDefault(date.toString(), new Integer[] { 0, 0 });

				if (alh.getFpdiAccessCount() > 0 && !fpdiMonthRecorded.getOrDefault(monthKey, false)) {
					accesses[CompanyAccessHistory.FPDI_ACCESS_IDX] = 1;
					fpdiMonthRecorded.put(monthKey, true);
				}

				if (alh.getFriscoAccessCount() > 0 && !friscoMonthRecorded.getOrDefault(monthKey, false)) {
					accesses[CompanyAccessHistory.FRISCO_ACCESS_IDX] = 1;
					friscoMonthRecorded.put(monthKey, true);
				}

				if (accesses[CompanyAccessHistory.FPDI_ACCESS_IDX] > 0 || accesses[CompanyAccessHistory.FRISCO_ACCESS_IDX] > 0) {
					history.put(date.toString(), accesses);
				}
			}

			companiesAccessHistory.add(new CompanyAccessHistory(entry.getKey(), history));
		}

		return new AccessHistoryResult(companiesAccessHistory);
	}

	private void fillAccessHistoryMissingDates(Map<String, Integer[]> history, List<AccessLogHistory> companyAccessLogHistoryList) {
		LocalDate lastDate = companyAccessLogHistoryList.get(companyAccessLogHistoryList.size() - 1).getDate();
		LocalDate currentDate = companyAccessLogHistoryList.get(0).getDate();
		while (currentDate.isBefore(lastDate)) {
			String currentDateString = currentDate.toString();
			if (!history.containsKey(currentDateString)) {
				history.put(currentDateString, new Integer[] { 0, 0 });
			}
			currentDate = currentDate.plusDays(1);
		}
	}


	public static class AccessHistoryResult {
		private final List<CompanyAccessHistory> companiesAccessHistory;

		public AccessHistoryResult(List<CompanyAccessHistory> companiesAccessHistory) {
			this.companiesAccessHistory = companiesAccessHistory;
		}

		public List<CompanyAccessHistory> getCompaniesAccessHistory() {
			return companiesAccessHistory;
		}

		public Map<Long, Integer[]> getCompaniesAccessHistoryFilteredByPeriod(
				List<Company> companies,
				Date companyCreationBegin,
				Date companyCreationEnd) {

			Map<Long, Integer[]> companiesAccessHistoryFiltered = new HashMap<>();
			companies.forEach(company -> {
				companiesAccessHistoryFiltered.put(company.getId(), new Integer[] {0, 0});
			});

			companiesAccessHistory.forEach(cah -> {
				long companyId = cah.getCompanyId();
				cah.getHistory().entrySet().forEach(entry -> {
					String dateStr = entry.getKey();
					Integer[] accesses = entry.getValue();
					Date date = GeneralUtils.parseDate(dateStr, new SimpleDateFormat("yyyy-MM-dd"));
					if ((companyCreationBegin == null || DateUtil.isAfterOrEqual(date, companyCreationBegin))
							&& (companyCreationEnd == null || DateUtil.isBeforeOrEqual(date, companyCreationEnd))
							&& companiesAccessHistoryFiltered.containsKey(companyId)) {
						Integer[] totalizedAcces = companiesAccessHistoryFiltered.get(companyId);
						totalizedAcces[FPDI_ACCESS_IDX] += accesses[FPDI_ACCESS_IDX];
						totalizedAcces[FRISCO_ACCESS_IDX] += accesses[FRISCO_ACCESS_IDX];
					}
				});
			});

			return companiesAccessHistoryFiltered;
		}
	}
}
