package org.forrisco.core.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.security.auth.UserSession;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.frisco.UnitReportGenerator;
import org.forpdi.system.reports.frisco.UnitReportGenerator.Params;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.UnitHistoryBS.UnitHistoryDTO;
import org.forrisco.core.unit.dto.DuplicateUnitDto;
import org.forrisco.core.unit.dto.ExportUnitReportDto;
import org.forrisco.core.unit.dto.UnitDto;
import org.forrisco.risk.RiskFilterParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.file.Archive;
import org.forpdi.core.storage.file.ArchiveBS;

import com.itextpdf.text.DocumentException;

/**
 * @author Matheus Nascimento
 */
@RestController
public class UnitController extends AbstractController {

	@Autowired
	private UnitBS unitBS;
	@Autowired
	private ProcessBS processBS;
	@Autowired
	private UnitReportGenerator unitReportGenerator;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private UnitReplicationBS unitReplicationBS;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private UnitHistoryBS unitHistoryBS; 
	@Autowired
	private StorageService storageService;
	@Autowired
	private ArchiveBS archiveBS;

	public static final String PATH = BASEPATH + "/unit";

	private final ConcurrentMap<UUID, JobInfo> reportJobs = new ConcurrentHashMap<>();

	private static final class JobInfo {
		enum Status {
			PENDING, RUNNING, SUCCEEDED, FAILED
		}

		enum Stage {
			QUEUED, GENERATING, WRITING, ARCHIVING, UPLOADING, READY
		}

		final UUID id;
		final String title;
		final UnitReportGenerator.Params params;

		volatile Status status;
		volatile Stage stage;
		volatile Integer progress;

		volatile Long archiveId;
		volatile String error;

		JobInfo(UUID id, String title, UnitReportGenerator.Params params) {
			this.id = id;
			this.title = title;
			this.params = params;
			this.status = Status.PENDING;
			this.stage = Stage.QUEUED;
			this.progress = 0;
		}
	}
	
/**
     * Retorna o histórico de alterações de uma unidade.
     *
     * @param id
     * Id da unidade.
     * @param params
     * Parâmetros de paginação, busca e ordenação.
     * @return PaginatedList<UnitHistoryDTO>
     * Retorna a lista paginada de histórico da unidade.
     */
    @GetMapping(PATH + "/{id}/history")
    public ResponseEntity<?> getUnitHistory(@PathVariable Long id, @ModelAttribute DefaultParams params, UserSession userSession) {


        try {
            Unit unit = this.unitBS.exists(id, Unit.class);
            if (unit == null || unit.isDeleted()) {
                return this.notFound();
            }

            boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
            if (!isSystemAdmin) {
                Long loggedInUserId = userSession.getAuthData().getId();
                Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
                if (userCompany == null || !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
                    LOGGER.warn(String.format(
                        "Tentativa de acesso indevido ao histórico da Unidade ID %d. Usuário: '%s' (ID: %d)",
                        id, userSession.getAuthData().getName(), loggedInUserId
                    ));
                    return this.fail("Acesso negado.");
                }
            }
            
            domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

            PaginatedList<UnitHistoryDTO> history = this.unitHistoryBS.listHistoryByUnit(id, params);
            return this.success(history);

        } catch (Throwable ex) {
            LOGGER.error("Erro inesperado ao buscar o histórico da unidade: " + id, ex);
            return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
        }
    }


	/**
	 * Salvar unidade
	 *
	 * @param Unit
	 *             unidade a ser salva
	 */
	@PostMapping(PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> save(@RequestBody UnitDto dto, UserSession userSession) {
		try {
			Unit unit = dto.unit();
			unit.setDescription(SanitizeUtil.sanitize(unit.getDescription()));

			PlanRisk planRisk = this.unitBS.exists(unit.getPlanRisk().getId(), PlanRisk.class);
			if (planRisk == null) {
				return this.fail("Unidade não possui Plano de Risco");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou criar Unidade no PlanRisk ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, planRisk.getId(),
							planRisk.getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());

			unit.setId(null);
			unit.setPlanRisk(planRisk);
			this.unitBS.save(unit);
			return this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Busca todas as unidades não deletadas
	 *
	 * @param page
	 *                 pagina atual
	 * @param pageSize
	 *                 quantidade de resultados por pagina
	 *                 return ResponseEntity<?>
	 */
	@GetMapping(PATH + "/units")
	public ResponseEntity<?> listAllUnits(
			@RequestParam(required = false) Long planRiskId,
			@ModelAttribute DefaultParams params) {
		try {
			if (this.domain != null) {
				PaginatedList<Unit> units = this.unitBS.listUnits(this.domain.get().getCompany(), planRiskId, params);
				return this.success(units);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Salvar subunidade
	 *
	 * @param Unit
	 *             subunidade a ser salva
	 */
	@PostMapping(PATH + "/subnew")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveSub(@RequestBody UnitDto dto, UserSession userSession) {
		try {
			Unit unit = dto.unit();

			if (unit.getPlanRisk() == null || unit.getParent() == null) {
				return this.fail("Requisição inválida. É necessário informar o Plano de Risco e a Unidade pai.");
			}

			PlanRisk planRisk = this.unitBS.exists(unit.getPlanRisk().getId(), PlanRisk.class);
			if (planRisk == null) {
				return this.fail("O Plano de Risco informado é inválido.");
			}

			Unit parentUnit = this.unitBS.exists(unit.getParent().getId(), Unit.class);
			if (parentUnit == null) {
				return this.fail("A unidade pai informada é inválida.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null) {
					LOGGER.error("Falha de segurança: Usuário com ID {} não está associado a nenhuma empresa.",
							loggedInUserId);
					return this.fail("Acesso negado.");
				}
				if (!planRisk.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar PlanRisk ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, planRisk.getId(),
							planRisk.getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
				if (!parentUnit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar Unidade Pai ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, parentUnit.getId(),
							parentUnit.getPlanRisk().getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(planRisk.getPolicy().getCompany());
			domain.validateTenant(parentUnit.getPlanRisk().getPolicy().getCompany());

			unit.setId(null);
			this.unitBS.save(unit);
			return this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Duplica unidades
	 *
	 * @Param List<Unit> lista de unidade com o id da unidade original
	 *        e o id no plano a ser salvo a unidade duplicado
	 * @return ResponseEntity<?>
	 */
	@PostMapping(PATH + "/duplicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> duplicateUnit(@RequestBody DuplicateUnitDto dto, UserSession userSession) {
		try {
			List<Unit> units = dto.units();
			PlanRisk planRisk = dto.planRisk();

			PlanRisk plan = this.unitBS.exists(planRisk.getId(), PlanRisk.class);
			if (plan == null || plan.isDeleted()) {
				return this.fail("Plano de Risco não encontrado");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			Company userCompany = null;
			Long loggedInUserId = null;
			if (!isSystemAdmin) {
				loggedInUserId = userSession.getAuthData().getId();
				userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					LOGGER.error("Falha de segurança: Usuário com ID {} não está associado a nenhuma empresa.",
							loggedInUserId);
					return this.fail("Acesso negado.");
				}

				if (!plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou duplicar para o PlanRisk ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			List<Unit> allunits = new ArrayList<>();
			Map<Long, Long> duplicatesUnitsId = new HashMap<Long, Long>();

			for (Unit unitItem : units) {
				Unit unit = this.unitBS.exists(unitItem.getId(), Unit.class);
				if (unit == null || unit.isDeleted()) {
					continue;
				}

				if (!isSystemAdmin) {
					if (!unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
						LOGGER.warn(String.format(
								"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou duplicar a Unidade ID %d da Empresa ID %d.",
								userSession.getAuthData().getName(), loggedInUserId, unit.getId(),
								unit.getPlanRisk().getPolicy().getCompany().getId()));
						continue;
					}
				}

				domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

				Unit newUnit = new Unit(unit);
				newUnit.setPlanRisk(plan);
				this.unitBS.save(newUnit);
				duplicatesUnitsId.put(unit.getId(), newUnit.getId());
				allunits.add(newUnit);

				PaginatedList<Unit> subunits = this.unitBS.listSubunitByUnit(unit);
				for (Unit subunit : subunits.getList()) {
					Unit newSubunit = new Unit(subunit);
					newSubunit.setParent(newUnit);
					newSubunit.setPlanRisk(plan);
					this.unitBS.save(newSubunit);
					duplicatesUnitsId.put(subunit.getId(), newSubunit.getId());
					allunits.add(newSubunit);
				}
			}

			for (Unit unit : allunits) {
				unitReplicationBS.replicateProcess(unit, duplicatesUnitsId);
				unitReplicationBS.replicateUnitRisk(duplicatesUnitsId, unit);
			}

			return this.success("successfully duplicated plan risk");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna unidades.
	 *
	 * @param PLanRisk
	 *                 Id do plano de risco.
	 * @return <PaginatedList> Unit
	 */
	@GetMapping(PATH + "")
	public ResponseEntity<?> listUnits(@RequestParam Long planId, UserSession userSession) {
		try {
			PlanRisk plan = this.unitBS.exists(planId, PlanRisk.class);
			if (plan == null) {
				return this.fail("O Plano de Risco não foi encontrado");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar unidades do PlanRisk ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			PaginatedList<Unit> units = this.unitBS.listOnlyUnitsbyPlanRisk(plan);
			return this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna unidade.
	 *
	 * @param id
	 *           Id da unidade a ser retornado.
	 * @return Unit Retorna a unidade de acordo com o id passado.
	 */
	@GetMapping(PATH + "/{id}")
	public ResponseEntity<?> getUnit(@PathVariable Long id, UserSession userSession) {
		try {
			Unit unit = this.unitBS.retrieveUnitById(id);
			if (unit == null) {
				return this.fail("A unidade solicitada não foi encontrada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar a Unidade ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, unit.getId(),
							unit.getPlanRisk().getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

			return this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna subunidades.
	 *
	 * @param unitId
	 *               Id da unidade parent.
	 * @return <PaginatedList> Subunidades filhas da unidade passada
	 */
	@GetMapping(PATH + "/listsub/{unitId}")
	public ResponseEntity<?> listSubunits(@PathVariable Long unitId, @ModelAttribute DefaultParams params,
			UserSession userSession) {
		try {
			Unit unit = this.unitBS.exists(unitId, Unit.class);
			if (unit == null) {
				return this.fail("A unidade não foi encontrada");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar subunidades da Unidade ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, unit.getId(),
							unit.getPlanRisk().getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

			PaginatedList<Unit> subunits = this.unitBS.listSubunitByUnit(unit, params);
			return this.success(subunits);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna todas subunidades do plano.
	 *
	 * @param planId
	 *               Id do plano de risco
	 * @return <PaginatedList> Subunidades
	 */
	@GetMapping(PATH + "/listsub")
	public ResponseEntity<?> listSubunitsByPlan(@RequestParam Long planId, UserSession userSession) {
		try {
			PlanRisk plan = this.unitBS.exists(planId, PlanRisk.class);
			if (plan == null || plan.isDeleted()) {
				return this.fail("O Plano de risco não foi encontrado");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar subunidades do PlanRisk ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan);
			List<Unit> list = new ArrayList<>();

			for (Unit unit : units.getList()) {
				PaginatedList<Unit> subunits = this.unitBS.listSubunitByUnit(unit);
				list.addAll(subunits.getList());
			}

			PaginatedList<Unit> result = new PaginatedList<Unit>();
			result.setList(list);
			result.setTotal((long) list.size());
			return this.success(result);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna todas unidades e subunidades do plano.
	 *
	 * @param planId
	 *               Id do plano de risco
	 * @return <PaginatedList>Unit
	 */
	@GetMapping(PATH + "/allByPlan")
	@CommunityDashboard
	public ResponseEntity<?> listAllUnitsByPlan(@RequestParam Long planId) {
		try {
			PlanRisk plan = this.unitBS.exists(planId, PlanRisk.class);

			if (plan == null || plan.isDeleted()) {
				return this.fail("O Plano de risco não foi encontrado");
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan);

			return this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna todas unidades e subunidades do plano.
	 *
	 * @param planId
	 *               Id do plano de risco
	 * @return <PaginatedList>Unit
	 */
	@GetMapping(PATH + "/allByPlanPaginated")
	public ResponseEntity<?> listAllUnitsByPlanPaginated(
			@RequestParam Long planRiskId,
			@ModelAttribute DefaultParams params, UserSession userSession) {
		try {
			PlanRisk plan = this.unitBS.exists(planRiskId, PlanRisk.class);
			if (plan == null || plan.isDeleted()) {
				return this.fail("O Plano de risco não foi encontrado");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !plan.getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar unidades do PlanRisk ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getPolicy().getCompany());

			PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan, params);
			return this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/list-to-select")
	public ResponseEntity<?> listToSelect(@RequestParam(required = false) Long planRiskId, UserSession userSession) {
		try {
			if (planRiskId != null) {
				PlanRisk planRisk = this.unitBS.exists(planRiskId, PlanRisk.class);
				if (planRisk == null) {
					return this.fail("O Plano de Risco não foi encontrado.");
				}

				domain.validateTenant(planRisk.getPolicy().getCompany());
			}
			List<UnitToSelect> units = this.unitBS.listToSelect(planRiskId);
			return this.success(new ListWrapper<UnitToSelect>(units));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Exclui unidade.
	 *
	 * @param id
	 *           Id da unidade a ser excluído.
	 *
	 */
	@DeleteMapping(PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deleteUnit(@PathVariable Long id, UserSession userSession) {
		try {
			Unit unit = this.unitBS.exists(id, Unit.class);
			if (GeneralUtils.isInvalid(unit)) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar a Unidade ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, unit.getId(),
							unit.getPlanRisk().getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(unit.getPlanRisk().getPolicy().getCompany());

			if (this.unitBS.deletableUnit(unit)) {
				PaginatedList<Process> processes = this.processBS.listProcessByUnit(unit);
				for (Process process : processes.getList()) {
					this.processBS.deleteProcess(process);
				}
				this.unitBS.delete(unit);
				return this.success(unit);
			} else {
				return this.fail("A unidade possui riscos ou subunidades, portanto não pode ser excluida.");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Atualiza unidade.
	 *
	 * @param unit
	 *             Unidade a ser atualizada.
	 *
	 */
	@PutMapping(PATH)
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> updateUnit(@RequestBody UnitDto dto, UserSession userSession) {
		try {
			Unit unitFromRequest = dto.unit();
			Unit existentUnit = this.unitBS.exists(unitFromRequest.getId(), Unit.class);
			if (existentUnit == null) {
				return this.notFound();
			}
			
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !existentUnit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar a Unidade ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, existentUnit.getId(),
							existentUnit.getPlanRisk().getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(existentUnit.getPlanRisk().getPolicy().getCompany());
			return this.success(unitBS.updateUnit(existentUnit, unitFromRequest));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}

	/**
	 * Cria arquivo pdf para exportar relatório
	 */
	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	@PostMapping(PATH + "/exportUnitReport")
	public ResponseEntity<?> exportReportPost(
			@RequestBody ExportUnitReportDto dto,
			@org.springframework.web.bind.annotation.RequestParam(name = "async", required = false, defaultValue = "false") boolean async,
			UserSession userSession) {

		try {
			PlanRisk planRisk = this.unitBS.exists(dto.getPlanRiskId(), PlanRisk.class);
			if (planRisk == null)
				return ResponseEntity.badRequest().body(Map.of("error", "Plano de risco não encontrado"));
			if (!hasAccess(planRisk, userSession))
				return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
						.body(Map.of("error", "Acesso negado."));

			final String safeTitle = (dto.getTitle() == null || dto.getTitle().isBlank()) ? "relatorio"
					: dto.getTitle();

			String unitsCsv = (dto.getUnits() == null)
					? null
					: dto.getUnits().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
			String subunitsCsv = (dto.getSubunits() == null || dto.getSubunits().isEmpty())
					? null
					: dto.getSubunits().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));

			UnitReportGenerator.Params params = new UnitReportGenerator.Params(
					dto.getPlanRiskId(), safeTitle, unitsCsv, subunitsCsv, dto.getSelectedYear(),
					dto.getRiskFilters());

			if (async) {
				UUID jobId = startReportJob(params, safeTitle, planRisk, userSession);

				Map<String, Object> body = new java.util.HashMap<>();
				body.put("jobId", jobId.toString());
				body.put("statusUrl", PATH + "/exportUnitReport/status/" + jobId);
				body.put("downloadUrl", PATH + "/exportUnitReport/download/" + jobId);

				return ResponseEntity.status(org.springframework.http.HttpStatus.ACCEPTED)
						.contentType(MediaType.APPLICATION_JSON)
						.body(body);
			}

			try (java.io.InputStream in = unitReportGenerator.generateReport(params)) {
				byte[] pdf = org.apache.commons.io.IOUtils.toByteArray(in);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_PDF);
				headers.set(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + sanitize(safeTitle) + ".pdf\"");
				return ResponseEntity.ok().headers(headers).body(pdf);
			}

		} catch (Exception ex) {
			LOGGER.error("Error while generating PDF", ex);
			return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("error", "Erro interno ao gerar PDF"));
		}
	}

	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	@org.springframework.web.bind.annotation.GetMapping(PATH + "/exportUnitReport/status/{jobId}")
	public ResponseEntity<?> reportStatus(
			@org.springframework.web.bind.annotation.PathVariable String jobId,
			UserSession userSession) {
		try {
			java.util.UUID id = java.util.UUID.fromString(jobId);
			JobInfo job = reportJobs.get(id);
			if (job == null)
				return ResponseEntity.badRequest().body(Map.of("error", "Job não encontrado"));

			String downloadUrl = "";
			String fileName = "";
			if (job.status == JobInfo.Status.SUCCEEDED && job.archiveId != null) {
				Archive arc = archiveBS.exists(job.archiveId, Archive.class);
				if (arc != null) {
					downloadUrl = "/" + org.forpdi.core.utils.Consts.FILES_ENDPOINT_BASE_URL + "/" + arc.getFileLink();
					fileName = arc.getName();
				}
			}

			String safeError = (job.error == null ? "" : job.error.replace("\"", "'"));
			int progress = (job.progress == null ? 0 : job.progress);
			String stage = (job.stage == null ? JobInfo.Stage.QUEUED.name() : job.stage.name());

			Map<String, Object> json = new java.util.HashMap<>();
			json.put("status", job.status.name());
			json.put("stage", stage);
			json.put("progress", progress);
			json.put("downloadUrl", downloadUrl);
			json.put("fileName", fileName);
			json.put("error", safeError);

			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(json);
		} catch (Exception e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("error", "Falha ao consultar status"));
		}
	}

	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	@org.springframework.web.bind.annotation.GetMapping(PATH + "/exportUnitReport/download/{jobId}")
	public ResponseEntity<Void> reportDownloadRedirect(
			@org.springframework.web.bind.annotation.PathVariable String jobId,
			UserSession userSession) {
		try {
			UUID id = UUID.fromString(jobId);
			JobInfo job = reportJobs.get(id);
			if (job == null || job.archiveId == null)
				return ResponseEntity.notFound().build();
			Archive arc = archiveBS.exists(job.archiveId, Archive.class);
			if (arc == null)
				return ResponseEntity.notFound().build();
			String url = "/" + org.forpdi.core.utils.Consts.FILES_ENDPOINT_BASE_URL + "/" + arc.getFileLink();
			return ResponseEntity.status(302).header("Location", url).build();
		} catch (Exception e) {
			return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private java.util.UUID startReportJob(UnitReportGenerator.Params params, String title,
			PlanRisk planRisk, UserSession userSession) {

		java.util.UUID id = java.util.UUID.randomUUID();
		JobInfo job = new JobInfo(id, title, params);
		reportJobs.put(id, job);

		CompanyDomain capturedDomain = domain.get();
		if (capturedDomain == null) {
			capturedDomain = new CompanyDomain();
			capturedDomain.setCompany(planRisk.getPolicy().getCompany());
		}
		final CompanyDomain domainForJob = capturedDomain;

		java.util.concurrent.CompletableFuture.runAsync(() -> {
			job.status = JobInfo.Status.RUNNING;
			job.stage = JobInfo.Stage.GENERATING;
			job.progress = 10;

			CompanyDomainContext.setCurrentDomain(domainForJob);
			java.nio.file.Path tmp = null;
			try (java.io.InputStream in = unitReportGenerator.generateReport(job.params)) {
				job.stage = JobInfo.Stage.WRITING;
				job.progress = 35;

				tmp = java.nio.file.Files.createTempFile("unit-report-", ".pdf");
				java.nio.file.Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

				long size = java.nio.file.Files.size(tmp);

				job.stage = JobInfo.Stage.ARCHIVING;
				job.progress = 55;

				Archive arc = new Archive();
				arc.setName(sanitize(title) + ".pdf");
				arc.setCompany(domainForJob.getCompany());
				arc = archiveBS.save(arc);

				job.stage = JobInfo.Stage.UPLOADING;
				job.progress = 75;

				try (java.io.InputStream uploadIn = java.nio.file.Files.newInputStream(tmp)) {
					storageService.uploadFile(uploadIn, "application/pdf", arc.getFileLink(), size);
				}

				job.archiveId = arc.getId();
				job.stage = JobInfo.Stage.READY;
				job.progress = 100;
				job.status = JobInfo.Status.SUCCEEDED;

			} catch (Throwable e) {
				LOGGER.error("Falha ao gerar relatório async", e);
				job.error = e.getMessage();
				job.status = JobInfo.Status.FAILED;
			} finally {
				try {
					if (tmp != null)
						java.nio.file.Files.deleteIfExists(tmp);
				} catch (Exception ignore) {
				}
				CompanyDomainContext.clear();
			}
		});

		return id;
	}

	/**
	 * Compat: endpoint legado por GET. Converte parâmetros e reusa o mesmo fluxo.
	 */
	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	@GetMapping(PATH + "/exportUnitReport")
	public ResponseEntity<byte[]> exportReportGet(
			@RequestParam Long planRiskId,
			@RequestParam String title,
			@RequestParam String units,
			@RequestParam(required = false) String subunits,
			@RequestParam(required = false) String riskFilters,
			@RequestParam Integer selectedYear,
			UserSession userSession) {

		try {
			PlanRisk planRisk = this.unitBS.exists(planRiskId, PlanRisk.class);
			if (planRisk == null) {
				return badRequestBytes("Plano de risco não encontrado");
			}

			if (!hasAccess(planRisk, userSession)) {
				return forbiddenBytes("Acesso negado.");
			}

			if (subunits != null && subunits.isEmpty()) {
				subunits = null;
			}
			RiskFilterParams riskFilterParams = RiskFilterParams.fromJson(riskFilters);

			UnitReportGenerator.Params params = new UnitReportGenerator.Params(
					planRiskId, title, units, subunits, selectedYear, riskFilterParams);

			try (InputStream in = unitReportGenerator.generateReport(params)) {
				byte[] pdf = IOUtils.toByteArray(in);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_PDF);
				headers.set(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + sanitize(title) + ".pdf\"");

				return ResponseEntity.ok()
						.headers(headers)
						.body(pdf);
			}
		} catch (Exception ex) {
			LOGGER.error("Error while generating PDF", ex);
			return serverErrorBytes("Erro interno ao gerar PDF");
		}
	}

	private boolean hasAccess(PlanRisk planRisk, UserSession userSession) {
		if (userSession == null || userSession.getAuthData() == null) {
			LOGGER.warn("Acesso sem sessão válida ao exportar relatório do PlanRisk ID {}.", planRisk.getId());
			return false;
		}
		boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
		if (isSystemAdmin)
			return true;

		Long loggedInUserId = userSession.getAuthData().getId();
		Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

		boolean ok = (userCompany != null)
				&& planRisk.getPolicy().getCompany().getId().equals(userCompany.getId());

		if (!ok) {
			LOGGER.warn(String.format(
					"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou exportar relatório do PlanRisk ID %d da Empresa ID %d.",
					userSession.getAuthData().getName(), loggedInUserId,
					planRisk.getId(), planRisk.getPolicy().getCompany().getId()));
		}
		return ok;
	}

	private ResponseEntity<byte[]> badRequestBytes(String msg) {
		return ResponseEntity.badRequest().body(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private ResponseEntity<byte[]> forbiddenBytes(String msg) {
		return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
				.body(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private ResponseEntity<byte[]> serverErrorBytes(String msg) {
		return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
				.body(msg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private String sanitize(String text) {
		return text.replaceAll("[\\\\/:*?\"<>|]", "_");
	}

}
