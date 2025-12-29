package org.forrisco.core.process;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

@Service
public class ProcessHistoryBS extends HibernateBusiness {

    @PersistenceContext
    private EntityManager em;

    public void save(ProcessHistory history) {
        history.setDeleted(false);
        this.persist(history);
    }

    /** Compara e salva cada campo alterado do Processo. */
    public void saveProcessChanges(
            Process oldProcess,
            Process newProcess,
            User responsibleUser,
            List<String> oldRelatedUnitNames,
            List<String> newRelatedUnitNames,
            List<String> oldObjectives,
            List<String> newObjectives
    ) {
        compareAndLog(text(oldProcess.getName()), text(newProcess.getName()), "Nome", oldProcess, responsibleUser);

        // fileLink (string simples)
        compareAndLog(text(oldProcess.getFileLink()), text(newProcess.getFileLink()),
                "Arquivo (link)", oldProcess, responsibleUser);

        // Objetivos (lista -> string ordenada)
        String oldObjStr = normalizedList(oldObjectives);
        String newObjStr = normalizedList(newObjectives);
        compareAndLog(oldObjStr, newObjStr, "Objetivos do Processo", oldProcess, responsibleUser);

        // Unidades relacionadas (lista -> string ordenada)
        String oldUnitsStr = normalizedList(oldRelatedUnitNames);
        String newUnitsStr = normalizedList(newRelatedUnitNames);
        compareAndLog(oldUnitsStr, newUnitsStr, "Unidades Relacionadas", oldProcess, responsibleUser);
    }

    private String normalizedList(List<String> data) {
        if (data == null) return "";
        return data.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> Jsoup.parse(s).text())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
    }

    private String text(String v) {
        return v == null ? "" : Jsoup.parse(v).text();
    }

    private void compareAndLog(String oldValue, String newValue, String fieldName, Process process, User responsibleUser) {
        if (!Objects.equals(oldValue, newValue)) {
            ProcessHistory h = new ProcessHistory();
            h.setProcess(process);
            h.setResponsibleUser(responsibleUser);
            h.setChangedField(fieldName);
            h.setOldValue(oldValue);
            h.setNewValue(newValue);
            h.setChangeDate(LocalDateTime.now());
            h.setDeleted(false);
            this.persist(h);
        }
    }

    /** Listagem paginada + busca (todas as colunas), ordenação (padrão: changeDate desc) */
    public PaginatedList<ProcessHistoryDTO> listHistoryByProcess(Long processId, DefaultParams params) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<ProcessHistory> cq = cb.createQuery(ProcessHistory.class);
        Root<ProcessHistory> root = cq.from(ProcessHistory.class);

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProcessHistory> countRoot = countQuery.from(ProcessHistory.class);

        Predicate[] predicates = getPredicates(cb, root, processId, params);
        Predicate[] countPredicates = getPredicates(cb, countRoot, processId, params);

        cq.select(root).where(predicates);
        countQuery.select(cb.countDistinct(countRoot.get("id"))).where(countPredicates);

        if (params.isSorting()) {
            org.forpdi.core.common.Order o = params.getSortOrder();
            cq.orderBy(o.isAscending() ? cb.asc(root.get(o.getPropertyName())) : cb.desc(root.get(o.getPropertyName())));
        } else {
            cq.orderBy(cb.desc(root.get("changeDate")));
        }

        TypedQuery<ProcessHistory> query = this.em.createQuery(cq)
                .setFirstResult((params.getPage() - 1) * params.getPageSize())
                .setMaxResults(params.getPageSize());

        List<ProcessHistory> list = query.getResultList();
        Long total = this.em.createQuery(countQuery).getSingleResult();

        PaginatedList<ProcessHistoryDTO> results = new PaginatedList<>();
        results.setList(list.stream().map(ProcessHistoryDTO::new).collect(Collectors.toList()));
        results.setTotal(total);
        return results;
    }

    private Predicate[] getPredicates(CriteriaBuilder cb, Root<ProcessHistory> root, Long processId, DefaultParams params) {
        Join<ProcessHistory, User> userJoin = root.join("responsibleUser", JoinType.INNER);
        List<Predicate> ps = new ArrayList<>();
        ps.add(cb.equal(root.get("deleted"), false));
        ps.add(cb.equal(root.get("process").get("id"), processId));

        if (params.hasTerm()) {
            String term = "%" + params.getTerm().toLowerCase() + "%";
            Expression<String> dateStr = cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y - %H:%i"));
            ps.add(cb.or(
                    cb.like(cb.lower(userJoin.get("name")), term),
                    cb.like(cb.lower(root.get("changedField")), term),
                    cb.like(cb.lower(cb.coalesce(root.get("oldValue"), "")), term),
                    cb.like(cb.lower(cb.coalesce(root.get("newValue"), "")), term),
                    cb.like(cb.lower(dateStr), term)
            ));
        }
        return ps.toArray(new Predicate[0]);
    }

    public static class ProcessHistoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String responsibleUser;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String changeDate;

        public ProcessHistoryDTO(ProcessHistory h) {
            this.id = h.getId();
            this.responsibleUser = h.getResponsibleUser().getName();
            this.changedField = h.getChangedField();
            this.oldValue = h.getOldValue();
            this.newValue = h.getNewValue();
            this.changeDate = h.getChangeDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
        }

        public Long getId() { return id; }
        public String getResponsibleUser() { return responsibleUser; }
        public String getChangedField() { return changedField; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }
        public String getChangeDate() { return changeDate; }
    }
}
