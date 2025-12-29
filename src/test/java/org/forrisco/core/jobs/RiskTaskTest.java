package org.forrisco.core.jobs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Order;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.jobs.EmailSenderTask;
import org.forpdi.core.notification.Notification;
import org.forpdi.core.notification.NotificationEmail;
import org.forpdi.system.jobsetup.JobsSetup;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.Risk;
import org.forrisco.risk.monitor.Monitor;
import org.forrisco.core.policy.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários para RiskTask.
 */
@ExtendWith(MockitoExtension.class)
class RiskTaskTest {

    @InjectMocks
    private RiskTask task;

    @Mock
    private HibernateDAO dao;
    @Mock
    private EmailSenderTask emailSenderTask;

    private void stubFluentCriteria(Criteria c) {
        lenient().when(c.add(any())).thenReturn(c);
        lenient().when(c.addOrder(any(Order.class))).thenReturn(c);
        lenient().when(c.createAlias(anyString(), anyString())).thenReturn(c);
        lenient().when(c.setProjection(any())).thenReturn(c);
        lenient().when(c.setFirstResult(anyInt())).thenReturn(c);
        lenient().when(c.setMaxResults(anyInt())).thenReturn(c);
    }

    private static void setId(Object entity, long id) {
        try {
            Field f = entity.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void beforeEach() {
    }

    @Test
    @DisplayName("execute: envia notificações para risco 'próximo ao vencimento' (3 usuários) e URL normalizada")
    void execute_sendsNotifications_forCloseToMaturity() {
        Criteria countCriteria = mock(Criteria.class);
        Criteria listCriteria = mock(Criteria.class);
        stubFluentCriteria(countCriteria);
        stubFluentCriteria(listCriteria);

        when(dao.newCriteria(eq(Risk.class))).thenReturn(countCriteria, listCriteria);
        when(countCriteria.uniqueResult()).thenReturn(1L);

        Risk risk = mock(Risk.class);
        when(risk.getId()).thenReturn(700L);
        when(risk.getName()).thenReturn("Risco X");
        when(risk.getPeriodicity()).thenReturn("custom");
        when(risk.getBegin()).thenReturn(new Date(System.currentTimeMillis() - 86_400_000L));

        org.forpdi.core.user.User u1 = mock(org.forpdi.core.user.User.class);
        when(u1.getEmail()).thenReturn("u1@mail");
        when(u1.getName()).thenReturn("U1");
        org.forpdi.core.user.User u2 = mock(org.forpdi.core.user.User.class);
        when(u2.getEmail()).thenReturn("u2@mail");
        when(u2.getName()).thenReturn("U2");
        org.forpdi.core.user.User u3 = mock(org.forpdi.core.user.User.class);
        when(u3.getEmail()).thenReturn("u3@mail");
        when(u3.getName()).thenReturn("U3");

        when(risk.getUser()).thenReturn(u1);
        when(risk.getManager()).thenReturn(u2);

        var unit = mock(org.forrisco.core.unit.Unit.class, Answers.RETURNS_DEEP_STUBS);
        when(unit.getUser()).thenReturn(u3);

        PlanRisk planRiskInRiskGraph = mock(PlanRisk.class);
        when(planRiskInRiskGraph.getId()).thenReturn(999L);
        when(planRiskInRiskGraph.getValidityEnd()).thenReturn(new Date(System.currentTimeMillis() + 86_400_000L));
        when(unit.getPlanRisk()).thenReturn(planRiskInRiskGraph);
        when(risk.getUnit()).thenReturn(unit);

        when(dao.findByCriteria(eq(listCriteria), eq(Risk.class))).thenReturn(List.of(risk));

        Criteria monitorCriteria = mock(Criteria.class);
        stubFluentCriteria(monitorCriteria);
        when(dao.newCriteria(eq(Monitor.class))).thenReturn(monitorCriteria);

        Date lastMonitorDateWithinWindow = new Date(System.currentTimeMillis() - (JobsSetup.RISK_TASK_FIXED_RATE / 2));
        Monitor lastMonitor = mock(Monitor.class);
        when(lastMonitor.getBegin()).thenReturn(lastMonitorDateWithinWindow);
        when(monitorCriteria.uniqueResult()).thenReturn(lastMonitor);

        PlanRisk planRiskFromDao = mock(PlanRisk.class, Answers.RETURNS_DEEP_STUBS);
        Policy policy = new Policy();
        Company company = new Company();
        company.setId(10L);
        policy.setCompany(company);
        when(planRiskFromDao.getPolicy()).thenReturn(policy);
        when(dao.exists(eq(999L), eq(PlanRisk.class))).thenReturn(planRiskFromDao);

        Criteria cdCriteria = mock(Criteria.class);
        stubFluentCriteria(cdCriteria);
        when(dao.newCriteria(eq(CompanyDomain.class))).thenReturn(cdCriteria);

        CompanyDomain cd = mock(CompanyDomain.class);
        when(cd.getCompany()).thenReturn(company);
        when(cd.getBaseUrl()).thenReturn("https://dominio/");
        when(cdCriteria.uniqueResult()).thenReturn(cd);

        task.execute();

        verify(emailSenderTask, times(3)).add(any(NotificationEmail.class));

        ArgumentCaptor<Notification> notifCap = ArgumentCaptor.forClass(Notification.class);
        verify(dao, atLeast(1)).persist(notifCap.capture());

        boolean hasNormalizedUrl = notifCap.getAllValues().stream()
                .map(Notification::getUrl)
                .anyMatch(url -> "https://dominio/#/forrisco/risk/700/details/monitors".equals(url));
        org.junit.jupiter.api.Assertions.assertTrue(
                hasNormalizedUrl,
                "URL deve ser normalizada para 'https://dominio/#/forrisco/risk/700/details/monitors'");
    }

    @Test
    @DisplayName("execute: não envia notificações quando não há riscos")
    void execute_noRisks() {
        Criteria countCriteria = mock(Criteria.class);
        stubFluentCriteria(countCriteria);
        when(dao.newCriteria(eq(Risk.class))).thenReturn(countCriteria);
        when(countCriteria.uniqueResult()).thenReturn(0L);

        task.execute();

        verify(emailSenderTask, never()).add(any(NotificationEmail.class));
        verify(dao, never()).persist(any(Notification.class));
    }

    @Test
    @DisplayName("execute: não envia notificações quando Plano de Risco está expirado")
    void execute_skipsExpiredPlanRisk() {
        Criteria countCriteria = mock(Criteria.class);
        Criteria listCriteria = mock(Criteria.class);
        stubFluentCriteria(countCriteria);
        stubFluentCriteria(listCriteria);

        when(dao.newCriteria(eq(Risk.class))).thenReturn(countCriteria, listCriteria);
        when(countCriteria.uniqueResult()).thenReturn(1L);

        Risk risk = mock(Risk.class);

        var unit = mock(org.forrisco.core.unit.Unit.class, Answers.RETURNS_DEEP_STUBS);
        PlanRisk pr = mock(PlanRisk.class);
        when(pr.getValidityEnd()).thenReturn(new Date(System.currentTimeMillis() - 86_400_000L));
        when(unit.getPlanRisk()).thenReturn(pr);
        when(risk.getUnit()).thenReturn(unit);

        when(dao.findByCriteria(eq(listCriteria), eq(Risk.class))).thenReturn(List.of(risk));

        task.execute();

        verify(emailSenderTask, never()).add(any(NotificationEmail.class));
        verify(dao, never()).persist(any(Notification.class));
        verify(dao, never()).newCriteria(eq(Monitor.class));
    }
}
