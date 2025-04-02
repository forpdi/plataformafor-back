package org.forrisco.risk;

import java.util.Collections;
import java.util.List;

import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.utils.SanitizeUtil;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.forrisco.risk.contingency.Contingency;
import org.forrisco.risk.contingency.ContingencyBS;
import org.forrisco.risk.preventiveaction.PreventiveAction;
import org.forrisco.risk.preventiveaction.PreventiveActionBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RiskReplicationBS extends HibernateBusiness {
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private PreventiveActionBS preventiveActionBS;
	@Autowired
	private ContingencyBS contingencyBS;
	@Autowired
	private UnitBS unitBS;

	
	public void replicateRisksInUnits(Risk riskBase, List<Long> targetUnitIds) {
		Risk existent = dao.exists(riskBase.getId(), Risk.class);
		if (existent == null) {
			throw new IllegalArgumentException("Risk not found");
		}

		for (Long unitId : targetUnitIds) {
			Risk riskReplicated = replicateRisk(riskBase, unitId);
			replicatePreventiveActions(riskBase, riskReplicated);
			replicateContingencies(riskBase, riskReplicated);
		}
	}
	
	private Risk replicateRisk(Risk riskBase, Long unitId) {
		Unit unit = unitBS.retrieveUnitById(unitId);
		
		Risk riskReplicated = Risk.from(riskBase);
		riskReplicated.setReason(SanitizeUtil.sanitize(riskReplicated.getReason()));
		riskReplicated.setResult(SanitizeUtil.sanitize(riskReplicated.getResult()));

		riskReplicated.setActivities(new PaginatedList<>());
		riskReplicated.setProcessObjectives(Collections.emptyList());
		riskReplicated.setAxes(new PaginatedList<>());
		riskReplicated.setStrategies(new PaginatedList<>());
		riskReplicated.setIndicators(new PaginatedList<>());
		riskReplicated.setGoals(new PaginatedList<>());
		
		riskReplicated.setSharedUnits(riskBase.getSharedUnits());
		riskReplicated.setUnit(unit);
		riskBS.createNewRisk(riskReplicated);

		return riskReplicated;
	}
	
	private void replicatePreventiveActions(Risk riskBase, Risk riskReplicated) {
		PaginatedList<PreventiveAction> actions = preventiveActionBS.listActionByRisk(riskBase);
		for (PreventiveAction actionBase : actions.getList()) {
			PreventiveAction action = new PreventiveAction(actionBase);
			action.setRisk(riskReplicated);
			action.setUser(riskReplicated.getUser());
			action.setManager(riskReplicated.getManager());
			preventiveActionBS.saveAction(action);
		}
	}
	
	private void replicateContingencies(Risk riskBase, Risk riskReplicated) {
		PaginatedList<Contingency> contingencies = contingencyBS.listContingenciesByRisk(riskBase);
		for (Contingency contingencyBase : contingencies.getList()) {
			Contingency contingency = new Contingency(contingencyBase);
			contingency.setRisk(riskReplicated);
			contingency.setUser(riskReplicated.getUser());
			contingency.setManager(riskReplicated.getManager());
			contingencyBS.saveContingency(contingency);
		}
	}

}
