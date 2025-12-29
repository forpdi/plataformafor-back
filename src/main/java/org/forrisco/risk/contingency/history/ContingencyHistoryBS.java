package org.forrisco.risk.contingency.history;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forrisco.risk.contingency.Contingency;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class ContingencyHistoryBS extends HibernateBusiness {

    @PersistenceContext
    private EntityManager em;

    public void save(ContingencyHistory history) {
        history.setDeleted(false);
        this.persist(history);
    }

    public void saveContingencyChanges(Contingency oldC, Contingency newC, User responsibleUser) {
        compareAndLog(stripHtml(oldC.getAction()), stripHtml(newC.getAction()), "Ação", oldC, responsibleUser);
        compareAndLog(dateToText(oldC.getValidityBegin()), dateToText(newC.getValidityBegin()), "Início Vigência", oldC, responsibleUser);
        compareAndLog(dateToText(oldC.getValidityEnd()),   dateToText(newC.getValidityEnd()),   "Fim Vigência",    oldC, responsibleUser);

        if (!Objects.equals(safeId(oldC.getUser()), safeId(newC.getUser()))) {
            compareAndLog(nameOrEmpty(oldC.getUser()), nameOrEmpty(newC.getUser()),
                    "Responsável Técnico", oldC, responsibleUser);
        }
        if (!Objects.equals(safeId(oldC.getManager()), safeId(newC.getManager()))) {
            compareAndLog(nameOrEmpty(oldC.getManager()), nameOrEmpty(newC.getManager()),
                    "Gestor", oldC, responsibleUser);
        }
    }

    private static Long safeId(org.forpdi.core.user.User u) { return (u == null) ? null : u.getId(); }
    private static String nameOrEmpty(org.forpdi.core.user.User u) { return u == null ? "" : u.getName(); }
    private static String stripHtml(String s) { return s == null ? "" : Jsoup.parse(s).text(); }
    private static String dateToText(java.util.Date d) {
        if (d == null) return "";
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(d);
    }

    private void compareAndLog(String oldVal, String newVal, String field, Contingency contingency, User responsibleUser) {
        String o = oldVal == null ? "" : oldVal;
        String n = newVal == null ? "" : newVal;
        if (!Objects.equals(o, n)) {
            ContingencyHistory h = new ContingencyHistory();
            h.setContingency(contingency);
            h.setResponsibleUser(responsibleUser);
            h.setChangedField(field);
            h.setOldValue(o);
            h.setNewValue(n);
            h.setChangeDate(LocalDateTime.now());
            h.setDeleted(false);
            this.persist(h);
        }
    }

    public PaginatedList<ContingencyHistoryDTO> listHistoryByContingency(Long contingencyId, DefaultParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<ContingencyHistory> cq = cb.createQuery(ContingencyHistory.class);
        Root<ContingencyHistory> root = cq.from(ContingencyHistory.class);

        CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
        Root<ContingencyHistory> countRoot = countQ.from(ContingencyHistory.class);

        Predicate[] p = getPredicates(cb, root, contingencyId, params);
        Predicate[] pc = getPredicates(cb, countRoot, contingencyId, params);

        cq.select(root).where(p);
        countQ.select(cb.countDistinct(countRoot.get("id"))).where(pc);

        if (params.isSorting()) {
            var so = params.getSortOrder();
            String prop = so.getPropertyName();
            cq.orderBy(so.isAscending() ? cb.asc(root.get(prop)) : cb.desc(root.get(prop)));
        } else {
            cq.orderBy(cb.desc(root.get("changeDate")));
        }

        TypedQuery<ContingencyHistory> q = em.createQuery(cq);
        q.setFirstResult((params.getPage() - 1) * params.getPageSize());
        q.setMaxResults(params.getPageSize());

        List<ContingencyHistory> list = q.getResultList();
        Long total = em.createQuery(countQ).getSingleResult();

        List<ContingencyHistoryDTO> dtos = list.stream().map(ContingencyHistoryDTO::new).collect(Collectors.toList());
        PaginatedList<ContingencyHistoryDTO> results = new PaginatedList<>();
        results.setList(dtos);
        results.setTotal(total);
        return results;
    }

    private Predicate[] getPredicates(CriteriaBuilder cb, Root<ContingencyHistory> root,
                                      Long contingencyId, DefaultParams params) {
        Join<ContingencyHistory, User> userJoin = root.join("responsibleUser");
        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("deleted"), false));
        preds.add(cb.equal(root.get("contingency").get("id"), contingencyId));

        if (params.hasTerm()) {
            String term = "%" + params.getTerm() + "%";
            Predicate searchDate = cb.like(
                cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y - %H:%i")),
                term
            );
            preds.add(cb.or(
                cb.like(cb.lower(userJoin.get("name")), term.toLowerCase()), 
                cb.like(cb.lower(root.get("changedField")), term.toLowerCase()), 
                cb.like(root.get("oldValue"), term),  
                cb.like(root.get("newValue"), term),  
                searchDate
            ));
        }
        return preds.toArray(new Predicate[0]);
    }

    public static class ContingencyHistoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String responsibleUser;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String changeDate;

        public ContingencyHistoryDTO(ContingencyHistory h) {
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
