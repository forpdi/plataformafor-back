package org.forrisco.core.policy;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
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
public class PolicyHistoryBS extends HibernateBusiness {

	@PersistenceContext
	private EntityManager em;

	public void save(PolicyHistory history) {
		history.setDeleted(false);
		this.persist(history);
	}

	public void saveValidityChange(Policy policy, Date oldEnd, Date newEnd, User responsibleUser) {
		String oldValue = formatDate(oldEnd);
		String newValue = formatDate(newEnd);

		if (Objects.equals(oldValue, newValue)) {
			return;
		}

		PolicyHistory h = new PolicyHistory();
		h.setPolicy(policy);
		h.setResponsibleUser(responsibleUser);
		h.setChangedField("Fim da Vigência");
		h.setOldValue(oldValue);
		h.setNewValue(newValue);
		h.setChangeDate(LocalDateTime.now());
		h.setDeleted(false);
		this.persist(h);
	}

	public PaginatedList<PolicyHistoryDTO> listHistoryByPolicy(Long policyId, DefaultParams params) {
		CriteriaBuilder cb = this.em.getCriteriaBuilder();

		CriteriaQuery<PolicyHistory> cq = cb.createQuery(PolicyHistory.class);
		Root<PolicyHistory> root = cq.from(PolicyHistory.class);

		Predicate[] predicates = this.getPredicates(cb, root, policyId, params);
		cq.select(root).where(predicates);

		if (params.isSorting()) {
			var so = params.getSortOrder();
			String property = so.getPropertyName();
			cq.orderBy(so.isAscending() ? cb.asc(root.get(property)) : cb.desc(root.get(property)));
		} else {
			cq.orderBy(cb.desc(root.get("changeDate")));
		}

		TypedQuery<PolicyHistory> query = this.em.createQuery(cq);
		int first = Math.max(0, (params.getPage() - 1) * params.getPageSize());
		query.setFirstResult(first);
		query.setMaxResults(params.getPageSize());

		List<PolicyHistory> list = query.getResultList();

		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<PolicyHistory> countRoot = countQuery.from(PolicyHistory.class);
		countQuery.select(cb.countDistinct(countRoot.get("id")))
				.where(this.getPredicates(cb, countRoot, policyId, params));
		Long total = this.em.createQuery(countQuery).getSingleResult();

		PaginatedList<PolicyHistoryDTO> result = new PaginatedList<>();
		result.setList(list.stream().map(PolicyHistoryDTO::new).collect(Collectors.toList()));
		result.setTotal(total);
		return result;
	}

	private Predicate[] getPredicates(CriteriaBuilder cb, Root<PolicyHistory> root, Long policyId, DefaultParams params) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("deleted"), false));
		predicates.add(cb.equal(root.get("policy").get("id"), policyId));

		if (params.hasTerm()) {
			String term = "%" + params.getTerm().toLowerCase() + "%";

			Expression<String> userName = cb.lower(root.get("responsibleUser").get("name").as(String.class));
			Expression<String> fieldExpr = cb.lower(root.get("changedField").as(String.class));
			Expression<String> oldExpr = cb.lower(cb.coalesce(root.get("oldValue"), ""));
			Expression<String> newExpr = cb.lower(cb.coalesce(root.get("newValue"), ""));
			Expression<String> dateExpr = cb.lower(
					cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y - %H:%i")));

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

	private String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		return new SimpleDateFormat("dd/MM/yyyy").format(date);
	}

	public static class PolicyHistoryDTO implements Serializable {
		private static final long serialVersionUID = 1L;

		private Long id;
		private String responsibleUser;
		private String changedField;
		private String oldValue;
		private String newValue;
		private String changeDate;

		public PolicyHistoryDTO(PolicyHistory h) {
			this.id = h.getId();
			this.responsibleUser = h.getResponsibleUser().getName();
			this.changedField = h.getChangedField();
			this.oldValue = h.getOldValue();
			this.newValue = h.getNewValue();
			this.changeDate = h.getChangeDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
		}

		public Long getId() {
			return id;
		}

		public String getResponsibleUser() {
			return responsibleUser;
		}

		public String getChangedField() {
			return changedField;
		}

		public String getOldValue() {
			return oldValue;
		}

		public String getNewValue() {
			return newValue;
		}

		public String getChangeDate() {
			return changeDate;
		}
	}
}
