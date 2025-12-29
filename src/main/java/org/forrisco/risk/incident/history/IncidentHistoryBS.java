package org.forrisco.risk.incident.history;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.forrisco.risk.incident.Incident;
import org.forrisco.risk.incident.IncidentType;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;

@Service
public class IncidentHistoryBS extends HibernateBusiness {

    @PersistenceContext
    private EntityManager em;

    public void save(IncidentHistory history) {
        history.setDeleted(false);
        this.persist(history);
    }

    public void saveIncidentChanges(Incident oldInc, Incident newInc, User responsibleUser) {
        // Texto (sanitize de comparação)
        compareAndLog(htmlToText(oldInc.getDescription()), htmlToText(newInc.getDescription()),
                "Descrição", oldInc, responsibleUser);
        compareAndLog(htmlToText(oldInc.getAction()), htmlToText(newInc.getAction()),
                "Ação Corretiva", oldInc, responsibleUser);

        // Tipo (int -> label)
        String oldType = toTypeLabel(oldInc.getType());
        String newType = toTypeLabel(newInc.getType());
        compareAndLog(oldType, newType, "Tipo", oldInc, responsibleUser);

        // Data/Hora (Date -> string)
        String oldBegin = formatDate(oldInc.getBegin());
        String newBegin = formatDate(newInc.getBegin());
        compareAndLog(oldBegin, newBegin, "Data/Hora", oldInc, responsibleUser);

        // Responsável técnico
        String oldResp = oldInc.getUser() == null ? "" : oldInc.getUser().getName();
        String newResp = newInc.getUser() == null ? "" : newInc.getUser().getName();
        compareAndLog(oldResp, newResp, "Responsável Técnico", oldInc, responsibleUser);

        // Gestor
        String oldMgr = oldInc.getManager() == null ? "" : oldInc.getManager().getName();
        String newMgr = newInc.getManager() == null ? "" : newInc.getManager().getName();
        compareAndLog(oldMgr, newMgr, "Gestor", oldInc, responsibleUser);
    }

    private String htmlToText(String v) {
        return v == null ? "" : Jsoup.parse(v).text();
    }
    private String toTypeLabel(int typeId) {
        IncidentType t = IncidentType.getById(typeId);
        return (t == null) ? "" : t.getLabel();
    }
    private String formatDate(Date d) {
        if (d == null) return "";
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
    }

    private void compareAndLog(String oldValue, String newValue, String fieldName, Incident incident, User responsibleUser) {
        String a = oldValue == null ? "" : oldValue;
        String b = newValue == null ? "" : newValue;
        if (!Objects.equals(a, b)) {
            IncidentHistory h = new IncidentHistory();
            h.setIncident(incident);
            h.setResponsibleUser(responsibleUser);
            h.setChangedField(fieldName);
            h.setOldValue(a);
            h.setNewValue(b);
            h.setChangeDate(LocalDateTime.now());
            h.setDeleted(false);
            this.persist(h);
        }
    }

    // ===== Listagem paginada do histórico do incidente =====
    public PaginatedList<IncidentHistoryDTO> listHistoryByIncident(Long incidentId, DefaultParams params) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<IncidentHistory> cq = cb.createQuery(IncidentHistory.class);
        Root<IncidentHistory> root = cq.from(IncidentHistory.class);

        CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
        Root<IncidentHistory> countRoot = countQ.from(IncidentHistory.class);

        Predicate[] ps = predicates(cb, root, incidentId, params);
        Predicate[] cps = predicates(cb, countRoot, incidentId, params);

        cq.select(root).where(ps);
        countQ.select(cb.countDistinct(countRoot.get("id"))).where(cps);

        if (params.isSorting()) {
            var o = params.getSortOrder();
            cq.orderBy(o.isAscending() ? cb.asc(root.get(o.getPropertyName())) : cb.desc(root.get(o.getPropertyName())));
        } else {
            cq.orderBy(cb.desc(root.get("changeDate")));
        }

        TypedQuery<IncidentHistory> q = em.createQuery(cq)
                .setFirstResult((params.getPage() - 1) * params.getPageSize())
                .setMaxResults(params.getPageSize());

        List<IncidentHistory> list = q.getResultList();
        Long total = em.createQuery(countQ).getSingleResult();

        PaginatedList<IncidentHistoryDTO> out = new PaginatedList<>();
        out.setList(list.stream().map(IncidentHistoryDTO::new).collect(Collectors.toList()));
        out.setTotal(total);
        return out;
    }

    private Predicate[] predicates(CriteriaBuilder cb, Root<IncidentHistory> root, Long incidentId, DefaultParams params) {
        Join<IncidentHistory, User> userJoin = root.join("responsibleUser", JoinType.INNER);
        List<Predicate> ps = new ArrayList<>();
        ps.add(cb.equal(root.get("deleted"), false));
        ps.add(cb.equal(root.get("incident").get("id"), incidentId));

        if (params.hasTerm()) {
            String term = "%" + params.getTerm().toLowerCase() + "%";
            Expression<String> dateStr = cb.function("DATE_FORMAT", String.class,
                    root.get("changeDate"), cb.literal("%d/%m/%Y - %H:%i"));
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

    public static class IncidentHistoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String responsibleUser;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String changeDate;

        public IncidentHistoryDTO(IncidentHistory h) {
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

