package org.forpdi.dashboard.goalsinfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.time.DateUtils;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.JoinType;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.PolarityMapHelper;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.Polarity;
import org.forpdi.planning.structure.StructureInstanceAttributeHelper;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

@Service
public class GoalsInfoBS extends HibernateBusiness {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	@Autowired
	private StructureInstanceAttributeHelper structureInstanceAttributeHelper;
	@Autowired
	private PolarityMapHelper polarityMapHelper;
	@Autowired
	private EntityManager entityManager;
	

	public synchronized Long getNumberOfGoalsLate(List<StructureLevelInstance> goals) {
		GoalsInfo info = this.calculateAdminGoalsInfo(goals);
		return (info != null && info.getLate() != null) ? info.getLate() : 0L;
	}

	public Long getNumberOfGoalsReached(List<StructureLevelInstance> goals) {
		GoalsInfo info = this.calculateAdminGoalsInfo(goals);
		return (info.getReached() + info.getAboveExpected());
	}
	
	public GoalsInfo calculateAdminGoalsInfo(List<StructureLevelInstance> goals) {
        GoalsInfo info = new GoalsInfo();
        long totalGoals = goals.size();
        if (totalGoals > 0) {
            structureInstanceAttributeHelper.setAttributesToGoalsInfoCalc(goals);
            Map<Long, AttributeInstance> polarityMap = this.polarityMapHelper.generatePolarityMap(goals);
            
            long inDay = 0;
            long late = 0;
            long belowMin = 0;
            long belowExp = 0;
            long reached = 0;
            long aboveExp = 0;
            long notStarted = 0;
            long finished = 0;
            long closeToMat = 0;

            for (StructureLevelInstance goal : goals) {
                Date finish = new Date();
                Double expected = null;
                Double reach = null;
                Double max = null;
                Double min = null;
                
                List<AttributeInstanceToGoalsInfo> attributeInstanceList = goal.getAttributeInstaceToGoalsInfoList();

                if (attributeInstanceList != null) {
                    for (AttributeInstanceToGoalsInfo attrInstance : attributeInstanceList) {
                        if (attrInstance.isFinishDate()) {
                            finish = attrInstance.getValueAsDate();
                        } else if (attrInstance.isExpectedField()) {
                            expected = attrInstance.getValueAsNumber();
                        } else if (attrInstance.isReachedField()) {
                            reach = attrInstance.getValueAsNumber();
                        } else if (attrInstance.isMinimumField()) {
                            min = attrInstance.getValueAsNumber();
                        } else if (attrInstance.isMaximumField()) {
                            max = attrInstance.getValueAsNumber();
                        }
                    }
                }

                AttributeInstance polarity = polarityMap.get(goal.getId());

                Date today = new Date();
                if (reach == null && ((min == null && expected == null && max == null) || finish.after(today))) {
                    notStarted++;
                } else {
                    if (goal.isClosed()) {
                        finished++;
                    } else if (finish.before(today)) {
                        late++;
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(today);
                        calendar.add(Calendar.DATE, 10);
                        if (calendar.getTime().after(finish) || DateUtils.isSameDay(calendar.getTime(), finish)) {
                            closeToMat++;
                        } else {
                            inDay++;
                        }
                    }
                    if (Polarity.polarityComparison(polarity, min, reach) || (reach == null && finish.before(today))) {
                        belowMin++;
                    } else if (Polarity.polarityComparison(polarity, expected, reach)) {
                        belowExp++;
                    } else if (Polarity.polarityComparison(polarity, max, reach)
                            || (reach != null && expected != null && (Double.compare(reach, expected) == 0))) {
                        reached++;
                    } else if (Polarity.polarityComparison(polarity, reach, max)
                            || (reach != null && max != null && (Double.compare(reach, max) == 0))) {
                        aboveExp++;
                    }
                }
            }

            info.setInDay(inDay);
            info.setLate(late);
            info.setBelowMininum(belowMin);
            info.setBelowExpected(belowExp);
            info.setReached(reached);
            info.setAboveExpected(aboveExp);
            info.setNotStarted(notStarted);
            info.setFinished(finished);
            info.setCloseToMaturity(closeToMat);
            info.setTotal(totalGoals);
            
            info.setInDayPercentage((double) inDay * 100.0 / totalGoals);
            info.setLatePercentage((double) late * 100.0 / totalGoals);
            info.setBelowMinimumPercentage((double) belowMin * 100.0 / totalGoals);
            info.setBelowExpectedPercentage((double) belowExp * 100.0 / totalGoals);
            info.setReachedPercentage((double) reached * 100.0 / totalGoals);
            info.setAboveExpectedPercentage((double) aboveExp * 100.0 / totalGoals);
            info.setNotStartedPercentage((double) notStarted * 100.0 / totalGoals);
            info.setFinishedPercentage((double) finished * 100.0 / totalGoals);
            info.setCloseToMaturityPercentage((double) closeToMat * 100.0 / totalGoals);
        }

        return info;
    }
	
	public GoalsInfo retrieveGoalsInfoByPlanMacro(long planMacroId) {
		Criteria criteria = this.dao.newCriteria(GoalsInfo.class);
		criteria.createAlias("planMacro", "planMacro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("planMacro.id", planMacroId));

		return (GoalsInfo) criteria.uniqueResult();
	}

	public GoalsInfoOverview getGoalsInfoOverviewByPlanMacro(PlanMacro planMacro) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GoalsInfoOverview> cq = cb.createQuery(GoalsInfoOverview.class);
		Root<GoalsInfo> root = cq.from(GoalsInfo.class);
		
		cq.where(cb.equal(root.get("planMacro").get("id"), planMacro.getId()));
		
		return getGoalsInfoOverview(cq, field -> cb.sum(root.get(field)));
	}

	public GoalsInfoOverview getGoalsInfoOverviewByCompany(Company company) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<GoalsInfoOverview> cq = cb.createQuery(GoalsInfoOverview.class);
		Root<GoalsInfo> root = cq.from(GoalsInfo.class);
		
		cq.where(cb.equal(root.get("planMacro").get("company"), company));
		
		return getGoalsInfoOverview(cq, field -> cb.sum(root.get(field)));
	}
	
	private GoalsInfoOverview getGoalsInfoOverview(CriteriaQuery<GoalsInfoOverview> cq, Function<String, Selection<?>> getProjection) {
		final String[] goalsInfoFields = {
				"inDay",
				"late",
				"belowMininum",
				"belowExpected",
				"reached",
				"aboveExpected",
				"notStarted",
				"finished",
				"closeToMaturity",
				"total",
		};
		
		List<Selection<?>> selections = new ArrayList<>();
		
		for (String field : goalsInfoFields) {
			selections.add(getProjection.apply(field));
		}
		
		cq.multiselect(selections);

		GoalsInfoOverview overview = entityManager.createQuery(cq).getSingleResult();
		
		if (overview == null) {
			return new GoalsInfoOverview();
		}
		return overview;
	}

	public GoalsInfoTable generateGoalInfo(StructureLevelInstance goal) {
		GoalsInfoTable goalInfo = new GoalsInfoTable();

		goalInfo.setGoalName(goal.getName());
		goalInfo.setIndicatorName(goal.getLevelParent().getName());
		goalInfo.setObjectiveName(goal.getLevelParent().getLevelParent().getName());
		goalInfo.setLastModification(DATE_FORMAT.format(goal.getModification()));
		goalInfo.setDeadLineStatus(goal.getDeadlineStatus());
		goalInfo.setProgressStatus(goal.getProgressStatus());

		for (AttributeInstance attrInstance : goal.getAttributeInstanceList()) {
			processAttributeInstance(goalInfo, attrInstance, goal.isClosed());
		}

		return goalInfo;
	}

	private void processAttributeInstance(GoalsInfoTable goalInfo, AttributeInstance attrInstance,
			boolean isGoalClosed) {
		Attribute attr = attrInstance.getAttribute();
		if (attr.isFinishDate()) {
			handleFinishDate(goalInfo, attrInstance.getValueAsDate(), isGoalClosed);
		} else if (attr.isReachedField()) {
			goalInfo.setReached(attrInstance.getValueAsNumber());
		} else if (attr.isExpectedField()) {
			goalInfo.setExpected(attrInstance.getValueAsNumber());
		}
	}

	private void handleFinishDate(GoalsInfoTable goalInfo, Date finishDate, boolean isGoalClosed) {
		goalInfo.setFinishDate(finishDate);
		Date today = new Date();
		long diferenca = (finishDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);

		if (diferenca >= 0 || isGoalClosed) {
			goalInfo.setGoalStatus("Em dia.");
		} else {
			goalInfo.setGoalStatus("Atraso em " + (-diferenca) + (diferenca == -1 ? " dia." : " dias."));
		}
	}

	public long getNumberOfGoalsCompleted(List<StructureLevelInstance> goals) {
		if (goals == null || goals.isEmpty()) {
			return 0L; 
		}

		long completedCount = 0;
		for (StructureLevelInstance goal : goals) {
			if (goal != null && goal.isClosed()) { 
				completedCount++;
			}
		}
		return completedCount;
	}
}
