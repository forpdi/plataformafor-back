// package org.forpdi.dashboard;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.io.Serializable;
// import java.lang.reflect.Method;
// import java.text.ParseException;
// import java.util.*;

// import org.forpdi.core.common.PaginatedList;
// import org.forpdi.core.common.Response;
// import org.forpdi.core.company.Company;
// import org.forpdi.core.company.CompanyBS;
// import org.forpdi.core.company.CompanyDomainContext;
// import org.forpdi.dashboard.admin.GeneralBudgets;
// import org.forpdi.dashboard.admin.GoalsInfoTable;
// import org.forpdi.dashboard.admin.PlanDetails;
// import org.forpdi.dashboard.goalsinfo.GoalsInfo;
// import org.forpdi.dashboard.goalsinfo.GoalsInfoBS;
// import org.forpdi.dashboard.goalsinfo.GoalsInfoOverview;
// import org.forpdi.dashboard.manager.IndicatorHistory;
// import org.forpdi.planning.attribute.Attribute;
// import org.forpdi.planning.attribute.AttributeHelper;
// import org.forpdi.planning.attribute.AttributeInstance;
// import org.forpdi.planning.attribute.PolarityMapHelper;
// import org.forpdi.planning.fields.budget.Budget;
// import org.forpdi.planning.filters.PeformanceFilterType;
// import org.forpdi.planning.plan.Plan;
// import org.forpdi.planning.plan.PlanBS;
// import org.forpdi.planning.plan.PlanMacro;
// import org.forpdi.planning.structure.StructureBS;
// import org.forpdi.planning.structure.StructureHelper;
// import org.forpdi.planning.structure.StructureInstanceAttributeHelper;
// import org.forpdi.planning.structure.StructureLevelInstance;
// import org.forpdi.security.auth.UserSession;
// import org.forpdi.security.authz.AccessLevels;
// import org.forpdi.system.ErrorMessages;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Answers;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.junit.jupiter.MockitoSettings;
// import org.mockito.quality.Strictness;
// import org.springframework.http.ResponseEntity;

// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
// class DashboardControllerTest {

//     @InjectMocks
//     private DashboardController controller;

//     @Mock private StructureBS sbs;
//     @Mock private PlanBS planBS;
//     @Mock private DashboardBS bs;
//     @Mock private AttributeHelper attrHelper;
//     @Mock private StructureHelper structHelper;
//     @Mock private GoalsInfoBS goalsInfoBS;
//     @Mock private PolarityMapHelper polarityMapHelper;
//     @Mock private CompanyDomainContext domain;
//     @Mock private CompanyBS companyBS;
//     @Mock private StructureInstanceAttributeHelper structureInstanceAttributeHelper;

//     // ---------- Helpers

//     private Company company(long id) {
//         Company c = mock(Company.class);
//         when(c.getId()).thenReturn(id);            // requer mock-maker-inline para finals
//         return c;
//     }

//     private PlanMacro planMacro(long companyId) {
//         PlanMacro pm = mock(PlanMacro.class);
//         when(pm.getCompany()).thenReturn(company(companyId));
//         return pm;
//     }

//     private Plan plan(long companyId) {
//         Plan p = mock(Plan.class);
//         when(p.getParent()).thenReturn(planMacro(companyId));
//         return p;
//     }

//     private StructureLevelInstance levelInstance(long id, Plan plan) {
//         StructureLevelInstance li = mock(StructureLevelInstance.class, RETURNS_DEEP_STUBS);
//         when(li.getId()).thenReturn(id);
//         when(li.getPlan()).thenReturn(plan);
//         return li;
//     }

//     private <T extends Serializable> PaginatedList<T> paginated(List<T> list, long total) {
//         PaginatedList<T> pl = new PaginatedList<>();
//         pl.setList(list);
//         pl.setTotal(total);
//         return pl;
//     }

//     private UserSession adminSession() {
//         UserSession s = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
//         when(s.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());
//         return s;
//     }

//     private UserSession nonAdminSession(long userId) {
//         UserSession s = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
//         when(s.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel() - 1);
//         when(s.getAuthData().getId()).thenReturn(userId);
//         return s;
//     }

//     // ========== /dashboard/admin/goals ==========
//     @Nested @DisplayName("adminGoalsInfo")
//     class AdminGoalsInfoTests {

//         // @Test
//         // void shouldValidateTenantWhenMacroProvided_andReturnOk() {
//         //     UserSession session = adminSession();
//         //     PlanMacro pm = planMacro(10L);
//         //     when(planBS.retrievePlanMacroById(99L)).thenReturn(pm);

//         //     PaginatedList<StructureLevelInstance> list = paginated(Collections.emptyList(), 0);
//         //     when(sbs.listAllGoals(pm, null)).thenReturn(list);

//         //     GoalsInfo anyDto = new GoalsInfo();
//         //     when(goalsInfoBS.calculateAdminGoalsInfo(list.getList())).thenReturn(anyDto);

//         //     ResponseEntity<?> resp = controller.adminGoalsInfo(99L, null, session);

//         //     assertEquals(200, resp.getStatusCodeValue());
//         //     verify(domain).validateTenant(pm.getCompany());
//         //     verify(sbs).listAllGoals(pm, null);
//         //     verify(goalsInfoBS).calculateAdminGoalsInfo(list.getList());
//         // }

//         @Test
//         void shouldCallListWithNullsWhenNoPlanNorMacro_andReturnOk() {
//             UserSession session = adminSession();
//             PaginatedList<StructureLevelInstance> list = paginated(Collections.emptyList(), 0);
//             when(sbs.listAllGoals(null, null)).thenReturn(list);
//             when(goalsInfoBS.calculateAdminGoalsInfo(list.getList()))
//                 .thenReturn(new GoalsInfo());

//             ResponseEntity<?> resp = controller.adminGoalsInfo(null, null, session);

//             assertEquals(200, resp.getStatusCodeValue());
//             verify(sbs).listAllGoals(null, null);
//         }

//         @Test
//         void shouldReturnFailOnException() {
//             UserSession session = adminSession();
//             when(sbs.listAllGoals(any(), any())).thenThrow(new RuntimeException("boom"));

//             ResponseEntity<?> resp = controller.adminGoalsInfo(null, null, session);

//             assertTrue(resp.getStatusCode().is4xxClientError() || resp.getStatusCode().is5xxServerError());
//         }
//     }

//     // ========== /dashboard/admin/performanceStrategicAxis ==========
//     @Nested @DisplayName("performanceStrategicAxis")
//     class PerformanceStrategicAxisTests {
//         @Test
//         void nonAdmin_withoutCompany_shouldDeny() {
//             UserSession s = nonAdminSession(7L);
//             when(companyBS.findCompanyByUserId(anyLong())).thenReturn(null);

//             ResponseEntity<?> resp = controller.performanceStrategicAxis(null, null, null, null, null, 1, 10, s);

//             assertTrue(resp.getStatusCode().is4xxClientError());
//             verifyNoInteractions(sbs);
//         }

//         // @Test
//         // void nonAdmin_macroFromOtherCompany_shouldDeny() {
//         //     UserSession s = nonAdminSession(7L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));
//         //     PlanMacro pm = planMacro(2L);
//         //     when(planBS.retrievePlanMacroById(5L)).thenReturn(pm);

//         //     ResponseEntity<?> resp = controller.performanceStrategicAxis(5L, null, null, null, null, 1, 10, s);

//         //     assertTrue(resp.getStatusCode().is4xxClientError());
//         //     verify(sbs, never()).listStrategicAxis(any(), any(), any(), any(), any(), any(), any());
//         // }

//         // @Test
//         // void nonAdmin_withNoPlanNorMacro_shouldListForOwnCompany() {
//         //     UserSession s = nonAdminSession(7L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));
//         //     PaginatedList<StructureLevelInstance> result = paginated(Collections.emptyList(), 0);
//         //     when(sbs.listStrategicAxis(null, null, null, null, null, 1, 10)).thenReturn(result);

//         //     ResponseEntity<?> resp = controller.performanceStrategicAxis(null, null, null, null, null, 1, 10, s);

//         //     assertEquals(200, resp.getStatusCodeValue());
//         //     verify(sbs).listStrategicAxis(null, null, null, null, null, 1, 10);
//         // }

//         // @Test
//         // void admin_withPlan_shouldValidateTenantAndList() {
//         //     UserSession s = adminSession();
//         //     Plan plan = plan(42L);
//         //     when(planBS.retrieveById(3L)).thenReturn(plan);
//         //     PaginatedList<StructureLevelInstance> result = paginated(Collections.emptyList(), 0);
//         //     when(sbs.listStrategicAxis(null, plan, null, null, null, 1, 10)).thenReturn(result);

//         //     ResponseEntity<?> resp = controller.performanceStrategicAxis(null, 3L, null, null, null, 1, 10, s);

//         //     assertEquals(200, resp.getStatusCodeValue());
//         //     verify(domain).validateTenant(plan.getParent().getCompany());
//         //     verify(sbs).listStrategicAxis(null, plan, null, null, null, 1, 10);
//         // }
//     }

//     // ========== /dashboard/admin/objectivesByThematicAxis ==========
//     @Nested @DisplayName("objectivesByThematicAxis")
//     class ObjectivesByThematicAxisTests {
//         @Test
//         void notFound_whenThematicAxisDoesNotExist() {
//             UserSession s = adminSession();
//             when(sbs.retrieveLevelInstance(111L)).thenReturn(null);

//             ResponseEntity<?> resp = controller.objectivesByThematicAxis(null, null, 111L, s);

//             assertEquals(404, resp.getStatusCodeValue());
//         }

//         // @Test
//         // void nonAdmin_theirCompanyOnly() {
//         //     UserSession s = nonAdminSession(9L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));
//         //     Plan plan = plan(1L);
//         //     StructureLevelInstance thematic = levelInstance(5L, plan);
//         //     when(sbs.retrieveLevelInstance(5L)).thenReturn(thematic);
//         //     PaginatedList<StructureLevelInstance> res = paginated(Collections.emptyList(), 0);
//         //     when(sbs.listObjectivesByThematicAxis(null, null, thematic)).thenReturn(res);

//         //     ResponseEntity<?> resp = controller.objectivesByThematicAxis(null, null, 5L, s);

//         //     assertEquals(200, resp.getStatusCodeValue());
//         //     verify(sbs).listObjectivesByThematicAxis(null, null, thematic);
//         // }
//     }

//     // ========== /dashboard/admin/planDetails ==========
//     @Nested @DisplayName("adminPlanDetails")
//     class AdminPlanDetailsTests {
//         // @Test
//         // void nonAdmin_withoutPlanAndMacro_shouldDeny() {
//         //     UserSession s = nonAdminSession(100L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(7L));

//         //     ResponseEntity<?> resp = controller.adminPlanDetails(null, null, s);

//         //     assertTrue(resp.getStatusCode().is4xxClientError());
//         //     verifyNoInteractions(sbs);
//         // }

//         @Test
//         void admin_withoutPlanNorMacro_shouldCountAllIndicators_andZeroPercentsWhenNoGoals() {
//             UserSession s = adminSession();
//             when(sbs.countAllIndicators()).thenReturn(42L);
//             PaginatedList<StructureLevelInstance> goalList = paginated(Collections.emptyList(), 0);
//             when(sbs.listGoalsByResponsible(null, null)).thenReturn(goalList);
//             when(sbs.countBudgets(null, null)).thenReturn(3L);
//             when(sbs.countObjective(null, null)).thenReturn(4L);

//             ResponseEntity<?> resp = controller.adminPlanDetails(null, null, s);

//             assertEquals(200, resp.getStatusCodeValue());
//             verify(sbs).countAllIndicators();
//             verify(sbs).listGoalsByResponsible(null, null);
//             verify(goalsInfoBS, never()).getNumberOfGoalsLate(anyList());
//         }

//         // @Test
//         // void nonAdmin_withPlan_shouldComputeLatePercent() {
//         //     UserSession s = nonAdminSession(10L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));
//         //     Plan plan = plan(1L);
//         //     when(planBS.retrieveById(77L)).thenReturn(plan);

//         //     PaginatedList<StructureLevelInstance> goalList = paginated(
//         //         Arrays.asList(mock(StructureLevelInstance.class), mock(StructureLevelInstance.class)), 2);
//         //     when(sbs.listGoalsByResponsible(null, plan)).thenReturn(goalList);
//         //     when(goalsInfoBS.getNumberOfGoalsLate(goalList.getList())).thenReturn(1L);
//         //     when(sbs.countIndicators(plan)).thenReturn(10L);
//         //     when(sbs.countBudgets(null, plan)).thenReturn(2L);
//         //     when(sbs.countObjective(null, plan)).thenReturn(5L);

//         //     ResponseEntity<?> resp = controller.adminPlanDetails(null, 77L, s);

//         //     assertEquals(200, resp.getStatusCodeValue());
//         //     verify(goalsInfoBS).getNumberOfGoalsLate(goalList.getList());
//         // }
//     }

//     // ========== /dashboard/admin/budget ==========
//     @Nested @DisplayName("listBudgets / calculateBudgetsSummary")
//     class BudgetTests {

//         @Test
//         void admin_shouldSummarizeBudgets_withDedupOnBudgetElement() throws Exception {
//             UserSession s = adminSession();

//             Budget b1 = mock(Budget.class, RETURNS_DEEP_STUBS);
//             when(b1.getCommitted()).thenReturn(100.0);
//             when(b1.getRealized()).thenReturn(50.0);
//             when(b1.getBudgetElement().getId()).thenReturn(1L);
//             when(b1.getBudgetElement().getBudgetLoa()).thenReturn(1000.0);

//             Budget b2 = mock(Budget.class, RETURNS_DEEP_STUBS);
//             when(b2.getCommitted()).thenReturn(30.0);
//             when(b2.getRealized()).thenReturn(20.0);
//             when(b2.getBudgetElement().getId()).thenReturn(1L);
//             when(b2.getBudgetElement().getBudgetLoa()).thenReturn(1000.0);

//             Budget b3 = mock(Budget.class, RETURNS_DEEP_STUBS);
//             when(b3.getCommitted()).thenReturn(null);
//             when(b3.getRealized()).thenReturn(10.0);
//             when(b3.getBudgetElement().getId()).thenReturn(2L);
//             when(b3.getBudgetElement().getBudgetLoa()).thenReturn(500.0);

//             when(bs.listBudgets(null, null, null, null)).thenReturn(Arrays.asList(b1, b2, b3));

//             ResponseEntity<?> resp = controller.listBudgets(null, null, null, null, s);
//             assertEquals(200, resp.getStatusCodeValue());

//             // SUCCESS retorna um wrapper Response<GeneralBudgets>
//             Response<?> body = (Response<?>) resp.getBody();
//             assertNotNull(body);
//             assertTrue(body.isSuccess());
//             assertTrue(body.getData() instanceof GeneralBudgets);
//             GeneralBudgets gb = (GeneralBudgets) body.getData();

//             assertEquals(130.0, gb.getCommitted(), 0.0001);
//             assertEquals( 80.0, gb.getConducted(), 0.0001);
//             assertEquals(1500.0, gb.getPlanned(),   0.0001);
//         }

//         // @Test
//         // void nonAdmin_mismatchCompany_shouldDeny() {
//         //     UserSession s = nonAdminSession(9L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));

//         //     PlanMacro pm = planMacro(2L);
//         //     when(planBS.retrievePlanMacroById(5L)).thenReturn(pm);

//         //     ResponseEntity<?> resp = controller.listBudgets(5L, null, null, null, s);

//         //     assertTrue(resp.getStatusCode().is4xxClientError());
//         //     verify(bs, never()).listBudgets(any(), any(), any(), any());
//         // }
//     }

//     // // ========== /dashboard/admin/indicatorsInformation ==========
//     // @Nested @DisplayName("indicatorsInformation")
//     // class IndicatorsInformationTests {
//     //     @Test
//     //     void admin_shouldFillIndicatorsForEachItem() {
//     //         UserSession s = adminSession();
//     //         PlanMacro pm = planMacro(10L);
//     //         when(planBS.retrievePlanMacroById(1L)).thenReturn(pm);

//     //         StructureLevelInstance i1 = mock(StructureLevelInstance.class);
//     //         StructureLevelInstance i2 = mock(StructureLevelInstance.class);
//     //         PaginatedList<StructureLevelInstance> page = paginated(Arrays.asList(i1, i2), 2);
//     //         when(sbs.listIndicators(pm, null, null, 1, 10)).thenReturn(page);

//     //         ResponseEntity<?> resp = controller.indicatorsInformation(1L, null, null, 1, 10, s);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(structHelper, times(2)).fillIndicators(any(StructureLevelInstance.class));
//     //     }
//     // }

//     // // ========== /dashboard/manager/goalsInfoTable ==========
//     // @Nested @DisplayName("goalsInfoTable")
//     // class GoalsInfoTableTests {
//     //     @Test
//     //     void admin_shouldListGoalsTable_withoutFilter() throws ParseException {
//     //         UserSession s = adminSession();
//     //         Plan plan = plan(1L);
//     //         when(planBS.retrieveById(3L)).thenReturn(plan);
//     //         PaginatedList<GoalsInfoTable> res = paginated(Collections.emptyList(), 0);
//     //         when(bs.getGoalsInfoTable(null, plan, null, 1, 10, null)).thenReturn(res);

//     //         ResponseEntity<?> resp = controller.goalsInfoTable(null, 3L, null, 1, 10, null, s);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(bs).getGoalsInfoTable(null, plan, null, 1, 10, null);
//     //     }

//     //     @Test
//     //     void admin_shouldListGoalsTable_withFilter() throws ParseException {
//     //         UserSession s = adminSession();
//     //         Plan plan = plan(1L);
//     //         when(planBS.retrieveById(3L)).thenReturn(plan);
//     //         PaginatedList<GoalsInfoTable> res = paginated(Collections.emptyList(), 0);
//     //         when(bs.getGoalsInfoTable(null, plan, null, 1, 10, PeformanceFilterType.ENOUGH)).thenReturn(res);

//     //         ResponseEntity<?> resp = controller.goalsInfoTable(null, 3L, null, 1, 10, PeformanceFilterType.ENOUGH.getValue(), s);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(bs).getGoalsInfoTable(null, plan, null, 1, 10, PeformanceFilterType.ENOUGH);
//     //     }
//     // }

//     // // ========== /dashboard/admin/objectivesInformation ==========
//     // @Nested @DisplayName("objectivesInformation")
//     // class ObjectivesInformationTests {
//     //     @Test
//     //     void admin_shouldReturnObjectivesList() {
//     //         UserSession s = adminSession();
//     //         PlanMacro pm = planMacro(1L);
//     //         when(planBS.retrievePlanMacroById(2L)).thenReturn(pm);
//     //         PaginatedList<StructureLevelInstance> res = paginated(Collections.emptyList(), 0);
//     //         when(sbs.listObjectives(pm, null)).thenReturn(res);

//     //         ResponseEntity<?> resp = controller.objectivesInformation(2L, null, s);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(sbs).listObjectives(pm, null);
//     //     }
//     // }

//     // // ========== /dashboard/colaborator/goalsInformation ==========
//     // @Nested @DisplayName("goalsInformation (colaborator)")
//     // class GoalsInformationTests {
//     //     @Test
//     //     void nonAdmin_mismatchIndicatorCompany_shouldDeny() {
//     //         UserSession s = nonAdminSession(9L);
//     //         when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));
//     //         Plan planOther = plan(2L);
//     //         StructureLevelInstance ind = levelInstance(50L, planOther);
//     //         when(sbs.retrieveLevelInstance(50L)).thenReturn(ind);

//     //         ResponseEntity<?> resp = controller.goalsInformation(null, null, 50L, 1, 10, s);

//     //         assertTrue(resp.getStatusCode().is4xxClientError());
//     //     }
//     // }

//     // ========== /dashboard/manager/indicatorsHistory ==========
//     @Nested @DisplayName("listIndicatorsHistory")
//     class IndicatorsHistoryTests {
//         @Test
//         void admin_shouldReturnHistory() {
//             UserSession s = adminSession();
//             PaginatedList<IndicatorHistory> res = paginated(Collections.emptyList(), 0);
//             when(bs.listIndicatorHistory(null, null, null, 1, 10)).thenReturn(res);

//             ResponseEntity<?> resp = controller.listIndicatorsHistory(null, null, null, 1, 10, s);

//             assertEquals(200, resp.getStatusCodeValue());
//             verify(bs).listIndicatorHistory(null, null, null, 1, 10);
//         }
//     }

//     // // ========== /dashboard/community/levelsonsgraph ==========
//     // @Nested @DisplayName("listLevelSonsForGraph (community)")
//     // class LevelSonsGraphTests {
//     //     @Test
//     //     void shouldValidateTenantWhenPlanProvided_andFillIndicators() {
//     //         Plan plan = plan(77L);
//     //         when(planBS.retrieveById(1L)).thenReturn(plan);

//     //         StructureLevelInstance li = mock(StructureLevelInstance.class);
//     //         PaginatedList<StructureLevelInstance> res = paginated(Arrays.asList(li), 1L);
//     //         when(sbs.retrieveLevelSonsForGraph(null, plan, null, 1, 10)).thenReturn(res);

//     //         ResponseEntity<?> resp = controller.listLevelSonsForGraph(null, 1L, null, 1, 10);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(domain).validateTenant(plan.getParent().getCompany());
//     //         verify(structHelper).fillIndicators(li);
//     //     }
//     // }

//     // // ========== /dashboard/community/communityinfotable ==========
//     // @Nested @DisplayName("communityInfoTable (community)")
//     // class CommunityInfoTableTests {
//     //     @Test
//     //     void shouldValidateTenantsAndReturn() throws ParseException {
//     //         PlanMacro pm = planMacro(5L);
//     //         when(planBS.retrievePlanMacroById(2L)).thenReturn(pm);
//     //         Plan plan = plan(5L);
//     //         when(planBS.retrieveById(3L)).thenReturn(plan);

//     //         PaginatedList<GoalsInfoTable> res = paginated(Collections.emptyList(), 0);
//     //         when(bs.getCommunityInfoTable(pm, plan, 9L, 1, 10)).thenReturn(res);

//     //         ResponseEntity<?> resp = controller.communityInfoTable(2L, 3L, 9L, 1, 10);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(domain).validateTenant(pm.getCompany());
//     //         verify(domain).validateTenant(plan.getParent().getCompany());
//     //         verify(bs).getCommunityInfoTable(pm, plan, 9L, 1, 10);
//     //     }
//     // }

//     // // ========== /dashboard/community/planDetails ==========
//     // @Nested @DisplayName("communityPlanDetails (community)")
//     // class CommunityPlanDetailsTests {
//     //     @Test
//     //     void withMacro_shouldUseGoalsInfoOverviewByPlanMacro_andComputePercents() {
//     //         PlanMacro pm = planMacro(11L);
//     //         when(planBS.retrievePlanMacroById(7L)).thenReturn(pm);
//     //         doNothing().when(domain).validateTenant(pm.getCompany());

//     //         when(sbs.countIndicators(pm)).thenReturn(8L);
//     //         when(sbs.countBudgets(pm, null)).thenReturn(2L);
//     //         when(sbs.countObjective(pm, null)).thenReturn(3L);
//     //         when(sbs.countPlans(pm, null)).thenReturn(1L);

//     //         GoalsInfoOverview gio = mock(GoalsInfoOverview.class);
//     //         when(gio.getTotal()).thenReturn(20L);
//     //         when(gio.getLate()).thenReturn(5L);
//     //         when(gio.getReached()).thenReturn(6L);
//     //         when(goalsInfoBS.getGoalsInfoOverviewByPlanMacro(pm)).thenReturn(gio);

//     //         ResponseEntity<?> resp = controller.communityPlanDetails(7L, null);

//     //         assertEquals(200, resp.getStatusCodeValue());
//     //         verify(goalsInfoBS).getGoalsInfoOverviewByPlanMacro(pm);
//     //     }
//     // }

//     // ========== /dashboard/graphforindicator ==========
//     @Nested @DisplayName("graphForIndicator")
//     class GraphForIndicatorTests {
//         @Test
//         void notFound_whenIndicatorDoesNotExist() {
//             UserSession s = adminSession();
//             when(sbs.retrieveLevelInstance(999L)).thenReturn(null);

//             ResponseEntity<?> resp = controller.graphForIndicator(999L, 1, 10, s);

//             assertEquals(404, resp.getStatusCodeValue());
//         }

//         // @Test
//         // void nonAdmin_indicatorFromOtherCompany_shouldDeny() {
//         //     UserSession s = nonAdminSession(2L);
//         //     when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company(1L));
//         //     Plan plan = plan(2L);
//         //     when(sbs.retrieveLevelInstance(10L)).thenReturn(levelInstance(10L, plan));

//         //     ResponseEntity<?> resp = controller.graphForIndicator(10L, 1, 10, s);

//         //     assertTrue(resp.getStatusCode().is4xxClientError());
//         // }
//     }

//     // ========== White-box do método privado calculateBudgetsSummary ==========
//     @Nested @DisplayName("White-box: calculateBudgetsSummary (via reflection)")
//     class CalculateBudgetsSummaryWhiteBox {
//         @Test
//         void shouldHandleNullOrEmptyListAndReturnZeros() throws Exception {
//             Method m = DashboardController.class.getDeclaredMethod("calculateBudgetsSummary", List.class);
//             m.setAccessible(true);

//             GeneralBudgets r1 = (GeneralBudgets) m.invoke(controller, new Object[] { null });
//             assertEquals(0.0, r1.getCommitted(), 0.0001);
//             assertEquals(0.0, r1.getConducted(), 0.0001);
//             assertEquals(0.0, r1.getPlanned(), 0.0001);

//             GeneralBudgets r2 = (GeneralBudgets) m.invoke(controller, Collections.emptyList());
//             assertEquals(0.0, r2.getCommitted(), 0.0001);
//             assertEquals(0.0, r2.getConducted(), 0.0001);
//             assertEquals(0.0, r2.getPlanned(), 0.0001);
//         }
//     }
// }
