package org.forpdi.planning.structure.goals.history;

import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.planning.structure.goals.history.GoalJustificationHistory.JustificationAndReachedValue;
import org.forpdi.security.auth.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GoalJustificationHistoryBS extends HibernateBusiness {
	
	@Autowired
	private UserSession userSession;

	public void saveHistory(JustificationAndReachedValue oldJustificationAndReachedValue,
			JustificationAndReachedValue newJustificationAndReachedValue, StructureLevelInstance levelInstance) {
		GoalJustificationHistory goalJustificationHistory = new GoalJustificationHistory();
		goalJustificationHistory.setJustification(oldJustificationAndReachedValue.getJustification());
		goalJustificationHistory.setReachedValue(oldJustificationAndReachedValue.getReachedValue());
		goalJustificationHistory.setUser(userSession.getUser());
		goalJustificationHistory.setLevelInstance(levelInstance);
		persist(goalJustificationHistory);
	}

	public List<GoalJustificationHistory> listByLevelInstance(long levelInstanceId) {
		Criteria criteria = this.dao.newCriteria(GoalJustificationHistory.class)
				.createAlias("levelInstance", "levelInstance")
				.add(Restrictions.eq("levelInstance.id", levelInstanceId));

		return this.dao.findByCriteria(criteria, GoalJustificationHistory.class);
	}
}
