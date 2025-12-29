package org.forpdi.dashboard;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.enums.Periodicity;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.filters.PeformanceFilterType;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.system.CriteriaCompanyFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardBSTest {

    @InjectMocks
    private DashboardBS dashboardBS;

    @Mock private UserSession userSession;
    @Mock private StructureBS sbs;
    @Mock private PlanBS pbs;
    @Mock private AttributeHelper attrHelper;
    @Mock private CriteriaCompanyFilter filter;
    @Mock private GoalsInfoTableHelper infoTableHelper;
    @Mock private GoalsInfoTableFilterHelper goalsFilterHelper;
    @Mock private HibernateDAO dao;
    @Mock private Criteria criteria;

    private PlanMacro macro;
    private Plan plan;
    private StructureLevelInstance obj;
    private Budget budget;

    @BeforeEach
    void setup() {
        macro = new PlanMacro();
        plan = new Plan();
        obj = new StructureLevelInstance();
        budget = new Budget();
        budget.setLevelInstance(obj);

        when(dao.newCriteria(eq(Budget.class))).thenReturn(criteria);
        when(userSession.getAccessLevel()).thenReturn(100);
        when(filter.filterAndList(any(Criteria.class), eq(Budget.class), anyString()))
                .thenReturn(List.of(budget));
    }

    @Test
    @DisplayName("listBudgets: deve retornar lista para System Admin")
    void testListBudgets_SystemAdmin() {
        List<Budget> result = dashboardBS.listBudgets(macro, plan, obj, "SubAção");
        assertNotNull(result);
        assertEquals(1, result.size());
    }

}
