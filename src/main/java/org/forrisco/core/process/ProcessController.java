package org.forrisco.core.process;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.Consts;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.RiskBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Matheus Nascimento
 */
@RestController
public class ProcessController extends AbstractController {

	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private ProcessBS processBS;
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private NotificationBS notificationBS;
	@Autowired
	private ArchiveBS archiveBS;
	
	protected static final String PATH =  BASEPATH +"/process";


	/**
	 * Salvar um processo em uma unidade.
	 * 
	 * @param Process
	 *            processo a ser salvo no banco de dados
	 * @return Process
	 */
	@PostMapping( PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> saveProcess(@RequestBody Process process) {
		try {
 			Unit unit = this.processBS.exists(process.getUnit().getId(), Unit.class);
			if (unit == null) {
				return this.fail("Unidade não encontrada.");
			}
			if(this.domain == null) {
				return this.fail("Instituição não definida");
			}
			process.setCompany(this.domain.get().getCompany());
			process.setUnitCreator(unit);
			this.processBS.save(process);

			ProcessUnit processUnit = new ProcessUnit();
			ProcessObjective processObj = new ProcessObjective();
			processUnit.setProcess(process);
			processUnit.setUnit(unit);
			this.processBS.save(processUnit);
			
			if (GeneralUtils.isEmpty(process.getAllObjectives())) {
				throw new IllegalStateException("O processo deve possuir pelo menos um objetivo");
			}

			for (ProcessObjective objective : process.getAllObjectives()) {	
				processObj = new ProcessObjective();
				processObj.setProcess(process);
				processObj.setDescription(objective.getDescription());

				this.processBS.saveObjective(processObj);
			}

			if (process.getRelatedUnits() != null) {
				for (Unit singleunit : process.getRelatedUnits()) {
					Unit relatedUnit = this.processBS.exists(singleunit.getId(), Unit.class);
					
					if (relatedUnit != null && !relatedUnit.isDeleted()) {
						processUnit = new ProcessUnit();
						processUnit.setProcess(process);
						processUnit.setUnit(relatedUnit);
						this.processBS.save(processUnit);
			
						sendMail(relatedUnit, unit, relatedUnit.getUser(), process);
					}
				}
			}

			return this.success(process);
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Retorna Processos de uma unidade.
	 * 
	 * @param Unit
	 *            Id da unidade.
	 * @return <PaginatedList> Process
	 */
	@GetMapping( PATH + "/{id}")
	public ResponseEntity<?> listProcesses(@PathVariable Long id, @ModelAttribute DefaultParams params) {
		try {
			Unit unit = this.processBS.exists(id, Unit.class);
			PaginatedList<Process> process = this.processBS.listProcessByUnit(unit, params);
			return this.success(process);
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Retorna processos da unidade que possuam riscos relacionados.
	 * 
	 * 
	 * @param Long 
	 * 			id	da unidade
	 * 
	 * @return PaginatedList<RiskProcess>
	 * 			Lista de Processos 
	 */
	@GetMapping(PATH + "/list-linked-to-risks")
	public ResponseEntity<?> listProcessesLinkedToRisks(@RequestParam Long unitId) {
		try {
			Unit unit = this.processBS.exists(unitId, Unit.class);
			if (unit == null || unit.isDeleted()) {
				return this.fail("Unidade não encontrada");
			}
			
			List<Process> list = this.processBS.listProcessesLinkedToRisks(unit);
			
			return this.success(new ListWrapper<Process>(list));
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}


	@GetMapping(PATH + "/list-linked-to-risks-by-plan")
	public ResponseEntity<?> listLinkedToRisksByPlan(@RequestParam Long planId) {
		try {
			
			PlanRisk planRisk = this.processBS.exists(planId, PlanRisk.class);
			if (planRisk == null || planRisk.isDeleted()) {
				return this.fail("Plano de Risco não encotrado");
			}
			
			List<ProcessLinkedToRiskBean> list = this.processBS.listProcessLinkedToRisksByPlan(planRisk);
			
			return this.success(new ListWrapper<ProcessLinkedToRiskBean>(list));
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	/**
	 * Retorna um processo por Id.
	 * 
	 * @param id
	 *            Id do processo
	 * @return Process
	 */
	@GetMapping( PATH + "/process/{id}")
	public ResponseEntity<?> retrieveProcess(@PathVariable Long id) {
		try {
			Process process = this.processBS.retrieveProcessById(id);
			if (process == null) {
				return this.fail("A processo solicitado não foi encontrado.");
			} else {
				return this.success(process);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}


	/**
	 * Retorna processos do Plano de Risco.
	 * 
	 * 
	 * @param Long 
	 * 			id	do plano de risco
	 * 
	 * @return PaginatedList<RiskProcess>
	 * 			Lista de Processos 
	 */
	@GetMapping(PATH + "/list-by-plan")
	public ResponseEntity<?> listProcessByPlan(
			@RequestParam Long planId,
			@RequestParam(required = false) List<Long> excludedIds,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String term) {

		try {
			
			PlanRisk planRisk = this.processBS.exists(planId, PlanRisk.class);
			if (planRisk == null || planRisk.isDeleted()) {
				return this.fail("Plano de Risco não encotrado");
			}
			
			PaginatedList<Process> list= this.processBS.listProcessByPlan(planRisk, excludedIds, page, pageSize, term);

			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	
	/**
	 * Deleta um processos
	 * 
	 * @param id
	 *            Id do processo
	 */
	@DeleteMapping(PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteProcesses(@PathVariable Long id) {
		try {
			Process process = this.processBS.exists(id, Process.class);
			if (process == null) {
				return this.fail("Processo não encontrado.");
			}
			
			if (this.riskBS.hasLinkedRiskActivity(process)) {
				return this.fail("Processo vinculado a um Risco. É necessário deletar a vinculação no Risco para depois excluir o processo.");
			}
			if (this.riskBS.hasLinkedRiskObjectiveProcess(process)) {
				return this.fail("Possui Objetivo(s) de processo vinculado a um Risco. É necessário deletar a vinculação no Risco para depois excluir o processo.");
			}
			
			this.processBS.deleteProcess(process);
				
			return this.success(process);

		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}	

	/**
	 * Alterar um processo
	 * 
	 * @param Process
	 *            processo a ser alterado no banco de dados
	 */
	@PutMapping(PATH)
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> updateProcess(@RequestBody Process process) {
		try {
			processBS.validateProcess(process);

			Process existent = this.processBS.exists(process.getId(), Process.class);
			if (existent == null) {
				return this.fail("Processo não encontrado.");
			}

			List<ProcessUnit> processUnitsExistent = this.processBS.getProcessUnitsByProcess(process);
			Map<Long, ProcessUnit> processUnitsExistentMap = mapProcessUnits(processUnitsExistent);

			List<ProcessUnit> newProcessUnits = updateProcessUnits(process, processUnitsExistentMap);
			persistProcessUnits(newProcessUnits, processUnitsExistentMap, process);

			List<ProcessObjective> allObjectives = this.processBS.listProcessObjectives(process);
			Map<Long, ProcessObjective> processObjectivesExistentMap = mapProcessObjectives(allObjectives);

			List<ProcessObjective> newProcessObjectives = updateProcessObjectives(process, processObjectivesExistentMap);
			persistProcessObjectives(newProcessObjectives, processObjectivesExistentMap, process);

			updateAndPersistProcess(process, existent);

			return this.success(existent);
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	private Map<Long, ProcessUnit> mapProcessUnits(List<ProcessUnit> processUnitsExistent) {
		Map<Long, ProcessUnit> map = new HashMap<>();
		for (ProcessUnit processUnit : processUnitsExistent) {
			map.put(processUnit.getUnit().getId(), processUnit);
		}
		return map;
	}

	private List<ProcessUnit> updateProcessUnits(Process process, Map<Long, ProcessUnit> processUnitsExistentMap) throws EmailException {
		List<ProcessUnit> newProcessUnits = new LinkedList<>();
		for (Unit relatedUnit : process.getRelatedUnits()) {
			ProcessUnit processUnit = processUnitsExistentMap.get(relatedUnit.getId());
			if (processUnit == null) {
				processUnit = createNewProcessUnit(process, relatedUnit);
				newProcessUnits.add(processUnit);
				notifyNewUnitRelation(process, relatedUnit);
			} else if (processUnit.isDeleted()) {
				processUnit.setDeleted(false);
				newProcessUnits.add(processUnit);
			}
			processUnitsExistentMap.remove(relatedUnit.getId());
		}
		return newProcessUnits;
	}

	private void persistProcessUnits(List<ProcessUnit> newProcessUnits, Map<Long, ProcessUnit> processUnitsExistentMap,
			Process process) {
		for (ProcessUnit processUnit : newProcessUnits) {
			this.processBS.persist(processUnit);
		}

		for (ProcessUnit processUnit : processUnitsExistentMap.values()) {
			if (!processUnit.getUnit().getId().equals(process.getUnit().getId())) {
				processUnit.setDeleted(true);
				this.processBS.persist(processUnit);
			}
		}
	}

	private ProcessUnit createNewProcessUnit(Process process, Unit relatedUnit) {
		ProcessUnit processUnit = new ProcessUnit();
		processUnit.setProcess(process);
		processUnit.setUnit(relatedUnit);
		return processUnit;
	}

	private void notifyNewUnitRelation(Process process, Unit relatedUnit) throws EmailException {
		Unit unit = this.processBS.exists(process.getUnit().getId(), Unit.class);
		sendMail(relatedUnit, unit, relatedUnit.getUser(), process);
	}

	private Map<Long, ProcessObjective> mapProcessObjectives(List<ProcessObjective> allObjectives) {
		Map<Long, ProcessObjective> map = new HashMap<>();
		for (ProcessObjective obj : allObjectives) {
			map.put(obj.getId(), obj);
		}
		return map;
	}

	private List<ProcessObjective> updateProcessObjectives(Process process,
			Map<Long, ProcessObjective> processObjectivesExistentMap) {
		List<ProcessObjective> newProcessObjectives = new LinkedList<>();
		for (ProcessObjective relatedObj : process.getAllObjectives()) {
			ProcessObjective processObj = processObjectivesExistentMap.get(relatedObj.getId());
			if (processObj == null) {
				processObj = createNewProcessObjective(process, relatedObj);
				newProcessObjectives.add(processObj);
			} else {
				processObj.setDescription(relatedObj.getDescription());
				this.processBS.persist(processObj);
				processObjectivesExistentMap.remove(relatedObj.getId());
			}
		}
		return newProcessObjectives;
	}

	private ProcessObjective createNewProcessObjective(Process process, ProcessObjective relatedObj) {
		ProcessObjective processObj = new ProcessObjective();
		processObj.setProcess(process);
		processObj.setDescription(relatedObj.getDescription());
		return processObj;
	}

	private void persistProcessObjectives(List<ProcessObjective> newProcessObjectives,
			Map<Long, ProcessObjective> processObjectivesExistentMap, Process process) {
		for (ProcessObjective processObj : newProcessObjectives) {
			this.processBS.persist(processObj);
		}

		for (ProcessObjective processObj : processObjectivesExistentMap.values()) {
			if (this.processBS.isLinkedRiskObjectiveProcess(processObj)) {
				throw new IllegalStateException(
						"Objetivo vinculado a um Risco. É necessário deletar a vinculação no Risco para depois excluir o objetivo.");
			}

			if (!process.getAllObjectives().contains(processObj)) {
				this.processBS.deleteObjective(processObj);
			}
		}
	}

	private void updateAndPersistProcess(Process process, Process existent) {
		Archive file = null;
		if (!GeneralUtils.isEmpty(process.getFileLink())) {
			file = archiveBS.getByFileLink(process.getFileLink());
		}
		existent.setFileLink(process.getFileLink());
		existent.setFile(file);
		existent.setName(process.getName());
		this.processBS.persist(existent);
	}

	/**
	 * Retorna Processos da instituição atual
	 * 
	 * @return <PaginatedList> Process
	 */
	@GetMapping( PATH + "")
	public ResponseEntity<?> listAllProcesses() {
		try {
			if(this.domain == null) {
				return this.fail("Instituição não definida");
			}
			PaginatedList<Process> process= this.processBS.listProcessByCompany(this.domain.get().getCompany());
			return this.success(process);
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	
	@GetMapping(PATH + "/list-objetives-by-plan")
	public ResponseEntity<?> listObjectivesByPlan(
			@RequestParam Long planId,
			@RequestParam(required = false) List<Long> excludedIds,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String term) {

		try {
			if (page == null || page < 1) {
				page = 1;
			}
			if (pageSize == null) {
				pageSize = Consts.MED_PAGE_SIZE;
			}
			
			PlanRisk planRisk = this.processBS.exists(planId, PlanRisk.class);
			if (planRisk == null || planRisk.isDeleted()) {
				return this.fail("Plano de Risco não encotrado");
			}
			
			PaginatedList<ProcessObjective> list= this.processBS.listObjectivesByPlan(planRisk, excludedIds, page, pageSize, term);
			
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	/**
	 * Envia email de notificação de nova unidade relacionado a um processo
	 * @throws EmailException 
	 *	@Param Unit relatedUnit
	 *  			Unidade que está sendo relacionada
	 *  @Param Unit unit
	 *  			Unidade do processo
	 *  @Param User user
	 *  		Responsável da unidade relacionada
	 *  @Param Process process
	 *  		Processo relacionado
	 */
	private void sendMail(Unit relatedUnit, Unit unit, User user, Process process) throws EmailException {
		String texto="Prezado(a) "+user.getName()+
				", A sua unidade ["+relatedUnit.getName()+"] foi relacionada ao Processo ["+process.getName()+
				"] com o(s) objetivo(s): ["+process.getProcessObjectivesDescriptionsString()+"] da unidade ["+unit.getName()+"]. O responsável"+
				" por essa unidade é o(a) "+unit.getUser().getName()+
				". Segue em anexo o Processo na qual a sua unidade foi relacionada.";						
		
		String url=this.domain.get().getBaseUrl()+"/#/forrisco/plan-risk/"+String.valueOf(relatedUnit.getPlanRisk().getId())+"/unit/"+String.valueOf(relatedUnit.getId())+"/info";

		if( process.getFile() !=null) {
			this.notificationBS.sendAttachedNotificationEmail(NotificationType.FORRISCO_PROCESS_CREATED, texto, "aux", user, url, process.getFile());
		}else {
			this.notificationBS.sendNotificationEmail(NotificationType.FORRISCO_PROCESS_CREATED, texto, "aux", user, url);
		}
			
	}
}
