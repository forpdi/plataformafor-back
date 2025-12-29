package org.forpdi.core.csvs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.GoalsFilterHelper;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.dto.RiskExportAxesCSVRequest;
import org.forrisco.risk.dto.RiskExportRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

class CsvExportControllerTest {

    private CsvExportController controller;
    private CsvPlanExport csvPlanExport;
    private CsvGoalExport csvGoalsExport;
    private CsvRiskExport csvRiskExport;
    private CsvLevelInstanceBudgetExport csvLevelInstanceBudgetExport;
    private CsvActionPlanExport csvActionPlanExport;
    private CsvBudgetExport csvBudgetExport;
    private CompanyDomainContext domain;
    private PlanBS planBS;
    private BudgetBS budgetBS;
    private StructureBS structureBS;
    private GoalsFilterHelper goalsFilterHelper;
    private CompanyBS companyBS;

    private MockHttpServletResponse response;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = CsvExportController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Company company(long id) {
        Company c = new Company();
        c.setId(id);
        c.setName("Empresa-" + id);
        return c;
    }

    private static PlanMacro planMacro(long id, Company comp) {
        PlanMacro pm = new PlanMacro();
        pm.setId(id);
        pm.setCompany(comp);
        return pm;
    }

    private static Plan plan(PlanMacro pm) {
        Plan p = new Plan();
        p.setParent(pm);
        return p;
    }

    private static StructureLevelInstance sli(long id, Plan p) {
        StructureLevelInstance s = new StructureLevelInstance();
        s.setId(id);
        s.setPlan(p);
        return s;
    }

    private static UserSession userSession(long userId, int level) {
        UserSession us = mock(UserSession.class, RETURNS_DEEP_STUBS);
        when(us.getAccessLevel()).thenReturn(level);
        AuthData ad = mock(AuthData.class);
        when(ad.getId()).thenReturn(userId);
        when(ad.getName()).thenReturn("tester");
        when(us.getAuthData()).thenReturn(ad);
        return us;
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new CsvExportController();

        csvPlanExport = mock(CsvPlanExport.class);
        csvGoalsExport = mock(CsvGoalExport.class);
        csvRiskExport = mock(CsvRiskExport.class);
        csvLevelInstanceBudgetExport = mock(CsvLevelInstanceBudgetExport.class);
        csvActionPlanExport = mock(CsvActionPlanExport.class);
        csvBudgetExport = mock(CsvBudgetExport.class);
        domain = mock(CompanyDomainContext.class, RETURNS_DEEP_STUBS);
        planBS = mock(PlanBS.class);
        budgetBS = mock(BudgetBS.class);
        structureBS = mock(StructureBS.class);
        goalsFilterHelper = mock(GoalsFilterHelper.class);
        companyBS = mock(CompanyBS.class);

        inject(controller, "csvPlanExport", csvPlanExport);
        inject(controller, "csvGoalsExport", csvGoalsExport);
        inject(controller, "csvRiskExport", csvRiskExport);
        inject(controller, "csvLevelInstanceBudgetExport", csvLevelInstanceBudgetExport);
        inject(controller, "csvActionPlanExport", csvActionPlanExport);
        inject(controller, "csvBudgetExport", csvBudgetExport);
        inject(controller, "domain", domain);
        inject(controller, "planBS", planBS);
        inject(controller, "budgetBS", budgetBS);
        inject(controller, "structureBS", structureBS);
        inject(controller, "goalsFilterHelper", goalsFilterHelper);
        inject(controller, "companyBS", companyBS);

        response = new MockHttpServletResponse();
        Field r = org.forpdi.core.common.AbstractController.class.getDeclaredField("response");
        r.setAccessible(true);
        r.set(controller, response);
    }

    @Test
    @DisplayName("exportPlan: sucesso (admin) seta header/content-type e chama exporter")
    void exportPlan_success_admin() throws Exception {
        Company comp = company(10L);
        PlanMacro pm = planMacro(100L, comp);

        when(planBS.retrievePlanMacroById(100L)).thenReturn(pm);
        when(domain.get().getCompany()).thenReturn(comp);
        when(planBS.listPlansForPlanMacro(pm)).thenReturn(List.of(plan(pm)));

        ResponseEntity<?> resp = controller.exportPlan(100L, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertTrue(response.getHeader("Content-Disposition").contains("plans-10-"));
        assertEquals("application/octet-stream", response.getContentType());
        verify(csvPlanExport).exportCSV(anyList(), any(OutputStream.class));
    }

    @Test
    @DisplayName("exportPlan: 404 quando não encontra PlanMacro")
    void exportPlan_notFound() throws Exception {
        when(planBS.retrievePlanMacroById(999L)).thenReturn(null);

        ResponseEntity<?> resp = controller.exportPlan(999L, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(404, response.getStatus());
        verify(csvPlanExport, never()).exportCSV(anyList(), any());
    }

    @Test
    @DisplayName("exportPlan: 403 quando não-admin e empresa não bate")
    void exportPlan_forbidden_nonAdmin() throws Exception {
        Company comp = company(10L);
        PlanMacro pm = planMacro(100L, comp);
        when(planBS.retrievePlanMacroById(100L)).thenReturn(pm);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        ResponseEntity<?> resp = controller.exportPlan(100L, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(403, response.getStatus());
        verify(csvPlanExport, never()).exportCSV(anyList(), any());
    }

    @Test
    @DisplayName("exportPlan: erro interno (500) ao exportar")
    void exportPlan_internalError() throws Exception {
        Company comp = company(10L);
        PlanMacro pm = planMacro(100L, comp);

        when(planBS.retrievePlanMacroById(100L)).thenReturn(pm);
        when(domain.get().getCompany()).thenReturn(comp);
        when(planBS.listPlansForPlanMacro(pm)).thenReturn(List.of(plan(pm)));

        doThrow(new RuntimeException("boom")).when(csvPlanExport).exportCSV(anyList(), any(OutputStream.class));

        ResponseEntity<?> resp = controller.exportPlan(100L, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(500, response.getStatus());
    }

    @Test
    @DisplayName("exportLevelInstanceBudget: sucesso (admin)")
    void exportLevelInstanceBudget_success() throws Exception {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance s = sli(77L, p);

        when(structureBS.retrieveLevelInstanceNoDeleted(77L)).thenReturn(s);
        when(domain.get().getCompany()).thenReturn(comp);

        ResponseEntity<?> resp = controller.exportLevelInstanceBudget(77L, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertTrue(response.getHeader("Content-Disposition").contains("budget-10-"));
        assertEquals("application/octet-stream", response.getContentType());
        verify(csvLevelInstanceBudgetExport).exportLevelInstaceBudgets(eq(s), any(OutputStream.class));
    }

    @Test
    @DisplayName("exportLevelInstanceBudget: 404 quando não encontra SLI")
    void exportLevelInstanceBudget_notFound() throws IOException {
        when(structureBS.retrieveLevelInstanceNoDeleted(77L)).thenReturn(null);

        ResponseEntity<?> resp = controller.exportLevelInstanceBudget(77L, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(404, response.getStatus());
        verify(csvLevelInstanceBudgetExport, never()).exportLevelInstaceBudgets(any(), any());
    }

    @Test
    @DisplayName("exportLevelInstanceBudget: 403 quando não-admin e empresa não bate")
    void exportLevelInstanceBudget_forbidden_nonAdmin() throws IOException {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance s = sli(77L, p);

        when(structureBS.retrieveLevelInstanceNoDeleted(77L)).thenReturn(s);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        ResponseEntity<?> resp = controller.exportLevelInstanceBudget(77L, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(403, response.getStatus());
        verify(csvLevelInstanceBudgetExport, never()).exportLevelInstaceBudgets(any(), any());
    }

    @Test
    @DisplayName("exportGoalCsv: sucesso (empresa bate) chama exporter")
    void exportGoalCsv_success() throws Exception {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance parent = sli(55L, p);

        when(structureBS.retrieveLevelInstance(55L)).thenReturn(parent);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(comp);
        when(domain.get().getCompany()).thenReturn(comp);

        PaginatedList<StructureLevelInstance> goals = new PaginatedList<>();
        goals.setList(List.of(sli(1L, p)));
        when(goalsFilterHelper.retrieveFilteredGoals(eq(55L), any(), any(), any(), eq(1), eq(Integer.MAX_VALUE)))
                .thenReturn(goals);

        ResponseEntity<?> resp = controller.exportGoalCsv(55L, null, null, null, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1));

        assertEquals(204, resp.getStatusCodeValue());
        assertTrue(response.getHeader("Content-Disposition").contains("goals-10-"));
        assertEquals("application/octet-stream", response.getContentType());
        verify(csvGoalsExport).exportGoalsCSV(eq(goals.getList()), any(OutputStream.class));
    }

    @Test
    @DisplayName("exportGoalCsv: 404 quando parent não encontrado")
    void exportGoalCsv_notFound() throws IOException {
        when(structureBS.retrieveLevelInstance(55L)).thenReturn(null);

        ResponseEntity<?> resp = controller.exportGoalCsv(55L, null, null, null, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(404, response.getStatus());
        verify(csvGoalsExport, never()).exportGoalsCSV(anyList(), any());
    }

    @Test
    @DisplayName("exportGoalCsv: 403 quando não-admin e empresa não bate")
    void exportGoalCsv_forbidden_nonAdmin() throws IOException {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance parent = sli(55L, p);

        when(structureBS.retrieveLevelInstance(55L)).thenReturn(parent);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        ResponseEntity<?> resp = controller.exportGoalCsv(55L, null, null, null, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(403, response.getStatus());
        verify(csvGoalsExport, never()).exportGoalsCSV(anyList(), any());
    }

    @Test
    @DisplayName("exportActionPlanCsv: sucesso monta actionPlans e exporta")
    void exportActionPlanCsv_success() throws Exception {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance s = sli(42L, p);

        when(structureBS.retrieveLevelInstance(42L)).thenReturn(s);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(comp);
        when(domain.get().getCompany()).thenReturn(comp);

        Attribute attr = new Attribute();
        List<Attribute> attrs = new ArrayList<>();
        attrs.add(attr);
        when(structureBS.retrieveLevelAttributes(s.getLevel())).thenReturn(attrs);

        when(structureBS.setActionPlansAttributes(eq(s), any(PaginatedList.class), eq(1), eq(Integer.MAX_VALUE),
                any(), any(), any(), any())).thenAnswer(inv -> {
            PaginatedList<Attribute> arg = inv.getArgument(1);
            ActionPlan ap = new ActionPlan();
            attr.setActionPlans(List.of(ap));
            arg.setList(List.of(attr));
            return arg;
        });

        ResponseEntity<?> resp = controller.exportActionPlanCsv(42L, null, null, null, null, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1));

        assertEquals(204, resp.getStatusCodeValue());
        assertTrue(response.getHeader("Content-Disposition").contains("action-plans-10-"));
        assertEquals("application/octet-stream", response.getContentType());
        verify(csvActionPlanExport).exportLevelInstaceActionPlanCSV(anyList(), any(OutputStream.class));
    }

    @Test
    @DisplayName("exportActionPlanCsv: 404 quando SLI não encontrado")
    void exportActionPlanCsv_notFound() throws IOException {
        when(structureBS.retrieveLevelInstance(42L)).thenReturn(null);

        ResponseEntity<?> resp = controller.exportActionPlanCsv(42L, null, null, null, null, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(404, response.getStatus());
        verify(csvActionPlanExport, never()).exportLevelInstaceActionPlanCSV(anyList(), any());
    }

    @Test
    @DisplayName("exportActionPlanCsv: 403 quando não-admin e empresa não bate")
    void exportActionPlanCsv_forbidden_nonAdmin() throws IOException {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance s = sli(42L, p);

        when(structureBS.retrieveLevelInstance(42L)).thenReturn(s);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        ResponseEntity<?> resp = controller.exportActionPlanCsv(42L, null, null, null, null, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1));

        assertEquals(204, resp.getStatusCodeValue());
        assertEquals(403, response.getStatus());
        verify(csvActionPlanExport, never()).exportLevelInstaceActionPlanCSV(anyList(), any());
    }

	@Test
	@DisplayName("budget/exportCSV: sucesso chama CsvBudgetExport")
	void exportBudgets_success() throws Exception {
		Company comp = company(10L);
		when(domain.get().getCompany()).thenReturn(comp);
		when(companyBS.findCompanyByUserId(5L)).thenReturn(comp);

		PaginatedList<BudgetElement> page = new PaginatedList<>();
		page.setList(List.of(mock(BudgetElement.class)));
		when(budgetBS.listBudgetElement(comp)).thenReturn(page);

		ResponseEntity<?> resp = controller.exportBudgets(userSession(5L, AccessLevels.COLABORATOR.getLevel()));

		assertEquals(204, resp.getStatusCodeValue());
		assertTrue(response.getHeader("Content-Disposition").contains("budgets-10-"));
		assertEquals("application/octet-stream", response.getContentType());
		verify(csvBudgetExport).exportBudgetCSV(eq(page.getList()), any(OutputStream.class));
    }

    @Test
    @DisplayName("risk/exportCSV: 404 quando PlanRisk não encontrado")
    void exportRiskCsv_notFound() throws Exception {
        when(planBS.exists(111L, PlanRisk.class)).thenReturn(null);

        RiskExportRequestDto req = mock(RiskExportRequestDto.class);
        when(req.planRiskId()).thenReturn(111L);

        MockHttpServletResponse resp = new MockHttpServletResponse();
        controller.exportRiskCsv(req, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()), resp);

        assertEquals(404, resp.getStatus());
        verify(csvRiskExport, never()).exportRiskCsv(anyLong(), any(), any());
    }

    @Test
    @DisplayName("risk/linked-axes: sucesso com axis = -1 (ignora busca do eixo)")
    void exportAxesRiskCSV_success_axisMinusOne() throws Exception {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));

        when(planBS.retrieveById(101L)).thenReturn(p);
        when(domain.get().getCompany()).thenReturn(comp);

        RiskExportAxesCSVRequest req = mock(RiskExportAxesCSVRequest.class);
        when(req.planId()).thenReturn(101L);
        when(req.selectedAxis()).thenReturn(-1L);
        when(req.selectedYear()).thenReturn(2025);

        ResponseEntity<?> out = controller.exportAxesRiskCSV(req, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));
        assertEquals(200, out.getStatusCodeValue());
        verify(csvRiskExport).exportAxesRiskCSV(eq(101L), eq(-1L), eq(2025), any(OutputStream.class));
    }

    // // ============================== POST risk/exportCSV
    // // ==============================
    // @Test
    // @DisplayName("risk/exportCSV: sucesso (admin) → chama csvRiskExport.exportRiskCsv")
    // void exportRiskCsv_success_admin() throws Exception {
    //     Company comp = company(10L);
    //     PlanRisk planRisk = new PlanRisk();
    //     var policy = new org.forrisco.risk.policy.Policy();
    //     policy.setCompany(comp);
    //     planRisk.setPolicy(policy);
    //     planRisk.setId(999L);

    //     when(planBS.exists(999L, PlanRisk.class)).thenReturn(planRisk);
    //     when(domain.get().getCompany()).thenReturn(comp);

    //     RiskExportRequestDto req = mock(RiskExportRequestDto.class);
    //     when(req.planRiskId()).thenReturn(999L);
    //     when(req.filters()).thenReturn("{}");

    //     MockHttpServletResponse resp = new MockHttpServletResponse();
    //     controller.exportRiskCsv(req, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()), resp);

    //     assertEquals(200, resp.getStatus());
    //     assertTrue(resp.getHeader("Content-Disposition").contains("riscos-10-"));
    //     assertEquals("text/csv", resp.getContentType());
    //     verify(csvRiskExport).exportRiskCsv(eq(999L), any(RiskFilterParams.class), any(OutputStream.class));
    // }

    // @Test
    // @DisplayName("risk/exportCSV: 403 quando não-admin e empresa não bate")
    // void exportRiskCsv_forbidden_nonAdmin() throws Exception {
    //     Company comp = company(10L);
    //     PlanRisk planRisk = new PlanRisk();
    //     var policy = new org.forrisco.risk.policy.Policy();
    //     policy.setCompany(comp);
    //     planRisk.setPolicy(policy);
    //     planRisk.setId(999L);

    //     when(planBS.exists(999L, PlanRisk.class)).thenReturn(planRisk);
    //     when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

    //     RiskExportRequestDto req = mock(RiskExportRequestDto.class);
    //     when(req.planRiskId()).thenReturn(999L);
    //     when(req.filters()).thenReturn("{}");

    //     MockHttpServletResponse resp = new MockHttpServletResponse();
    //     controller.exportRiskCsv(req, userSession(123L, AccessLevels.SYSTEM_ADMIN.getLevel() - 1), resp);

    //     assertEquals(403, resp.getStatus());
    //     verify(csvRiskExport, never()).exportRiskCsv(anyLong(), any(), any());
    // }

    // @Test
    // @DisplayName("risk/exportCSV: erro interno (500) durante export")
    // void exportRiskCsv_internalError() throws Exception {
    //     Company comp = company(10L);
    //     PlanRisk planRisk = new PlanRisk();
    //     var policy = new org.forrisco.risk.policy.Policy();
    //     policy.setCompany(comp);
    //     planRisk.setPolicy(policy);
    //     planRisk.setId(999L);

    //     when(planBS.exists(999L, PlanRisk.class)).thenReturn(planRisk);
    //     when(domain.get().getCompany()).thenReturn(comp);

    //     RiskExportRequestDto req = mock(RiskExportRequestDto.class);
    //     when(req.planRiskId()).thenReturn(999L);
    //     when(req.filters()).thenReturn("{}");

    //     // Força erro no export
    //     doThrow(new RuntimeException("boom"))
    //             .when(csvRiskExport).exportRiskCsv(anyLong(), any(RiskFilterParams.class), any(OutputStream.class));

    //     MockHttpServletResponse resp = new MockHttpServletResponse();
    //     controller.exportRiskCsv(req, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()), resp);

    //     assertEquals(500, resp.getStatus());
    // }

    @Test
    @DisplayName("risk/linked-axes: sucesso com axis != -1 (busca SLI)")
    void exportAxesRiskCSV_success_axisGiven() throws Exception {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        StructureLevelInstance axis = sli(7L, p);

        when(planBS.retrieveById(101L)).thenReturn(p);
        when(structureBS.retrieveLevelInstance(7L)).thenReturn(axis);
        when(domain.get().getCompany()).thenReturn(comp);

        RiskExportAxesCSVRequest req = mock(RiskExportAxesCSVRequest.class);
        when(req.planId()).thenReturn(101L);
        when(req.selectedAxis()).thenReturn(7L);
        when(req.selectedYear()).thenReturn(2025);

        ResponseEntity<?> out = controller.exportAxesRiskCSV(req, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));
        assertEquals(200, out.getStatusCodeValue());
        verify(csvRiskExport).exportAxesRiskCSV(eq(101L), eq(7L), eq(2025), any(OutputStream.class));
    }

    @Test
    @DisplayName("risk/linked-axes: 404 quando plan não encontrado")
    void exportAxesRiskCSV_notFound_plan() throws IOException {
        when(planBS.retrieveById(101L)).thenReturn(null);

        RiskExportAxesCSVRequest req = mock(RiskExportAxesCSVRequest.class);
        when(req.planId()).thenReturn(101L);
        when(req.selectedAxis()).thenReturn(-1L);

        ResponseEntity<?> out = controller.exportAxesRiskCSV(req, userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));
        assertEquals(204, out.getStatusCodeValue());
        assertEquals(404, response.getStatus());
        verify(csvRiskExport, never()).exportAxesRiskCSV(anyLong(), anyLong(), anyInt(), any());
    }

    @Test
    @DisplayName("risk/linked-axes: 404 quando axis != -1 e SLI não encontrado")
    void exportAxesRiskCSV_notFound_axis() throws IOException {
        Company comp = company(10L);
        Plan p = plan(planMacro(100L, comp));
        when(planBS.retrieveById(101L)).thenReturn(p);
        when(structureBS.retrieveLevelInstance(7L)).thenReturn(null);

        RiskExportAxesCSVRequest req = mock(RiskExportAxesCSVRequest.class);
        when(req.planId()).thenReturn(101L);
        when(req.selectedAxis()).thenReturn(7L);
        when(req.selectedYear()).thenReturn(2025);

        ResponseEntity<?> out = controller.exportAxesRiskCSV(
                req,
                userSession(1L, AccessLevels.SYSTEM_ADMIN.getLevel()));

        assertEquals(404, response.getStatus());
        assertEquals(404, out.getStatusCodeValue());

        verify(csvRiskExport, never()).exportAxesRiskCSV(anyLong(), anyLong(), anyInt(), any());
    }
}
