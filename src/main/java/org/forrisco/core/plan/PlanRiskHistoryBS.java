package org.forrisco.core.plan;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class PlanRiskHistoryBS extends HibernateBusiness {

    @PersistenceContext
    private EntityManager em;

    public void save(PlanRiskHistory history) {
        history.setDeleted(false);
        this.persist(history);
    }

    public void savePlanChanges(PlanRisk oldPlan, PlanRisk newPlan, User responsibleUser) {
        // Nome
        compareAndLog(oldPlan.getName(), newPlan.getName(), "Nome", oldPlan, responsibleUser);

        String oldDesc = oldPlan.getDescription();
        String newDesc = newPlan.getDescription();
        if (oldDesc != null) oldDesc = Jsoup.parse(oldDesc).text();
        if (newDesc != null) newDesc = Jsoup.parse(newDesc).text();
        compareAndLog(oldDesc, newDesc, "Descrição", oldPlan, responsibleUser);

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        String oldBegin = oldPlan.getValidityBegin() != null ? fmt.format(oldPlan.getValidityBegin()) : null;
        String newBegin = newPlan.getValidityBegin() != null ? fmt.format(newPlan.getValidityBegin()) : null;
        compareAndLog(oldBegin, newBegin, "Início da Vigência", oldPlan, responsibleUser);

        String oldEnd = oldPlan.getValidityEnd() != null ? fmt.format(oldPlan.getValidityEnd()) : null;
        String newEnd = newPlan.getValidityEnd() != null ? fmt.format(newPlan.getValidityEnd()) : null;
        compareAndLog(oldEnd, newEnd, "Fim da Vigência", oldPlan, responsibleUser);

        String oldPolicy = oldPlan.getPolicy() != null ? oldPlan.getPolicy().getName() : null;
        String newPolicy = newPlan.getPolicy() != null ? newPlan.getPolicy().getName() : null;
        compareAndLog(oldPolicy, newPolicy, "Política", oldPlan, responsibleUser);
    }

    private void compareAndLog(String oldVal, String newVal, String fieldName, PlanRisk plan, User responsibleUser) {
        String normOld = normalize(oldVal);
        String normNew = normalize(newVal);
        if (!Objects.equals(normOld, normNew)) {
            saveHistoryEntry(normOld, normNew, fieldName, plan, responsibleUser);
        }
    }

    private String normalize(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void saveHistoryEntry(String oldValue, String newValue, String fieldName, PlanRisk plan, User responsibleUser) {
        PlanRiskHistory h = new PlanRiskHistory();
        h.setPlanRisk(plan);
        h.setResponsibleUser(responsibleUser);
        h.setChangedField(fieldName);
        h.setOldValue(oldValue);
        h.setNewValue(newValue);
        h.setChangeDate(LocalDateTime.now());
        h.setDeleted(false);
        this.save(h);
    }

    public PaginatedList<PlanRiskHistoryDTO> listHistoryByPlan(Long planId, DefaultParams params) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();


        CriteriaQuery<PlanRiskHistory> cq = cb.createQuery(PlanRiskHistory.class);
        Root<PlanRiskHistory> root = cq.from(PlanRiskHistory.class);

        Predicate[] predicates = getPredicates(cb, root, planId, params);
        cq.select(root).where(predicates);

        if (params.isSorting()) {
            org.forpdi.core.common.Order sortOrder = params.getSortOrder();
            String property = sortOrder.getPropertyName();
            cq.orderBy(sortOrder.isAscending() ? cb.asc(root.get(property)) : cb.desc(root.get(property)));
        } else {
            cq.orderBy(cb.desc(root.get("changeDate")));
        }


        TypedQuery<PlanRiskHistory> query = this.em.createQuery(cq);
        int first = Math.max(0, (params.getPage() - 1) * params.getPageSize());
        query.setFirstResult(first);
        query.setMaxResults(params.getPageSize());

        List<PlanRiskHistory> list = query.getResultList();


        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PlanRiskHistory> countRoot = countQuery.from(PlanRiskHistory.class);
        countQuery.select(cb.countDistinct(countRoot.get("id")))
                  .where(getPredicates(cb, countRoot, planId, params));
        Long total = this.em.createQuery(countQuery).getSingleResult();


        List<PlanRiskHistoryDTO> dto = list.stream()
                .map(PlanRiskHistoryDTO::new)
                .collect(Collectors.toList());

        PaginatedList<PlanRiskHistoryDTO> result = new PaginatedList<>();
        result.setList(dto);
        result.setTotal(total); 
        return result;
    }

    private Predicate[] getPredicates(CriteriaBuilder cb, Root<PlanRiskHistory> root, Long planId, DefaultParams params) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("deleted"), false));
        predicates.add(cb.equal(root.get("planRisk").get("id"), planId));

        if (params.hasTerm()) {
            String term = "%" + params.getTerm().toLowerCase() + "%";

            Expression<String> userName = cb.lower(root.get("responsibleUser").get("name").as(String.class));
            Expression<String> fieldExpr = cb.lower(root.get("changedField").as(String.class));
            Expression<String> oldExpr   = cb.lower(root.get("oldValue").as(String.class));
            Expression<String> newExpr   = cb.lower(root.get("newValue").as(String.class));
            Expression<String> dateExpr  = cb.lower(
                cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y"))
            );

            predicates.add(cb.or(
                cb.like(userName, term),
                cb.like(fieldExpr, term),
                cb.like(oldExpr, term),
                cb.like(newExpr, term),
                cb.like(dateExpr, term)
            ));
        }
        return predicates.toArray(new Predicate[0]);
    }


    public static class PlanRiskHistoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String responsibleUser;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String changeDate;

        public PlanRiskHistoryDTO(PlanRiskHistory h) {
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
