package org.forpdi.dashboard.indicators;

import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FPDI_ACCESS_IDX;
import static org.forpdi.dashboard.indicators.CompanyAccessHistory.FRISCO_ACCESS_IDX;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.csvs.CsvExportHelper;
import org.forpdi.core.location.CompanyIndicators;
import org.forpdi.core.utils.Counter;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.dashboard.indicators.AccessHistoryCalculator.AccessHistoryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvIndicatorsDashboardExport {

	@Autowired
	private CsvExportHelper helper;
	@Autowired
	private IndicatorsDashboardBS bs;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private IndicatorsDashboardHelper indicatorsDashboardHelper;

	
	public void exportAllIndicatorsCsv(Writer writer, ExportFiltersDto filters) throws IOException {
		exportRegionsCountsCsv(writer, filters);
		writer.write("\n");
		exportAccessHistoryCsv(writer, filters);
		writer.write("\n");
		exportFpdiIndicatorsCsv(writer, filters);
		writer.write("\n");
		exportFriscoIndicatorsCsv(writer, filters);
		writer.write("\n");
		exportCompaniesIndicatorsCsv(writer, filters);
	}
	
	public void exportRegionsCountsCsv(Writer writer, ExportFiltersDto filters) throws IOException {
		
		List<Company> companies = companyBS.listAll();
		List<RegionCounts> regionsCounts = bs.getRegionsCounts(companies);
		
		writeRegions(writer, filters.regionId(), regionsCounts, companies.size());
		
		writer.write("\n");
		
		writeCompanyType(writer, regionsCounts);
		
		writer.write("\n");
		
		writeAdhesions(writer, companies);
	}

	private void writeRegions(Writer writer, long regionId, List<RegionCounts> regionsCounts, int totalCompanies) throws IOException {
		writer.write("REGIÕES\n");
		
		String[] header = {
				"REGIÃO", "UNIVERSIDADES", "INSTITUTOS FEDERAIS", "PERCENTUAL"
		};
		writer.write(helper.createRow(header));
		
		for (RegionCounts rc : regionsCounts) {
			if (regionId == -1 || regionId == rc.getRegionId()) {
				double percent = totalCompanies > 0
						? ((double) rc.getCompaniesCount() / totalCompanies) * 100
						: 0;
				String[] values = {
						rc.getRegionName(),
						String.valueOf(rc.getUniversityCount()),
						String.valueOf(rc.getInstituteCount()),
						"\"" + String.format("%.2f", percent) + "\"" 
				};
				writer.write(helper.createRow(values));
			}
		}
	}

	private void writeCompanyType(Writer writer, List<RegionCounts> regionsCounts) throws IOException {
		writer.write("UNIVERSIDADES/INSTITUTOS\n");
		
		String[] header = { "TIPO", "QUANTIDADE", "PERCENTUAL" };
		writer.write(helper.createRow(header));
		
		long totalUniversities = indicatorsDashboardHelper.getTotalUniversities(regionsCounts);
		long totalInstitutes   = indicatorsDashboardHelper.getTotalInstitutes(regionsCounts);
		long totalOthers       = indicatorsDashboardHelper.getTotalOthers(regionsCounts); 
		
		long total = totalUniversities + totalInstitutes + totalOthers;
		
		double percentUniversities = total > 0 ? (double) totalUniversities / total * 100 : 0;
		double percentInstitutes   = total > 0 ? (double) totalInstitutes   / total * 100 : 0;
		double percentOthers       = total > 0 ? (double) totalOthers       / total * 100 : 0;
		
		String[] universitiesRow = {
				"Universidade",
				String.valueOf(totalUniversities),
				getFormatedFloat(percentUniversities)
		};
		String[] institutesRow = {
				"Instituto Federal",
				String.valueOf(totalInstitutes),
				getFormatedFloat(percentInstitutes)
		};
		String[] othersRow = {
				"Outros",
				String.valueOf(totalOthers),
				getFormatedFloat(percentOthers)
		};
		
		writer.write(helper.createRow(universitiesRow));
		writer.write(helper.createRow(institutesRow));
		writer.write(helper.createRow(othersRow));
	}

	private void writeAdhesions(Writer writer, List<Company> companies) throws IOException {
		writer.write("ADESÕES\n");
		
		var adhesionByYearMonths = indicatorsDashboardHelper.getAhesionsByYearMonths(companies);
		
		String[] header = { "MÊS/ANO", "ADESÕES" };
		writer.write(helper.createRow(header));
		
		Map<YearMonth, Counter> adhesionsCountMap = adhesionByYearMonths.adhesionCountMap();
		for (YearMonth ym : adhesionByYearMonths.sortedYearMonths()) {
			String fmtYearMonth = DateUtil.formatYearMonth(ym);
			String adhesions = String.valueOf(adhesionsCountMap.get(ym).getCounter());
			String[] values = { fmtYearMonth, adhesions };
			writer.write(helper.createRow(values));
		}
	}

	public long getTotalOthers(List<RegionCounts> regionsCounts) {
		long total = 0;
		for (RegionCounts rc : regionsCounts) {
			total += rc.getOtherCount();
		}
		return total;
	}

	
	public void exportAccessHistoryCsv(Writer writer, ExportFiltersDto filters) throws IOException {
		writer.write("ACESSOS\n");
		
		AccessHistoryResult accessHistoryResult = bs.getCompaniesAccessHistoryByPeriod(
				filters.companyCreationBegin(), filters.companyCreationEnd());
		Map<YearMonth, Integer[]> yearMonthAccesses = new HashMap<>();
		
		int totalFpdiAccess = 0;
		int totalFriscoAccess = 0;

		for (var cah : accessHistoryResult.getCompaniesAccessHistory()) {
			if (filters.companyIds().contains(cah.getCompanyId())) {
				for (var entry : cah.getHistory().entrySet()) {
					LocalDate date = LocalDate.parse(entry.getKey());
					YearMonth yearMonth = YearMonth.from(date);
					
					if (!yearMonthAccesses.containsKey(yearMonth)) {
						yearMonthAccesses.put(yearMonth, new Integer[] { 0, 0 });
					}
					Integer[] accesses = entry.getValue();
					int fpdiAccess = accesses[FPDI_ACCESS_IDX];
					int friscoAccess = accesses[FRISCO_ACCESS_IDX];
					totalFpdiAccess += fpdiAccess;
					totalFriscoAccess += friscoAccess;
					yearMonthAccesses.get(yearMonth)[FPDI_ACCESS_IDX] += accesses[FPDI_ACCESS_IDX];
					yearMonthAccesses.get(yearMonth)[FRISCO_ACCESS_IDX] += accesses[FRISCO_ACCESS_IDX];
				}
			}
		}
		
		writeAccessHistory(writer, yearMonthAccesses);

		writer.write("\n");
		
		writeTotalizedAccess(writer, totalFpdiAccess, totalFriscoAccess);
	}


	private void writeAccessHistory(Writer writer, Map<YearMonth, Integer[]> yearMonthAccesses)
			throws IOException {
		List<YearMonth> yearMonths = new ArrayList<YearMonth>(yearMonthAccesses.keySet());
		Collections.sort(yearMonths);

		writer.write("HISTÓRICO DE ACESSOS\n");
		String[] header = { "MÊS/ANO", "ACESSOS FORPDI", "ACESSOS FORRISCO" };
		writer.write(helper.createRow(header));
		
		
		for (YearMonth ym : yearMonths) {
			String fmtYearMonth = DateUtil.formatYearMonth(ym);
			String fpdiAccess = String.valueOf(yearMonthAccesses.get(ym)[FPDI_ACCESS_IDX]);
			String friscoAccess = String.valueOf(yearMonthAccesses.get(ym)[FRISCO_ACCESS_IDX]);
			String[] values = { fmtYearMonth, fpdiAccess, friscoAccess };
			writer.write(helper.createRow(values));
		}
	}
	
	private void writeTotalizedAccess(Writer writer, int totalFpdiAccess, int totalFriscoAccess) throws IOException {
		writer.write("TOTAL DE ACESSOS\n");

		String[] header = { "PLATAFOMA", "ACESSOS", "PERCENTUAL" };
		writer.write(helper.createRow(header));

		int totalAccess = totalFpdiAccess + totalFriscoAccess;
		
		double fpdiPercent = totalAccess > 0 ? ((double) totalFpdiAccess / totalAccess) * 100 : 0;
		double friscoPercent = totalAccess > 0 ? ((double) totalFriscoAccess / totalAccess) * 100 : 0;
		
		String[] fpdiValues = { "FORPDI", String.valueOf(totalFpdiAccess), getFormatedFloat(fpdiPercent) };
		String[] friscoValues = { "FORRISCO", String.valueOf(totalFriscoAccess), getFormatedFloat(friscoPercent) };
		
		writer.write(helper.createRow(fpdiValues));
		writer.write(helper.createRow(friscoValues));
	}

	public void exportFpdiIndicatorsCsv(Writer writer, ExportFiltersDto filters) throws IOException {
		writer.write("PDI EM NÚMEROS\n");
		
		String[] header = {
				"INDICADOR", "QUANTIDADE"
		};
		
		writer.write(helper.createRow(header));
		
		List<Company> companies = companyBS.listAll();
		List<CompanyFpdiIndicators> companiesFpdiIndicators = bs.getCompaniesFpdiIndicators(companies);
		List<CompanyFpdiIndicators> filteredCompaniesFpdiIndicators = companiesFpdiIndicators.stream()
				.filter(cfi -> filters.companyIds().contains(cfi.getCompany().getId()))
				.toList();
		
		List<Long> fpdiIndicators = indicatorsDashboardHelper.countFpdiIndicators(filteredCompaniesFpdiIndicators);
		
		String[] planMacros = {"Quantidade de PDI vigentes", fpdiIndicators.get(0).toString()};
		String[] plans = {"Plano de metas", fpdiIndicators.get(1).toString()};
		String[] axes = {"Eixos temáticos", fpdiIndicators.get(2).toString()};
		String[] objectives = {"Objetivos", fpdiIndicators.get(3).toString()};
		String[] indicators = {"Indicadores", fpdiIndicators.get(4).toString()};
		String[] goals = {"Metas", fpdiIndicators.get(5).toString()};
		
		writer.write(helper.createRow(planMacros));
		writer.write(helper.createRow(plans));
		writer.write(helper.createRow(axes));
		writer.write(helper.createRow(objectives));
		writer.write(helper.createRow(indicators));
		writer.write(helper.createRow(goals));
	}

	public void exportFriscoIndicatorsCsv(Writer writer, ExportFiltersDto filters) throws IOException {
		writer.write("RISCOS EM NÚMEROS\n");
		
		String[] header = {
				"INDICADOR", "QUANTIDADE"
		};
		
		writer.write(helper.createRow(header));
		
		List<Company> companies = companyBS.listAll();
		List<CompanyForriscoIndicators> companiesFriscoIndicators = bs.getCompaniesFriscoIndicators(companies);
		List<CompanyForriscoIndicators> filteredCompaniesFrsicoIndicators = companiesFriscoIndicators.stream()
				.filter(cfi -> filters.companyIds().contains(cfi.getCompany().getId()))
				.toList();
		
		List<Long> friscoIndicators = indicatorsDashboardHelper.countFriscoIndicators(filteredCompaniesFrsicoIndicators);
		
		String[] planRisks = {"Quantidade de Plano de Gestão de riscos vigentes", friscoIndicators.get(0).toString()};
		String[] policies = {"Políticas", friscoIndicators.get(1).toString()};
		String[] risks = {"Riscos", friscoIndicators.get(2).toString()};
		String[] monitoredRisks = {"Realizando monitoramento", friscoIndicators.get(3).toString()};
		
		writer.write(helper.createRow(planRisks));
		writer.write(helper.createRow(policies));
		writer.write(helper.createRow(risks));
		writer.write(helper.createRow(monitoredRisks));
	}

	public void exportCompaniesIndicatorsCsv(Writer writer, ExportFiltersDto filters) throws IOException {
		writer.write("INSTITUIÇÕES\n");
		
		String[] header = {
				"SIGLA", "NOME DA INSTITUIÇÃO", "DATA DE ADESÃO", "PERÍODO", "ACESSOS PDI", "ACESSOS RISCOS", "PDIS CADASTRADOS", "PLANOS DE RISCO CADASTRADOS"
		};
		
		writer.write(helper.createRow(header));
		
		List<Company> companies = companyBS.listAll();
		AccessHistoryResult accessHistoryResult = bs.getCompaniesAccessHistory();
		List<CompanyFpdiIndicators> companiesFpdiIndicators = bs.getCompaniesFpdiIndicators(companies);
		List<CompanyForriscoIndicators> companiesFriscoIndicators = bs.getCompaniesFriscoIndicators(companies);
		List<CompanyIndicators> companiesIndicators = bs.getCompaniesIndicators(companies, companiesFpdiIndicators, companiesFriscoIndicators);
		Map<Long, Integer[]> companiesAccessHistoryFiltered = accessHistoryResult.getCompaniesAccessHistoryFilteredByPeriod(
				companies, filters.companyCreationBeginAsDate(), filters.companyCreationEndAsDate());

		String periodValue = this.getPeriodValue(filters.companyCreationBegin(), filters.companyCreationEnd());
		
		for (CompanyIndicators ci : companiesIndicators) {
			if (filters.companyIds().contains(ci.getId())) {
				Integer[] accesses = companiesAccessHistoryFiltered.get(ci.getId());
				String[] values = {
						ci.getInitials(),
						ci.getName(),
						ci.getCreation().toString(),
						periodValue,
						accesses[CompanyAccessHistory.FPDI_ACCESS_IDX].toString(),
						accesses[CompanyAccessHistory.FRISCO_ACCESS_IDX].toString(),
						String.valueOf(ci.getPlanMacrosCount()),
						String.valueOf(ci.getPlanRisksCount())
				};
				writer.write(helper.createRow(values));
			}
		}
	}
	
	private String getPeriodValue(String beginDate, String endDate) {
		if (beginDate != null && endDate == null) {
			return "A partir de " + beginDate;
		}
		if (beginDate == null && endDate != null) {
			return "Até " + endDate;
		}

		if (beginDate != null && endDate != null) {
			return beginDate + " à " + endDate;
		}
		
		return "";
	}
	
	private String getFormatedFloat(double value) {
		return "\"" + String.format("%.2f", value) + "\"";
	}
}
