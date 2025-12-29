package org.forrisco.risk.contingency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.security.auth.AuthData;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.contingency.dto.ContingencyDto;
import org.forrisco.risk.contingency.history.ContingencyHistoryBS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class ContingencyControllerTest {

    private ContingencyController controller;

    private ContingencyBS bs;
    private CompanyDomainContext domain;
    private RiskBS riskBS;
    private CompanyBS companyBS;
    private ContingencyHistoryBS historyBS;

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = ContingencyController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Company company(long id) {
        Company c = new Company();
        c.setId(id);
        return c;
    }

    private static Risk mockedRiskWithCompany(long companyId, Long riskId) {
        Risk risk = mock(Risk.class, RETURNS_DEEP_STUBS);
        when(risk.getUnit().getPlanRisk().getPolicy().getCompany().getId()).thenReturn(companyId);
        if (riskId != null) when(risk.getId()).thenReturn(riskId);
        return risk;
    }

    private static Contingency contingencyOf(Risk risk, long userId, long managerId, Long contingencyId) {
        Contingency c = new Contingency();
        c.setRisk(risk);

        User u = new User(); u.setId(userId);
        c.setUser(u);

        User m = new User(); m.setId(managerId);
        c.setManager(m);

        if (contingencyId != null) c.setId(contingencyId);
        return c;
    }

    private static UserSession userSession(long userId, int level) {
        UserSession us = mock(UserSession.class, RETURNS_DEEP_STUBS);
        when(us.getAccessLevel()).thenReturn(level);

        AuthData ad = mock(AuthData.class);
        when(ad.getId()).thenReturn(userId);
        when(us.getAuthData()).thenReturn(ad);
        return us;
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new ContingencyController();

        bs = mock(ContingencyBS.class);
        domain = mock(CompanyDomainContext.class, RETURNS_DEEP_STUBS);
        riskBS = mock(RiskBS.class);
        companyBS = mock(CompanyBS.class);
        historyBS = mock(ContingencyHistoryBS.class);

        inject(controller, "bs", bs);
        inject(controller, "domain", domain);
        inject(controller, "riskBS", riskBS);
        inject(controller, "companyBS", companyBS);
        inject(controller, "contingencyHistoryBS", historyBS);
    }

    @Test
    @DisplayName("save: sucesso (admin) → persiste e notifica")
    void save_success_admin() throws Throwable {
        Risk risk = mockedRiskWithCompany(10L, 500L);
        Contingency input = contingencyOf(risk, 2L, 3L, null);
        ContingencyDto dto = new ContingencyDto(input);

        when(bs.exists(500L, Risk.class)).thenReturn(risk);
        when(bs.exists(2L, User.class)).thenReturn(new User());
        when(bs.exists(3L, User.class)).thenReturn(new User());

        doNothing().when(riskBS).validateManager(any(User.class));
        doNothing().when(riskBS).validateVigency(any(), any());

        RiskItemPermissionInfo pi = mock(RiskItemPermissionInfo.class);
        when(pi.isRiskResponsibleOrHasPermission()).thenReturn(true);
        when(riskBS.validateRiskItemPermissions(eq(risk), any(User.class))).thenReturn(pi);

        CompanyDomain cd = new CompanyDomain();
        cd.setBaseUrl("https://dominio");
        when(companyBS.retrieveCompanyByDomain(any(Company.class))).thenReturn(List.of(cd));

        UserSession us = userSession(99L, AccessLevels.SYSTEM_ADMIN.getLevel());
        ResponseEntity<?> resp = controller.save(dto, us);
        assertEquals(200, resp.getStatusCodeValue());
        verify(bs).saveContingency(any(Contingency.class));
        verify(riskBS).sendNotificationToRiskItemManager(eq(risk), any(User.class),
                contains("/contingency/"), contains("Contingenciamento"),
                eq(NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM));
    }

    @Test
    @DisplayName("save: acesso negado quando não é admin e empresa não bate")
    void save_accessDenied_companyMismatch() {
        Risk risk = mockedRiskWithCompany(10L, 500L);
        ContingencyDto dto = new ContingencyDto(contingencyOf(risk, 2L, 3L, null));

        when(bs.exists(500L, Risk.class)).thenReturn(risk);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        UserSession us = userSession(123L, NON_ADMIN);
        ResponseEntity<?> resp = controller.save(dto, us);

        assertEquals(400, resp.getStatusCodeValue());
        verify(bs, never()).saveContingency(any());
    }

    @Test
    @DisplayName("listContingencies: sucesso (mesma empresa) retorna lista/total")
    void listContingencies_success() {
        Risk risk = mockedRiskWithCompany(10L, null);
        when(riskBS.exists(7L, Risk.class)).thenReturn(risk);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(10L));

        PaginatedList<Contingency> page = new PaginatedList<>();
        page.setList(List.of(new Contingency(), new Contingency()));
        page.setTotal(2L);
        when(bs.listContingenciesByRisk(eq(risk), any(DefaultParams.class))).thenReturn(page);

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        UserSession us = userSession(123L, NON_ADMIN);
        ResponseEntity<?> resp = controller.listContingencies(7L, new DefaultParams(), us);

        assertEquals(200, resp.getStatusCodeValue());
        verify(bs).listContingenciesByRisk(eq(risk), any(DefaultParams.class));
    }

    @Test
    @DisplayName("listContingencies: acesso negado quando empresa não bate")
    void listContingencies_denied() {
        Risk risk = mockedRiskWithCompany(10L, null);
        when(riskBS.exists(7L, Risk.class)).thenReturn(risk);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        UserSession us = userSession(123L, NON_ADMIN);
        ResponseEntity<?> resp = controller.listContingencies(7L, new DefaultParams(), us);

        assertEquals(400, resp.getStatusCodeValue());
        verify(bs, never()).listContingenciesByRisk(any(Risk.class), any(DefaultParams.class));
    }

    @Test
    @DisplayName("findContingency: não encontrado")
    void findContingency_notFound() {
        when(bs.findByContingencyId(9L)).thenReturn(null);

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        ResponseEntity<?> resp = controller.findContingency(9L, userSession(1L, NON_ADMIN));
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("findContingency: sucesso (mesma empresa)")
    void findContingency_success() {
        Risk risk = mockedRiskWithCompany(10L, null);
        Contingency c = contingencyOf(risk, 2L, 3L, null);
        when(bs.findByContingencyId(9L)).thenReturn(c);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(10L));

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        ResponseEntity<?> resp = controller.findContingency(9L, userSession(123L, NON_ADMIN));
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deleteContigency: sem permissão de acesso")
    void deleteContingency_noPermission() {
        Risk risk = mockedRiskWithCompany(10L, null);
        Contingency c = contingencyOf(risk, 2L, 3L, null);
        when(riskBS.exists(8L, Contingency.class)).thenReturn(c);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(10L));

        RiskItemPermissionInfo pi = mock(RiskItemPermissionInfo.class);
        when(pi.isRiskResponsibleOrHasPermission()).thenReturn(false);
        when(riskBS.validateRiskItemPermissions(eq(risk), eq(c.getUser()))).thenReturn(pi);

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        ResponseEntity<?> resp = controller.deleteContigency(8L, userSession(123L, NON_ADMIN));
        assertEquals(400, resp.getStatusCodeValue());
        verify(bs, never()).delete(any());
    }

    @Test
    @DisplayName("deleteContigency: sucesso")
    void deleteContingency_success() {
        Risk risk = mockedRiskWithCompany(10L, null);
        Contingency c = contingencyOf(risk, 2L, 3L, null);
        when(riskBS.exists(8L, Contingency.class)).thenReturn(c);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(10L));

        RiskItemPermissionInfo pi = mock(RiskItemPermissionInfo.class);
        when(pi.isRiskResponsibleOrHasPermission()).thenReturn(true);
        when(riskBS.validateRiskItemPermissions(eq(risk), eq(c.getUser()))).thenReturn(pi);

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        ResponseEntity<?> resp = controller.deleteContigency(8L, userSession(123L, NON_ADMIN));
        assertEquals(200, resp.getStatusCodeValue());
        verify(bs).delete(c);
    }

    @Test
    @DisplayName("updateContingency: sucesso com managerChanged = true (notificação LINKED)")
    void updateContingency_success_managerChanged() throws Throwable {
        Risk risk = mockedRiskWithCompany(10L, 700L);

        Contingency oldC = contingencyOf(risk, 2L, 30L, 55L);
        Contingency newC = contingencyOf(risk, 2L, 31L, 55L);
        ContingencyDto dto = new ContingencyDto(newC);

        when(riskBS.exists(700L, Risk.class)).thenReturn(risk);
        when(riskBS.exists(55L, Contingency.class)).thenReturn(oldC);

        when(riskBS.exists(2L, User.class)).thenReturn(new User());
        when(riskBS.exists(31L, User.class)).thenReturn(new User());
        doNothing().when(riskBS).validateManager(any(User.class));
        doNothing().when(riskBS).validateVigency(any(), any());

        RiskItemPermissionInfo pi = mock(RiskItemPermissionInfo.class);
        when(pi.isRiskResponsibleOrHasPermission()).thenReturn(true);
        when(riskBS.validateRiskItemPermissions(eq(risk), eq(oldC.getUser()))).thenReturn(pi);

        when(riskBS.exists(999L, User.class)).thenReturn(new User());

        CompanyDomain cd = new CompanyDomain(); cd.setBaseUrl("https://dominio");
        when(companyBS.retrieveCompanyByDomain(any(Company.class))).thenReturn(List.of(cd));
        when(companyBS.findCompanyByUserId(999L)).thenReturn(company(10L));

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        UserSession us = userSession(999L, NON_ADMIN);
        ResponseEntity<?> resp = controller.updateContingency(dto, us);

        assertEquals(200, resp.getStatusCodeValue());
        verify(historyBS).saveContingencyChanges(eq(oldC), any(Contingency.class), any(User.class));
        verify(bs).saveContingency(eq(oldC));
        verify(riskBS).sendNotificationToRiskItemManager(eq(risk), any(User.class),
                contains("/contingency/55"), contains("Contingenciamento"),
                eq(NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM));
    }

    @Test
    @DisplayName("updateContingency: sucesso com managerChanged = false (UPDATED + texto capitalizado)")
    void updateContingency_success_sameManager() throws Exception {
        Risk risk = mockedRiskWithCompany(10L, 700L);

        User sharedManager = new User();
        sharedManager.setId(31L);

        Contingency oldC = contingencyOf(risk, 2L, 31L, 55L);
        Contingency newC = contingencyOf(risk, 2L, 31L, 55L);
        oldC.setManager(sharedManager);
        newC.setManager(sharedManager);

        ContingencyDto dto = new ContingencyDto(newC);

        when(riskBS.exists(700L, Risk.class)).thenReturn(risk);
        when(riskBS.exists(55L, Contingency.class)).thenReturn(oldC);

        when(riskBS.exists(2L, User.class)).thenReturn(new User());
        when(riskBS.exists(31L, User.class)).thenReturn(sharedManager);

        doNothing().when(riskBS).validateManager(any(User.class));
        doNothing().when(riskBS).validateVigency(any(), any());

        RiskItemPermissionInfo pi = mock(RiskItemPermissionInfo.class);
        when(pi.isRiskResponsibleOrHasPermission()).thenReturn(true);
        when(riskBS.validateRiskItemPermissions(eq(risk), eq(oldC.getUser()))).thenReturn(pi);

        when(riskBS.exists(999L, User.class)).thenReturn(new User());

        CompanyDomain cd = new CompanyDomain();
        cd.setBaseUrl("https://dominio");
        when(companyBS.retrieveCompanyByDomain(any(Company.class))).thenReturn(List.of(cd));

        when(companyBS.findCompanyByUserId(999L)).thenReturn(company(10L));

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        UserSession us = userSession(999L, NON_ADMIN);

        ResponseEntity<?> resp = controller.updateContingency(dto, us);

        assertEquals(200, resp.getStatusCodeValue());
        verify(riskBS).sendNotificationToRiskItemManager(
                eq(risk),
                any(User.class),
                contains("/contingency/55"),
                eq("Um Contingenciamento"),
                eq(NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED));
    }

    @Test
    @DisplayName("listEditHistory: sucesso (mesma empresa) retorna lista/total")
    void listEditHistory_success() {
        Risk risk = mockedRiskWithCompany(10L, null);
        Contingency c = contingencyOf(risk, 2L, 3L, 99L);

        when(riskBS.exists(99L, Contingency.class)).thenReturn(c);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(10L));

        PaginatedList page = new PaginatedList();
        page.setList(Collections.emptyList());
        page.setTotal(0L);

        when(historyBS.listHistoryByContingency(eq(99L), any(DefaultParams.class)))
                .thenReturn(page);

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        ResponseEntity<?> resp = controller.listEditHistory(99L, new DefaultParams(), userSession(123L, NON_ADMIN));
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("listEditHistory: acesso negado quando empresa não bate")
    void listEditHistory_denied() {
        Risk risk = mockedRiskWithCompany(10L, null);
        Contingency c = contingencyOf(risk, 2L, 3L, 99L);

        when(riskBS.exists(99L, Contingency.class)).thenReturn(c);
        when(companyBS.findCompanyByUserId(123L)).thenReturn(company(99L));

        int NON_ADMIN = AccessLevels.SYSTEM_ADMIN.getLevel() - 1;
        ResponseEntity<?> resp = controller.listEditHistory(99L, new DefaultParams(), userSession(123L, NON_ADMIN));
        assertEquals(400, resp.getStatusCodeValue());
    }
}
