package org.forpdi.dashboard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Restrictions;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.dashboard.goalsinfo.GoalsInfoBS;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Classe auxiliar na geracao da tabela de informacoes das metas
 *  
 * @author Erick Alves
 *
 */
@Component
public class GoalsInfoTableHelper {
	@Autowired
	private HibernateDAO dao;
	@Autowired
	private GoalsInfoBS goalsInfoBS;
	
	/**
	 * Recebe uma lista de metas e seta os indicadores em levelParent
	 * 
	 * @param goals 
	 */
	public void setIndicators(final List<StructureLevelInstance> goals) {
		if (goals.size() == 0) {
			return;
		}
		List<Long> parentIds = new ArrayList<>(goals.size());
		for (StructureLevelInstance goal : goals) {
			parentIds.add(goal.getParent());
		}
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
			.add(Restrictions.in("id", parentIds));
		List<StructureLevelInstance> parents = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		Map<Long, StructureLevelInstance> parentsMap = new HashMap<>(goals.size());
		for (StructureLevelInstance parent : parents) {
			parentsMap.put(parent.getId(), parent);
		}
		for (StructureLevelInstance goal : goals) {
			goal.setLevelParent(parentsMap.get(goal.getParent()));
		}
	}
	
	/**
	 * Recebe uma lista de metas e seta os objetivos em no parent de levelParent
	 * 
	 * @param goals 
	 */
	public void setObjectives(final List<StructureLevelInstance> goals) {
		if (goals.size() == 0) {
			return;
		}
		List<Long> parentIds = new ArrayList<>(goals.size());
		for (StructureLevelInstance goal : goals) {
			parentIds.add(goal.getLevelParent().getParent());
		}
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
			.add(Restrictions.in("id", parentIds));
		List<StructureLevelInstance> parents = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		Map<Long, StructureLevelInstance> parentsMap = new HashMap<>(goals.size());
		for (StructureLevelInstance parent : parents) {
			parentsMap.put(parent.getId(), parent);
		}
		for (StructureLevelInstance goal : goals) {
			goal.getLevelParent()
				.setLevelParent(parentsMap.get(goal.getLevelParent().getParent()));
		}
	}

	public ArrayList<GoalsInfoTable> generateGoalsInfo(List<StructureLevelInstance> goals) {
		ArrayList<GoalsInfoTable> goalsInfo = new ArrayList<>(goals.size());
		for (StructureLevelInstance goal : goals) {
			GoalsInfoTable goalInfo = this.goalsInfoBS.generateGoalInfo(goal);
			goalsInfo.add(goalInfo);
		}
		return goalsInfo;
	}

}
