package org.forrisco.core.process;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.utils.Util;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.links.RiskProcessObjective;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author Matheus Nascimento
 */
@Service
public class ProcessBS extends HibernateBusiness {
	
	@Autowired
	private ArchiveBS archiveBS;
	@Autowired
	private EntityManager entityManager;
	
	/**
	 * Salvar um novo processo
	 * 
	 * @param Process,
	 *			instância do processo
	 * @throws Exception 
	 *            
	 */
	public void save(Process process) throws Exception {
		Archive file = null;
		this.validateProcess(process);
		if(!GeneralUtils.isEmpty(process.getFileLink())) {
			file = archiveBS.getByFileLink(process.getFileLink());
		}
		process.setDeleted(false);
		process.setFile(file);
		this.persist(process);
	}
	
	public void validateProcess(Process process) {
		/* não é mais obrigatório
		 * if (GeneralUtils.isEmpty(process.getFileLink())) {
			throw new IllegalArgumentException("Arquivo de anexo obrigatório.");
		}*/
	}
	
	/**
	 * Salvar um novo processounit
	 * 
	 * @param ProcessUnit,
	 *			instância do processounit
	 *            
	 */
	public void save(ProcessUnit processunit) {
		processunit.setDeleted(false);
		this.persist(processunit);	
	}
	
	/**
	 * Salvar um novo processObjective
	 * 
	 * @param ProcessUnit,
	 *			instância do processounit
	 *            
	 */
	public void saveObjective(ProcessObjective objective) {
		objective.setId(null);
		this.persist(objective);	
	}
	
	/**
	 * Retorna um processObjective
	 *
	 * @param id
	 * 		Id do processObjective
	 */
	public ProcessObjective retrieveProcessObjectiveById(Long id) {
		Criteria criteria = this.dao.newCriteria(ProcessObjective.class)
				.add(Restrictions.eq("id", id));

		return (ProcessObjective) criteria.uniqueResult();
	}
	
	public void deleteObjective(ProcessObjective objective) {
		this.remove(objective);
	}
	
	/**
	 * Recuperar process de uma unidade
	 * 
	 * @param Unit,
	 *            instância da unidade
	 *
	 */
	public PaginatedList<Process> listProcessByUnit(Unit unit) {
		
		PaginatedList<Process> results = new PaginatedList<Process>();
		List<Process> list = new LinkedList<Process>();
		
		Criteria criteria = this.dao.newCriteria(ProcessUnit.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("unit", unit));

		for(ProcessUnit processUnit : this.dao.findByCriteria(criteria, ProcessUnit.class)){
			Process process = processUnit.getProcess();
			List<Unit> relatedUnits = this.listRelatedUnits(process,unit.getPlanRisk());
			List<ProcessObjective> objectives = this.listProcessObjectives(process);
			process.setRelatedUnits(relatedUnits);
			process.setAllObjectives(objectives);
			list.add(process);
		}
		results.setList(list);
		results.setTotal((long) list.size());
	 
		return results;
	}

	public PaginatedList<Process> listProcessByUnit(Unit unit, DefaultParams params) {
		PaginatedList<Process> results = new PaginatedList<Process>();
		List<Process> list = new LinkedList<Process>();
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<ProcessUnit> cq = builder.createQuery(ProcessUnit.class);
		CriteriaQuery<Long> count = builder.createQuery(Long.class);
		
		Root<ProcessUnit> root = cq.from(ProcessUnit.class);
		Root<ProcessUnit> countRoot = count.from(ProcessUnit.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("deleted"), false));
		predicates.add(builder.equal(root.get("unit"), unit));
		
		if (params.hasTerm()) {
			String term = params.getTerm();
			Subquery<ProcessUnit> subquery = cq.subquery(ProcessUnit.class);
			Root<ProcessUnit> sqRoot = subquery.from(ProcessUnit.class);
			Join<ProcessUnit, Process> processJoin = sqRoot.join("process", javax.persistence.criteria.JoinType.INNER);
			Join<Process, Archive> archiveJoin = processJoin.join("file", javax.persistence.criteria.JoinType.LEFT);
			
			subquery.select(sqRoot.get("process").get("id"));
			
			List<Predicate> sqPredicates = new ArrayList<>();
			sqPredicates.add(builder.equal(sqRoot.get("deleted"), false));

			Predicate name = Util.getLikePredicate(builder, processJoin.get("name"), term);
			Predicate objectProcess = Util.getLikePredicate(builder, processJoin.get("objective"), term);
			Predicate unitName = Util.getLikePredicate(builder, sqRoot.get("unit").get("name"), term);
			Predicate fileName = builder.or(Util.getLikePredicate(builder, archiveJoin.get("name"), term));
			
			subquery.where(
					builder.equal(sqRoot.get("deleted"), false),
					builder.or(name, objectProcess, unitName, fileName)
			);
			
			predicates.add(root.get("process").get("id").in(subquery));
		}
		
		if (params.isSorting()) {
			String[] sortedBy = params.getSortedBy();
			String field = sortedBy[0];
			Boolean asc = !sortedBy[1].equals("desc");

			Expression<String> entityPredicate = this.getSortPredicate(builder, root, field);
			
			if (entityPredicate != null) {
				cq.orderBy(asc ? builder.asc(entityPredicate) : builder.desc(entityPredicate));
			}
		}

		cq.where(predicates.toArray(new Predicate[] {}));
		
		count.select(builder.count(countRoot.get("id")));
		count.where(predicates.toArray(new Predicate[] {}));
		
		int page = params.getPage();
		int pageSize = params.getPageSize();
		List<ProcessUnit> processUnits = entityManager.createQuery(cq)
				.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();
		long total = entityManager.createQuery(count).getSingleResult();
		

		for(ProcessUnit processUnit : processUnits){
			Process process = processUnit.getProcess();
			List<Unit> relatedUnits = this.listRelatedUnits(process,unit.getPlanRisk());
			List<ProcessObjective> objectives = this.listProcessObjectives(process);
			process.setRelatedUnits(relatedUnits);
			process.setAllObjectives(objectives);
			list.add(process);
		}
		results.setList(list);
		results.setTotal(total);
	 
		return results;
	}
	
	private Expression<String> getSortPredicate(CriteriaBuilder builder, Root<ProcessUnit> root, String field) {
		String[] joinFields = field.split("\\.");

		if (joinFields.length!=2) {
			return null;
		}
		
		String joinField = joinFields[0];
		String joinAttribute = joinFields[1];

		Join<ProcessUnit, Process> processJoin = root.join("process", javax.persistence.criteria.JoinType.INNER);
		Join<Process, Archive> archiveJoin = processJoin.join("file", javax.persistence.criteria.JoinType.LEFT);
		Join<Process, Unit> unitCreatorJoin = processJoin.join("unitCreator", javax.persistence.criteria.JoinType.LEFT);

		Map<String, Join<?, ?>> joinMap = Map.ofEntries(
            Map.entry("process", processJoin),
            Map.entry("file", archiveJoin),
            Map.entry("unitCreator", unitCreatorJoin)
        );

		Join<?, ?> join = joinMap.get(joinField);

		return join != null ? builder.lower(join.get(joinAttribute)) : null;
	}


	public List<ProcessLinkedToRiskBean> listProcessLinkedToRisksByPlan(PlanRisk planRisk) {
		Set<ProcessLinkedToRiskBean> processes = new HashSet<>();
		processes.addAll(this.listProcessLinkedToRisksByPlanId(planRisk.getId()));
		processes.addAll(this.listProcessObjLinkedToRisksByPlanId(planRisk.getId()));
		return new ArrayList<ProcessLinkedToRiskBean>(processes);
	}

	private List<ProcessLinkedToRiskBean> listProcessLinkedToRisksByPlanId(Long planRiskId) {
	    Criteria criteria;

        criteria = this.dao.newCriteria(RiskActivity.class);

	    criteria.add(Restrictions.eq("deleted", false))
	            .createAlias("process", "process", JoinType.INNER_JOIN)
	            .createAlias("risk", "risk", JoinType.INNER_JOIN)
	            .createAlias("process.unitCreator", "unit", JoinType.INNER_JOIN)
	            .createAlias("unit.planRisk", "planRisk", JoinType.INNER_JOIN)
	            .add(Restrictions.eq("planRisk.id", planRiskId));

	    
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("process.id"), "id")
				.add(Projections.property("process.name"), "name")
				.add(Projections.property("risk.id"), "riskId")
				.add(Projections.property("unit.id"), "unitId");

		criteria.setProjection(projList)
				.setResultTransformer(ProcessLinkedToRiskBean.class);

	    return this.dao.findByCriteria(criteria, ProcessLinkedToRiskBean.class);
	}
	
	private  List<ProcessLinkedToRiskBean> listProcessObjLinkedToRisksByPlanId(Long planRiskId) {
	    Criteria criteria;

        criteria = this.dao.newCriteria(RiskProcessObjective.class);

	    criteria.add(Restrictions.eq("deleted", false))
	    		.createAlias("processObjective", "processObjective", JoinType.INNER_JOIN)
	            .createAlias("processObjective.process", "process", JoinType.INNER_JOIN)
	            .createAlias("risk", "risk", JoinType.INNER_JOIN)
	            .createAlias("process.unitCreator", "unit", JoinType.INNER_JOIN)
	            .createAlias("unit.planRisk", "planRisk", JoinType.INNER_JOIN)
	            .add(Restrictions.eq("planRisk.id", planRiskId));

	    
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("process.id"), "id")
				.add(Projections.property("process.name"), "name")
				.add(Projections.property("risk.id"), "riskId")
				.add(Projections.property("unit.id"), "unitId");

		criteria.setProjection(projList)
				.setResultTransformer(ProcessLinkedToRiskBean.class);

	    return this.dao.findByCriteria(criteria, ProcessLinkedToRiskBean.class);
	}
	
	/**
	 * Recuperar process de uma unidade que tenham riscos relacionados
	 * 
	 * @param Unit,
	 *            instância da unidade
	 *
	 */
	public List<Process> listProcessesLinkedToRisks(Unit unit) {
		List<Long> processesIds = new ArrayList<>();
		processesIds.addAll(this.listProcessIdsLinkedToRisksByUnitId(unit.getId(), RiskActivity.class));
		processesIds.addAll(this.listProcessObjIdsLinkedToRisksByUnitId(unit.getId()));

        if (processesIds.isEmpty()) {
        	return Collections.emptyList();
        }
        
		Criteria criteria = this.dao.newCriteria(Process.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("id", processesIds));
        
		return this.dao.findByCriteria(criteria, Process.class);
	}
	
	private <E extends Serializable> List<Long> listProcessIdsLinkedToRisksByUnitId(Long unitId, Class<E> targetClass) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<Long> cq = builder.createQuery(Long.class);
	    Root<?> root = cq.from(targetClass);
	    
	    cq.select(root.get("process").get("id")).distinct(true);
	    
	    cq.where(
	    		builder.equal(root.get("deleted"), false),
	    		builder.equal(root.get("risk").get("unit").get("id"), unitId)
	    );
	    
	    List<Long> processesIds = entityManager.createQuery(cq).getResultList();

	    return processesIds;
	}
	
	private List<Long> listProcessObjIdsLinkedToRisksByUnitId(Long unitId) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
	    CriteriaQuery<Long> cq = builder.createQuery(Long.class);
	    Root<?> root = cq.from(RiskProcessObjective.class);
	    
	    cq.select(root.get("processObjective").get("process").get("id"))
	    		.distinct(true);
	    
	    cq.where(
	    		builder.equal(root.get("deleted"), false),
	    		builder.equal(root.get("risk").get("unit").get("id"), unitId)
	    );
	    
	    List<Long> processesIds = entityManager.createQuery(cq).getResultList();

	    return processesIds;
	}

	public List<Unit> listRelatedUnits(Process process, PlanRisk planRisk) {
		List<Unit> units = new LinkedList<Unit>();
		Criteria criteria = this.dao.newCriteria(ProcessUnit.class)
				.createAlias("unit", "unit")
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("process", process))
				.add(Restrictions.eq("unit.planRisk", planRisk));

				for (ProcessUnit processUnit : this.dao.findByCriteria(criteria, ProcessUnit.class)) {
					Unit unit = processUnit.getUnit();
					//unit.setUser(null);
					units.add(unit);
				}
				return units;
	}

	public List<Unit> listRelatedUnits(Process process, PlanRisk planRisk, String term) {
		List<Unit> units = new LinkedList<Unit>();
		Criteria criteria = this.dao.newCriteria(ProcessUnit.class)
				.createAlias("unit", "unit", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("process", process))
				.add(Restrictions.eq("unit.planRisk", planRisk));

		if (term != null) {
			Criterion name = Restrictions.like("unit.name", "%" + term + "%").ignoreCase();

			criteria.add(name);
		}
			
		for (ProcessUnit processUnit : this.dao.findByCriteria(criteria, ProcessUnit.class)) {
			Unit unit = processUnit.getUnit();
			//unit.setUser(null);
			units.add(unit);
		}
		return units;
	}
	
	public List<ProcessObjective> listProcessObjectives(Process process) {
		Criteria criteria = this.dao.newCriteria(ProcessObjective.class)
			.createAlias("process", "process", JoinType.INNER_JOIN)
			.add(Restrictions.eq("process", process));
		return this.dao.findByCriteria(criteria, ProcessObjective.class);
	}
	
	public boolean isLinkedRiskObjectiveProcess(ProcessObjective processObj) {		
		
	    Criteria criteria = this.dao.newCriteria(RiskProcessObjective.class);

	    criteria.createAlias("processObjective", "processObjective", JoinType.INNER_JOIN)
	    		.add(Restrictions.eq("processObjective.id", processObj.getId()));

		criteria.setMaxResults(1);

		return criteria.uniqueResult() != null;
	}

	public List<ProcessUnit> getProcessUnitsByProcess(Process process) {
		Criteria criteria = this.dao.newCriteria(ProcessUnit.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("process", process));
		return this.dao.findByCriteria(criteria, ProcessUnit.class);
	}	
	
	/**
	 * Retorna todos os processo da instituição
	 * 
	 *@Param CompanyDomain instituição
	 *
	 * @return PaginatedList<Process>  lista de processos
	 */
	public PaginatedList<Process> listProcessByCompany(Company company) {
		
		Criteria criteria = this.dao.newCriteria(Process.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("company", company));

		Criteria count = this.dao.newCriteria(Process.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("company", company))
				.setProjection(Projections.countDistinct("id"));
		
		PaginatedList<Process> results = new PaginatedList<Process>();
		results.setList(this.dao.findByCriteria(criteria, Process.class));
		results.setTotal((Long) count.uniqueResult());
	 
		return results;
	}

	/**
	 * Retorna todos os processo do plano de risco
	 * 
	 * @Param PlanRisk plano
	 *
	 * @return PaginatedList<Process>  lista de processos
	 */
	public PaginatedList<Process> listProcessByPlan(PlanRisk planRisk, List<Long> excludedIds,
			Integer page, Integer pageSize, String term) {
		PaginatedList<Process> results = new PaginatedList<Process>();
		
		Criteria criteria = this.dao.newCriteria(Process.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("unitCreator","unit")
				.add(Restrictions.eq("unit.planRisk", planRisk));

		Criteria count = this.dao.newCriteria(Process.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("unitCreator","unit")
				.add(Restrictions.eq("unit.planRisk", planRisk))
				.setProjection(Projections.countDistinct("id"));
		
		if (excludedIds != null) {
			criteria.add(Restrictions.not(Restrictions.in("id", excludedIds)));
			count.add(Restrictions.not(Restrictions.in("id", excludedIds)));
		}
		
		if (term != null) {
			criteria.add(Restrictions.like("name", "%" + term + "%").ignoreCase());
			count.add(Restrictions.like("name", "%" + term + "%").ignoreCase());
		}
		
		criteria.setFirstResult((page-1) * pageSize)
				.setMaxResults(pageSize);
		count.setProjection(Projections.countDistinct("id"));
		
		List<Process> processes = this.dao.findByCriteria(criteria, Process.class);
	
		results.setList(processes);
		results.setTotal((Long) count.uniqueResult());
	 
		return results;
	}

	/**
	 * Retorna um processo
	 *
	 * @param id
	 * 		Id do processo
	 */
	public Process retrieveProcessById(Long id) {
		Criteria criteria = this.dao.newCriteria(Process.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));
			criteria.setMaxResults(1);

		Process process = (Process) criteria.uniqueResult();
		List<Unit> relatedUnits = this.listRelatedUnits(process, process.getUnitCreator().getPlanRisk());
		List<ProcessObjective> objectives = this.listProcessObjectives(process);
		process.setRelatedUnits(relatedUnits);
		process.setAllObjectives(objectives);

		return process;
	}
	
	

	public void deleteProcess(Process process) {
			
		process.setDeleted(true);
		this.persist(process);
		List<ProcessUnit> processUnits = this.getProcessUnitsByProcess(process);
		for (ProcessUnit processUnit : processUnits) {
			processUnit.setDeleted(true);
			this.persist(processUnit);
		}

	}

	public PaginatedList<ProcessObjective> listObjectivesByPlan(PlanRisk planRisk, List<Long> excludedIds, Integer page,
			Integer pageSize, String term) {
		PaginatedList<ProcessObjective> results = new PaginatedList<ProcessObjective>();

		Criteria criteria = this.dao.newCriteria(ProcessObjective.class)
				.createAlias("process","process")
				.createAlias("process.unitCreator","unit")
				.add(Restrictions.eq("process.deleted", false))
				.add(Restrictions.eq("unit.planRisk", planRisk));

		Criteria count = this.dao.newCriteria(ProcessObjective.class)
				.createAlias("process","process")
				.createAlias("process.unitCreator","unit")
				.add(Restrictions.eq("process.deleted", false))
				.add(Restrictions.eq("unit.planRisk", planRisk))
				.setProjection(Projections.countDistinct("id"));

		if (excludedIds != null) {
			criteria.add(Restrictions.not(Restrictions.in("id", excludedIds)));
			count.add(Restrictions.not(Restrictions.in("id", excludedIds)));
		}
		
		if (term != null) {
			Criterion objectiveNameCriterion = Restrictions.like("description", "%" + term + "%").ignoreCase();
			Criterion processNameCriterion = Restrictions.like("process.name", "%" + term + "%").ignoreCase();
			Criterion orCriterion = Restrictions.or(objectiveNameCriterion, processNameCriterion);
			criteria.add(orCriterion);
			count.add(orCriterion);
		}
		
		criteria.setFirstResult((page-1) * pageSize)
				.setMaxResults(pageSize);
		count.setProjection(Projections.countDistinct("id"));
		
		List<ProcessObjective> processes = this.dao.findByCriteria(criteria, ProcessObjective.class);
	
		results.setList(processes);
		results.setTotal((Long) count.uniqueResult());
	 
		return results;
	}
}
