package org.forrisco.risk.incident;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.forrisco.risk.RiskBS;
import org.forrisco.risk.RiskController;
import org.forrisco.risk.RiskItemPermissionInfo;
import org.forrisco.risk.incident.dto.FilterIncidentsDto;
import org.forrisco.risk.incident.dto.IncidentDto;
import org.forrisco.risk.incident.dto.ListIncidentsDto;
import org.forrisco.risk.incident.history.IncidentHistoryBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IncidentController extends AbstractController {

	public static final String PATH =  RiskController.PATH;
	
	@Autowired
	private IncidentBS bs;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private RiskBS riskBS;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private IncidentHistoryBS incidentHistoryBS;

	/**
	 * Salvar Novo incidente
	 * 
	 * @Param Incident
	 * 			instancia de uma novo incidente
	 */
	@PostMapping(PATH + "/incidentnew")
	public ResponseEntity<?> save(@RequestBody IncidentDto dto, UserSession userSession) {
		try {
			Incident incident = dto.incident();
			if (incident.getBegin().after(new Date())) {
				return this.fail("A data e hora do incidente não deve ser maior que a data e hora atual.");
			}

			Risk risk = this.bs.exists(incident.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			User user = this.bs.exists(incident.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("Usuário solicitada não foi encontrado.");
			}
			
			User manager = this.bs.exists(incident.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			if (IncidentType.getById(incident.getType()) == null) {
				return this.fail("Tipo de incidente inválido.");
			}
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, user);
			
			if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
				return this.fail("Usuário sem permissão de acesso");
			}

			incident.setId(null);
			incident.setDescription(SanitizeUtil.sanitize(incident.getDescription()));
			
			this.bs.saveIncident(incident);
			
			Company riskCompany = risk.getUnit().getPlanRisk().getPolicy().getCompany();
			List<CompanyDomain> domains = this.companyBS.retrieveCompanyByDomain(riskCompany);
			if (domains != null && !domains.isEmpty()) {
				String domainUrl = domains.get(0).getBaseUrl();
				String incidentUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
					+ "/incident/" + incident.getId() + "/info";

				User riskManager = risk.getManager();
				User riskUser = risk.getUser();

				this.bs.notifySharedRiskManager(risk.getName(), manager, incidentUrl, incident.getBegin());
				if (!riskManager.equals(manager)) {
					this.bs.notifySharedRiskManager(risk.getName(), riskManager, incidentUrl, incident.getBegin());
				}
				this.bs.notifySharedRiskResponsible(risk.getName(), user, incidentUrl, incident.getBegin());
				if (!riskUser.equals(user)) {
					this.bs.notifySharedRiskResponsible(risk.getName(), riskUser, incidentUrl, incident.getBegin());
				}

				this.riskBS.sendNotificationToRiskItemManager(
					risk,
					manager,
					incidentUrl,
					"um Incidente",
					NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM
				);
			}
			
			return this.success(incident);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna monitoramento.
	 * 
	 * @param id
	 *            Id do monitoramento a ser retornado.
	 */
	@GetMapping( PATH + "/incident")
	public ResponseEntity<?> listIncident(@RequestParam Long riskId, @ModelAttribute DefaultParams params, UserSession userSession) {
		try {
			Risk risk = this.riskBS.exists(riskId, Risk.class);
			if (risk == null || risk.isDeleted()) {
				return this.fail("O risco solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !risk.getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			PaginatedList<Incident> incident = this.bs.listIncidentsByRisk(risk, params);
			
			return this.success(incident.getList(), incident.getTotal());

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
}

	/**
	 * Retorna incidente.
	 * 
	 * @param incidentId
	 *            Id do incidente a ser retornado.
	 */
	@GetMapping(PATH + "/incident/{incidentId}")
	public ResponseEntity<?> findIncident(@PathVariable Long incidentId, UserSession userSession) {
		try {
			Incident incident = this.riskBS.exists(incidentId, Incident.class);
			if (incident == null || incident.isDeleted()) {
				return this.fail("O incidente solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !incident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			return this.success(incident);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna incidentes.
	 * 
	 * @param planId
	 *			Id do plano.
	 * @return <PaginedList> Incident
	 * 			 Retorna lista de incidentes do risco.
	 */
	@GetMapping( PATH + "/incidents")
		@CommunityDashboard
		public ResponseEntity<?> listIncidents(@RequestParam Long planId) {
			try {
				PlanRisk plan = this.riskBS.exists(planId, PlanRisk.class);
				if (plan == null) {
					return this.fail("O risco solicitado não foi encontrado.");
				} 
				domain.validateTenant(plan.getPolicy().getCompany());
				
				List<IncidentBean> list = this.bs.listIncidentsByPlanRisk(plan);
		
				return this.success(new ListWrapper<IncidentBean>(list));
			} catch (Throwable ex) {
				LOGGER.error("Unexpected runtime error", ex);
				return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
			}
		}
	/**
	 * Retorna incidentes em uma lista paginada.
	 * 
	 * @param planId
	 *			Id do plano.
	 * @return <PaginedList> Incident
	 * 			 Retorna lista de incidentes do risco.
	 */
	@PostMapping( PATH + "/incidentsPaginated")
	public ResponseEntity<?> listIncidentsPaginated(@RequestBody ListIncidentsDto dto, UserSession userSession) {
		try {
			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					return this.fail("Acesso negado. Usuário não associado a uma empresa.");
				}

				for (Long incidentId : dto.incidentsId()) {
					Incident incident = this.riskBS.exists(incidentId, Incident.class);
					if (incident == null || !incident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
						return this.fail("Acesso negado.");
					}
				}
			}

			return this.success(this.bs.pagitaneIncidents(dto.incidentsId(), dto.page(), dto.pageSize()));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Retorna riscos a partir de uma unidade
	 * @param unitId
	 */
	
	@GetMapping(PATH + "/incidentByUnit")
	public ResponseEntity<?> listIncidentsByUnit(@RequestParam Long unitId, UserSession userSession) {
		
		try {
			Unit unit = this.riskBS.exists(unitId, Unit.class);
			if (unit == null) {
				return this.fail("A unidade solicitada não foi encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			List<Risk> risks = new ArrayList<>();
			List<Incident> incidents = new ArrayList<>();
			
			PaginatedList<Risk> listRisk = this.riskBS.listRiskByUnit(unit);
			risks.addAll(listRisk.getList());
			
			for(Risk risk : risks) {
				PaginatedList<Incident> list = this.bs.listIncidentsByRisk(risk);
				incidents.addAll(list.getList());
			}
			
			PaginatedList<Incident> incident = new PaginatedList<Incident>();
			
			incident.setList(incidents);
			incident.setTotal((long) incidents.size());
			
			return this.success(incident);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Atualiza incidente.
	 *
	 * @param id
	 *           Id do incidente a ser atualizado.
	 */
	@PostMapping(PATH + "/incident/update")
	public ResponseEntity<?> updateIncident(@RequestBody IncidentDto dto, UserSession userSession) {
		try {
			Incident incident = dto.incident();
			if (incident.getBegin().after(new Date())) {
				return this.fail("A data do incidente não deve ser maior que a data atual.");
			}

			Risk risk = this.riskBS.exists(incident.getRisk().getId(), Risk.class);
			if (risk == null) {
				return this.fail("Risco solicitado não foi encontrado.");
			}

			Incident oldIncident = this.riskBS.exists(incident.getId(), Incident.class);
			if (oldIncident == null) {
				return this.fail("A ação solicitado não foi encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null ||
						!oldIncident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId()
								.equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			User user = this.riskBS.exists(incident.getUser().getId(), User.class);
			if (user == null) {
				return this.fail("O 'responsável técnico' solicitado não foi encontrado.");
			}

			User manager = this.riskBS.exists(incident.getManager().getId(), User.class);
			try {
				this.riskBS.validateManager(manager);
			} catch (IllegalStateException ex) {
				return this.fail(ex.getMessage());
			}

			if (IncidentType.getById(incident.getType()) == null) {
				return this.fail("Tipo de incidente inválido.");
			}

			boolean managerChanged = oldIncident.getManager() == null || !oldIncident.getManager().equals(manager);
			Boolean responsibleChanged = oldIncident.getUser() == null || !oldIncident.getUser().equals(user);

			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(risk, oldIncident.getUser());

			Incident before = new Incident();
			before.setId(oldIncident.getId());
			before.setRisk(oldIncident.getRisk());
			before.setUser(oldIncident.getUser());
			before.setManager(oldIncident.getManager());
			before.setDescription(oldIncident.getDescription());
			before.setAction(oldIncident.getAction());
			before.setType(oldIncident.getType());
			before.setBegin(oldIncident.getBegin());

			if (permissionInfo.isRiskResponsibleOrHasPermission()) {
				oldIncident.setUser(user);
				oldIncident.setManager(manager);
			}
			oldIncident.setAction(incident.getAction());
			oldIncident.setDescription(SanitizeUtil.sanitize(incident.getDescription()));
			oldIncident.setType(incident.getType());
			oldIncident.setBegin(incident.getBegin());

			User responsibleUser = this.riskBS.exists(userSession.getAuthData().getId(), User.class);
			if (responsibleUser == null) {
				return this.fail("Usuário responsável pela alteração não foi encontrado.");
			}

			try {
				this.incidentHistoryBS.saveIncidentChanges(before, oldIncident, responsibleUser);
			} catch (Throwable hx) {
				LOGGER.warn("Falha ao registrar histórico do incidente ID {}: {}", oldIncident.getId(), hx.getMessage(),
						hx);
			}

			this.bs.saveIncident(oldIncident);

			Company riskCompany = oldIncident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany();
			List<CompanyDomain> domains = this.companyBS.retrieveCompanyByDomain(riskCompany);
			if (domains != null && !domains.isEmpty()) {
				String domainUrl = domains.get(0).getBaseUrl();
				String incidentUrl = domainUrl + "/#/forrisco/" + "risk/" + risk.getId()
						+ "/incident/" + incident.getId() + "/info";

				NotificationType notificationType;
				String notificationAuxText = "um Incidente";
				if (managerChanged) {
					this.bs.notifySharedRiskManager(risk.getName(), manager, incidentUrl, incident.getBegin());
					notificationType = NotificationType.FORRISCO_MANAGER_LINKED_TO_RISK_ITEM;
				} else {
					notificationType = NotificationType.FORRISCO_MANAGER_RISK_ITEM_UPDATED;
					notificationAuxText = StringUtils.capitalize(notificationAuxText);
				}

				if (responsibleChanged) {
					this.bs.notifySharedRiskResponsible(risk.getName(), responsibleUser, incidentUrl, incident.getBegin());
				}

				this.riskBS.sendNotificationToRiskItemManager(
						risk,
						manager,
						incidentUrl,
						notificationAuxText,
						notificationType);
			}

			return this.success(oldIncident);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}
	
	/**
	 * Exclui incidente.
	 * 
	 * @param id
	 *            Id do incidente a ser excluido.
	 */
	@DeleteMapping(PATH + "/incident/{incidentId}")
	public ResponseEntity<?> deleteIncident(@PathVariable Long incidentId, UserSession userSession) {
		try {
			Incident incident = this.riskBS.exists(incidentId, Incident.class);
			if (incident == null) {
				return this.fail("O incidente solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !incident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			RiskItemPermissionInfo permissionInfo = riskBS.validateRiskItemPermissions(incident.getRisk(), incident.getUser());
			
			if (!permissionInfo.isRiskResponsibleOrHasPermission()) {
				return this.fail("Usuário sem permissão de acesso");
			}

			bs.delete(incident);
			return this.success(incidentId);
			
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/incident/{incidentId}/history")
	public ResponseEntity<?> getIncidentHistory(@PathVariable Long incidentId,
			@ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			Incident incident = this.riskBS.exists(incidentId, Incident.class);
			if (incident == null || incident.isDeleted()) {
				return this.fail("O incidente solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null ||
						!incident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany().getId()
								.equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(incident.getRisk().getUnit().getPlanRisk().getPolicy().getCompany());

			PaginatedList<IncidentHistoryBS.IncidentHistoryDTO> history = this.incidentHistoryBS
					.listHistoryByIncident(incidentId, params);
			return this.success(history);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@PostMapping(PATH + "/incident/filter")
	public ResponseEntity<?> filterIncidents(@RequestBody FilterIncidentsDto dto, UserSession userSession) {
		try {
			if (dto.getPlanRiskId() == null) {
				return this.fail("planRiskId é obrigatório.");
			}

			PlanRisk plan = this.riskBS.exists(dto.getPlanRiskId(), PlanRisk.class);
			if (plan == null) {
				return this.fail("Plano de gestão não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			this.domain.validateTenant(plan.getPolicy().getCompany());

			final Set<Long> unitFilter = dto.getUnitIds() == null
					? Collections.emptySet()
					: new HashSet<>(dto.getUnitIds());
			final String needle = dto.getIncident() == null ? null : dto.getIncident().trim().toLowerCase();
			final Integer type = dto.getType();
			final Date start = dto.getStartCreation();
			final Date end = dto.getEndCreation();
			final int page = (dto.getPage() == null || dto.getPage() < 1) ? 1 : dto.getPage();
			final int pageSize = (dto.getPageSize() == null || dto.getPageSize() < 1) ? 50 : dto.getPageSize();

			List<Incident> all = new ArrayList<>();
			if (unitFilter.isEmpty()) {
				return this.fail("unitIds é obrigatório.");
			} else {
				for (Long unitId : unitFilter) {
					Unit unit = this.riskBS.exists(unitId, Unit.class);
					if (unit == null)
						continue;
					if (!unit.getPlanRisk().getId().equals(plan.getId()))
						continue;

					PaginatedList<Risk> risks = this.riskBS.listRiskByUnit(unit);
					for (Risk risk : risks.getList()) {
						PaginatedList<Incident> incs = this.bs.listIncidentsByRisk(risk);
						all.addAll(incs.getList());
					}
				}
			}

			List<Incident> filtered = all.stream().filter(inc -> {
				Risk risk = inc.getRisk();
				if (risk == null || risk.getUnit() == null || risk.getUnit().getPlanRisk() == null)
					return false;
				if (!risk.getUnit().getPlanRisk().getId().equals(plan.getId()))
					return false;

				if (!unitFilter.isEmpty()) {
					Unit u = risk.getUnit();
					Long uid = u.getId();
					Long pid = (u.getParent() != null ? u.getParent().getId() : null);
					if (!unitFilter.contains(uid) && (pid == null || !unitFilter.contains(pid)))
						return false;
				}

				if (needle != null && !needle.isEmpty()) {
					String desc = inc.getDescription() != null ? inc.getDescription().toLowerCase() : "";
					String action = inc.getAction() != null ? inc.getAction().toLowerCase() : "";
					if (!desc.contains(needle) && !action.contains(needle))
						return false;
				}

				if (type != null && inc.getType() != type.intValue())
					return false;

				Date begin = inc.getBegin();
				if (start != null && (begin == null || begin.before(start)))
					return false;
				if (end != null && (begin == null || begin.after(end)))
					return false;

				return true;
			}).collect(Collectors.toList());

			long total = filtered.size();
			int from = Math.max(0, (page - 1) * pageSize);
			int to = Math.min(filtered.size(), from + pageSize);
			List<Incident> pageList = (from >= to) ? Collections.emptyList() : filtered.subList(from, to);

			PaginatedList<Incident> out = new PaginatedList<>();
			out.setList(pageList);
			out.setTotal(total);

			return this.success(out.getList(), out.getTotal());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
}
