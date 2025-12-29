package org.forrisco.core.unit;

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
import org.forrisco.core.plan.PlanRisk;
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
public class UnitHistoryBS extends HibernateBusiness {

    @PersistenceContext
	private EntityManager em;


    /**
     * Salva um novo registro de histórico para uma Unidade. 
     * @param history A instância de UnitHistory a ser persistida.
     */
    public void save(UnitHistory history) {
        history.setDeleted(false);
        this.persist(history);
    }

/**
     * Compara duas instâncias de Unidade e salva um registro de histórico para cada campo alterado.
     * 
     * @param oldUnit A versão da Unidade antes da alteração.
     * 
     * @param newUnit A versão da Unidade com os novos dados da requisição.
     * 
     * @param newUser O objeto User completo do novo responsável.
     * 
     * @param newPlanRisk O objeto PlanRisk completo do novo plano.
     * 
     * @param responsibleUser O usuário que realizou a alteração.
     */
    public void saveUnitChanges(Unit oldUnit, Unit newUnit, User newUser, PlanRisk newPlanRisk, User responsibleUser) {
        compareAndLog(oldUnit.getName(), newUnit.getName(), "Nome", oldUnit, responsibleUser);
        compareAndLog(oldUnit.getAbbreviation(), newUnit.getAbbreviation(), "Sigla", oldUnit, responsibleUser);
        compareAndLog(oldUnit.getDescription(), newUnit.getDescription(), "Descrição", oldUnit, responsibleUser);
        if (!Objects.equals(oldUnit.getUser().getId(), newUnit.getUser().getId())) {
             compareAndLog(oldUnit.getUser().getName(), newUser.getName(), "Responsável Técnico", oldUnit, responsibleUser);
        }
        if (!Objects.equals(oldUnit.getPlanRisk().getId(), newUnit.getPlanRisk().getId())) {
             compareAndLog(oldUnit.getPlanRisk().getName(), newPlanRisk.getName(), "Plano de Risco Vinculado", oldUnit, responsibleUser);
        }
    }
    private void saveHistoryEntry(String oldValue, String newValue, String fieldName, Unit unit, User responsibleUser) {
        UnitHistory history = new UnitHistory();
        history.setUnit(unit);
        history.setChangedField(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setResponsibleUser(responsibleUser);
        history.setChangeDate(LocalDateTime.now());
        history.setDeleted(false);
        this.persist(history);
    }
    private void compareAndLog(String oldValue, String newValue, String fieldName, Unit unit, User responsibleUser) {
        String oldVal = (oldValue == null) ? "" : oldValue;
        String newVal = (newValue == null) ? "" : newValue;
        if (!Objects.equals(oldVal, newVal)) {
            if ("Descrição".equals(fieldName)) {
                oldVal = Jsoup.parse(oldVal).text();
                newVal = Jsoup.parse(newVal).text();
            }
            saveHistoryEntry(oldVal, newVal, fieldName, unit, responsibleUser);
        }
    }

    public PaginatedList<UnitHistoryDTO> listHistoryByUnit(Long unitId, DefaultParams params) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        
        CriteriaQuery<UnitHistory> cq = cb.createQuery(UnitHistory.class);
        Root<UnitHistory> root = cq.from(UnitHistory.class);
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UnitHistory> countRoot = countQuery.from(UnitHistory.class);
        
        Predicate[] predicates = this.getPredicates(cb, root, unitId, params);
        Predicate[] countPredicates = this.getPredicates(cb, countRoot, unitId, params);
        
        cq.select(root).where(predicates);
        countQuery.select(cb.countDistinct(countRoot.get("id"))).where(countPredicates);
        
        if (params.isSorting()) {
            org.forpdi.core.common.Order sortOrder = params.getSortOrder();
            String property = sortOrder.getPropertyName();
            if (sortOrder.isAscending()) {
                cq.orderBy(cb.asc(root.get(property)));
            } else {
                cq.orderBy(cb.desc(root.get(property)));
            }
        } else {
            cq.orderBy(cb.desc(root.get("changeDate")));
        }

        TypedQuery<UnitHistory> query = this.em.createQuery(cq);
        query.setFirstResult((params.getPage() - 1) * params.getPageSize());
        query.setMaxResults(params.getPageSize());
        
        List<UnitHistory> historyList = query.getResultList();
        Long total = this.em.createQuery(countQuery).getSingleResult();

        List<UnitHistoryDTO> dtoList = historyList.stream()
            .map(UnitHistoryDTO::new)
            .collect(Collectors.toList());
        
        PaginatedList<UnitHistoryDTO> results = new PaginatedList<>();
        results.setList(dtoList);
        results.setTotal(total);
        return results;
    }

    private Predicate[] getPredicates(CriteriaBuilder cb, Root<UnitHistory> root, Long unitId, DefaultParams params) {
        Join<UnitHistory, User> userJoin = root.join("responsibleUser");
        
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("deleted"), false));
        predicates.add(cb.equal(root.get("unit").get("id"), unitId));

        if (params.hasTerm()) {
            String term = "%" + params.getTerm() + "%";
            Predicate searchDate = cb.like(cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y")), term);

            predicates.add(cb.or(
                cb.like(cb.lower(userJoin.get("name")), term),
                cb.like(cb.lower(root.get("changedField")), term),
                cb.like(root.get("oldValue"), term),
                cb.like(root.get("newValue"), term),
                searchDate
            ));
        }
        
        return predicates.toArray(new Predicate[0]);
    }

    public static class UnitHistoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String responsibleUser;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String changeDate;

        public UnitHistoryDTO(UnitHistory history) {
            this.id = history.getId();
            this.responsibleUser = history.getResponsibleUser().getName();
            this.changedField = history.getChangedField();
            this.oldValue = history.getOldValue();
            this.newValue = history.getNewValue();
            this.changeDate = history.getChangeDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
        }

        public Long getId() { return id; }
        public String getResponsibleUser() { return responsibleUser; }
        public String getChangedField() { return changedField; }
        public String getOldValue() { return oldValue; }
        public String getNewValue() { return newValue; }
        public String getChangeDate() { return changeDate; }
    }
}