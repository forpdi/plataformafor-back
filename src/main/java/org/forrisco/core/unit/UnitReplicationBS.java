package org.forrisco.core.unit;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.forpdi.core.common.PaginatedList;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.process.ProcessUnit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.contingency.Contingency;
import org.forrisco.risk.contingency.ContingencyBS;
import org.forrisco.risk.incident.Incident;
import org.forrisco.risk.incident.IncidentBS;
import org.forrisco.risk.links.RiskActivity;
import org.forrisco.risk.links.RiskProcessObjective;
import org.forrisco.risk.links.RiskStrategy;
import org.forrisco.risk.monitor.Monitor;
import org.forrisco.risk.monitor.MonitorBS;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnitReplicationBS {

	@Autowired
	private UnitBS unitBS;
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private PreventiveActionBS preventiveActionBS;
	@Autowired
	private MonitorBS monitorBS;
	@Autowired
	private IncidentBS incidentBS;
	@Autowired
	private ContingencyBS contingencyBS;
	@Autowired
	private ProcessBS processBS;

	private PaginatedList<Risk> getReplicatedRisks(Map<Long, Long> replicatesUnitsId, Unit unit) {

		PaginatedList<Risk> risks = new PaginatedList<>();

		for (Entry<Long, Long> entry : replicatesUnitsId.entrySet()) {
			Long key = entry.getKey();
			Long value = entry.getValue();

			if (value.equals(unit.getId())) {
				Unit original = this.unitBS.exists(key, Unit.class);
				if (original == null || original.isDeleted()) {
					continue;
				}
				risks = this.riskBS.listRiskByUnit(original);
				break;
			}
		}
		
		return risks;
	}

	public void replicateUnitRisk(Map<Long, Long> replicatesUnitsId, Unit unit) {
		PaginatedList<Risk> risks = getReplicatedRisks(replicatesUnitsId, unit);

		for (Risk risk : risks.getList()) {
			Risk riskReplicated = Risk.from(risk);
			riskReplicated.setUnit(unit);
			riskBS.saveRisk(riskReplicated);

			replicateEntities(risk, riskReplicated);
		}
	}

	private void replicateEntities(Risk riskBase, Risk riskReplicated) {
		replicateActions(riskBase, riskReplicated);
		replicateMonitors(riskBase, riskReplicated);
		replicateIncidents(riskBase, riskReplicated);
		replicateContingencies(riskBase, riskReplicated);
		replicateStrategies(riskBase, riskReplicated);
		replicateActivities(riskBase, riskReplicated);
		replicateObjectives(riskBase, riskReplicated);
	}

	private void replicateActions(Risk riskBase, Risk riskReplicated) {
		PaginatedList<PreventiveAction> actions = preventiveActionBS.listPreventiveActionByRisk(riskBase);
		for (PreventiveAction action : actions.getList()) {
			PreventiveAction act = new PreventiveAction(action);
			act.setRisk(riskReplicated);
			preventiveActionBS.saveAction(act);
		}
	}

	private void replicateMonitors(Risk riskBase, Risk riskReplicated) {
		PaginatedList<Monitor> monitors = monitorBS.listMonitorByRisk(riskBase);
		for (Monitor monitor : monitors.getList()) {
			monitor = new Monitor(monitor);
			monitor.setRisk(riskReplicated);
			monitorBS.saveMonitor(monitor);
			// nao esta salvando no banco
		}
	}

	private void replicateIncidents(Risk riskBase, Risk riskReplicated) {
		PaginatedList<Incident> incidents = incidentBS.listIncidentsByRisk(riskBase);
		for (Incident incident : incidents.getList()) {
			Incident icd = new Incident(incident);
			icd.setRisk(riskReplicated);
			incidentBS.saveIncident(icd);
		}
	}

	private void replicateContingencies(Risk riskBase, Risk riskReplicated) {
		PaginatedList<Contingency> contingencies = contingencyBS.listContingenciesByRisk(riskBase);
		for (Contingency contingency : contingencies.getList()) {
			Contingency cont = new Contingency(contingency);
			cont.setRisk(riskReplicated);
			contingencyBS.saveContingency(cont);
		}
	}

	private void replicateStrategies(Risk riskBase, Risk riskReplicated) {
		PaginatedList<RiskStrategy> strategies = riskBS.listRiskStrategy(riskBase);
		if (strategies != null) {
			for (RiskStrategy strategy : strategies.getList()) {
				RiskStrategy str = new RiskStrategy();
				str.setLinkFPDI(strategy.getLinkFPDI());
				str.setName(strategy.getName());
				str.setRisk(riskReplicated);
				str.setStructure(strategy.getStructure());
				riskBS.persist(str);
			}
		}
	}

	private void replicateActivities(Risk riskBase, Risk riskReplicated) {
		PaginatedList<RiskActivity> activities = riskBS.listRiskActivity(riskBase);
		if (activities != null) {
			for (RiskActivity activity : activities.getList()) {
				RiskActivity act = new RiskActivity(activity);
				act.setRisk(riskReplicated);
				act.setProcess(activity.getProcess());
				riskBS.persist(act);
			}
		}
	}

	private void replicateObjectives(Risk riskBase, Risk riskReplicated) {
		List<RiskProcessObjective> objectives = riskBS.listRiskProcessObjectives(riskBase);
		if (objectives != null) {
			for (RiskProcessObjective objective : objectives) {
				RiskProcessObjective newObjective = new RiskProcessObjective();
				newObjective.setRisk(riskReplicated);
				newObjective.setProcessObjective(objective.getProcessObjective());
				riskBS.persist(newObjective);
			}
		}
	}

	public void replicateProcess(Unit unit, Map<Long, Long> duplicatesUnitsId) {

		unit = this.unitBS.exists(unit.getId(), Unit.class);
		if (unit == null || unit.isDeleted()) {
			return;
		}

		Unit original = null;
		for(Entry<Long, Long> entry : duplicatesUnitsId.entrySet()) {
		    Long key = entry.getKey();
		    Long value = entry.getValue();

		    if(value.equals(unit.getId())) {
		    	original = this.unitBS.exists(key, Unit.class);
		    	break;
		    }
		}

		if(original!=null) {

			PaginatedList<Process> processes= this.processBS.listProcessByUnit(original);

			for(Process process : processes.getList()) {

				ProcessUnit processUnit = new ProcessUnit();
				Unit mapedUnit = this.unitBS.exists(duplicatesUnitsId.get(original.getId()), Unit.class);

				processUnit.setUnit(mapedUnit);
				processUnit.setProcess(process);
				process.setRelatedUnits(null);
				this.processBS.save(processUnit);
			}

		}

	}
}
