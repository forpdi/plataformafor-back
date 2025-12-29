package org.forrisco.risk.preventiveaction;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Criterion;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.ProjectionList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.DateUtil;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.risk.Risk;
import org.forpdi.core.common.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PreventiveActionBS extends HibernateBusiness {

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ArchiveBS archiveBS;

	@Autowired
	private PreventiveActionHistoryBS historyBS;
	/**
	 * Salva no banco de dados uma nova ação preventiva
	 * 
	 * @param PreventiveAction,
	 *            instância de uma ação preventiva a ser salva
	 */
	public void saveAction(PreventiveAction action) {
		Archive file = null;
		if(!GeneralUtils.isEmpty(action.getFileLink())) {
			file = archiveBS.getByFileLink(action.getFileLink());
		}
		action.setDeleted(false);
		action.setFile(file);
		// action.setAccomplished(false);
		this.persist(action);
	}


	/**
	 * Deleta do banco de dados uma ação de prevenção
	 * 
	 * @param PreventiveAction,
	 *            instância da a ação ser deletado
	 */
	public void delete(PreventiveAction action) {
		action.setDeleted(true);
		this.persist(action);
	}

	/**
	 * Retorna as ações preventivas a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<PreventiveAction>
	 */
	public PaginatedList<PreventiveAction> listActionByRisk(Risk risk) {
		return listActionByRisk(risk, -1);
	}
	
	/**
	 * Retorna as ações preventivas a partir de um risco
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<PreventiveAction>
	 */
	public PaginatedList<PreventiveAction> listActionByRisk(Risk risk, Integer selectedYear) {

		PaginatedList<PreventiveAction> results = new PaginatedList<PreventiveAction>();

		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PreventiveAction> cq = builder.createQuery(PreventiveAction.class);
		Root<PreventiveAction> root = cq.from(PreventiveAction.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("deleted"), false));
		predicates.add(builder.equal(root.get("risk"), risk));
		
		if (selectedYear != -1) {
			Expression<Integer> yearExpression = builder.function("year", Integer.class, root.get("begin"));
			predicates.add(builder.equal(yearExpression, selectedYear));
		}
		
		cq.where(predicates.toArray(new Predicate[] {}));

		List<PreventiveAction> list = entityManager.createQuery(cq).getResultList();
		
		results.setList(list);	
		results.setTotal((long) list.size());

		return results;		
	}

	public List<PreventiveActionBean> listActionsByPlanRisk(PlanRisk planRisk) {
		Criteria criteria = this.dao.newCriteria(PreventiveAction.class)
				.createAlias("risk", "risk")
				.createAlias("risk.unit", "unit")
				.createAlias("unit.planRisk", "planRisk")
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRisk.id", planRisk.getId()))
				.add(Restrictions.eq("risk.archived", false));


		ProjectionList projList = Projections.projectionList()
				.add(Projections.property("id"), "id")
				.add(Projections.property("risk.id"), "riskId")
				.add(Projections.property("risk.name"), "riskName")
				.add(Projections.property("risk.type"), "riskType")
				.add(Projections.property("unit.id"), "unitId")
				.add(Projections.property("action"), "action")
				.add(Projections.property("accomplished"), "accomplished")
				.add(Projections.property("validityBegin"), "validityBegin");
		
		criteria.setProjection(projList)
				.setResultTransformer(PreventiveActionBean.class);
		
		return this.dao.findByCriteria(criteria, PreventiveActionBean.class);
	}

	
	/**
	 * Retorna as ações preventivas a partir de um risco com paginacao
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<PreventiveAction>
	 */
	public PaginatedList<PreventiveAction> listActionByRisk(Risk risk, DefaultParams params) {

		PaginatedList<PreventiveAction> results = new PaginatedList<PreventiveAction>();

		Criteria criteria = this.dao.newCriteria(PreventiveAction.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(PreventiveAction.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("user", "user", JoinType.INNER_JOIN)
				.createAlias("manager", "manager", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.eq("risk", risk))
				.setProjection(Projections.countDistinct("id"));

		if (params.hasTerm()) {
			String term = params.getTerm();
			Criterion action = Restrictions.like("action", "%" + term + "%").ignoreCase();
			Criterion manager = Restrictions.like("manager.name", "%" + term + "%").ignoreCase();
			Criterion responsible = Restrictions.like("user.name", "%" + term + "%").ignoreCase();
			Criterion validityBegin = Restrictions.like("validityBegin", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();
			Criterion validityEnd = Restrictions.like("validityEnd", "%" + DateUtil.formatSearchDate(term) + "%").ignoreCase();

			criteria.add(Restrictions.or(action, manager, responsible, validityBegin, validityEnd));
			count.add(Restrictions.or(action, manager, responsible, validityBegin, validityEnd));
		}
		
		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page-1) * pageSize)
			.setMaxResults(pageSize);

		results.setList(this.dao.findByCriteria(criteria, PreventiveAction.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}

	/**
	 * Retorna uma ação de prevenção
	 * 
	 * @param id
	 *            Id da ação de prevenção
	 */
	public PreventiveAction findByActionId(Long id) {
		Criteria criteria = this.dao.newCriteria(PreventiveAction.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));

		return (PreventiveAction) criteria.uniqueResult();
	}

	public List<PreventiveAction> listByActionsIds(List<Long> idList) {
		Criteria criteria = this.dao.newCriteria(PreventiveAction.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.in("id", idList));

		return this.dao.findByCriteria(criteria, PreventiveAction.class);
	}
	
	/**
	 * Retorna as ações preventivas
	 * 
	 * @param Risk
	 *            instância de um risco
	 * 
	 * @return PaginatedList<PreventiveAction>
	 */
	public PaginatedList<PreventiveAction> listPreventiveActionByRisk(Risk risk) {
		PaginatedList<PreventiveAction> results = new PaginatedList<PreventiveAction>();

		Criteria criteria = this.dao.newCriteria(PreventiveAction.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk));

		Criteria count = this.dao.newCriteria(PreventiveAction.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("risk", risk)).setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, PreventiveAction.class));
		results.setTotal((Long) count.uniqueResult());
		return results;

	}

	/**
	 * Atualiza a ação aplicando as mudanças e registrando o histórico.
	 * @param existent Ação do banco
	 * @param incoming Dados novos (DTO->entity)
	 * @param newResponsible novo responsável técnico (pode ser null)
	 * @param newManager novo gestor (pode ser null)
	 * @param responsibleUser usuário logado que está efetuando a alteração
	 * @param canChangeAssignment se pode trocar responsável/gestor
	 */
	public PreventiveAction updateWithHistory(
			PreventiveAction existent,
			PreventiveAction incoming,
			User newResponsible,
			User newManager,
			User responsibleUser,
			boolean canChangeAssignment) {

		// 1) Log de mudanças
		historyBS.saveActionChanges(existent, incoming, newResponsible, newManager, responsibleUser, canChangeAssignment);

		// 2) Aplicar mudanças
		// Atribuições
		if (canChangeAssignment) {
			existent.setUser(newResponsible);
			existent.setManager(newManager);
		}
		// Arquivo
		Archive file = null;
		if (incoming.getFileLink() != null && !incoming.getFileLink().isEmpty()) {
			file = archiveBS.getByFileLink(incoming.getFileLink());
		}
		existent.setFileLink(incoming.getFileLink());
		existent.setFile(file);

		// Demais campos
		existent.setAccomplished(incoming.isAccomplished());
		existent.setAction(incoming.getAction());
		existent.setValidityBegin(incoming.getValidityBegin());
		existent.setValidityEnd(incoming.getValidityEnd());

		// 3) Persistir
		this.persist(existent);
		return existent;
	}

}
