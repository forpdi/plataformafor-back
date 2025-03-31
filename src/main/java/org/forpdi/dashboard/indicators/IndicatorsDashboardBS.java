package org.forpdi.dashboard.indicators;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyType;
import org.forpdi.core.location.CompanyIndicators;
import org.forpdi.core.location.Region;
import org.forpdi.core.location.RegionRepository;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.indicators.AccessHistoryCalculator.AccessHistoryResult;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.accesslog.AccessLogBS;
import org.forpdi.system.accesslog.AccessLogHistory;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.policy.Policy;
import org.forrisco.risk.Risk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class IndicatorsDashboardBS extends HibernateBusiness {
	@Autowired
	private EntityManager em;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private AccessLogBS accessLogBS;

	public IndicatorsData getIndicatorsData() {
		IndicatorsData data = new IndicatorsData();
		List<Company> companies = companyBS.listAll();
		var regionsCounts = getRegionsCounts(companies);
		var accessHistoryResult = getCompaniesAccessHistory();
		var companiesFpdiIndicators = getCompaniesFpdiIndicators(companies);
		var companiesFriscoIndicators = getCompaniesFriscoIndicators(companies);
		var companiesIndicators = getCompaniesIndicators(companies, companiesFpdiIndicators, companiesFriscoIndicators);
		data.setRegionsCounts(regionsCounts);
		data.setCompaniesAccessHistory(accessHistoryResult.getCompaniesAccessHistory());
		data.setCompaniesFpdiIndicators(companiesFpdiIndicators);
		data.setCompaniesFriscoIndicators(companiesFriscoIndicators);
		data.setCompaniesIndicators(companiesIndicators);
		return data;
	}

	public List<RegionCounts> getRegionsCounts(List<Company> companies) {
		List<Region> regions = regionRepository.findAll();
		List<RegionCounts> regionsCounts = new ArrayList<>(regions.size());

		Map<Long, List<Company>> regionCompanyMap = Util.generateGroupedMap(
			companies, c -> c.getCounty().getUf().getRegion().getId());

		regions.forEach(region -> {
			List<Company> regionCompanies = regionCompanyMap
				.getOrDefault(region.getId(), Collections.emptyList());

			long universityCount = regionCompanies.stream()
				.filter(c -> c.getType() == CompanyType.UNIVERSITY.getId())
				.count();
			long instituteCount = regionCompanies.stream()
				.filter(c -> c.getType() == CompanyType.INSTITUTE.getId())
				.count();
			long otherCount = regionCompanies.stream()
				.filter(c -> c.getType() == CompanyType.OTHER.getId())
				.count();

			var rc = new RegionCounts(
				region.getId(),
				region.getName(),
				universityCount,
				instituteCount,
				otherCount
			);

			regionsCounts.add(rc);
		});

		return regionsCounts;
	}


	public AccessHistoryResult getCompaniesAccessHistory() {
		List<AccessLogHistory> accessLogHistoryList = accessLogBS.listAccessLogHistory();
		return new AccessHistoryCalculator(accessLogHistoryList).getAccessHistoryResultOneByMonth();
	}

	public AccessHistoryResult getCompaniesAccessHistoryByPeriod(String beginStr, String endStr) {
		LocalDate begin = beginStr != null ? LocalDate.parse(beginStr, DateUtil.LOCAL_DATE_FORMATTER) : LocalDate.of(1000, 1, 1);
		LocalDate end = endStr != null ? LocalDate.parse(endStr, DateUtil.LOCAL_DATE_FORMATTER) : LocalDate.of(9999, 1, 1);

		List<AccessLogHistory> accessLogHistoryList = accessLogBS.listAccessLogHistoryByPeriod(begin, end);
		return new AccessHistoryCalculator(accessLogHistoryList).getAccessHistoryResultOneByMonth();
	}

	public List<CompanyFpdiIndicators> getCompaniesFpdiIndicators(List<Company> companies) {
		Map<Company, Long> companiesPlanMacros = countPlanMacroByCompanies();
		Map<Company, Long> companiesPlans = countPlansByCompanies();
		Map<Company, Long> companiesAxes = countAxesByCompanies();
		Map<Company, Long> companiesObjectives = countObjectivesByCompanies();
		Map<Company, Long> companiesIndicators = countIndicatorsByCompanies();
		Map<Company, Long> companiesGoals = countGoalsByCompanies();

		var indicatorsList = new ArrayList<CompanyFpdiIndicators>(companies.size());
		companies.forEach(company -> {
			CompanyFpdiIndicators cfi = new CompanyFpdiIndicators();
			cfi.setCompany(company);
			cfi.setPlanMacros(companiesPlanMacros.getOrDefault(company, 0L));
			cfi.setPlans(companiesPlans.getOrDefault(company, 0L));
			cfi.setAxes(companiesAxes.getOrDefault(company, 0L));
			cfi.setObjectives(companiesObjectives.getOrDefault(company, 0L));
			cfi.setIndicators(companiesIndicators.getOrDefault(company, 0L));
			cfi.setGoals(companiesGoals.getOrDefault(company, 0L));
			indicatorsList.add(cfi);
		});

		return indicatorsList;
	}

	public List<CompanyForriscoIndicators> getCompaniesFriscoIndicators(List<Company> companies) {
		Map<Company, Long> companiesPolicies = countPoliciesByCompanies();
		Map<Company, Long> companiesPlanRisks = countPlanRisksByCompanies();
		Map<Company, Long> companiesRisks = countRisksByCompanies();
		Map<Long, Long> companiesMonitoredRisks = countMonitoredRisksByCompanies();

		var indicatorsList = new ArrayList<CompanyForriscoIndicators>(companies.size());
		companies.forEach(company -> {
			CompanyForriscoIndicators cfi = new CompanyForriscoIndicators();
			cfi.setCompany(company);
			cfi.setPolicies(companiesPolicies.getOrDefault(company, 0L));
			cfi.setPlanRisks(companiesPlanRisks.getOrDefault(company, 0L));
			cfi.setRisks(companiesRisks.getOrDefault(company, 0L));
			cfi.setMonitoredRisks(companiesMonitoredRisks.getOrDefault(company.getId(), 0L));
			indicatorsList.add(cfi);
		});

		return indicatorsList;
	}

	public List<CompanyIndicators> getCompaniesIndicators(
			List<Company> companies,
			List<CompanyFpdiIndicators> companiesFpdiIndicators,
			List<CompanyForriscoIndicators> companiesFriscoIndicators) {
		Map<Long, CompanyFpdiIndicators> companyFpdiMap = Util.generateMap(companiesFpdiIndicators, cfi -> cfi.getCompany().getId());
		Map<Long, CompanyForriscoIndicators> companyFriscoMap = Util.generateMap(companiesFriscoIndicators, cfi -> cfi.getCompany().getId());
		List<CompanyIndicators> companiesIndicators = new ArrayList<CompanyIndicators>(companies.size());
		companies.forEach(company -> {
			long planMacrosCount = companyFpdiMap.get(company.getId()).getPlanMacros();
			long planRisksCount = companyFriscoMap.get(company.getId()).getPlanRisks();
			var ci = new CompanyIndicators.Builder()
					.company(company)
					.planMacrosCount(planMacrosCount)
					.planRisksCount(planRisksCount)
					.build();
			companiesIndicators.add(ci);
		});

		return companiesIndicators;
	}

	private Map<Company, Long> countPlanMacroByCompanies() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<PlanMacro> root = cq.from(PlanMacro.class);

		cq.multiselect(root.get("company"), cb.count(root.get("id")));

		cq.where(planMacroIsValid(root, cb));

		cq.groupBy(root.get("company"));

		return executeCounts(cq);
	}

	private Map<Company, Long> countPlansByCompanies() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<Plan> root = cq.from(Plan.class);

		cq.multiselect(root.get("parent").get("company"), cb.count(root.get("id")));

		cq.where(planIsValid(root, cb));

		cq.groupBy(root.get("parent").get("company"));

		return executeCounts(cq);
	}

	private Map<Company, Long> countGoalsByCompanies() {
		return countStructureLevelInstacesByCompanies(true, false, false);
	}

	private Map<Company, Long> countIndicatorsByCompanies() {
		return countStructureLevelInstacesByCompanies(false, true, false);
	}

	private Map<Company, Long> countObjectivesByCompanies() {
		return countStructureLevelInstacesByCompanies(false, false, true);
	}

	private Map<Company, Long> countAxesByCompanies() {
		return countStructureLevelInstacesByCompanies(false, false, false);
	}

	private Map<Company, Long> countStructureLevelInstacesByCompanies(boolean goal, boolean indicator, boolean objective) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<StructureLevelInstance> root = cq.from(StructureLevelInstance.class);

		Path<?> companyPath = root.get("plan").get("parent").get("company");
		cq.multiselect(companyPath, cb.count(root.get("id")));

		cq.where(
				cb.equal(root.get("deleted"), false),
				cb.equal(root.get("level").get("goal"), goal),
				cb.equal(root.get("level").get("indicator"), indicator),
				cb.equal(root.get("level").get("objective"), objective),
				planIsValid(root.get("plan"), cb)
		);

		cq.groupBy(companyPath);

		return executeCounts(cq);
	}

	private Predicate planIsValid(Path<?> planRoot, CriteriaBuilder cb) {
		return cb.and(
				cb.equal(planRoot.get("deleted"), false),
				planMacroIsValid(planRoot.get("parent"), cb)
		);
	}

	private Predicate planMacroIsValid(Path<?> planMacroRoot, CriteriaBuilder cb) {
		Date today = new Date();
		return cb.and(
				cb.equal(planMacroRoot.get("deleted"), false),
				cb.equal(planMacroRoot.get("archived"), false),
				cb.equal(planMacroRoot.get("company").get("deleted"), false),
				cb.lessThanOrEqualTo(planMacroRoot.get("begin"), today),
				cb.greaterThanOrEqualTo(planMacroRoot.get("end"), today)
		);
	}

	private Map<Company, Long> countPoliciesByCompanies() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<Policy> root = cq.from(Policy.class);

		cq.multiselect(root.get("company"), cb.count(root.get("id")));

		cq.where(cb.equal(root.get("deleted"), false));

		cq.groupBy(root.get("company"));

		return executeCounts(cq);
	}

	private Map<Company, Long> countPlanRisksByCompanies() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<PlanRisk> root = cq.from(PlanRisk.class);

		cq.multiselect(root.get("policy").get("company"), cb.count(root.get("id")));

		cq.where(
				cb.equal(root.get("deleted"), false),
				planRiskIsValid(root, cb)
		);

		cq.groupBy(root.get("policy").get("company"));

		return executeCounts(cq);
	}

	private Map<Company, Long> countRisksByCompanies() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<Risk> root = cq.from(Risk.class);

		Path<?> companyPath = root.get("unit").get("planRisk").get("policy").get("company");
		cq.multiselect(companyPath, cb.count(root.get("id")));

		cq.where(
				cb.equal(root.get("deleted"), false),
				planRiskIsValid(root.get("unit").get("planRisk"), cb)
		);

		cq.groupBy(companyPath);

		return executeCounts(cq);
	}

	@SuppressWarnings("unchecked")
	private Map<Long, Long> countMonitoredRisksByCompanies() {
		LocalDate today = LocalDate.now();
		String subquery = "select p.company_id as company, count(m.risk_id) as counter "
				+ "from frisco_monitor m "
				+ "inner join frisco_risk r on r.id = m.risk_id "
				+ "inner join frisco_unit u on u.id = r.unit_id "
				+ "inner join frisco_plan_risk pr on pr.id = u.planRisk_id "
				+ "inner join frisco_policy p on p.id = pr.policy_id "
				+ "inner join fpdi_company c on c.id = p.company_id "
				+ "where m.deleted = false and c.deleted = false and pr.validity_begin <= '" + today + "' and pr.validity_end >= '" + today + "' "
				+ "group by p.company_id, m.risk_id";

		String query = "select sq.company, count(sq.counter) from (" + subquery + ") as sq group by sq.company";

		List<Tuple> list = em.createNativeQuery(query, Tuple.class).getResultList();

		var results = new HashMap<Long, Long>(list.size());

		list.forEach(tuple -> {
			Long companyId = tuple.get(0, BigInteger.class).longValue();
			long count = tuple.get(1, BigInteger.class).longValue();
			results.put(companyId, count);
		});

		return results;
	}

	private Predicate planRiskIsValid(Path<?> planRiskRoot, CriteriaBuilder cb) {
		Date today = new Date();
		return cb.and(
				cb.equal(planRiskRoot.get("deleted"), false),
				cb.equal(planRiskRoot.get("policy").get("company").get("deleted"), false),
				cb.lessThanOrEqualTo(planRiskRoot.get("validityBegin"), today),
				cb.greaterThanOrEqualTo(planRiskRoot.get("validityEnd"), today)
		);
	}

	private Map<Company, Long> executeCounts(CriteriaQuery<Tuple> cq) {
		List<Tuple> list = em.createQuery(cq).getResultList();

		var results = new HashMap<Company, Long>(list.size());

		list.forEach(tuple -> {
			Company company = tuple.get(0, Company.class);
			long count = tuple.get(1, Long.class);
			results.put(company, count);
		});

		return results;
	}
}
