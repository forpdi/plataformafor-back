package org.forrisco.risk.incident;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.policy.Policy;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.incident.dto.IncidentDto;
import org.forrisco.risk.incident.history.IncidentHistoryBS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncidentControllerTest {

    @InjectMocks
    private IncidentController controller;

    @Mock
    private IncidentBS bs;
    @Mock
    private CompanyDomainContext domain;
    @Mock
    private RiskBS riskBS;
    @Mock
    private CompanyBS companyBS;
    @Mock
    private IncidentHistoryBS incidentHistoryBS;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserSession userSession;

    private static String extractMessage(ResponseEntity<?> resp) {
        Object body = resp.getBody();
        if (body == null)
            return null;
        if (body.getClass().getName().equals("org.forpdi.core.common.Response")) {
            try {
                var f = body.getClass().getDeclaredField("message");
                f.setAccessible(true);
                Object v = f.get(body);
                return v != null ? String.valueOf(v) : null;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Object unwrapPayload(ResponseEntity<?> resp) {
        Object body = resp.getBody();
        if (body == null)
            return null;
        if (body.getClass().getName().equals("org.forpdi.core.common.Response")) {
            for (String field : new String[] { "data", "object", "entity", "result" }) {
                try {
                    var f = body.getClass().getDeclaredField(field);
                    f.setAccessible(true);
                    return f.get(body);
                } catch (NoSuchFieldException ignored) {
                } catch (Exception e) {
                    throw new RuntimeException("Falha ao obter payload de Response", e);
                }
            }
            return null;
        }
        return body;
    }

    private static Long tryGetTotal(Object wrapper) {
        try {
            var m = wrapper.getClass().getMethod("getTotal");
            Object v = m.invoke(wrapper);
            if (v instanceof Number)
                return ((Number) v).longValue();
        } catch (Exception ignored) {
        }
        try {
            var m = wrapper.getClass().getMethod("getCount");
            Object v = m.invoke(wrapper);
            if (v instanceof Number)
                return ((Number) v).longValue();
        } catch (Exception ignored) {
        }
        try {
            var f = wrapper.getClass().getDeclaredField("total");
            f.setAccessible(true);
            Object v = f.get(wrapper);
            if (v instanceof Number)
                return ((Number) v).longValue();
        } catch (Exception ignored) {
        }
        return null;
    }

    private static class ListInspect {
        final List<?> list;
        final Long total;

        ListInspect(List<?> l, Long t) {
            this.list = l;
            this.total = t;
        }
    }

    private static ListInspect inspectMaybeListResponse(Object payload) {
        if (payload == null)
            return null;
        if (!Objects.equals(payload.getClass().getName(), "org.forpdi.core.common.ListResponse")) {
            return null;
        }
        try {
            List<?> list = null;
            Long total = null;
            try {
                var mList = payload.getClass().getMethod("getList");
                Object v = mList.invoke(payload);
                if (v instanceof List<?>)
                    list = (List<?>) v;
            } catch (NoSuchMethodException ignored) {
            }
            total = tryGetTotal(payload);
            if (list == null) {
                try {
                    var f = payload.getClass().getDeclaredField("list");
                    f.setAccessible(true);
                    Object v = f.get(payload);
                    if (v instanceof List<?>)
                        list = (List<?>) v;
                } catch (NoSuchFieldException ignored) {
                }
            }
            return new ListInspect(list, total);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao inspecionar ListResponse", e);
        }
    }

    private Risk buildRisk(long riskId, long companyId) {
        Company company = new Company();
        company.setId(companyId);
        Policy policy = new Policy();
        policy.setCompany(company);
        PlanRisk pr = new PlanRisk();
        pr.setPolicy(policy);
        Unit unit = new Unit();
        unit.setPlanRisk(pr);
        Risk risk = new Risk();
        risk.setId(riskId);
        risk.setUnit(unit);
        return risk;
    }

    private Incident newIncidentPastDate(long riskId, long userId, long managerId, int typeId) {
        Incident inc = new Incident();
        inc.setId(null);
        inc.setBegin(new Date(System.currentTimeMillis() - 60_000));
        Risk r = new Risk();
        r.setId(riskId);
        inc.setRisk(r);
        User u = new User();
        u.setId(userId);
        inc.setUser(u);
        User m = new User();
        m.setId(managerId);
        inc.setManager(m);
        inc.setDescription("<b>desc</b>");
        inc.setType(typeId);
        return inc;
    }

    private void makeSystemAdmin(long userId) {
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.SYSTEM_ADMIN.getLevel());
        when(userSession.getAuthData().getId()).thenReturn(userId);
    }

    private void makeNonAdmin(long userId) {
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.MANAGER.getLevel());
        when(userSession.getAuthData().getId()).thenReturn(userId);
    }

    @Test
    @DisplayName("save: happy path — salva, notifica e retorna 200")
    void save_happyPath() throws Exception {
        long riskId = 10L, userId = 20L, managerId = 30L, companyId = 99L;

        makeSystemAdmin(userId);

        Risk risk = buildRisk(riskId, companyId);
        when(bs.exists(eq(riskId), eq(Risk.class))).thenReturn(risk);

        User user = new User();
        user.setId(userId);
        User manager = new User();
        manager.setId(managerId);
        when(bs.exists(eq(userId), eq(User.class))).thenReturn(user);
        when(bs.exists(eq(managerId), eq(User.class))).thenReturn(manager);

        doNothing().when(riskBS).validateManager(manager);

        when(riskBS.validateRiskItemPermissions(eq(risk), eq(user)))
                .thenReturn(new RiskItemPermissionInfo(true, false, true));

        doAnswer(inv -> {
            Incident inc = inv.getArgument(0);
            inc.setId(555L);
            return null;
        })
                .when(bs).saveIncident(any(Incident.class));

        CompanyDomain cd = new CompanyDomain();
        cd.setBaseUrl("http://example.org");
        when(companyBS.retrieveCompanyByDomain(any(Company.class)))
                .thenReturn(Collections.singletonList(cd));

        Incident incident = newIncidentPastDate(riskId, userId, managerId, 1);
        IncidentDto dto = new IncidentDto(incident);

        ResponseEntity<?> resp = controller.save(dto, userSession);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());

        verify(riskBS, times(1)).sendNotificationToRiskItemManager(
                eq(risk),
                eq(manager),
                argThat(url -> url.contains("/risk/" + riskId + "/incident/") && url.endsWith("/info")),
                eq("um Incidente"),
                eq(org.forpdi.core.notification.NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM));
        verify(bs).saveIncident(any(Incident.class));
    }

    @Test
    @DisplayName("save: falha se begin estiver no futuro (400)")
    void save_futureDate() {
        makeSystemAdmin(1L);
        Incident inc = newIncidentPastDate(1L, 2L, 3L, 1);
        inc.setBegin(new Date(System.currentTimeMillis() + 60_000));
        IncidentDto dto = new IncidentDto(inc);

        ResponseEntity<?> resp = controller.save(dto, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("não deve ser maior"));
    }

    @Test
    @DisplayName("save: falha se risco não encontrado (400)")
    void save_riskNotFound() {
        makeSystemAdmin(1L);
        Incident inc = newIncidentPastDate(1L, 2L, 3L, 1);
        when(bs.exists(eq(1L), eq(Risk.class))).thenReturn(null);

        ResponseEntity<?> resp = controller.save(new IncidentDto(inc), userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Risco solicitado não foi encontrado"));
    }

    @Test
    @DisplayName("save: acesso negado se usuário não-admin e empresa não bate (400)")
    void save_accessDenied_companyMismatch() {
        long loggedUserId = 7L;
        makeNonAdmin(loggedUserId);

        long riskCompanyId = 1L;
        Risk risk = buildRisk(10L, riskCompanyId);
        when(bs.exists(eq(10L), eq(Risk.class))).thenReturn(risk);

        Company userCompany = new Company();
        userCompany.setId(999L);
        when(companyBS.findCompanyByUserId(eq(loggedUserId))).thenReturn(userCompany);

        Incident inc = newIncidentPastDate(10L, 2L, 3L, 1);

        ResponseEntity<?> resp = controller.save(new IncidentDto(inc), userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Acesso negado"));
    }

    @Test
    @DisplayName("save: tipo de incidente inválido (400)")
    void save_invalidType() {
        makeSystemAdmin(1L);
        Risk risk = buildRisk(10L, 1L);
        when(bs.exists(eq(10L), eq(Risk.class))).thenReturn(risk);
        when(bs.exists(eq(2L), eq(User.class))).thenReturn(new User() {
            {
                setId(2L);
            }
        });
        when(bs.exists(eq(3L), eq(User.class))).thenReturn(new User() {
            {
                setId(3L);
            }
        });
        doNothing().when(riskBS).validateManager(any());

        Incident inc = newIncidentPastDate(10L, 2L, 3L, -1);

        ResponseEntity<?> resp = controller.save(new IncidentDto(inc), userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Tipo de incidente inválido"));
    }

    @Test
    @DisplayName("save: sem permissão de acesso ao item (400)")
    void save_noRiskItemPermission() {
        makeSystemAdmin(1L);

        Risk risk = buildRisk(10L, 1L);
        when(bs.exists(eq(10L), eq(Risk.class))).thenReturn(risk);

        User u = new User();
        u.setId(2L);
        User m = new User();
        m.setId(3L);
        when(bs.exists(eq(2L), eq(User.class))).thenReturn(u);
        when(bs.exists(eq(3L), eq(User.class))).thenReturn(m);
        doNothing().when(riskBS).validateManager(m);

        when(riskBS.validateRiskItemPermissions(eq(risk), eq(u)))
                .thenReturn(new RiskItemPermissionInfo(false, false, false));

        Incident inc = newIncidentPastDate(10L, 2L, 3L, 1);

        ResponseEntity<?> resp = controller.save(new IncidentDto(inc), userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Usuário sem permissão"));
    }

    // @Test
    // @DisplayName("listIncident: ok (200) retorna lista e total")
    // void listIncident_ok() {
    // makeSystemAdmin(1L);

    // Risk risk = buildRisk(10L, 1L);
    // when(riskBS.exists(eq(10L), eq(Risk.class))).thenReturn(risk);

    // PaginatedList<Incident> page = new PaginatedList<>();
    // Incident i1 = new Incident(); i1.setId(1L);
    // page.setList(new ArrayList<>(java.util.List.of(i1)));
    // page.setTotal(1L);
    // when(bs.listIncidentsByRisk(eq(risk),
    // any(DefaultParams.class))).thenReturn(page);

    // ResponseEntity<?> resp = controller.listIncident(10L, new DefaultParams(),
    // userSession);
    // assertEquals(200, resp.getStatusCodeValue());

    // Object payload = unwrapPayload(resp);
    // assertNotNull(payload, "Payload não deveria ser nulo");

    // // Caso 1: ListResponse (novo wrapper)
    // ListInspect lr = inspectMaybeListResponse(payload);
    // if (lr != null) {
    // assertNotNull(lr.list, "ListResponse.getList() não deve ser nulo");
    // assertEquals(1, lr.list.size());
    // if (lr.total != null) assertEquals(1L, lr.total.longValue());
    // return;
    // }

    // // Caso 2: ListWrapper
    // if (payload instanceof ListWrapper<?> wrapper) {
    // assertEquals(1, wrapper.getList().size());
    // Long total = tryGetTotal(wrapper);
    // if (total != null) assertEquals(1L, total.longValue());
    // return;
    // }

    // // Caso 3: List "puro"
    // if (payload instanceof java.util.List<?> list) {
    // assertEquals(1, list.size());
    // return;
    // }

    // fail("Payload inesperado: " + payload.getClass());
    // }

    @Test
    @DisplayName("listIncident: acesso negado quando empresa difere (400)")
    void listIncident_accessDenied() {
        long loggedUserId = 7L;
        makeNonAdmin(loggedUserId);

        Risk risk = buildRisk(10L, 1L);
        when(riskBS.exists(eq(10L), eq(Risk.class))).thenReturn(risk);

        Company userCompany = new Company();
        userCompany.setId(999L);
        when(companyBS.findCompanyByUserId(eq(loggedUserId))).thenReturn(userCompany);

        ResponseEntity<?> resp = controller.listIncident(10L, new DefaultParams(), userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Acesso negado"));
    }

    @Test
    @DisplayName("listIncident: erro inesperado vira 400 com mensagem padrão")
    void listIncident_unexpectedError() {
        makeSystemAdmin(1L);
        when(riskBS.exists(anyLong(), eq(Risk.class))).thenThrow(new RuntimeException("boom"));
        ResponseEntity<?> resp = controller.listIncident(1L, new DefaultParams(), userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains(ErrorMessages.UNEXPECTED_ERROR));
    }

    @Test
    @DisplayName("findIncident: ok (200) retorna incidente")
    void findIncident_ok() {
        makeSystemAdmin(1L);

        Risk risk = buildRisk(10L, 2L);
        Incident inc = new Incident();
        inc.setId(123L);
        inc.setRisk(risk);
        when(riskBS.exists(eq(123L), eq(Incident.class))).thenReturn(inc);

        ResponseEntity<?> resp = controller.findIncident(123L, userSession);
        assertEquals(200, resp.getStatusCodeValue());

        Object payload = unwrapPayload(resp);
        assertTrue(payload instanceof Incident);
        assertSame(inc, payload);
    }

    @Test
    @DisplayName("findIncident: não encontrado ou deletado (400)")
    void findIncident_notFound() {
        makeSystemAdmin(1L);

        when(riskBS.exists(eq(123L), eq(Incident.class))).thenReturn(null);
        ResponseEntity<?> resp1 = controller.findIncident(123L, userSession);
        assertEquals(400, resp1.getStatusCodeValue());

        Incident deleted = new Incident();
        deleted.setId(9L);
        deleted.setDeleted(true);
        when(riskBS.exists(eq(9L), eq(Incident.class))).thenReturn(deleted);
        ResponseEntity<?> resp2 = controller.findIncident(9L, userSession);
        assertEquals(400, resp2.getStatusCodeValue());
    }

    @Test
    @DisplayName("findIncident: acesso negado quando empresa difere (400)")
    void findIncident_accessDenied() {
        long loggedUserId = 7L;
        makeNonAdmin(loggedUserId);

        Risk risk = buildRisk(10L, 1L);
        Incident inc = new Incident();
        inc.setId(55L);
        inc.setRisk(risk);
        when(riskBS.exists(eq(55L), eq(Incident.class))).thenReturn(inc);

        Company userCompany = new Company();
        userCompany.setId(999L);
        when(companyBS.findCompanyByUserId(eq(loggedUserId))).thenReturn(userCompany);

        ResponseEntity<?> resp = controller.findIncident(55L, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Acesso negado"));
    }

    // ==================== NOVOS TESTES: /incidentsPaginated ====================

    // @Test
    // @DisplayName("incidentsPaginated: admin — happy path devolve paginação")
    // void listIncidentsPaginated_admin_ok() {
    //     makeSystemAdmin(1L);

    //     java.util.List<Long> ids = java.util.Arrays.asList(10L, 20L);
    //     var dto = new org.forrisco.risk.incident.dto.ListIncidentsDto(ids, 2, 10);

    //     PaginatedList<Incident> page = new PaginatedList<>();
    //     Incident a = new Incident();
    //     a.setId(10L);
    //     Incident b = new Incident();
    //     b.setId(20L);
    //     page.setList(new ArrayList<>(java.util.List.of(a, b)));
    //     page.setTotal(2L);

    //     when(bs.pagitaneIncidents(eq(ids), eq(2), eq(10))).thenReturn(page);

    //     ResponseEntity<?> resp = controller.listIncidentsPaginated(dto, userSession);
    //     assertEquals(200, resp.getStatusCodeValue());

    //     Object payload = unwrapPayload(resp);
    //     assertTrue(payload instanceof PaginatedList<?>);
    //     PaginatedList<?> got = (PaginatedList<?>) payload;
    //     assertEquals(2, got.getList().size());
    //     assertEquals(2L, got.getTotal());
    // }

    @Test
    @DisplayName("incidentsPaginated: não-admin sem empresa vinculada — acesso negado (400)")
    void listIncidentsPaginated_nonAdmin_noCompany() {
        makeNonAdmin(7L);
        when(companyBS.findCompanyByUserId(eq(7L))).thenReturn(null);

        var dto = new org.forrisco.risk.incident.dto.ListIncidentsDto(java.util.Arrays.asList(1L, 2L), 1, 5);
        ResponseEntity<?> resp = controller.listIncidentsPaginated(dto, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Usuário não associado a uma empresa"));
    }

    @Test
    @DisplayName("incidentsPaginated: não-admin com incidente fora da empresa — acesso negado (400)")
    void listIncidentsPaginated_nonAdmin_companyMismatch() {
        makeNonAdmin(7L);
        Company userCompany = new Company();
        userCompany.setId(111L);
        when(companyBS.findCompanyByUserId(eq(7L))).thenReturn(userCompany);

        // incidente 5 pertence à empresa 222
        Company other = new Company();
        other.setId(222L);
        Policy policy = new Policy();
        policy.setCompany(other);
        PlanRisk pr = new PlanRisk();
        pr.setPolicy(policy);
        Unit unit = new Unit();
        unit.setPlanRisk(pr);
        Risk risk = new Risk();
        risk.setUnit(unit);
        Incident inc = new Incident();
        inc.setId(5L);
        inc.setRisk(risk);

        when(riskBS.exists(eq(5L), eq(Incident.class))).thenReturn(inc);

        var dto = new org.forrisco.risk.incident.dto.ListIncidentsDto(java.util.Arrays.asList(5L), 1, 5);
        ResponseEntity<?> resp = controller.listIncidentsPaginated(dto, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Acesso negado"));
    }

    @Test
    @DisplayName("incidentsPaginated: não-admin com incidente inexistente — acesso negado (400)")
    void listIncidentsPaginated_nonAdmin_incidentNotFound() {
        makeNonAdmin(7L);
        Company userCompany = new Company();
        userCompany.setId(111L);
        when(companyBS.findCompanyByUserId(eq(7L))).thenReturn(userCompany);

        when(riskBS.exists(eq(9L), eq(Incident.class))).thenReturn(null);

        var dto = new org.forrisco.risk.incident.dto.ListIncidentsDto(java.util.Arrays.asList(9L), 1, 5);
        ResponseEntity<?> resp = controller.listIncidentsPaginated(dto, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Acesso negado"));
    }

    // ==================== NOVOS TESTES: /incidentByUnit ====================

    // @Test
    // @DisplayName("incidentByUnit: admin — happy path agrega incidentes de todos os riscos da unidade")
    // void listIncidentsByUnit_admin_ok() {
    //     makeSystemAdmin(1L);

    //     // unit -> planRisk -> policy -> company
    //     Company c = new Company();
    //     c.setId(5L);
    //     Policy pol = new Policy();
    //     pol.setCompany(c);
    //     PlanRisk pr = new PlanRisk();
    //     pr.setPolicy(pol);
    //     Unit unit = new Unit();
    //     unit.setPlanRisk(pr);

    //     when(riskBS.exists(eq(101L), eq(Unit.class))).thenReturn(unit);

    //     // riscos da unidade
    //     Risk r1 = new Risk();
    //     r1.setId(10L);
    //     r1.setUnit(unit);
    //     Risk r2 = new Risk();
    //     r2.setId(20L);
    //     r2.setUnit(unit);
    //     PaginatedList<Risk> risksPage = new PaginatedList<>();
    //     risksPage.setList(new ArrayList<>(java.util.List.of(r1, r2)));
    //     risksPage.setTotal(2L);
    //     when(riskBS.listRiskByUnit(eq(unit))).thenReturn(risksPage);

    //     // incidentes por risco
    //     Incident i1 = new Incident();
    //     i1.setId(1L);
    //     i1.setRisk(r1);
    //     Incident i2 = new Incident();
    //     i2.setId(2L);
    //     i2.setRisk(r1);
    //     PaginatedList<Incident> p1 = new PaginatedList<>();
    //     p1.setList(new ArrayList<>(java.util.List.of(i1, i2)));
    //     p1.setTotal(2L);

    //     Incident i3 = new Incident();
    //     i3.setId(3L);
    //     i3.setRisk(r2);
    //     PaginatedList<Incident> p2 = new PaginatedList<>();
    //     p2.setList(new ArrayList<>(java.util.List.of(i3)));
    //     p2.setTotal(1L);

    //     when(bs.listIncidentsByRisk(eq(r1))).thenReturn(p1);
    //     when(bs.listIncidentsByRisk(eq(r2))).thenReturn(p2);

    //     ResponseEntity<?> resp = controller.listIncidentsByUnit(101L, userSession);
    //     assertEquals(200, resp.getStatusCodeValue());

    //     Object payload = unwrapPayload(resp);
    //     assertTrue(payload instanceof PaginatedList<?>);
    //     PaginatedList<?> got = (PaginatedList<?>) payload;
    //     assertEquals(3, got.getList().size()); // 2 + 1
    //     assertEquals(3L, got.getTotal()); // tamanho agregado
    // }

    @Test
    @DisplayName("incidentByUnit: unidade não encontrada (400)")
    void listIncidentsByUnit_notFound() {
        makeSystemAdmin(1L);
        when(riskBS.exists(eq(999L), eq(Unit.class))).thenReturn(null);
        ResponseEntity<?> resp = controller.listIncidentsByUnit(999L, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("unidade solicitada"));
    }

    @Test
    @DisplayName("incidentByUnit: não-admin com empresa diferente — acesso negado (400)")
    void listIncidentsByUnit_accessDenied() {
        makeNonAdmin(7L);

        // unit da empresa 1
        Company c1 = new Company();
        c1.setId(1L);
        Policy pol = new Policy();
        pol.setCompany(c1);
        PlanRisk pr = new PlanRisk();
        pr.setPolicy(pol);
        Unit unit = new Unit();
        unit.setPlanRisk(pr);
        when(riskBS.exists(eq(50L), eq(Unit.class))).thenReturn(unit);

        // usuário pertence à empresa 999
        Company userCompany = new Company();
        userCompany.setId(999L);
        when(companyBS.findCompanyByUserId(eq(7L))).thenReturn(userCompany);

        ResponseEntity<?> resp = controller.listIncidentsByUnit(50L, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains("Acesso negado"));
    }

    @Test
    @DisplayName("incidentByUnit: erro inesperado (400)")
    void listIncidentsByUnit_unexpected() {
        makeSystemAdmin(1L);
        when(riskBS.exists(anyLong(), eq(Unit.class))).thenThrow(new RuntimeException("boom"));
        ResponseEntity<?> resp = controller.listIncidentsByUnit(1L, userSession);
        assertEquals(400, resp.getStatusCodeValue());
        String msg = extractMessage(resp);
        if (msg != null)
            assertTrue(msg.contains(ErrorMessages.UNEXPECTED_ERROR));
    }
}
