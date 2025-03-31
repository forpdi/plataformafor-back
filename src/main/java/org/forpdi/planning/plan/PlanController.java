package org.forpdi.planning.plan;
	
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.forpdi.core.bean.IdDto;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.document.Document;
import org.forpdi.planning.document.DocumentBS;
import org.forpdi.planning.plan.dto.DuplicatePlanMacroDto;
import org.forpdi.planning.plan.dto.PlanMacroDto;
import org.forpdi.planning.plan.dto.UpdatePlanDto;
import org.forpdi.planning.structure.Structure;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Renato R. R. de Oliveira
 */
@RestController
public class PlanController extends AbstractController {

	@Autowired
	private PlanBS bs;
	@Autowired
	private StructureBS sbs;
	@Autowired
	private UserBS ubs;
	@Autowired
	private DocumentBS dbs;
	@Autowired
	private CompanyDomainContext domain;

	/**
	 * Salva plano macro.
	 * 
	 * @param plan
	 *            Plano macro a ser salvo.
	 * @return PlanMacro Plano macro salvo.
	 */
	@PostMapping(BASEPATH + "/planmacro")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveMacro(@RequestBody PlanMacroDto dto) {
		try {
			PlanMacro plan = dto.plan();
			plan.setId(null);
			plan.setDescription(SanitizeUtil.sanitize(plan.getDescription()));
			plan.setCompany(this.domain.get().getCompany());
			this.bs.savePlanMacro(plan);
			String url = domain.get().getBaseUrl() + "/#/plan/" + plan.getId();

			// this.notificationBS.sendNotification(NotificationType.PLAN_MACRO_CREATED,
			// plan.getName(), null, null, url);

			return this.success(plan);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Edita plano macro.
	 * 
	 * @param plan
	 *            Plano macro a ser alterado com os novos valores.
	 * @return PlanMacro Retorna plano macro alterado.
	 */
	@PutMapping(BASEPATH + "/planmacro")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> updateMacro(@RequestBody PlanMacroDto dto) {
		try {
			PlanMacro plan = dto.plan();
			PlanMacro existent = this.bs.exists(plan.getId(), PlanMacro.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			existent.setDescription(SanitizeUtil.sanitize(plan.getDescription()));
			existent.setName(plan.getName());
			existent.setBegin(plan.getBegin());
			existent.setEnd(plan.getEnd());
			existent.setDocumented(plan.isDocumented());
			
			if(this.bs.listPlansForPlanMacro(existent).isEmpty()){
				existent.setHaveSons(false);	
			} else {
				existent.setHaveSons(true);
			}
			
			Document document = this.dbs.retrieveByPlan(existent);
			if (document != null) {
				document.setTitle("Documento - " + plan.getName());
				this.bs.persist(document);
			}
			
			this.bs.persist(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna plano macro.
	 * 
	 * @param id
	 *            Id do plano macro a ser retornado.
	 * @return PlanMacro Retorna o plano macro de acordo com o id passado.
	 */
	@GetMapping(BASEPATH + "/planmacro/{id}")
	public ResponseEntity<?> retrieveMacro(@PathVariable Long id) {
		try {
			PlanMacro plan = this.bs.exists(id, PlanMacro.class);
			if (plan == null) {
				return this.fail("O plano solicitado não foi encontrado.");
			} else {
				if(this.bs.listPlansForPlanMacro(plan).size() > 0)
					plan.setHaveSons(true);
				else
					plan.setHaveSons(false);
				return this.success(plan);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Lista os planos macro da companhia.
	 * 
	 * @param page
	 *            Número da página da listagem a ser acessada.
	 * @return PaginatedList<PlanMacro> Lista de planos macro da companhia.
	 */
	@GetMapping(BASEPATH + "/planmacro")
    @CommunityDashboard
	public ResponseEntity<?> listMacros(@RequestParam(required = false) Integer page) {
		if (page == null)
			page = 0;
		try {
			if (this.domain != null) {
				PaginatedList<PlanMacro> plans = this.bs.listMacros(this.domain.get().getCompany(), false, page);
				for(PlanMacro p : plans.getList()){
					if(this.bs.listPlansForPlanMacro(p).size() > 0)
						p.setHaveSons(true);
					else
						p.setHaveSons(false);
				}
				return this.success(plans);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Lista os planos macro arquivados companhia.
	 * 
	 * @param page
	 *            Número da página da listagem a ser acessada.
	 * @return PaginatedList<PlanMacro> Lista de planos macro arquivados da
	 *         companhia.
	 */
	@GetMapping(BASEPATH + "/planmacro/archivedplanmacro")
    @CommunityDashboard
	public ResponseEntity<?> listMacrosArchived(@RequestParam(required = false) Integer page) {
		if (page == null)
			page = 0;
		try {
			if (this.domain != null) {
				PaginatedList<PlanMacro> plans = this.bs.listMacros(this.domain.get().getCompany(), true, page);
				return this.success(plans);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Lista os planos macro não arquivados companhia.
	 * 
	 * @param page
	 *            Número da página da listagem a ser acessada.
	 * @return PaginatedList<PlanMacro> Lista de planos macro arquivados da
	 *         companhia.
	 */
	@GetMapping(BASEPATH + "/planmacro/unarchivedplanmacro")
	public ResponseEntity<?> listMacrosUnarchived(@RequestParam(required = false) Integer page) {
		if (page == null)
			page = 0;
		try {
			if (this.domain != null) {
				PaginatedList<PlanMacro> plans = this.bs.listMacros(this.domain.get().getCompany(), false, page);
				return this.success(plans);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Exclui plano macro.
	 * 
	 * @param id
	 *            Id do plano macro a ser excluído.
	 * @return
	 */
	@DeleteMapping(BASEPATH + "/planmacro/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deleteMacro(@PathVariable Long id) {
		try {
			PlanMacro existent = this.bs.exists(id, PlanMacro.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			existent.setDeleted(true);
			this.bs.persist(existent);
			return this.success();
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Cria um novo plano de metas.
	 * 
	 * @param plan
	 *            Plano de metas a ser criado.
	 * @return Plan Retorna plano de metas criado.
	 */
	@PostMapping(BASEPATH + "/plan")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> savePlan(@RequestBody Plan plan) {
		
		if (Util.hasOnlySpecialCharactersString(plan.getName())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}
		
		try {
			plan.setId(null);
			
			Structure structure = this.sbs.retrieveById(plan.getStructure().getId());
			if (structure == null) {
				throw new IllegalArgumentException("Structure not found");
			}
			structure.setLevels(this.sbs.listStructureLevels(structure));
			plan.setStructure(structure);
			plan.setDescription(SanitizeUtil.sanitize(plan.getDescription()));  
			this.bs.persist(plan);
			PlanMacro macro = this.bs.retrievePlanMacroById(plan.getParent().getId());
			String url = domain.get().getBaseUrl() + "/#/plan/" + macro.getId() + "/details/subplan/" + plan.getId();
			// this.notificationBS.sendNotification(NotificationType.PLAN_CREATED,
			// plan.getName(), macro.getName(),null, url);
			return this.success(plan);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Edita plano de metas.
	 * 
	 * @param plan
	 *            Plano de metas a ser alterado com os novos valores.
	 * @return Plan Retorna o plano alterado.
	 */
	@PutMapping(BASEPATH + "/plan")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> update(@RequestBody Plan plan) {
		try {
			Plan existent = this.bs.exists(plan.getId(), Plan.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			existent.setDescription(SanitizeUtil.sanitize(plan.getDescription()));
			existent.setName(plan.getName());
			existent.setBegin(plan.getBegin());
			existent.setEnd(plan.getEnd());
			this.bs.persist(existent);
			existent.getStructure().setLevels(this.sbs.listStructureLevels(existent.getStructure()));
			existent.setUpdated(true);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualiza plano de metas.
	 * 
	 * @param id
	 *            Id do plano de metas a ser alterado.
	 * @param name
	 *            Novo nome do plano de metas.
	 * @param description
	 *            Nova descrição do plano de metas.
	 * @param begin
	 *            Nova data de início do plano de metas.
	 * @param end
	 *            Nova data de fim do plano de metas.
	 * @return Plan Retorna o plano de metas alterado.
	 */
	@PostMapping(BASEPATH + "/plan/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> updatePlan(@RequestBody UpdatePlanDto dto) {
		
		if (Util.hasOnlySpecialCharactersString(dto.name())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}
		
		try {
			Plan existent = this.bs.exists(dto.id(), Plan.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}

			if (dto.begin() != null && dto.end() != null) {
				DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				existent.setBegin((Date) formatter.parse(dto.begin()));
				existent.setEnd((Date) formatter.parse(dto.end()));
			} else {
				throw new IllegalArgumentException("As datas devem ser preenchidas!");
			}
			existent.setName(dto.name());
			existent.setDescription(SanitizeUtil.sanitize(dto.description()));
			this.bs.persist(existent);
			existent.setUpdated(true);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna plano de metas pelo id passado por parâmetro.
	 * 
	 * @param id
	 *            Id do plano de metas a ser retornado.
	 * @return Plan Retorna o plano de metas.
	 */
	@GetMapping(BASEPATH + "/plan/{id}")
	public ResponseEntity<?> retrievePlan(@PathVariable Long id) {
		try {
			Plan plan = this.bs.exists(id, Plan.class);
			if (plan == null) {
				return this.fail("O subplano solicitado não foi encontrado.");
			} else {
				if(this.sbs.listLevelsInstance(plan, null).getList().size() > 0){
					plan.setHaveSons(true);
				}else{
					plan.setHaveSons(false);
				}
				return this.success(plan);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	@GetMapping(BASEPATH + "/plan/performance")
	public ResponseEntity<?> retrievePlansPerformance(@RequestParam Long parentId) {
		try {
			PlanMacro macro = this.bs.exists(parentId, PlanMacro.class);
			List<Plan> list = this.bs.listPlansForPlanMacro(macro);
			List<Plan> result = new ArrayList<Plan>(list.size());
			
			List<PlanDetailed> planDetailedList = new ArrayList<PlanDetailed>();
			for (Plan p : list) {
				Structure s = p.getStructure();
				s.setLevels(this.sbs.listStructureLevels(s));
				p.setStructure(s);
				
				planDetailedList = this.bs.listPlansDetailed(p);
				p.setPlanDetailedList(new ArrayList<PlanDetailed>());
				for (int i=0; i<12; i++) {
					PlanDetailed planDetailed = null;
					for (PlanDetailed pDetailed : planDetailedList) {
						pDetailed.setPlan(null);
						if (pDetailed.getMonth() == i+1) {
							planDetailed = pDetailed;
						}
					}
					p.getPlanDetailedList().add(planDetailed);
				}
				
				result.add(p);
			}
			
			return this.success(result, (long) result.size());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna os planos de metas com seus níveis de um plano macro.
	 * 
	 * @param parentId
	 *            Id do plano macro pai.
	 * @param page
	 *            Número da página da lista de plano de metas.
	 * @return List<Plan> Retorna lista de plano de metas com seu níveis.
	 */
	@GetMapping(BASEPATH + "/plan")
    @CommunityDashboard
	public ResponseEntity<?> listPlans(
			@RequestParam Long parentId,
			@RequestParam(required = false) Integer page) {

		if (page == null)
			page = 0;
		try {
			PlanMacro macro = this.bs.exists(parentId, PlanMacro.class);
			PaginatedList<Plan> plans = this.bs.listPlans(macro, false, page, null, null, 0);
			List<Plan> list = plans.getList();
			List<Plan> result = new ArrayList<Plan>(list.size());
			for (Plan p : list) {
				Structure s = p.getStructure();
				s.setLevels(this.sbs.listStructureLevels(s));
				p.setStructure(s);
				if(this.sbs.listLevelsInstance(p, null).getList().size() > 0){
					p.setHaveSons(true);
				}else{
					p.setHaveSons(false);
				}
				result.add(p);
			}
			
			plans.setList(result);

			return this.success(plans);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar Planos e seus níveis segundo uma chave de busca.
	 * 
	 * @param parentId
	 *            Id do plano macro pai.
	 * @param page
	 *            Número da página da lista de plano de metas.
	 * @param terms
	 *            Termo de busca.
	 * @param subPlansSelect
	 *            Conjunto de planos a serem buscados.
	 * @param levelsSelect
	 *            Conjunto de níveis que podem ser buscados.
	 * @param dataInit
	 *            Filtro de data inicial.
	 * @param dataEnd
	 *            Filtro de data final.
	 * @param ordResult
	 *            Ordenação do resultado, 1 para crescente e 2 para decrescente.
	 * @return PaginatedList<Plan> Retorna lista de planos de metas de acordo
	 *         com os filtros.
	 */
	@GetMapping(BASEPATH + "/plan/findTerms")
	public ResponseEntity<?> listPlansTerms(
			@RequestParam Long parentId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) String terms,
			@RequestParam(required = false) Long subPlansSelect[],
			@RequestParam(required = false) Long levelsSelect[],
			@RequestParam int ordResult,
			@RequestParam(required = false) Long limit) {

    if (page == null) {
        page = 0;
    }

    try {
        PlanMacro macro = this.bs.exists(parentId, PlanMacro.class);
        if (macro == null) {
            return this.fail("PlanMacro not found");
        }

        List<Plan> allPlans = this.bs.listPlansForPlanMacro(macro);
		List<Plan> filteredPlans = allPlans.stream()
				.filter(plan -> {
					if (plan.getDescription() == null) {
						plan.setDescription("Descrição não informada");
					}
					return (terms == null || terms.isEmpty()) ||
							plan.getName().toLowerCase().contains(terms.toLowerCase()) ||
							plan.getDescription().toLowerCase().contains(terms.toLowerCase());
				}).collect(Collectors.toList());
        List<StructureLevelInstance> levelInstanceList = new ArrayList<>();
        for (Plan plan : filteredPlans) {
            StructureLevelInstance planLevelInstance = new StructureLevelInstance();
            planLevelInstance.setPlan(plan);
            levelInstanceList.add(planLevelInstance);
        }

        PaginatedList<User> users = this.ubs.listUsersBySearch(terms, this.domain.get().getCompany());
        List<StructureLevelInstance> listLevelInstancesByResponsible = this.sbs
                .listLevelInstancesByResponsible(users.getList());

        List<StructureLevelInstance> levelInstanceTermsList = this.sbs.listLevelsInstanceTerms(macro, terms,
                subPlansSelect, levelsSelect, ordResult);
        levelInstanceList.addAll(levelInstanceTermsList);

        List<AttributeInstance> attributeInstanceList = this.sbs.listAttributesTerms(macro, terms, subPlansSelect,
                levelsSelect, ordResult);

        for (AttributeInstance attributeInstance : attributeInstanceList) {
            boolean exist = false;
            for (StructureLevelInstance levelInstance : levelInstanceList) {
                if (attributeInstance.getLevelInstance().getId().equals(levelInstance.getId())) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                levelInstanceList.add(this.sbs.retrieveLevelInstance(attributeInstance.getLevelInstance().getId()));
            }
        }

        int firstResult = 0;
        int maxResult = 0;
        int count = 0;
        int add = 0;
        if (limit != null) {
            firstResult = (int) ((page - 1) * limit);
            maxResult = limit.intValue();
        }

        PaginatedList<StructureLevelInstance> levelInstances = new PaginatedList<StructureLevelInstance>();
        levelInstances.setList(new ArrayList<StructureLevelInstance>());
        for (StructureLevelInstance levelInstance : levelInstanceList) {
            if (limit != null) {
                if (count >= firstResult && add < maxResult) {
                    levelInstances.getList().add(levelInstance);
                    count++;
                    add++;
                } else {
                    count++;
                }
            } else {
                levelInstances.getList().add(levelInstance);
            }
        }

        for (StructureLevelInstance levelInstance : listLevelInstancesByResponsible) {
            if (limit != null) {
                if (count >= firstResult && add < maxResult) {
                    levelInstances.getList().add(levelInstance);
                    count++;
                    add++;
                } else {
                    count++;
                }
            } else {
                levelInstances.getList().add(levelInstance);
            }
        }

        levelInstances.setTotal((long) count);
        return this.success(levelInstances);
    } catch (Throwable ex) {
        LOGGER.error("Unexpected runtime error", ex);
        return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
    }
}

	/**
	 * Exclui plano de metas.
	 * 
	 * @param id
	 *            Id do plano de metas a ser excluído.
	 * @return
	 */
	@DeleteMapping(BASEPATH + "/plan/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deletePlan(@PathVariable Long id) {
		try {
			Plan existent = this.bs.exists(id, Plan.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			if(this.sbs.listLevelsInstance(existent, null).getList().size()>0){
				return this.fail("Impossível excluir plano de metas com níveis filhos");
			}else{			
				existent.setDeleted(true);
				List<StructureLevelInstance> planStructures = this.sbs.listLevelInstanceByPlan(existent);
				for(StructureLevelInstance structure : planStructures){
					structure.setDeleted(true);
					this.sbs.persist(structure);
				}
				this.bs.persist(existent);
				return this.success();
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Duplica plano macro.
	 * 
	 * @param macro
	 *            Plano macro a ser duplicado.
	 * @param keepPlanLevel
	 *            Flag para manter estrutura do plano.
	 * @param keepPlanContent
	 *            Flag para manter conteúdo do plano.
	 * @param keepDocSection
	 *            Flag para manter seções do documento.
	 * @param keepDocContent
	 *            Flag para manter conteúdo das seções do documento.
	 * @return PlanMacro Retorna o plano macro duplicado.
	 */
	@PostMapping(BASEPATH + "/planmacro/duplicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> duplicate(@RequestBody DuplicatePlanMacroDto dto) {
		try {
			PlanMacro macro = dto.macro();
			PlanMacro existent = this.bs.retrievePlanMacroById(macro.getId());
			if (existent == null) {
				return this.fail("Plano inexistente.");
			} else {
				
				LOGGER.info("Starting Plan duplicate: "+macro.getName());
				macro.setCompany(existent.getCompany());
				macro.setId(null);
				macro.setDocumented(existent.isDocumented());
				macro = this.bs.duplicatePlanMacro(macro);
				
				if (macro != null) {
					
					if (dto.keepPlanLevel()) {
						if (this.bs.duplicatePlanLevel(existent, macro, sbs, dto.keepPlanContent())) {
							LOGGER.info("Níveis do plano duplicados com sucesso.");
						} else {
							return this.fail("Não foi possível duplicar os níveis do plano.");
						}
					}
					if (dto.keepDocSection()) {
						if (this.dbs.duplicateDocument(existent, macro, dto.keepDocContent())) {
							LOGGER.info("Seções do documento duplicados com sucesso.");
						} else {
							return this.fail("Não foi possível duplicar as seções do documento.");
						}
					}
				} else {
					return this.fail("Não foi possível duplicar o plano.");
				}

				/*
				 * CompanyUser companyUser =
				 * this.ubs.retrieveCompanyUser(this.userSession.getUser(),
				 * this.domain.get().getCompany()); if
				 * (companyUser.getNotificationSetting() ==
				 * NotificationSetting.DEFAULT.getSetting() || companyUser
				 * .getNotificationSetting() ==
				 * NotificationSetting.DO_NOT_RECEIVE_EMAIL.getSetting()) {
				 * this.notificationBS.sendNotification(NotificationType.
				 * PLAN_MACRO_CREATED, macro.getName(), null, null, url); } else
				 * if (companyUser.getNotificationSetting() ==
				 * NotificationSetting.RECEIVE_ALL_BY_EMAIL .getSetting()) {
				 * this.notificationBS.sendNotification(NotificationType.
				 * PLAN_MACRO_CREATED, macro.getName(), null, null, url);
				 * this.notificationBS.sendNotificationEmail(NotificationType.
				 * PLAN_MACRO_CREATED, macro.getName(), null, null, url); }
				 */
				return this.success(macro);
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Arquiva plano macro.
	 * 
	 * @param id
	 *            Id do plano macro a ser arquivado.
	 * @return
	 * 
	 */
	@PostMapping(BASEPATH + "/planmacro/archive")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> archivePlan(@RequestBody IdDto dto) {
		try {
			PlanMacro plan = this.bs.exists(dto.id(), PlanMacro.class);
			if (GeneralUtils.isInvalid(plan)) {
				return this.notFound();
			}
			plan.setArchived(true);
			this.bs.persist(plan);
			return this.success(plan);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Desarquiva plano macro.
	 * 
	 * @param id
	 *            Id do plano macro a ser desarquivado.
	 * @return
	 * 
	 */
	@PostMapping(BASEPATH + "/planmacro/unarchive")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> unarchivePlan(@RequestBody IdDto dto) {
		try {
			PlanMacro plan = this.bs.exists(dto.id(), PlanMacro.class);
			if (GeneralUtils.isInvalid(plan)) {
				return this.notFound();
			}
			plan.setArchived(false);
			this.bs.persist(plan);
			return this.success(plan);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Agenda um recálculo das médias dos planos.
	 * 
	 * @param id
	 *            Id do plano macro.
	 * @return
	 * 
	 */
	@PostMapping(BASEPATH + "/planmacro/{id}/schedule-recalculation")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> scheduleRecalculation(@PathVariable Long id) {
		try {
			PlanMacro plan = this.bs.exists(id, PlanMacro.class);
			if (GeneralUtils.isInvalid(plan)) {
				return this.notFound();
			}
			this.bs.scheduleRecalculation(plan);
			return this.success();
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

}
