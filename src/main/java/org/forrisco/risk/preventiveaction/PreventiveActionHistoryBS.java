package org.forrisco.risk.preventiveaction;

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
public class PreventiveActionHistoryBS extends HibernateBusiness {

	@PersistenceContext
	private EntityManager em;

	public void save(PreventiveActionHistory history) {
		history.setDeleted(false);
		this.persist(history);
	}

	/* ====== Gravação das mudanças campo a campo ====== */

	private void saveHistoryEntry(String oldValue, String newValue, String fieldName,
	                              PreventiveAction action, User responsible) {
		String oldVal = oldValue == null ? "" : oldValue;
		String newVal = newValue == null ? "" : newValue;

		if (!Objects.equals(oldVal, newVal)) {
			PreventiveActionHistory h = new PreventiveActionHistory();
			h.setAction(action);
			h.setResponsibleUser(responsible);
			h.setChangedField(fieldName);
			h.setOldValue(oldVal);
			h.setNewValue(newVal);
			h.setChangeDate(LocalDateTime.now());
			this.save(h);
		}
	}

	/** Compara strings; para “Descrição” remove HTML para comparação justa */
	private void compareAndSave(String fieldLabel, String oldVal, String newVal,
	                            PreventiveAction action, User responsible) {
		String ov = oldVal == null ? "" : oldVal;
		String nv = newVal == null ? "" : newVal;

		if ("Descrição".equals(fieldLabel)) {
			ov = Jsoup.parse(ov).text();
			nv = Jsoup.parse(nv).text();
		}
		saveHistoryEntry(ov, nv, fieldLabel, action, responsible);
	}

	private String formatBool(boolean v) { return v ? "Sim" : "Não"; }

	private String formatDate(java.util.Date d) {
		if (d == null) return "";
		// Mantém o padrão usado no restante do sistema
		return new java.text.SimpleDateFormat("dd/MM/yyyy").format(d);
	}

	private String formatUser(User u) { return (u == null) ? "" : u.getName(); }

	/**
	 * Registra as alterações relevantes da ação preventiva.
	 * - Caso “canChangeAssignment” seja false, não registra nem altera Responsável/Gestor.
	 */
	public void saveActionChanges(PreventiveAction existent, PreventiveAction incoming,
	                              User newResponsible, User newManager, User responsibleUser,
	                              boolean canChangeAssignment) {

		// Descrição (campo "action")
		compareAndSave("Descrição", existent.getAction(), incoming.getAction(), existent, responsibleUser);

		// Realizada?
		saveHistoryEntry(formatBool(existent.isAccomplished()), formatBool(incoming.isAccomplished()),
				"Realizada", existent, responsibleUser);

		// Vigência
		saveHistoryEntry(formatDate(existent.getValidityBegin()), formatDate(incoming.getValidityBegin()),
				"Início da Vigência", existent, responsibleUser);
		saveHistoryEntry(formatDate(existent.getValidityEnd()), formatDate(incoming.getValidityEnd()),
				"Fim da Vigência", existent, responsibleUser);

		// Anexo (compara pelo link)
		compareAndSave("Anexo", existent.getFileLink(), incoming.getFileLink(), existent, responsibleUser);

		// Atribuições (apenas se permitido)
		if (canChangeAssignment) {
			compareAndSave("Responsável Técnico", formatUser(existent.getUser()), formatUser(newResponsible),
					existent, responsibleUser);
			compareAndSave("Gestor", formatUser(existent.getManager()), formatUser(newManager),
					existent, responsibleUser);
		}
	}

	/* ====== Listagem paginada do histórico por Ação ====== */

	public PaginatedList<PreventiveActionHistoryDTO> listHistoryByAction(Long actionId, DefaultParams params) {
		CriteriaBuilder cb = this.em.getCriteriaBuilder();

		// Query principal
		CriteriaQuery<PreventiveActionHistory> cq = cb.createQuery(PreventiveActionHistory.class);
		Root<PreventiveActionHistory> root = cq.from(PreventiveActionHistory.class);
		cq.select(root).where(getPredicates(cb, root, actionId, params));

		// Ordenação
		if (params.isSorting()) {
			org.forpdi.core.common.Order sortOrder = params.getSortOrder();
			String prop = sortOrder.getPropertyName();
			cq.orderBy(sortOrder.isAscending() ? cb.asc(root.get(prop)) : cb.desc(root.get(prop)));
		} else {
			cq.orderBy(cb.desc(root.get("changeDate")));
		}

		TypedQuery<PreventiveActionHistory> query = this.em.createQuery(cq);
        int pageSize = params.getPageSize() <= 0 ? 10 : params.getPageSize();
        int page     = params.getPage()     <= 0 ? 1  : params.getPage();    

        int offset = (page - 1) * pageSize;

        query.setMaxResults(pageSize);
        query.setFirstResult(offset);

		List<PreventiveActionHistory> list = query.getResultList();

		// Query de total
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<PreventiveActionHistory> countRoot = countQuery.from(PreventiveActionHistory.class);
		countQuery.select(cb.countDistinct(countRoot.get("id")))
		          .where(getPredicates(cb, countRoot, actionId, params));
		Long total = this.em.createQuery(countQuery).getSingleResult();

		// DTO + retorno
		List<PreventiveActionHistoryDTO> dtoList = list.stream()
			.map(PreventiveActionHistoryDTO::new)
			.collect(Collectors.toList());

		PaginatedList<PreventiveActionHistoryDTO> result = new PaginatedList<>();
		result.setList(dtoList);
		result.setTotal(total);
		return result;
	}

	private Predicate[] getPredicates(CriteriaBuilder cb, Root<PreventiveActionHistory> root,
	                                  Long actionId, DefaultParams params) {
		List<Predicate> preds = new ArrayList<>();
		preds.add(cb.equal(root.get("deleted"), false));
		preds.add(cb.equal(root.get("action").get("id"), actionId));

		if (params.hasTerm()) {
			String term = "%" + params.getTerm().toLowerCase() + "%";
			Join<PreventiveActionHistory, User> userJoin = root.join("responsibleUser");

			// like em nome do usuário, nome do campo, valores e data formatada dd/MM/yyyy
			Predicate dateLike = cb.like(
				cb.function("DATE_FORMAT", String.class, root.get("changeDate"), cb.literal("%d/%m/%Y")),
				"%" + params.getTerm() + "%"
			);

			preds.add(cb.or(
				cb.like(cb.lower(userJoin.get("name")), term),
				cb.like(cb.lower(root.get("changedField")), term),
				cb.like(cb.lower(root.get("oldValue")), term),
				cb.like(cb.lower(root.get("newValue")), term),
				dateLike
			));
		}
		return preds.toArray(new Predicate[0]);
	}

	/* ====== DTO ====== */
	public static class PreventiveActionHistoryDTO implements Serializable {
		private static final long serialVersionUID = 1L;

		private Long id;
		private String responsibleUser;
		private String changedField;
		private String oldValue;
		private String newValue;
		private String changeDate;

		public PreventiveActionHistoryDTO(PreventiveActionHistory h) {
			this.id = h.getId();
			this.responsibleUser = h.getResponsibleUser() != null ? h.getResponsibleUser().getName() : "";
			this.changedField = h.getChangedField();
			this.oldValue = h.getOldValue();
			this.newValue = h.getNewValue();
			this.changeDate = h.getChangeDate()
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
		}

		public Long getId() { return id; }
		public String getResponsibleUser() { return responsibleUser; }
		public String getChangedField() { return changedField; }
		public String getOldValue() { return oldValue; }
		public String getNewValue() { return newValue; }
		public String getChangeDate() { return changeDate; }
	}
}
