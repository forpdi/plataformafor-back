package org.forrisco.risk.history;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskLevel;
import org.forrisco.risk.RiskOrganizationalLevel;
import org.forrisco.risk.RiskResponse;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

@Service
public class RiskEditHistoryBS extends HibernateBusiness {

    @PersistenceContext
    private EntityManager em;

    private static final DateTimeFormatter DATE_OUT = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
    private static final DateTimeFormatter BEGIN_OUT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void save(RiskEditHistory history) {
        history.setDeleted(false);
        this.persist(history);
    }

    /** Campos cujo update só ocorre se o usuário tiver permissão elevada (vide RiskBS.updateRisk). */
    private static final Set<String> PRIV_FIELDS = Set.of("Nome", "Código", "Gestor", "Responsável Técnico");

    /**
     * Compara old vs new e persiste entradas de histórico para cada campo alterado.
     * @param hasPermission se verdadeiro, considerar também os campos privilegiados (nome/código/gestor/responsável)
     */
    public void saveRiskChanges(
            Risk oldRisk, Risk newRisk,
            Unit newUnit, User newUser, User newManager, RiskLevel newRiskLevel,
            boolean hasPermission, User responsibleUser) {

        Map<String,String> L = labels();

        compareAndLog(oldRisk.getName(), newRisk.getName(), L.get("name"), oldRisk, responsibleUser, hasPermission);
        compareAndLog(oldRisk.getCode(), newRisk.getCode(), L.get("code"), oldRisk, responsibleUser, hasPermission);

        compareAndLogHtml(oldRisk.getReason(), newRisk.getReason(), L.get("reason"), oldRisk, responsibleUser);
        compareAndLogHtml(oldRisk.getResult(), newRisk.getResult(), L.get("result"), oldRisk, responsibleUser);

        compareAndLog(oldRisk.getProbability(), newRisk.getProbability(), L.get("probability"), oldRisk, responsibleUser);
        compareAndLog(oldRisk.getImpact(), newRisk.getImpact(), L.get("impact"), oldRisk, responsibleUser);
        compareAndLog(oldRisk.getPeriodicity(), newRisk.getPeriodicity(), L.get("periodicity"), oldRisk, responsibleUser);
        compareAndLog(oldRisk.getTipology(), newRisk.getTipology(), L.get("tipology"), oldRisk, responsibleUser);
        compareAndLog(oldRisk.getOtherTipologies(), newRisk.getOtherTipologies(), L.get("otherTipologies"), oldRisk, responsibleUser);
        compareAndLog(oldRisk.getType(), newRisk.getType(), L.get("type"), oldRisk, responsibleUser);

        compareAndLogBool(oldRisk.isRisk_pdi(), newRisk.isRisk_pdi(), L.get("risk_pdi"), oldRisk, responsibleUser);
        compareAndLogBool(oldRisk.isRisk_pdi_axis(), newRisk.isRisk_pdi_axis(), L.get("risk_pdi_axis"), oldRisk, responsibleUser);
        compareAndLogBool(oldRisk.isRisk_obj_process(), newRisk.isRisk_obj_process(), L.get("risk_obj_process"), oldRisk, responsibleUser);
        compareAndLogBool(oldRisk.isRisk_act_process(), newRisk.isRisk_act_process(), L.get("risk_act_process"), oldRisk, responsibleUser);
        compareAndLogBool(oldRisk.isArchived(), newRisk.isArchived(), L.get("archived"), oldRisk, responsibleUser);

        compareAndLogDate(oldRisk.getBegin(), newRisk.getBegin(), L.get("begin"), oldRisk, responsibleUser);
        compareAndLog(oldRisk.getLinkFPDI(), newRisk.getLinkFPDI(), L.get("linkFPDI"), oldRisk, responsibleUser);

        compareAndLogById(
            oldRisk.getUser(), newUser, L.get("user"),
            User::getName, oldRisk, responsibleUser, hasPermission);
        compareAndLogByIdNullable(
            oldRisk.getManager(), newManager, L.get("manager"),
            User::getName, oldRisk, responsibleUser, hasPermission);
        compareAndLogById(
            oldRisk.getUnit(), newUnit, L.get("unit"),
            Unit::getName, oldRisk, responsibleUser, true /* sempre */);

        compareAndLogById(
            oldRisk.getRiskLevel(), newRiskLevel, L.get("riskLevel"),
            RiskLevel::getLevel, oldRisk, responsibleUser, true);

        compareAndLogLabel(
            oldRisk.getResponse(), newRisk.getResponse(), L.get("response"),
            id -> id == null ? "" : RiskResponse.GetById(id).getLabel(),
            oldRisk, responsibleUser);

        compareAndLogLabel(
            oldRisk.getLevel(), newRisk.getLevel(), L.get("orgLevel"),
            id -> id == null ? "" : Optional.ofNullable(RiskOrganizationalLevel.getById(id)).map(RiskOrganizationalLevel::getLabel).orElse(""),
            oldRisk, responsibleUser);

        compareAndLogLabel(
            oldRisk.getExistentControllerId(),
            newRisk.getExistentControllerId(),
            L.get("existentControllers"),
            this::existentControllerLabel, oldRisk, responsibleUser);

        compareAndLogLabel(
            oldRisk.getExecutionControllerId(),
            newRisk.getExecutionControllerId(),
            L.get("executionControllers"),
            this::executionControllerLabel, oldRisk, responsibleUser);

        Long riskId = oldRisk.getId();

        List<String> activitiesOld = preferNonEmpty(safeList(oldRisk.getActivitiesDescriptions()), fetchActivities(riskId));
        List<String> activitiesNew = preferNonEmpty(safeList(newRisk.getActivitiesDescriptions()), fetchActivities(riskId));
        compareAndLogListSmart(activitiesOld, activitiesNew, L.get("activities"), oldRisk, responsibleUser);

        List<String> procObjOld = preferNonEmpty(extractProcessObjectivesDescriptions(oldRisk.getProcessObjectives()), fetchProcessObjectives(riskId));
        List<String> procObjNew = coalesceNonEmpty(
            extractProcessObjectivesDescriptions(newRisk.getProcessObjectives()),
            safeList(newRisk.getProcessObjectivesDescriptions())
        );
        compareAndLogListSmart(procObjOld, procObjNew, L.get("processObjectives"), oldRisk, responsibleUser);

        List<String> axesOld = preferNonEmpty(extractStructureNamesFromPaginated(oldRisk.getAxes()), fetchAxes(riskId));
        List<String> axesNew = coalesceNonEmpty(
            extractStructureNamesFromPaginated(newRisk.getAxes()),
            safeList(newRisk.getAxesDescriptions())
        );
        compareAndLogListSmart(axesOld, axesNew, L.get("pdiAxes"), oldRisk, responsibleUser);

        List<String> strategiesOld = preferNonEmpty(
            coalesceNonEmpty(extractStructureNamesFromPaginated(oldRisk.getStrategies()), safeList(oldRisk.getStrategiesDescriptions())),
            fetchStrategies(riskId)
        );
        List<String> strategiesNew = coalesceNonEmpty(
            extractStructureNamesFromPaginated(newRisk.getStrategies()),
            safeList(newRisk.getStrategiesDescriptions())
        );
        compareAndLogListSmart(strategiesOld, strategiesNew, L.get("pdiObjectives"), oldRisk, responsibleUser);

        List<String> indicatorsOld = preferNonEmpty(extractStructureNamesFromPaginated(oldRisk.getIndicators()), fetchIndicators(riskId));
        List<String> indicatorsNew = coalesceNonEmpty(
            extractStructureNamesFromPaginated(newRisk.getIndicators()),
            safeList(newRisk.getIndicatorsDescriptions())
        );
        compareAndLogListSmart(indicatorsOld, indicatorsNew, L.get("pdiIndicators"), oldRisk, responsibleUser);

        List<String> goalsOld = preferNonEmpty(extractStructureNamesFromPaginated(oldRisk.getGoals()), fetchGoals(riskId));
        List<String> goalsNew = coalesceNonEmpty(
            extractStructureNamesFromPaginated(newRisk.getGoals()),
            safeList(newRisk.getGoalsDescriptions())
        );
        compareAndLogListSmart(goalsOld, goalsNew, L.get("pdiGoals"), oldRisk, responsibleUser);
    }

    public PaginatedList<RiskEditHistoryDTO> listHistoryByRisk(Long riskId, DefaultParams params) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<RiskEditHistory> cq = cb.createQuery(RiskEditHistory.class);
        Root<RiskEditHistory> root = cq.from(RiskEditHistory.class);
        Join<RiskEditHistory, User> userJoin = root.join("responsibleUser", JoinType.INNER);

        List<Predicate> filters = new ArrayList<>();
        filters.add(cb.equal(root.get("deleted"), false));
        filters.add(cb.equal(root.get("risk").get("id"), riskId));

        if (params.hasTerm()) {
            String t = "%" + params.getTerm().toLowerCase() + "%";
            Predicate byDate = cb.like(
                    cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y")),
                    "%" + params.getTerm() + "%");
            filters.add(cb.or(
                    cb.like(cb.lower(userJoin.get("name")), t),
                    cb.like(cb.lower(root.get("changedField")), t),
                    cb.like(cb.lower(root.get("oldValue")), t),
                    cb.like(cb.lower(root.get("newValue")), t),
                    byDate));
        }

        cq.select(root).where(filters.toArray(new Predicate[0]));

        if (params.isSorting()) {
            var order = params.getSortOrder();
            String prop = order.getPropertyName();
            Expression<?> sortExpr;

            if ("responsibleUserName".equals(prop)) {
                sortExpr = userJoin.get("name");
            } else if ("responsibleUserId".equals(prop)) {
                sortExpr = userJoin.get("id");
            } else {
                sortExpr = root.get(prop);
            }

            cq.orderBy(order.isAscending() ? cb.asc(sortExpr) : cb.desc(sortExpr));
        } else {
            cq.orderBy(cb.desc(root.get("changeDate")));
        }

        TypedQuery<RiskEditHistory> query = this.em.createQuery(cq);
        query.setFirstResult((params.getPage() - 1) * params.getPageSize());
        query.setMaxResults(params.getPageSize());
        List<RiskEditHistory> items = query.getResultList();

        CriteriaQuery<Long> countCQ = cb.createQuery(Long.class);
        Root<RiskEditHistory> countRoot = countCQ.from(RiskEditHistory.class);
        List<Predicate> countFilters = new ArrayList<>();
        countFilters.add(cb.equal(countRoot.get("deleted"), false));
        countFilters.add(cb.equal(countRoot.get("risk").get("id"), riskId));

        if (params.hasTerm()) {
            String t = "%" + params.getTerm().toLowerCase() + "%";
            Join<RiskEditHistory, User> countUserJoin = countRoot.join("responsibleUser", JoinType.INNER);
            Predicate byDate = cb.like(
                    cb.function("DATE_FORMAT", String.class, countRoot.get("changeDate"), cb.literal("%d/%m/%Y")),
                    "%" + params.getTerm() + "%");
            countFilters.add(cb.or(
                    cb.like(cb.lower(countUserJoin.get("name")), t),
                    cb.like(cb.lower(countRoot.get("changedField")), t),
                    cb.like(cb.lower(countRoot.get("oldValue")), t),
                    cb.like(cb.lower(countRoot.get("newValue")), t),
                    byDate));
        }

        countCQ.select(cb.countDistinct(countRoot.get("id"))).where(countFilters.toArray(new Predicate[0]));
        Long total = this.em.createQuery(countCQ).getSingleResult();

        PaginatedList<RiskEditHistoryDTO> results = new PaginatedList<>();
        results.setList(items.stream().map(RiskEditHistoryDTO::new).collect(Collectors.toList()));
        results.setTotal(total);
        return results;
    }

    private void saveHistoryEntry(String oldValue, String newValue, String fieldName, Risk risk, User responsibleUser) {
        if (Objects.equals(nullToEmpty(oldValue), nullToEmpty(newValue))) return;
        RiskEditHistory h = new RiskEditHistory();
        h.setRisk(risk);
        h.setResponsibleUser(responsibleUser);
        h.setChangedField(fieldName);
        h.setOldValue(nullToEmpty(oldValue));
        h.setNewValue(nullToEmpty(newValue));
        h.setChangeDate(LocalDateTime.now());
        h.setDeleted(false);
        this.persist(h);
    }

    private void compareAndLog(String oldVal, String newVal, String field, Risk risk, User resp, boolean consider) {
        if (!consider && PRIV_FIELDS.contains(field)) return;
        if (!Objects.equals(nullToEmpty(oldVal), nullToEmpty(newVal))) {
            saveHistoryEntry(oldVal, newVal, field, risk, resp);
        }
    }
    private void compareAndLog(String oldVal, String newVal, String field, Risk risk, User resp) {
        compareAndLog(oldVal, newVal, field, risk, resp, true);
    }
    private void compareAndLogHtml(String oldVal, String newVal, String field, Risk risk, User resp) {
        String o = Jsoup.parse(nullToEmpty(oldVal)).text();
        String n = Jsoup.parse(nullToEmpty(newVal)).text();
        if (!Objects.equals(o, n)) saveHistoryEntry(o, n, field, risk, resp);
    }
    private void compareAndLogBool(boolean oldVal, boolean newVal, String field, Risk risk, User resp) {
        String o = boolPt(oldVal);
        String n = boolPt(newVal);
        if (!Objects.equals(o, n)) saveHistoryEntry(o, n, field, risk, resp);
    }
    private void compareAndLogDate(Date o, Date n, String field, Risk risk, User resp) {
        Function<Date,String> fmt = d -> d == null ? "" : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(BEGIN_OUT);
        String os = fmt.apply(o), ns = fmt.apply(n);
        if (!Objects.equals(os, ns)) saveHistoryEntry(os, ns, field, risk, resp);
    }
    private <T> void compareAndLogById(T oldObj, T newObj, String field, Function<T,String> toLabel, Risk risk, User resp, boolean consider) {
        if (!consider && PRIV_FIELDS.contains(field)) return;
        Long oldId = idOf(oldObj);
        Long newId = idOf(newObj);
        if (!Objects.equals(oldId, newId)) {
            saveHistoryEntry(labelOf(oldObj, toLabel), labelOf(newObj, toLabel), field, risk, resp);
        }
    }
    private <T> void compareAndLogByIdNullable(T oldObj, T newObj, String field, Function<T,String> toLabel, Risk risk, User resp, boolean consider) {
        if (!consider && PRIV_FIELDS.contains(field)) return;
        Long oldId = idOf(oldObj);
        Long newId = idOf(newObj);
        if (!Objects.equals(oldId, newId)) {
            saveHistoryEntry(labelOfNull(oldObj, toLabel), labelOfNull(newObj, toLabel), field, risk, resp);
        }
    }
    private void compareAndLogLabel(Integer oldId, Integer newId, String field, Function<Integer,String> labelFn, Risk risk, User resp) {
        String o = labelFn.apply(oldId);
        String n = labelFn.apply(newId);
        if (!Objects.equals(o, n)) saveHistoryEntry(o, n, field, risk, resp);
    }

    /**
     * Evita "logar sempre" as coleções e resolve coleções vazias não populadas:
     * - Normaliza listas old/new.
     * - Compara com o último NEW_VALUE logado para o mesmo campo.
     * - Se igual, não loga.
     * - Se diferente, usa o último NEW_VALUE como oldValue; se não existir, usa a lista old (ou vazio).
     */
    private void compareAndLogListSmart(List<String> oldVals, List<String> newVals, String field, Risk risk, User resp) {
        List<String> oN = normalizeList(oldVals);
        List<String> nN = normalizeList(newVals);
        String current = String.join(", ", nN);

        String last = fetchLastLoggedNewValue(risk.getId(), field);
        if (last != null && Objects.equals(last, current)) {
            return;
        }
        String oldShown = (last != null) ? last : String.join(", ", oN);
        if (!Objects.equals(nullToEmpty(oldShown), nullToEmpty(current))) {
            saveHistoryEntry(oldShown, current, field, risk, resp);
        }
    }

    private String fetchLastLoggedNewValue(Long riskId, String field) {
        try {
            CriteriaBuilder cb = this.em.getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<RiskEditHistory> root = cq.from(RiskEditHistory.class);
            cq.select(root.get("newValue"))
              .where(
                  cb.equal(root.get("deleted"), false),
                  cb.equal(root.get("risk").get("id"), riskId),
                  cb.equal(root.get("changedField"), field)
              )
              .orderBy(cb.desc(root.get("changeDate")));
            TypedQuery<String> q = this.em.createQuery(cq);
            q.setMaxResults(1);
            List<String> res = q.getResultList();
            return res.isEmpty() ? null : res.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> fetchActivities(Long riskId) {
        try {
            TypedQuery<String> q = em.createQuery(
                "SELECT CONCAT(a.name, ' - ', p.name) " +
                "FROM org.forrisco.risk.links.RiskActivity a " +
                "JOIN a.process p " +
                "WHERE a.deleted = false AND a.risk.id = :rid", String.class);
            q.setParameter("rid", riskId);
            return normalizeList(q.getResultList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<String> fetchProcessObjectives(Long riskId) {
        try {
            TypedQuery<String> q = em.createQuery(
                "SELECT rpo.processObjective.description " +
                "FROM org.forrisco.risk.links.RiskProcessObjective rpo " +
                "WHERE rpo.deleted = false AND rpo.risk.id = :rid", String.class);
            q.setParameter("rid", riskId);
            return normalizeList(q.getResultList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<String> fetchAxes(Long riskId) {
        try {
            TypedQuery<String> q = em.createQuery(
                "SELECT ra.structure.name " +
                "FROM org.forrisco.risk.links.RiskAxis ra " +
                "WHERE ra.deleted = false AND ra.risk.id = :rid", String.class);
            q.setParameter("rid", riskId);
            return normalizeList(q.getResultList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<String> fetchStrategies(Long riskId) {
        try {
            TypedQuery<String> q = em.createQuery(
                "SELECT rs.structure.name " +
                "FROM org.forrisco.risk.links.RiskStrategy rs " +
                "WHERE rs.deleted = false AND rs.risk.id = :rid", String.class);
            q.setParameter("rid", riskId);
            return normalizeList(q.getResultList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<String> fetchIndicators(Long riskId) {
        try {
            TypedQuery<String> q = em.createQuery(
                "SELECT ri.structure.name " +
                "FROM org.forrisco.risk.links.RiskIndicator ri " +
                "WHERE ri.deleted = false AND ri.risk.id = :rid", String.class);
            q.setParameter("rid", riskId);
            return normalizeList(q.getResultList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private List<String> fetchGoals(Long riskId) {
        try {
            TypedQuery<String> q = em.createQuery(
                "SELECT rg.structure.name " +
                "FROM org.forrisco.risk.links.RiskGoal rg " +
                "WHERE rg.deleted = false AND rg.risk.id = :rid", String.class);
            q.setParameter("rid", riskId);
            return normalizeList(q.getResultList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private String existentControllerLabel(Integer id) {
        if (id == null) return "";
        switch (id) {
            case 1: return "Não há procedimentos de controle";
            case 2: return "Há procedimentos de controles, mas são insuficientes";
            case 3: return "Todos os controles existentes estão formalizados, mas são insuficientes para o nível de risco";
            case 4: return "Os controles existentes são adequados (suficientes) ao nível de risco, mas nem todos (ou nenhum) estão formalizados";
            case 5: return "Há procedimentos de controle adequados (suficientes) e todos estão formalizados";
            default: return String.valueOf(id);
        }
    }
    private String executionControllerLabel(Integer id) {
        if (id == null) return "";
        switch (id) {
            case 1: return "Não se aplica";
            case 2: return "Os controles existentes não são executados";
            case 3: return "Os controles existentes estão sendo parcialmente executados (alguns sim, outros não)";
            case 4: return "Todos os controles existentes são executados, mas não há evidência (registros) de sua realização";
            case 5: return "Todos os controles existentes são executados e tem evidência (registros) de sua realização";
            default: return String.valueOf(id);
        }
    }

    private static List<String> preferNonEmpty(List<String> fromObject, List<String> fromDB) {
        List<String> a = normalizeList(fromObject);
        return a.isEmpty() ? normalizeList(fromDB) : a;
    }

    private static List<String> extractProcessObjectivesDescriptions(List<?> pos) {
        if (pos == null) return Collections.emptyList();
        List<String> out = new ArrayList<>(pos.size());
        for (Object it : pos) {
            try {
                Object po = it.getClass().getMethod("getProcessObjective").invoke(it);
                if (po == null) continue;
                Object desc = po.getClass().getMethod("getDescription").invoke(po);
                if (desc != null) out.add(String.valueOf(desc));
            } catch (Exception ignored) {}
        }
        return normalizeList(out);
    }

    private static List<String> extractStructureNamesFromPaginated(PaginatedList<?> pl) {
        if (pl == null || pl.getList() == null) return Collections.emptyList();
        List<String> out = new ArrayList<>(pl.getList().size());
        for (Object it : pl.getList()) {
            try {
                Object structure = it.getClass().getMethod("getStructure").invoke(it);
                if (structure == null) continue;
                Object name = structure.getClass().getMethod("getName").invoke(structure);
                if (name != null) out.add(String.valueOf(name));
            } catch (Exception ignored) {}
        }
        return normalizeList(out);
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String boolPt(boolean b) { return b ? "Sim" : "Não"; }
    private static <T> Long idOf(T obj) {
        try { return (obj == null) ? null : (Long) obj.getClass().getMethod("getId").invoke(obj); }
        catch (Exception e) { return null; }
    }
    private static <T> String labelOf(T obj, Function<T,String> fn) { return obj == null ? "" : fn.apply(obj); }
    private static <T> String labelOfNull(T obj, Function<T,String> fn) { return obj == null ? "" : fn.apply(obj); }
    private static List<String> safeList(List<String> list) {
        return list == null ? Collections.emptyList() : list;
    }
    private static List<String> normalizeList(List<String> l) {
        if (l == null) return Collections.emptyList();
        return l.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
    private static List<String> coalesceNonEmpty(List<String> primary, List<String> fallback) {
        List<String> p = normalizeList(primary);
        return p.isEmpty() ? normalizeList(fallback) : p;
    }

    private static Map<String,String> labels() {
        Map<String,String> L = new LinkedHashMap<>();
        L.put("name", "Nome");
        L.put("code", "Código");
        L.put("reason", "Causa");
        L.put("result", "Consequência");
        L.put("probability", "Probabilidade");
        L.put("impact", "Impacto");
        L.put("periodicity", "Periodicidade");
        L.put("tipology", "Tipologia");
        L.put("otherTipologies", "Outras Tipologias");
        L.put("type", "Tipo");
        L.put("risk_pdi", "Risco PDI");
        L.put("risk_pdi_axis", "Risco por Eixo do PDI");
        L.put("risk_obj_process", "Risco por Objetivo de Processo");
        L.put("risk_act_process", "Risco por Atividade de Processo");
        L.put("begin", "Início");
        L.put("linkFPDI", "Link ForPDI");
        L.put("user", "Responsável Técnico");
        L.put("manager", "Gestor");
        L.put("unit", "Unidade");
        L.put("riskLevel", "Grau de Risco");
        L.put("response", "Resposta");
        L.put("orgLevel", "Nível Organizacional");
        L.put("archived", "Arquivado");

        L.put("existentControllers", "Controles Existentes");
        L.put("executionControllers", "Execução dos controles");

        L.put("activities", "Atividade(s) do(s) processo(s) vinculado(s)");
        L.put("processObjectives", "Objetivo(s) do(s) processo(s) vinculado(s)");
        L.put("pdiAxes", "Eixo(s) do PDI vinculado(s)");
        L.put("pdiObjectives", "Objetivo(s) estratégico(s) do PDI vinculado(s)");
        L.put("pdiIndicators", "Indicador(es) do PDI vinculado(s)");
        L.put("pdiGoals", "Meta(s) do PDI vinculado(s)");
        return L;
    }

    public static class RiskEditHistoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String responsibleUser;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String changeDate;

        public RiskEditHistoryDTO(RiskEditHistory h) {
            this.id = h.getId();
            this.responsibleUser = h.getResponsibleUser().getName();
            this.changedField = h.getChangedField();
            this.oldValue = h.getOldValue();
            this.newValue = h.getNewValue();
            this.changeDate = h.getChangeDate().format(DATE_OUT);
        }

        public Long getId() { return id; }
        public String getResponsibleUser() { return responsibleUser; }
        public String getChangedField() { return changedField; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }
        public String getChangeDate() { return changeDate; }
    }
}
