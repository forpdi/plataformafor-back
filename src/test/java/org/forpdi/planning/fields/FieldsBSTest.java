package org.forpdi.planning.fields;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.notification.NotificationBS;
import org.forpdi.core.notification.NotificationSetting;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.document.DocumentAttribute;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.attachment.Attachment;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.schedule.ScheduleInstance;
import org.forpdi.planning.fields.schedule.ScheduleStructure;
import org.forpdi.planning.fields.schedule.ScheduleValues;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.fields.table.TableInstance;
import org.forpdi.planning.fields.table.TableStructure;
import org.forpdi.planning.fields.table.TableValues;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Testes unitários para FieldsBS.
 *
 * Estratégia:
 * - Mock reflexivo do "dao" herdado de HibernateBusiness (não amarra ao tipo
 * concreto).
 * - Mock de Criteria com RETURNS_SELF; uniqueResult() e findByCriteria(...)
 * controlados por filas.
 * - Strictness LENIENT para evitar UnnecessaryStubbing em criteria não
 * consumidos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FieldsBSTest {

    private FieldsBS service;
    private NotificationBS nbs;
    private UserBS ubs;
    private UserSession userSession;
    private CompanyDomainContext domain;
    private AttributeHelper attributeHelper;

    private Object daoMock;
    private DaoStub daoStub;

    @BeforeEach
    void setup() throws Exception {
        service = spy(new FieldsBS());

        nbs = mock(NotificationBS.class);
        ubs = mock(UserBS.class);
        userSession = mock(UserSession.class, Answers.RETURNS_DEEP_STUBS);
        domain = mock(CompanyDomainContext.class, Answers.RETURNS_DEEP_STUBS);
        attributeHelper = mock(AttributeHelper.class);

        ReflectionTestUtils.setField(service, "nbs", nbs);
        ReflectionTestUtils.setField(service, "ubs", ubs);
        ReflectionTestUtils.setField(service, "userSession", userSession);
        ReflectionTestUtils.setField(service, "domain", domain);
        ReflectionTestUtils.setField(service, "attributeHelper", attributeHelper);

        Class<?> hibernateBusiness = service.getClass().getSuperclass();
        Field daoField = hibernateBusiness.getDeclaredField("dao");
        daoField.setAccessible(true);
        Class<?> daoClass = daoField.getType();

        daoStub = new DaoStub();
        daoMock = mock(daoClass, daoStub);
        ReflectionTestUtils.setField(service, "dao", daoMock);
    }


    private static class DaoStub implements Answer<Object> {
        private final Map<Class<?>, Deque<Object>> uniqueByClass = new ConcurrentHashMap<>();
        private final Map<Class<?>, Deque<List<?>>> listByClass = new ConcurrentHashMap<>();

        final List<Object> persisted = new ArrayList<>();

        void enqueueUnique(Class<?> clazz, Object value) {
            uniqueByClass.computeIfAbsent(clazz, k -> new ArrayDeque<>()).addLast(value);
        }

        @SafeVarargs
        final <T> void enqueueList(Class<T> clazz, List<T>... lists) {
            Deque<List<?>> q = listByClass.computeIfAbsent(clazz, k -> new ArrayDeque<>());
            for (List<T> l : lists)
                q.addLast(l);
        }

        @Override
        public Object answer(InvocationOnMock inv) throws Throwable {
            String name = inv.getMethod().getName();
            switch (name) {
                case "newCriteria": {
                    Class<?> clazz = (Class<?>) inv.getArgument(0);
                    Criteria crit = mock(Criteria.class, Answers.RETURNS_SELF);
                    lenient().when(crit.uniqueResult()).thenAnswer(ci -> {
                        Deque<Object> q = uniqueByClass.get(clazz);
                        return (q == null || q.isEmpty()) ? null : q.removeFirst();
                    });
                    return crit;
                }
                case "findByCriteria": {
                    Class<?> clazz = (Class<?>) inv.getArgument(1);
                    Deque<List<?>> q = listByClass.get(clazz);
                    if (q == null || q.isEmpty())
                        return Collections.emptyList();
                    return q.removeFirst();
                }
                case "persist": {
                    Object entity = inv.getArgument(0);
                    persisted.add(entity);
                    return null;
                }
                default:
                    return Answers.RETURNS_DEFAULTS.answer(inv);
            }
        }
    }

    private static Budget newBudget(Long id) {
        Budget b = new Budget();
        b.setId(id);
        return b;
    }

    private static Schedule newSchedule(Long id) {
        Schedule s = new Schedule();
        s.setId(id);
        return s;
    }

    private static ScheduleInstance newScheduleInstance(Long id) {
        ScheduleInstance si = new ScheduleInstance();
        si.setId(id);
        return si;
    }

    private static ScheduleStructure newScheduleStructure(Long id) {
        ScheduleStructure ss = new ScheduleStructure();
        ss.setId(id);
        return ss;
    }

    private static ScheduleValues newScheduleValues(ScheduleStructure ss, String value) {
        ScheduleValues sv = new ScheduleValues();
        sv.setScheduleStructure(ss);
        sv.setValue(value);
        return sv;
    }

    private static TableFields newTableFields(Long id) {
        TableFields tf = new TableFields();
        tf.setId(id);
        return tf;
    }

    private static TableInstance newTableInstance(Long id) {
        TableInstance ti = new TableInstance();
        ti.setId(id);
        return ti;
    }

    private static TableStructure newTableStructure(Long id) {
        TableStructure ts = new TableStructure();
        ts.setId(id);
        return ts;
    }

    private static TableValues newTableValues(TableStructure ts, String value) {
        TableValues tv = new TableValues();
        tv.setTableStructure(ts);
        tv.setValue(value);
        return tv;
    }

    @Test
    @DisplayName("saveBudget: marca deleted=false e persiste")
    void saveBudget_persist() {
        Budget b = new Budget();
        service.saveBudget(b);
        assertFalse(b.isDeleted());
        assertTrue(daoStub.persisted.contains(b));
    }

    @Test
    @DisplayName("update(Budget): seta deleted=false, creation!=null e persiste")
    void updateBudget_persist() {
        Budget b = new Budget();
        service.update(b);
        assertFalse(b.isDeleted());
        assertNotNull(b.getCreation());
        assertTrue(daoStub.persisted.contains(b));
    }

    @Test
    @DisplayName("budgetExistsBySubAction: retorna uniqueResult do criteria")
    void budgetExistsBySubAction_unique() {
        Budget expected = newBudget(10L);
        daoStub.enqueueUnique(Budget.class, expected);
        Budget got = service.budgetExistsBySubAction("AOC-123");
        assertSame(expected, got);
    }

    @Test
    @DisplayName("budgetExistsById: retorna uniqueResult do criteria")
    void budgetExistsById_unique() {
        Budget expected = newBudget(11L);
        daoStub.enqueueUnique(Budget.class, expected);
        Budget got = service.budgetExistsById(11L);
        assertSame(expected, got);
    }

    @Test
    @DisplayName("deleteBudget: marca deleted=true e persiste")
    void deleteBudget_softDelete() {
        Budget b = newBudget(12L);
        service.deleteBudget(b);
        assertTrue(b.isDeleted());
        assertTrue(daoStub.persisted.contains(b));
    }

    @Test
    @DisplayName("retrieveSchedule: uniqueResult")
    void retrieveSchedule_unique() {
        Schedule expected = newSchedule(1L);
        daoStub.enqueueUnique(Schedule.class, expected);
        assertSame(expected, service.retrieveSchedule(1L));
    }

    @Test
    @DisplayName("retrieveScheduleInstance: uniqueResult")
    void retrieveScheduleInstance_unique() {
        ScheduleInstance expected = newScheduleInstance(2L);
        daoStub.enqueueUnique(ScheduleInstance.class, expected);
        assertSame(expected, service.retrieveScheduleInstance(2L));
    }

    @Test
    @DisplayName("getScheduleInstanceNumber: quando existe max(number)=7 => retorna 8")
    void getScheduleInstanceNumber_withExisting() {
        Schedule s = newSchedule(3L);
        daoStub.enqueueUnique(ScheduleInstance.class, Long.valueOf(7));
        assertEquals(8L, service.getScheduleInstanceNumber(s));
    }

    @Test
    @DisplayName("getScheduleInstanceNumber: quando não há instâncias => retorna 1")
    void getScheduleInstanceNumber_whenNull() {
        Schedule s = newSchedule(3L);
        assertEquals(1L, service.getScheduleInstanceNumber(s));
    }

    @Test
    @DisplayName("retrieveScheduleStructure e retrieveScheduleValues: uniqueResult")
    void retrieveScheduleStructure_values() {
        ScheduleStructure ss = newScheduleStructure(5L);
        ScheduleInstance si = newScheduleInstance(6L);

        daoStub.enqueueUnique(ScheduleStructure.class, ss);
        assertSame(ss, service.retrieveScheduleStructure(5L));

        ScheduleValues sv = new ScheduleValues();
        daoStub.enqueueUnique(ScheduleValues.class, sv);
        assertSame(sv, service.retrieveScheduleValues(ss, si));
    }

    @Test
    @DisplayName("retrieveScheduleInstanceByAttribute: findByCriteria retorna lista")
    void retrieveScheduleInstanceByAttribute_list() {
        ScheduleInstance a = newScheduleInstance(1L);
        ScheduleInstance b = newScheduleInstance(2L);
        daoStub.enqueueList(ScheduleInstance.class, List.of(a, b));
        List<ScheduleInstance> list = service.retrieveScheduleInstanceByAttribute(100L, true);
        assertEquals(List.of(a, b), list);
    }

    @Test
    @DisplayName("retrieveTableFields/Instance/Structure/Values: uniqueResult")
    void retrieveTable_all_unique() {
        TableFields tf = newTableFields(10L);
        daoStub.enqueueUnique(TableFields.class, tf);
        assertSame(tf, service.retrieveTableFields(10L));

        TableInstance ti = newTableInstance(20L);
        daoStub.enqueueUnique(TableInstance.class, ti);
        assertSame(ti, service.retrieveTableInstance(20L));

        TableStructure ts = newTableStructure(30L);
        daoStub.enqueueUnique(TableStructure.class, ts);
        assertSame(ts, service.retrieveTableStructure(30L));

        TableValues tv = new TableValues();
        daoStub.enqueueUnique(TableValues.class, tv);
        assertSame(tv, service.retrieveTableValues(ts, ti));
    }

    @Test
    @DisplayName("saveScheduleValues: atualiza existente e cria quando não existe")
    void saveScheduleValues_mix() {
        ScheduleInstance si = newScheduleInstance(1L);

        ScheduleStructure ss1 = newScheduleStructure(100L);
        ScheduleValues input1 = newScheduleValues(ss1, "A");
        ScheduleValues existing1 = new ScheduleValues();

        ScheduleStructure ss2 = newScheduleStructure(200L);
        ScheduleValues input2 = newScheduleValues(ss2, "B");

        daoStub.enqueueUnique(ScheduleStructure.class, ss1);
        daoStub.enqueueUnique(ScheduleStructure.class, ss2);

        daoStub.enqueueUnique(ScheduleValues.class, existing1);

        service.saveScheduleValues(si, List.of(input1, input2));

        assertTrue(daoStub.persisted.contains(existing1));
        assertSame(ss1, existing1.getScheduleStructure());
        assertSame(si, existing1.getScheduleInstance());
        assertEquals("A", existing1.getValue());

        boolean createdOk = daoStub.persisted.stream()
                .filter(ScheduleValues.class::isInstance)
                .map(ScheduleValues.class::cast)
                .anyMatch(sv -> ss2.equals(sv.getScheduleStructure())
                        && si.equals(sv.getScheduleInstance())
                        && "B".equals(sv.getValue()));
        assertTrue(createdOk, "Deveria ter sido criado um ScheduleValues para ss2/si/B");
    }

    @Test
    @DisplayName("saveTableValues: atualiza existente e cria quando não existe")
    void saveTableValues_mix() {
        TableInstance ti = newTableInstance(1L);

        TableStructure ts1 = newTableStructure(10L);
        TableValues input1 = newTableValues(ts1, "X");
        TableValues existing1 = new TableValues();

        TableStructure ts2 = newTableStructure(20L);
        TableValues input2 = newTableValues(ts2, "Y");

        daoStub.enqueueUnique(TableStructure.class, ts1);
        daoStub.enqueueUnique(TableStructure.class, ts2);

        daoStub.enqueueUnique(TableValues.class, existing1);

        service.saveTableValues(ti, List.of(input1, input2));

        assertTrue(daoStub.persisted.contains(existing1));
        assertSame(ts1, existing1.getTableStructure());
        assertSame(ti, existing1.getTableInstance());
        assertEquals("X", existing1.getValue());

        boolean createdOk = daoStub.persisted.stream()
                .filter(TableValues.class::isInstance)
                .map(TableValues.class::cast)
                .anyMatch(tv -> ts2.equals(tv.getTableStructure())
                        && ti.equals(tv.getTableInstance())
                        && "Y".equals(tv.getValue()));
        assertTrue(createdOk);
    }

    @Test
    @DisplayName("deleteScheduleInstance/deleteTableInstance: soft delete e persist")
    void deleteInstances_soft() {
        ScheduleInstance si = newScheduleInstance(9L);
        service.deleteScheduleInstance(si);
        assertTrue(si.isDeleted());
        assertTrue(daoStub.persisted.contains(si));

        TableInstance ti = newTableInstance(7L);
        service.deleteTableInstance(ti);
        assertTrue(ti.isDeleted());
        assertTrue(daoStub.persisted.contains(ti));
    }

    @Test
    @DisplayName("listBudgetsByInstance: lista + total")
    void listBudgetsByInstance_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();
        Budget b1 = newBudget(1L);
        Budget b2 = newBudget(2L);

        daoStub.enqueueList(Budget.class, List.of(b1, b2));
        daoStub.enqueueUnique(Budget.class, 2L);

        PaginatedList<Budget> page = service.listBudgetsByInstance(inst);
        assertEquals(List.of(b1, b2), page.getList());
        assertEquals(2L, page.getTotal());
    }

    @Test
    @DisplayName("scheduleByAttribute/tableFieldsByAttribute: uniqueResult")
    void byAttribute_unique() {
        Schedule s = newSchedule(1L);
        daoStub.enqueueUnique(Schedule.class, s);
        assertSame(s, service.scheduleByAttribute(100L, true));

        TableFields tf = newTableFields(2L);
        daoStub.enqueueUnique(TableFields.class, tf);
        assertSame(tf, service.tableFieldsByAttribute(200L, false));
    }

    @Test
    @DisplayName("retrieveTableInstanceByAttribute: lista")
    void retrieveTableInstanceByAttribute_ok() {
        TableInstance a = newTableInstance(1L);
        TableInstance b = newTableInstance(2L);
        daoStub.enqueueList(TableInstance.class, List.of(a, b));
        assertEquals(List.of(a, b), service.retrieveTableInstanceByAttribute(10L, true));
    }

    @Test
    @DisplayName("getBudgets: monta DTO a partir de listBudgetsByInstance")
    void getBudgets_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();

        BudgetElement be = new BudgetElement();
        be.setBudgetLoa(new Double("123.45"));
        be.setBalanceAvailable(new Double("77.00"));

        Budget b = newBudget(5L);
        b.setBudgetElement(be);

        PaginatedList<Budget> page = new PaginatedList<>();
        page.setList(List.of(b));
        doReturn(page).when(service).listBudgetsByInstance(inst);

        List<BudgetDTO> dtos = service.getBudgets(inst);
        assertEquals(1, dtos.size());
        assertSame(b, dtos.get(0).getBudget());
        assertEquals(new Double("123.45"), dtos.get(0).getBudgetLoa());
        assertEquals(new Double("77.00"), dtos.get(0).getBalanceAvailable());
    }

    @Test
    @DisplayName("listActionPlansByInstance: lista")
    void listActionPlansByInstance_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();
        ActionPlan a = new ActionPlan();
        a.setDescription("A");
        ActionPlan b = new ActionPlan();
        b.setDescription("B");

        daoStub.enqueueList(ActionPlan.class, List.of(a, b));
        assertEquals(List.of(a, b), service.listActionPlansByInstance(inst).getList());
    }

    @Test
    @DisplayName("listActionPlansByInstancePagined: paginação + total")
    void listActionPlansByInstancePagined_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();
        ActionPlan a = new ActionPlan();
        a.setDescription("A");
        ActionPlan b = new ActionPlan();
        b.setDescription("B");

        daoStub.enqueueList(ActionPlan.class, List.of(a, b)); // lista
        daoStub.enqueueUnique(ActionPlan.class, 2L); // total

        PaginatedList<ActionPlan> page = service.listActionPlansByInstancePagined(inst, 1, 5, null, null, null, null);
        assertEquals(List.of(a, b), page.getList());
        assertEquals(2L, page.getTotal());
    }

    @Test
    @DisplayName("listActionPlansByInstancePaginedAndOrdered: paginação + total")
    void listActionPlansByInstancePaginedAndOrdered_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();
        ActionPlan a = new ActionPlan();
        a.setDescription("A");
        ActionPlan b = new ActionPlan();
        b.setDescription("B");

        daoStub.enqueueList(ActionPlan.class, List.of(a, b)); // lista
        daoStub.enqueueUnique(ActionPlan.class, 2L); // total

        PaginatedList<ActionPlan> page = service.listActionPlansByInstancePaginedAndOrdered(
                inst, 1, 5, null, null, null, null, List.of("begin", "asc"));
        assertEquals(List.of(a, b), page.getList());
        assertEquals(2L, page.getTotal());
    }

    @Test
    @DisplayName("listActionPlans: lista")
    void listActionPlans_ok() {
        ActionPlan a = new ActionPlan();
        daoStub.enqueueList(ActionPlan.class, List.of(a));
        assertEquals(List.of(a), service.listActionPlans().getList());
    }

    @Test
    @DisplayName("actionPlanExistsById/Description: unique")
    void actionPlanExists_unique() {
        ActionPlan ap1 = new ActionPlan();
        daoStub.enqueueUnique(ActionPlan.class, ap1);
        assertSame(ap1, service.actionPlanExistsById(10L));

        ActionPlan ap2 = new ActionPlan();
        daoStub.enqueueUnique(ActionPlan.class, ap2);
        assertSame(ap2, service.actionPlanExistsByDescription("desc"));
    }

    @Test
    @DisplayName("deleteActionPlan: soft delete e persiste")
    void deleteActionPlan_soft() {
        ActionPlan ap = new ActionPlan();
        service.deleteActionPlan(ap);
        assertTrue(ap.isDeleted());
        assertTrue(daoStub.persisted.contains(ap));
    }

    @Test
    @DisplayName("retrieveBudgetElement: usa domain.get().getCompany e uniqueResult")
    void retrieveBudgetElement_ok() {
        Company company = new Company();
        when(domain.get().getCompany()).thenReturn(company);

        BudgetElement be = new BudgetElement();
        daoStub.enqueueUnique(BudgetElement.class, be);

        assertSame(be, service.retrieveBudgetElement("SUB-123"));
    }

    @Test
    @DisplayName("listBudget(company): lista")
    void listBudget_ok() {
        Company c = new Company();
        BudgetElement be1 = new BudgetElement();
        BudgetElement be2 = new BudgetElement();

        daoStub.enqueueList(BudgetElement.class, List.of(be1, be2));
        assertEquals(List.of(be1, be2), service.listBudget(c).getList());
    }

    @Test
    @DisplayName("saveActionPlan: checked=true & DEFAULT => envia apenas notification")
    void saveActionPlan_checked_default_notifyOnly() throws Exception {
        Company company = new Company();
        when(domain.get().getCompany()).thenReturn(company);
        when(domain.get().getBaseUrl()).thenReturn("http://base");

        StructureLevelInstance level = mock(StructureLevelInstance.class, Answers.RETURNS_DEEP_STUBS);
        when(level.getId()).thenReturn(2L);
        when(level.getName()).thenReturn("Nível X");
        PlanMacro macro = new PlanMacro();
        macro.setId(1L);
        when(level.getPlan().getParent()).thenReturn(macro);

        User responsible = new User();
        responsible.setId(77L);
        when(ubs.retrieveResponsible(level)).thenReturn(responsible);

        CompanyUser cu = new CompanyUser();
        cu.setNotificationSetting(NotificationSetting.DEFAULT.getSetting());
        when(ubs.retrieveCompanyUser(responsible, company)).thenReturn(cu);

        ActionPlan ap = new ActionPlan();
        ap.setLevelInstance(level);
        ap.setDescription("Desc");
        ap.setChecked(true);

        service.saveActionPlan(ap);

        assertFalse(ap.isDeleted());
        assertTrue(daoStub.persisted.contains(ap));

        verify(nbs, times(1)).sendNotification(
                eq(NotificationType.ACTION_PLAN_CLOSED),
                eq("Desc"),
                eq("Nível X"),
                eq(77L),
                eq("http://base/#/plan/1/details/subplan/level/2"));
        verify(nbs, never()).sendNotificationEmail(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("saveActionPlan: checked=true & RECEIVE_ALL => envia notification + email")
    void saveActionPlan_checked_all_notifyAndEmail() throws Exception {
        Company company = new Company();
        when(domain.get().getCompany()).thenReturn(company);
        when(domain.get().getBaseUrl()).thenReturn("http://base");

        StructureLevelInstance level = mock(StructureLevelInstance.class, Answers.RETURNS_DEEP_STUBS);
        when(level.getId()).thenReturn(3L);
        when(level.getName()).thenReturn("Nível Y");
        PlanMacro macro = new PlanMacro();
        macro.setId(9L);
        when(level.getPlan().getParent()).thenReturn(macro);

        User responsible = new User();
        responsible.setId(31L);
        when(ubs.retrieveResponsible(level)).thenReturn(responsible);

        CompanyUser cu = new CompanyUser();
        cu.setNotificationSetting(NotificationSetting.RECEIVE_ALL_BY_EMAIL.getSetting());
        when(ubs.retrieveCompanyUser(responsible, company)).thenReturn(cu);

        ActionPlan ap = new ActionPlan();
        ap.setLevelInstance(level);
        ap.setDescription("Fechado");
        ap.setChecked(true);

        service.saveActionPlan(ap);

        verify(nbs, times(1)).sendNotification(
                eq(NotificationType.ACTION_PLAN_CLOSED),
                eq("Fechado"),
                eq("Nível Y"),
                eq(31L),
                eq("http://base/#/plan/9/details/subplan/level/3"));
        verify(nbs, times(1)).sendNotificationEmail(
                eq(NotificationType.ACTION_PLAN_CLOSED),
                eq("Fechado"),
                eq("Nível Y"),
                eq(responsible),
                eq("http://base/#/plan/9/details/subplan/level/3"));
    }

    @Test
    @DisplayName("saveActionPlan: checked=false => apenas persiste, sem notificação")
    void saveActionPlan_unchecked_noNotification() throws Exception {
        ActionPlan ap = new ActionPlan();
        ap.setLevelInstance(mock(StructureLevelInstance.class, Answers.RETURNS_DEEP_STUBS));
        ap.setDescription("x");
        ap.setChecked(false);

        service.saveActionPlan(ap);

        assertTrue(daoStub.persisted.contains(ap));
        verifyNoInteractions(nbs);
    }

    @Test
    @DisplayName("validateActionPlanStatus: checked=true e notChecked=true => lança exceção")
    void validateActionPlanStatus_invalid() {
        ActionPlan ap = new ActionPlan();
        ap.setChecked(true);
        ap.setNotChecked(true);
        assertThrows(IllegalStateException.class, () -> service.validateActionPlanStatus(ap));
    }

    @Test
    @DisplayName("validateActionPlanStatus: combinações válidas não lançam")
    void validateActionPlanStatus_valid() {
        ActionPlan ap = new ActionPlan();
        ap.setChecked(true);
        ap.setNotChecked(false);
        assertDoesNotThrow(() -> service.validateActionPlanStatus(ap));

        ActionPlan ap2 = new ActionPlan();
        ap2.setChecked(false);
        ap2.setNotChecked(true);
        assertDoesNotThrow(() -> service.validateActionPlanStatus(ap2));
    }

    @Test
    @DisplayName("getOptionsField: lista por attributeId/deleted")
    void getOptionsField_ok() {
        OptionsField a = new OptionsField();
        OptionsField b = new OptionsField();
        daoStub.enqueueList(OptionsField.class, List.of(a, b));
        assertEquals(List.of(a, b), service.getOptionsField(99L, false).getList());
    }

    @Test
    @DisplayName("listOptionsFieldsByAttrsAndDocAttrs: vazio => emptyList")
    void listOptionsFields_emptyInputs() {
        assertTrue(service.listOptionsFieldsByAttrsAndDocAttrs(Collections.emptyList(), Collections.emptyList())
                .isEmpty());
    }

    @Test
    @DisplayName("listOptionsFieldsByAttrsAndDocAttrs: retorna lista do DAO")
    void listOptionsFields_nonEmpty() {
        Attribute attr = new Attribute();
        attr.setId(1L);
        DocumentAttribute da = new DocumentAttribute();
        da.setId(2L);

        OptionsField of1 = new OptionsField();
        daoStub.enqueueList(OptionsField.class, List.of(of1));

        List<OptionsField> list = service.listOptionsFieldsByAttrsAndDocAttrs(List.of(attr), List.of(da));
        assertEquals(List.of(of1), list);
    }

    @Test
    @DisplayName("listSchedulesByAttrsAndDocAttrs: vazio => emptyList; não vazio => lista")
    void listSchedulesByAttrsAndDocAttrs_ok() {
        assertTrue(service.listSchedulesByAttrsAndDocAttrs(Collections.emptyList(), Collections.emptyList()).isEmpty());

        Attribute a = new Attribute();
        a.setId(1L);
        Schedule s = new Schedule();
        daoStub.enqueueList(Schedule.class, List.of(s));
        assertEquals(List.of(s), service.listSchedulesByAttrsAndDocAttrs(List.of(a), Collections.emptyList()));
    }

    @Test
    @DisplayName("listTableFieldsByAttrsAndDocAttrs: retorna lista")
    void listTableFieldsByAttrsAndDocAttrs_ok() {
        Attribute a = new Attribute();
        a.setId(1L);
        TableFields tf = new TableFields();
        daoStub.enqueueList(TableFields.class, List.of(tf));
        assertEquals(List.of(tf), service.listTableFieldsByAttrsAndDocAttrs(List.of(a), Collections.emptyList()));
    }

    @Test
    @DisplayName("getOptionsForColumn: lista")
    void getOptionsForColumn_ok() {
        OptionsField of = new OptionsField();
        daoStub.enqueueList(OptionsField.class, List.of(of));
        assertEquals(List.of(of), service.getOptionsForColumn(1L, 2L, true));
    }

    @Test
    @DisplayName("listTableStructureByFields / listTableInstanceByFields / listTableValuesByInstance / listTableValuesByStructure")
    void table_lists_ok() {
        TableFields tf = newTableFields(1L);
        TableStructure ts = newTableStructure(2L);
        TableInstance ti = newTableInstance(3L);
        TableValues tv = new TableValues();

        daoStub.enqueueList(TableStructure.class, List.of(ts));
        assertEquals(List.of(ts), service.listTableStructureByFields(tf));

        daoStub.enqueueList(TableInstance.class, List.of(ti));
        assertEquals(List.of(ti), service.listTableInstanceByFields(tf, false));

        daoStub.enqueueList(TableValues.class, List.of(tv));
        assertEquals(List.of(tv), service.listTableValuesByInstance(ti, false));

        daoStub.enqueueList(TableValues.class, List.of(tv));
        assertEquals(List.of(tv), service.listTableValuesByStructure(ts, false));
    }

    @Test
    @DisplayName("listScheduleStructure / listScheduleInstance / lsitScheduleValues(instance/structure)")
    void schedule_lists_ok() {
        Schedule s = newSchedule(9L);
        ScheduleStructure ss = newScheduleStructure(10L);
        ScheduleInstance si = newScheduleInstance(11L);
        ScheduleValues sv = new ScheduleValues();

        daoStub.enqueueList(ScheduleStructure.class, List.of(ss));
        assertEquals(List.of(ss), service.listScheduleStructure(s));

        daoStub.enqueueList(ScheduleInstance.class, List.of(si));
        assertEquals(List.of(si), service.listScheduleInstance(s));

        daoStub.enqueueList(ScheduleValues.class, List.of(sv));
        assertEquals(List.of(sv), service.lsitScheduleValues(si));

        daoStub.enqueueList(ScheduleValues.class, List.of(sv));
        assertEquals(List.of(sv), service.lsitScheduleValues(ss));
    }

    @Test
    @DisplayName("saveAttachment: sanitiza nome, seta flags e autor; persiste")
    void saveAttachment_ok() {
        User author = new User();
        author.setId(5L);
        when(userSession.getUser()).thenReturn(author);

        Attachment att = new Attachment();
        att.setName("arquivo.txt");
        att.setDescription("d");

        Attachment saved = service.saveAttachment(att);

        assertSame(att, saved);
        assertFalse(saved.isDeleted());
        assertNotNull(saved.getCreation());
        assertSame(author, saved.getAuthor());
        assertTrue(daoStub.persisted.contains(saved));
    }

    @Test
    @DisplayName("listAllAttachment: pagina + total")
    void listAllAttachment_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();
        Attachment a = new Attachment();
        Attachment b = new Attachment();

        daoStub.enqueueList(Attachment.class, List.of(a, b));
        daoStub.enqueueUnique(Attachment.class, 2L);

        PaginatedList<Attachment> page = service.listAllAttachment(inst, 1, 5);
        assertEquals(List.of(a, b), page.getList());
        assertEquals(2L, page.getTotal());
    }

    @Test
    @DisplayName("retrieveById: unique")
    void retrieveById_ok() {
        Attachment a = new Attachment();
        daoStub.enqueueUnique(Attachment.class, a);
        assertSame(a, service.retrieveById(10L));
    }

    @Test
    @DisplayName("deleteAttachment: null => false; não-null => soft delete + persist + true")
    void deleteAttachment_ok() {
        assertFalse(service.deleteAttachment(null));
        Attachment a = new Attachment();
        assertTrue(service.deleteAttachment(a));
        assertTrue(a.isDeleted());
        assertTrue(daoStub.persisted.contains(a));
    }

    @Test
    @DisplayName("updateAttachment: inexistente => false; existente => atualiza descrição e persiste")
    void updateAttachment_ok() {
        Attachment param = new Attachment();
        param.setId(9L);
        param.setDescription("nova");

        assertFalse(service.updateAttachment(param));

        Attachment exist = new Attachment();
        exist.setId(9L);
        exist.setDescription("old");
        daoStub.enqueueUnique(Attachment.class, exist);

        assertTrue(service.updateAttachment(param));
        assertEquals("nova", exist.getDescription());
        assertTrue(daoStub.persisted.contains(exist));
    }

    @Test
    @DisplayName("deleteAttachmentList: delega para deleteAttachment(item)")
    void deleteAttachmentList_ok() {
        Attachment a = new Attachment();
        Attachment b = new Attachment();

        doReturn(true).when(service).deleteAttachment(a);
        doReturn(true).when(service).deleteAttachment(b);

        service.deleteAttachmentList(List.of(a, b));

        verify(service, times(1)).deleteAttachment(a);
        verify(service, times(1)).deleteAttachment(b);
    }

    @Test
    @DisplayName("listAll* by collections: vazias => empty; não vazias => lista do DAO")
    void listAll_byCollections_ok() {
        assertTrue(service.listAllScheduleInstancesBySchedules(Collections.emptyList()).isEmpty());
        Schedule s = newSchedule(1L);
        ScheduleInstance si = newScheduleInstance(2L);
        daoStub.enqueueList(ScheduleInstance.class, List.of(si));
        assertEquals(List.of(si), service.listAllScheduleInstancesBySchedules(List.of(s)));

        ScheduleStructure ss = newScheduleStructure(3L);
        daoStub.enqueueList(ScheduleStructure.class, List.of(ss));
        assertEquals(List.of(ss), service.listAllScheduleStructuresBySchedules(List.of(s)));

        TableFields tf = newTableFields(4L);
        TableInstance ti = newTableInstance(5L);
        daoStub.enqueueList(TableInstance.class, List.of(ti));
        assertEquals(List.of(ti), service.listAllTableInstancesByTableFields(List.of(tf)));

        TableStructure ts = newTableStructure(6L);
        daoStub.enqueueList(TableStructure.class, List.of(ts));
        assertEquals(List.of(ts), service.listAllTableStructuresByTableFields(List.of(tf)));

        ScheduleValues sv = new ScheduleValues();
        daoStub.enqueueList(ScheduleValues.class, List.of(sv));
        assertEquals(List.of(sv), service.listAllScheduleValuesByInstancesAndStructures(List.of(si), List.of(ss)));

        TableValues tv = new TableValues();
        daoStub.enqueueList(TableValues.class, List.of(tv));
        assertEquals(List.of(tv), service.listAllTableValuesByInstancesAndStructures(List.of(ti), List.of(ts)));
    }

    @Test
    @DisplayName("listAllActionPlansByInstance: lista")
    void listAllActionPlansByInstance_ok() {
        StructureLevelInstance inst = new StructureLevelInstance();
        ActionPlan a = new ActionPlan();
        daoStub.enqueueList(ActionPlan.class, List.of(a));
        assertEquals(List.of(a), service.listAllActionPlansByInstance(inst));
    }

    @Test
    @DisplayName("sendUserLinkedToActionPlanNotification: envia notification + email")
    void sendUserLinkedToActionPlanNotification_ok() throws Exception {
        when(domain.get().getBaseUrl()).thenReturn("http://base");

        StructureLevelInstance level = mock(StructureLevelInstance.class, Answers.RETURNS_DEEP_STUBS);
        when(level.getId()).thenReturn(21L);
        when(level.getName()).thenReturn("Nível Z");
        PlanMacro macro = new PlanMacro();
        macro.setId(99L);
        when(level.getPlan().getParent()).thenReturn(macro);

        ActionPlan ap = new ActionPlan();
        ap.setLevelInstance(level);
        ap.setDescription("AP");

        User user = new User();
        user.setId(8L);

        service.sendUserLinkedToActionPlanNotification(ap, user);

        verify(nbs, times(1)).sendNotification(
                eq(NotificationType.USER_LINKED_TO_ACTION_PLAN),
                eq("AP"),
                eq("Nível Z"),
                eq(8L),
                eq("http://base/#/plan/99/details/subplan/level/21"));
        verify(nbs, times(1)).sendNotificationEmail(
                eq(NotificationType.USER_LINKED_TO_ACTION_PLAN),
                eq("AP"),
                eq("Nível Z"),
                eq(user),
                eq("http://base/#/plan/99/details/subplan/level/21"));
    }

    @Test
    @DisplayName("getActionPlanPermissionInfo(user,parent): verifica interações principais e retorno não-nulo")
    void getActionPlanPermissionInfo_ok() {
        User logged = new User();
        logged.setId(5L);
        when(userSession.getUser()).thenReturn(logged);
        when(userSession.getAccessLevel()).thenReturn(AccessLevels.MANAGER.getLevel());

        AttributeInstance ai = new AttributeInstance();
        ai.setValue("5");
        StructureLevelInstance parent = new StructureLevelInstance();
        when(attributeHelper.retrieveAttributeInstance(parent, ResponsibleField.class)).thenReturn(ai);

        User responsible = new User();
        responsible.setId(5L);

        ActionPlan.ActionPlanPermissionInfo info = service.getActionPlanPermissionInfo(responsible, parent);
        assertNotNull(info);

        verify(userSession, atLeastOnce()).getUser();
        verify(userSession, atLeastOnce()).getAccessLevel();
        verify(attributeHelper, times(1)).retrieveAttributeInstance(parent, ResponsibleField.class);
    }

    @Test
    @DisplayName("getActionPlanPermissionInfo(ActionPlan): delega para sobrecarga (user, parent)")
    void getActionPlanPermissionInfo_fromActionPlan() {
        ActionPlan ap = mock(ActionPlan.class);
        User u = new User();
        StructureLevelInstance parent = new StructureLevelInstance();
        when(ap.getUser()).thenReturn(u);
        when(ap.getLevelInstance()).thenReturn(parent);

        doReturn(new ActionPlan.ActionPlanPermissionInfo(true, true, true))
                .when(service).getActionPlanPermissionInfo(u, parent);

        ActionPlan.ActionPlanPermissionInfo info = service.getActionPlanPermissionInfo(ap);
        assertNotNull(info);
        verify(service, times(1)).getActionPlanPermissionInfo(u, parent);
    }
}
