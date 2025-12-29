package org.forrisco.risk.monitor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.user.User;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MonitoringHistoryBS extends HibernateBusiness {

    @Autowired
  
	@PersistenceContext
	private EntityManager em;

	public void save(MonitoringHistory history) {
		history.setDeleted(false);
		this.persist(history);
	}

	/* ===================== Registro de mudanças ===================== */

	private String formatBool(Boolean v) { return (v != null && v) ? "Sim" : "Não"; }

	private String formatDate(Object d) {
		if (d == null) return "";
		if (d instanceof java.util.Date)
			return new java.text.SimpleDateFormat("dd/MM/yyyy").format((java.util.Date) d);
		if (d instanceof LocalDate)
			return ((LocalDate) d).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		if (d instanceof LocalDateTime)
			return ((LocalDateTime) d).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
		return d.toString();
	}

	private String toDisplay(Object val) {
		if (val == null) return "";
		if (val instanceof User) return ((User) val).getName();
		if (val instanceof Boolean) return formatBool((Boolean) val);
		if (val instanceof Enum) return ((Enum<?>) val).name();
		return formatDate(val); // cobre datas e fallback para toString
	}

	private Object callGetter(Object bean, String getter) {
		if (bean == null) return null;
		try {
			Method m = bean.getClass().getMethod(getter);
			return m.invoke(bean);
		} catch (Exception ignore) {
			return null; // getter não existe neste modelo: silenciosamente ignora
		}
	}

	private void logIfGetter(String label, String getter, Monitor existing, Monitor incoming,
	                         MonitoringHistoryCarrier carrier) {
		Object ov = callGetter(existing, getter);
		Object nv = callGetter(incoming, getter);
		String oldVal = toDisplay(ov);
		String newVal = toDisplay(nv);

		// Normaliza descrição removendo HTML
		if ("Parecer".equals(label)) {
			oldVal = Jsoup.parse(oldVal).text();
			newVal = Jsoup.parse(newVal).text();
		}
		if (!Objects.equals(oldVal, newVal)) {
			carrier.add(label, oldVal, newVal);
		}
	}

	private void logIfAnyGetter(String label, List<String> getters, Monitor existing, Monitor incoming,
	                            MonitoringHistoryCarrier carrier) {
		for (String g : getters) {
			// usa o primeiro getter que existir no seu modelo
			Object ov = callGetter(existing, g);
			Object nv = callGetter(incoming, g);
			if (ov != null || nv != null) {
				logIfGetter(label, g, existing, incoming, carrier);
				return;
			}
		}
	}

	/** Container para acumular e salvar entradas de histórico num shot. */
	private class MonitoringHistoryCarrier {
		private final Monitor monitoring;
		private final User editor;
		private final List<MonitoringHistory> entries = new ArrayList<>();
		MonitoringHistoryCarrier(Monitor monitoring, User editor) {
			this.monitoring = monitoring; this.editor = editor;
		}
		void add(String fieldLabel, String oldVal, String newVal) {
			MonitoringHistory h = new MonitoringHistory();
			h.setMonitoring(monitoring);
			h.setResponsibleUser(editor);
			h.setChangedField(fieldLabel);
			h.setOldValue(oldVal == null ? "" : oldVal);
			h.setNewValue(newVal == null ? "" : newVal);
			h.setChangeDate(LocalDateTime.now());
			entries.add(h);
		}
		void flush() { entries.forEach(MonitoringHistoryBS.this::save); }
	}

	/**
	 * Registra as alterações relevantes do Monitoramento.
	 * É tolerante a variações de nomes de getters (usa reflexão): escolhe o primeiro que existir.
	 * Campos rastreados (labels padronizados para a HU02):
	 *   - Descrição, Status, Observação, Início/Fim da Vigência, Anexo, Responsável Técnico, Gestor.
	 */
	public void saveMonitoringChanges(Monitor existing, Monitor incoming,
	                                  User newResponsible, User newManager, User editor,
	                                  boolean canChangeAssignment) {

		MonitoringHistoryCarrier carrier = new MonitoringHistoryCarrier(existing, editor);

		// Descrição
		logIfAnyGetter("Descrição",
				List.of("getDescription", "getDetails", "getObservation", "getNotes"),
				existing, incoming, carrier);

		// Status (Enum/String/Boolean)
		logIfAnyGetter("Status",
				List.of("getStatus", "getSituation", "getState"),
				existing, incoming, carrier);

		// Observação
		logIfAnyGetter("Observação",
				List.of("getObservation", "getNotes", "getComment"),
				existing, incoming, carrier);

		// Vigência (datas)
		logIfAnyGetter("Início da Vigência",
				List.of("getValidityBegin", "getStartDate", "getBeginDate"),
				existing, incoming, carrier);
		logIfAnyGetter("Fim da Vigência",
				List.of("getValidityEnd", "getEndDate", "getDueDate"),
				existing, incoming, carrier);

		// Anexo (link)
		logIfAnyGetter("Anexo",
				List.of("getFileLink", "getEvidenceLink"),
				existing, incoming, carrier);

		// Atribuições (quando permitido)
		if (canChangeAssignment) {
			String oldResp = toDisplay(callGetter(existing, "getUser"));
			String newResp = toDisplay(newResponsible);
			if (!Objects.equals(oldResp, newResp)) carrier.add("Responsável Técnico", oldResp, newResp);

			String oldMgr = toDisplay(callGetter(existing, "getManager"));
			String newMgr = toDisplay(newManager);
			if (!Objects.equals(oldMgr, newMgr)) carrier.add("Gestor", oldMgr, newMgr);
		}

		carrier.flush();
	}

	/* ===================== Listagem paginada/filtrada ===================== */

	public PaginatedList<MonitoringHistoryDTO> listHistoryByMonitoring(Long monitoringId, DefaultParams params) {
		CriteriaBuilder cb = this.em.getCriteriaBuilder();

		// Query principal
		CriteriaQuery<MonitoringHistory> cq = cb.createQuery(MonitoringHistory.class);
		Root<MonitoringHistory> root = cq.from(MonitoringHistory.class);
		cq.select(root).where(getPredicates(cb, root, monitoringId, params));

		// Ordenação
		if (params.isSorting()) {
			org.forpdi.core.common.Order sort = params.getSortOrder();
			String prop = sort.getPropertyName();
			cq.orderBy(sort.isAscending() ? cb.asc(root.get(prop)) : cb.desc(root.get(prop)));
		} else {
			cq.orderBy(cb.desc(root.get("changeDate")));
		}

		TypedQuery<MonitoringHistory> query = this.em.createQuery(cq);

		// Paginação (DefaultParams usa int)
		int pageSize = params.getPageSize() <= 0 ? 10 : params.getPageSize();
		int page     = params.getPage()     <= 0 ? 1  : params.getPage();
		int offset   = (page - 1) * pageSize;
		query.setMaxResults(pageSize);
		query.setFirstResult(offset);

		List<MonitoringHistory> list = query.getResultList();

		// Count
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<MonitoringHistory> countRoot = countQuery.from(MonitoringHistory.class);
		countQuery.select(cb.countDistinct(countRoot.get("id")))
				  .where(getPredicates(cb, countRoot, monitoringId, params));
		Long total = this.em.createQuery(countQuery).getSingleResult();

		// DTOs
		List<MonitoringHistoryDTO> dtoList = list.stream()
				.map(MonitoringHistoryDTO::new)
				.collect(Collectors.toList());

		PaginatedList<MonitoringHistoryDTO> result = new PaginatedList<>();
		result.setList(dtoList);
		result.setTotal(total);
		return result;
	}

	private Predicate[] getPredicates(CriteriaBuilder cb, Root<MonitoringHistory> root,
	                                  Long monitoringId, DefaultParams params) {
		List<Predicate> preds = new ArrayList<>();
		preds.add(cb.equal(root.get("deleted"), false));
		preds.add(cb.equal(root.get("monitoring").get("id"), monitoringId));

		if (params.hasTerm()) {
			String term = "%" + params.getTerm().toLowerCase() + "%";
			Join<MonitoringHistory, User> userJoin = root.join("responsibleUser");

			// like em nome do usuário, nome do campo, valores, e data (dd/MM/yyyy)
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

	/* ===================== DTO ===================== */

	public static class MonitoringHistoryDTO implements Serializable {
		private static final long serialVersionUID = 1L;

		private Long id;
		private String responsibleUser;
		private String changedField;
		private String oldValue;
		private String newValue;
		private String changeDate;

		public MonitoringHistoryDTO(MonitoringHistory h) {
			this.id = h.getId();
			this.responsibleUser = h.getResponsibleUser() != null ? h.getResponsibleUser().getName() : "";
			this.changedField = h.getChangedField();
			this.oldValue = h.getOldValue();
			this.newValue = h.getNewValue();
			this.changeDate = h.getChangeDate()
					.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
		}

		// getters
		public Long getId() { return id; }
		public String getResponsibleUser() { return responsibleUser; }
		public String getChangedField() { return changedField; }
		public String getOldValue() { return oldValue; }
		public String getNewValue() { return newValue; }
		public String getChangeDate() { return changeDate; }
	}


/**
 * Registra histórico quando houve mudança (versão simples e direta).
 * Usa o ID do monitor para não exigir o objeto carregado.
 */
public void addIfChanged(Long monitoringId, User editor, String field, Object oldVal, Object newVal) {
	// formata os valores para exibição, seguindo o padrão utilitário da classe
	String oldDisplay = toDisplay(oldVal);
	String newDisplay = toDisplay(newVal);

	// normalização específica por campo (ex.: report/descrição sem HTML)
	oldDisplay = Jsoup.parse(oldDisplay == null ? "" : oldDisplay).text().trim();
	newDisplay = Jsoup.parse(newDisplay == null ? "" : newDisplay).text().trim();

	if (Objects.equals(oldDisplay, newDisplay)) {
		return; // nada mudou, não registra
	}

	MonitoringHistory h = new MonitoringHistory();
	// referenciamos apenas por ID, sem dar select
	h.setMonitoring(this.em.getReference(Monitor.class, monitoringId));
	h.setResponsibleUser(editor);
	h.setChangedField(field);
	h.setOldValue(oldDisplay);
	h.setNewValue(newDisplay);
	h.setChangeDate(LocalDateTime.now());
	this.save(h);
}

/** Overload conveniente quando já se tem o objeto Monitor. */
public void addIfChanged(Monitor monitoring, User editor, String field, Object oldVal, Object newVal) {
	if (monitoring == null || monitoring.getId() == null) return;
	this.addIfChanged(monitoring.getId(), editor, field, oldVal, newVal);
}
	
}
