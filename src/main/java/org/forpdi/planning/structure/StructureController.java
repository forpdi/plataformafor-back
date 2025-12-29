package org.forpdi.planning.structure;

import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.forpdi.core.storage.file.Archive;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;

import org.forpdi.core.company.CompanyDomain;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.storage.StorageService;
import org.forpdi.core.storage.file.ArchiveBS;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.admin.GoalsInfoTable;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.AttributeTypeFactory;
import org.forpdi.planning.attribute.StaticAttributeLabels;
import org.forpdi.planning.attribute.types.AttachmentField;
import org.forpdi.planning.attribute.types.ManagerField;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.TextArea;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.plan.PDIFilterParams;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS.GoalsReportParams;
import org.forpdi.planning.structure.StructureBS.JobInfo;
import org.forpdi.planning.structure.dto.CreateLevelInstanceDto;
import org.forpdi.planning.structure.dto.DeleteGoalsDto;
import org.forpdi.planning.structure.dto.DuplicateLevelInstanceRequestDto;
import org.forpdi.planning.structure.dto.GoalsGenerateDto;
import org.forpdi.planning.structure.dto.GoalsReportRequestDto;
import org.forpdi.planning.structure.dto.LevelInstanceIdDto;
import org.forpdi.planning.structure.dto.SaveAttributeInstanceDto;
import org.forpdi.planning.structure.dto.StructureLevelInstanceDto;
import org.forpdi.planning.structure.dto.UpdateGoalDto;
import org.forpdi.planning.structure.goals.history.GoalJustificationHistory.JustificationAndReachedValue;
import org.forpdi.planning.structure.goals.history.GoalJustificationHistoryBS;
import org.forpdi.planning.structure.job.JobInfoEntity;
import org.forpdi.planning.structure.job.JobInfoRepository;
import org.forpdi.planning.structure.xml.StructureBeans;
import org.forpdi.planning.structure.xml.StructureImporter;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.fpdi.GoalsReportGenerator;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.multipart.MultipartFile;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.dashboard.DashboardBS;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.itextpdf.text.DocumentException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Renato R. R. de Oliveira
 */
@RestController
public class StructureController extends AbstractController {

	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private StructureBS bs;
	@Autowired
	private StructureHelper helper;
	@Autowired
	private AttributeHelper attrHelper;
	@Autowired
	private PlanBS planBS;
	@Autowired
	private UserBS userBS;
	@Autowired
	private UnitBS unitBS;
	@Autowired
	private BudgetBS budgetBS;
	@Autowired
	private FieldsBS fieldBs;
	@Autowired
	private GoalsFilterHelper goalsFilterHelper;
	@Autowired
	private DuplicatedStructureCheckHelper duplicatedStructureCheckHelper;
	@Autowired
	private GoalJustificationHistoryBS goalJustificationHistoryBS;
	@Autowired
	private StructureInstanceFilterBS structureInstanceFilterBS;
	@Autowired
	private DashboardBS dashboardBS;
	@Autowired
	private GoalsReportGenerator goalsReportGenerator;
	@Autowired
	private CompanyBS companyBS;
	@Autowired
	private StorageService storageService;
	@Autowired
	private ArchiveBS archiveBS;
	@Autowired
	private PlatformTransactionManager transactionManager;
	@Autowired
	private StructureBS structureBS;
	@Autowired
	private JobInfoRepository jobRepo;
	@Autowired
	@Qualifier("reportsExecutor")
	private Executor reportsExecutor;

	/**
	 * Listar os tipos de atributos existentes.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return ResponseEntity<?>
	 */


	@GetMapping(BASEPATH + "/structure/attributetypes")
	public ResponseEntity<?> listAttributeTypes() {
		try {
			this.response.setCharacterEncoding("UTF-8");
			this.response.setContentType("application/json");
			OutputStreamWriter writer = new OutputStreamWriter(this.response.getOutputStream(),
					Charset.forName("UTF-8"));
			writer.write(AttributeTypeFactory.getInstance().toJSON());
			writer.flush();
			writer.close();
			return this.nothing();
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Importar o xml da estrutura dos planos.
	 *
	 * @param file
	 *             Arquivo xml.
	 * @return structure Estrutura importada.
	 */
	@PostMapping(BASEPATH + "/structure/import")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> importStructure(@RequestBody MultipartFile file) {
		try {
			if (file == null) {
				return this.fail("Nenhum arquivo enviado.");
			} else {
				String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."),
						file.getOriginalFilename().length());
				if (!extension.equals(".xml")) {
					return this.fail("Formato de arquivo inválido. Por favor, importe um arquivo no formato XML");
				} else {
					File temp = File.createTempFile("fpdi", "readedStructure-import", TempFilesManager.getTempDir());
					try (FileOutputStream copyStream = new FileOutputStream(temp)) {
						GeneralUtils.streamingPipe(file.getInputStream(), copyStream);
					} catch (IOException e) {
						LOGGER.error("Unexpected runtime error", e);
						return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
					}

					StructureImporter importer = this.bs.getStructureImporter(temp);
					StructureBeans.Structure readedStructure = importer.getReadedStructure();
					Structure duplicatedStructure = duplicatedStructureCheckHelper
							.retrieveDuplicatedStructure(readedStructure);
					if (duplicatedStructure != null) {
						if (duplicatedStructure.getName().equals(readedStructure.name)) {
							return this.fail("Estrutura do Plano de Metas já cadastrada.");
						} else {
							return this.fail("A estrutura " + readedStructure.name + " é igual à "
									+ duplicatedStructure.getName() + " já existente no sistema!");
						}
					} else {
						if (!temp.delete()) {
							LOGGER.warn("Cannot delete a temp file: " + temp.getAbsolutePath());
						}
						Structure structure = this.bs.importStructure(importer);
						structure.setCompany(this.domain.get().getCompany());
						this.bs.persist(structure);
						return this.success(structure);
					}
				}
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Salvar a estrutura dos planos no banco de dados.
	 *
	 * @param structure
	 *                  Estrutura a se salva.
	 * @return structure Estrutura salva.
	 */
	@PostMapping(BASEPATH + "/structure")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> saveStructure(@RequestBody Structure structure) {
		try {
			structure.setId(null);
			structure.setCompany(this.domain.get().getCompany());
			this.bs.persist(structure);
			return this.success(structure);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualizar a estrutura no banco de dados.
	 *
	 * @param structure
	 *                  Estrutura a ser atualizada.
	 * @return structure Estrutura atualizada
	 */
	@PutMapping(BASEPATH + "/structure")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> updateStructure(@RequestBody Structure structure) {
		try {
			Structure existent = this.bs.exists(structure.getId(), Structure.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			if (!existent.getCompany().getId().equals(this.domain.get().getCompany().getId())) {
				return this.fail("Acesso negado: a estrutura não pertence a esta instituição.");
			}

			existent.setDescription(structure.getDescription());
			existent.setName(structure.getName());
			this.bs.persist(existent);

			return this.success("Estrutura atualizada com sucesso.");

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Buscar a estrutura pelo id.
	 *
	 * @param id
	 *           Id da estrutura a ser buscada.
	 * @return structure Estrutura encontrada.
	 */
	@GetMapping(BASEPATH + "/structure/{id}")
	public ResponseEntity<?> retrieveStructure(@PathVariable Long id, UserSession userSession) {
		try {
			Structure structure = this.bs.exists(id, Structure.class);
			if (structure == null) {
				return this.fail("A estrutura solicitada não foi encontrada.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !structure.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar Structure ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, structure.getId(),
							structure.getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(structure.getCompany());

			structure.setLevels(this.bs.listLevels(structure).getList());
			for (StructureLevel level : structure.getLevels()) {
				level.setAttributes(this.bs.listAttributes(level, false).getList());
			}
			return this.success(structure);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as estruturas por página.
	 *
	 * @param page
	 *             Número da página a ser listada.
	 * @return structures Lista de estruturas.
	 */
	@GetMapping(BASEPATH + "/structure")
	public ResponseEntity<?> listStructures() {
		try {
			PaginatedList<Structure> structures = this.bs.list();
			for (Structure structure : structures.getList()) {
				structure.setLevels(this.bs.listLevels(structure).getList());
				for (StructureLevel level : structure.getLevels()) {
					level.setAttributes(this.bs.listAttributes(level, false).getList());
				}
			}
			return this.success(structures);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Deletar a estrutura pelo id.
	 *
	 * @param id
	 *           Id da estrutura a ser deletada.
	 * @return ResponseEntity<?>
	 */
	@DeleteMapping(BASEPATH + "/structure/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_SYSTEM_ADMIN)
	public ResponseEntity<?> delete(@PathVariable Long id) {
		try {
			Structure existent = this.bs.exists(id, Structure.class);
			if (GeneralUtils.isInvalid(existent)) {
				return this.notFound();
			}
			List<Plan> relatedPlans = planBS.listPlansByStructureId(id);
			if (!relatedPlans.isEmpty()) {
				throw new IllegalStateException("Não é possível excluir uma estrutura com planos relacionados");
			}
			if (!existent.getCompany().equals(this.domain.get().getCompany())) {
				throw new IllegalAccessException("Permission denied");
			}

			existent.setDeleted(true);
			this.bs.persist(existent);
			return this.success();
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Listar as instâncias de um level.
	 *
	 * @param levelId
	 *                 Id do level.
	 * @param planId
	 *                 Id do plano de metas.
	 * @param parentId
	 *                 Id do level pai.
	 * @return list Lista de instâncias de um level.
	 */
	@GetMapping(BASEPATH + "/structure/levelInstancelist")
	public ResponseEntity<?> listLevelInstance(
			@RequestParam Long sequence,
			@RequestParam Long planId,
			@RequestParam(required = false) Long parentId,
			UserSession userSession) {

		try {
			Plan plan = this.planBS.retrieveById(planId);
			if (plan == null) {
				return this.fail("Plano não encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !plan.getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar Plan ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getParent().getCompany());

			StructureLevel level = this.bs.retrieveNextLevel(plan.getStructure(), sequence.intValue());
			if (level == null) {
				return this.fail("Sequência de nível inválida para esta estrutura de plano.");
			}

			if (parentId != null && parentId == 0)
				parentId = null;

			boolean haveBudget = false;
			PaginatedList<StructureLevelInstance> dto = new PaginatedList<>();
			List<StructureLevelInstance> list = this.bs.listLevelsInstance(level, plan, parentId);
			dto.setList(list);
			dto.setTotal((long) list.size());

			List<StructureLevelInstance> structureList = dto.getList();
			for (int count = 0; count < structureList.size(); count++) {
				List<Budget> budgetList = this.budgetBS.listBudgetByLevelInstance(structureList.get(count));
				if (!budgetList.isEmpty())
					haveBudget = true;
				else
					haveBudget = false;
				structureList.get(count).setHaveBudget(haveBudget);
			}

			dto.setList(structureList);
			if (dto.getTotal() > 0) {
				StructureLevelInstance firstLevelInstance = structureList.get(0);
				List<StructureLevelInstance> parents = bs.getParents(firstLevelInstance);
				return ResponseEntity.ok(new StructureLevelInstanceListDto(list, (long) list.size(), parents));
			} else
				return this.success(level);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/** Listar instâncias de níveis para exibição de performance. */
	@GetMapping(BASEPATH + "/structure/levelinstance/performance")
	public ResponseEntity<?> listLevelInstancePerformance(
			@RequestParam Long planId,
			@RequestParam(required = false) Long parentId,
			UserSession userSession) {

		try {
			Plan plan = this.planBS.retrieveById(planId);
			if (plan == null) {
				return this.fail("Plano não encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !plan.getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar Plan ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getParent().getCompany());

			if (parentId != null && parentId == 0)
				parentId = null;

			PaginatedList<StructureLevelInstance> list = this.bs.listLevelsInstance(plan, parentId);

			List<StructureLevelInstanceDetailed> levelInstanceDetailedList;
			for (StructureLevelInstance levelInstance : list.getList()) {
				levelInstanceDetailedList = this.bs.listLevelInstanceDetailed(levelInstance);
				levelInstance.setLevelInstanceDetailedList(new ArrayList<>());
				for (int i = 0; i < 12; i++) {
					StructureLevelInstanceDetailed levelInstDetailed = null;
					for (StructureLevelInstanceDetailed levelInstanceDetailed : levelInstanceDetailedList) {
						levelInstanceDetailed.setLevelInstance(null);
						if (levelInstanceDetailed.getMonth() == i + 1) {
							levelInstDetailed = levelInstanceDetailed;
						}
					}
					levelInstance.getLevelInstanceDetailedList().add(levelInstDetailed);
				}
			}
			return this.success(list);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Criar uma instância de um level.
	 *
	 * @param planId
	 *                     Id do plano.
	 * @param levelId
	 *                     Id do level.
	 * @param instanceName
	 *                     Nome do levelInstance.
	 * @param parentId
	 *                     Id do level pai.
	 * @return levelInstance Retorna a instância criada.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> createLevelInstance(@RequestBody CreateLevelInstanceDto dto, UserSession userSession) {

		if (Util.hasOnlySpecialCharactersString(dto.instanceName())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}

		try {
			Long parentId = dto.parentId();

			Plan plan = this.planBS.retrieveById(dto.planId());
			if (plan == null) {
				return this.fail("Plano incorreto!");
			}

			StructureLevel level = this.bs.retrieveStructureLevelById(dto.levelId());
			if (level == null) {
				return this.fail("Estrutura incorreta!");
			}

			StructureLevelInstance parent = null;
			if (parentId != null) {
				parent = this.bs.retrieveLevelInstance(parentId);
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !plan.getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou criar instância no Plan ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, plan.getId(),
							plan.getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}

				if (parent != null && !parent.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou usar como pai a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, parent.getId(),
							parent.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(plan.getParent().getCompany());
			if (parent != null) {
				domain.validateTenant(parent.getPlan().getParent().getCompany());
				if (parent.isAggregate()) {
					return this.fail("Indicadores agregados não podem ter metas");
				}
			}

			StructureLevelInstance levelInstance = new StructureLevelInstance();
			levelInstance.setName(dto.instanceName());
			levelInstance.setDeleted(false);
			levelInstance.setPlan(plan);
			levelInstance.setLevel(level);
			levelInstance.setCreation(new Date());
			levelInstance.setParent(parentId);
			levelInstance.setVisualized(false);

			StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo = bs
					.getStructureInstancePermissionInfo(levelInstance);
			if (!permissionInfo.hasPermissionToAdd()) {
				return this.fail("Você não possui permissão de acesso");
			}

			this.bs.persist(levelInstance);
			this.bs.updateLevelValues(levelInstance);
			AttributeInstance attrInst = this.attrHelper
					.retrievePolarityAttributeInstance(levelInstance.getParent());
			if (attrInst != null)
				levelInstance.setPolarity(attrInst.getValue());
			return this.success(levelInstance);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Buscar uma instância de um level pelo id
	 *
	 * @param levelInstanceId
	 *                        Id da instância.
	 * @return levelInstance Instância encontrada.
	 */
	@GetMapping(BASEPATH + "/structure/levelinstance")
	public ResponseEntity<?> retrieveLevelInstance(@RequestParam Long levelInstanceId, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(levelInstanceId);
			if (levelInstance == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			return this.success(levelInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar os atributos de um level.
	 *
	 * @param id
	 *           Id da instância de um level.
	 * @return levelInstance Instância de um level com a lista de atributos.
	 */
	@GetMapping(BASEPATH + "/structure/levelattributes")
	public ResponseEntity<?> retrieveLevelAttributes(@RequestParam Long id, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstanceNoDeleted(id);
			if (levelInstance == null) {
				return this.fail("Estrutura incorreta!");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar atributos da Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			List<Attribute> attributeList = this.bs.retrieveLevelAttributes(levelInstance.getLevel());
			attributeList = this.bs.setAttributesInstances(levelInstance, attributeList);

			StructureLevel structureLevel = levelInstance.getLevel();
			if (structureLevel.isGoal()) {
				boolean hasManagerField = false;
				boolean hasAttachmentField = false;
				for (Attribute attribute : attributeList) {
					if (attribute.getType().equals(ManagerField.class.getCanonicalName())) {
						hasManagerField = true;
					}
					if (attribute.getType().equals(AttachmentField.class.getCanonicalName())) {
						hasAttachmentField = true;
					}
					if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
						attribute.setLabel(StaticAttributeLabels.GOAL_RESPONSIBLE_LABEL);
					}
				}

				if (!hasManagerField) {
					Attribute managerAttribute = new Attribute();
					managerAttribute.setDescription("Informe um gestor");
					managerAttribute.setLabel(StaticAttributeLabels.GOAL_MANAGER_LABEL);
					managerAttribute.setType(ManagerField.class.getCanonicalName());
					managerAttribute.setRequired(true);
					managerAttribute.setLevel(structureLevel);
					PaginatedList<User> users = this.userBS.listUsersByCompany();
					managerAttribute.setUsers(users.getList());
					this.bs.persist(managerAttribute);
					attributeList.add(0, managerAttribute);
				}

				if (!hasAttachmentField) {
					Attribute attachmentAttribute = new Attribute();
					attachmentAttribute.setDescription("Anexe arquivos referente a essa meta");
					attachmentAttribute.setLabel(StaticAttributeLabels.GOAL_ATTACHMENT_LABEL);
					attachmentAttribute.setType(AttachmentField.class.getCanonicalName());
					attachmentAttribute.setVisibleInTables(false);
					attachmentAttribute.setLevel(structureLevel);
					this.bs.persist(attachmentAttribute);
					attributeList.add(attachmentAttribute);
				}
			}

			if (structureLevel.isIndicator()) {
				boolean hasJustificationField = false;
				for (Attribute attribute : attributeList) {
					if (attribute.isJustificationField()) {
						hasJustificationField = true;
						break;
					}
				}

				if (!hasJustificationField) {
					Attribute justificationAttribute = new Attribute();
					justificationAttribute.setDescription("Informe uma justificativa para o formato");
					justificationAttribute.setLabel(StaticAttributeLabels.INDICATOR_JUSTIFICATION_LABEL);
					justificationAttribute.setType(TextArea.class.getCanonicalName());
					justificationAttribute.setJustificationField(true);
					justificationAttribute.setRequired(false);
					justificationAttribute.setLevel(structureLevel);
					this.bs.persist(justificationAttribute);
					attributeList.add(5, justificationAttribute);
				}
			}

			levelInstance.getLevel().setAttributes(attributeList);

			AttributeInstance attributeInstance;
			if (levelInstance.getLevel().isIndicator())
				attributeInstance = this.attrHelper.retrievePolarityAttributeInstance(levelInstance);
			else
				attributeInstance = this.attrHelper.retrievePolarityAttributeInstance(levelInstance.getParent());
			if (attributeInstance != null)
				levelInstance.setPolarity(attributeInstance.getValue());

			levelInstance.setParents(this.bs.getParents(levelInstance));

			PaginatedList<FavoriteLevelInstance> favorites = this.bs
					.listFavoriteLevelInstances(levelInstance.getPlan().getParent());
			boolean favoriteExistent = false;
			for (FavoriteLevelInstance favorite : favorites.getList()) {
				if (favorite.getLevelInstance().getId().equals(levelInstance.getId())) {
					favoriteExistent = true;
					break;
				}
			}
			levelInstance.setFavoriteExistent(favoriteExistent);
			levelInstance.setFavoriteTotal(favorites.getList().size());
			levelInstance.setIndicatorList(this.bs.listAggregateIndicatorsByAggregate(levelInstance));

			return this.success(levelInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar as instâncias dos atributos de um level.
	 *
	 * @param levelInstance
	 *                      Instância de um level com a lista de atributos e seus
	 *                      valores.
	 * @return existentLevelInstance Instância com a lista de atributos e seus
	 *         valores salvos.
	 */
	@PostMapping(BASEPATH + "/structure/levelattributes")
	public ResponseEntity<?> saveLevelAttributesInstance(@RequestBody SaveAttributeInstanceDto dto,
			UserSession userSession) {

		StructureLevelInstance levelInstance = dto.levelInstance();
		String url = dto.url();
		if (Util.hasOnlySpecialCharactersString(levelInstance.getName())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}

		try {
			boolean responsibleChanged = false;
			boolean managerChanged = false;
			Long userId = null;
			Long managerId = null;

			String[] mainUrl = url.split("\\?");
			if (mainUrl.length > 0) {
				url = mainUrl[0];
			}

			StructureLevelInstance existentLevelInstance = this.bs.retrieveLevelInstance(levelInstance.getId());
			if (existentLevelInstance == null) {
				return this.fail("Estrutura incorreta!");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !existentLevelInstance.getPlan().getParent().getCompany().getId()
						.equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou salvar atributos na Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, existentLevelInstance.getId(),
							existentLevelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(existentLevelInstance.getPlan().getParent().getCompany());

			if (existentLevelInstance.isClosed()) {
				return this.fail("Esta meta já foi concluída!");
			}

			boolean isGoal = existentLevelInstance.getLevel().isGoal();

			StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo = bs
					.getStructureInstancePermissionInfo(existentLevelInstance);

			if (!permissionInfo.hasPermissionToEdit() && !permissionInfo.isColaboratorResponsibleForGoal()) {
				return this.fail("Você não possui permissão de acesso");
			}

			if (!existentLevelInstance.getName().equals(levelInstance.getName())) {
				existentLevelInstance.setName(levelInstance.getName());
				this.bs.persist(existentLevelInstance);
			}
			List<Attribute> attributes = new ArrayList<>();

			JustificationAndReachedValue oldJustificationAndReachedValue = new JustificationAndReachedValue();
			JustificationAndReachedValue newJustificationAndReachedValue = new JustificationAndReachedValue();

			for (int i = 0; i < levelInstance.getLevel().getAttributes().size(); i++) {
				Attribute attribute = this.bs
						.retrieveAttribute(levelInstance.getLevel().getAttributes().get(i).getId());
				if (attribute == null) {
					return this.fail("Atributo incorreto!");
				} else {
					AttributeInstance attInst = this.bs.valueStringToValueType(attribute,
							levelInstance.getLevel().getAttributes().get(i).getAttributeInstance().getValue());
					AttributeInstance attributeInstance = this.attrHelper
							.retrieveAttributeInstance(existentLevelInstance, attribute);
					if (attributeInstance == null)
						attributeInstance = new AttributeInstance();
					if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
						userId = Long.valueOf(attInst.getValue());
						if (attributeInstance.getValue() == null
								|| !attributeInstance.getValue().equals(attInst.getValue())) {
							responsibleChanged = true;
						}
					}
					if (attribute.getType().equals(ManagerField.class.getCanonicalName())) {
						managerId = Long.valueOf(attInst.getValue());
						if (attributeInstance.getValue() == null
								|| !attributeInstance.getValue().equals(attInst.getValue())) {
							managerChanged = true;
						}
					}

					String oldValue = attributeInstance.getValue();
					String newValue = !attribute.getType().equals(TextArea.class.getCanonicalName())
							? SanitizeUtil.sanitize(attInst.getValue())
							: attInst.getValue();

					if (existentLevelInstance.getLevel().isGoal()) {
						boolean isJustificationField = attribute.isJustificationField();
						boolean isReachedField = attribute.isReachedField();
						if (isJustificationField) {
							oldJustificationAndReachedValue.setJustification(oldValue);
							newJustificationAndReachedValue.setJustification(newValue);
						} else if (isReachedField) {
							oldJustificationAndReachedValue.setReachedValue(attributeInstance.getValueAsNumber());
							newJustificationAndReachedValue.setReachedValue(attInst.getValueAsNumber());
						}
					}

					attribute.setUsers(this.userBS.listUsersByCompany().getList());
					attributeInstance.setDeleted(false);
					attributeInstance.setCreation(new Date());
					attributeInstance.setLevelInstance(existentLevelInstance);
					attributeInstance.setAttribute(attribute);
					attributeInstance.setValue(newValue);
					attributeInstance.setValueAsNumber(attInst.getValueAsNumber());
					attributeInstance.setValueAsDate(attInst.getValueAsDate());

					this.bs.persist(attributeInstance);

					attribute.setAttributeInstance(attributeInstance);
					attributes.add(attribute);
				}
			}

			if (!oldJustificationAndReachedValue.isEmpty()
					&& !oldJustificationAndReachedValue.equals(newJustificationAndReachedValue)) {
				goalJustificationHistoryBS.saveHistory(oldJustificationAndReachedValue,
						newJustificationAndReachedValue, existentLevelInstance);
			}

			if (userId == null) {
				return this.fail("Responsável técnico solicitado não foi encontrado");
			}

			if (isGoal && managerId == null) {
				return this.fail("Gestor solicitado não foi encontrado");
			}

			existentLevelInstance.setAggregate(levelInstance.isAggregate());
			existentLevelInstance.setCalculation(levelInstance.getCalculation());
			if (existentLevelInstance.isAggregate()) {
				StructureLevelInstance indicator = this.bs.isAggregating(existentLevelInstance);
				if (indicator != null) {
					return this.fail("Esse indicador já está agregado ao indicador " + indicator.getName() + ".");
				}
			}
			this.bs.persist(existentLevelInstance);
			attributes = this.bs.setAttributesInstances(existentLevelInstance, attributes);
			existentLevelInstance.getLevel().setAttributes(attributes);

			if (existentLevelInstance.isAggregate()) {
				this.bs.saveIndicators(levelInstance.getIndicatorList());
			}
			existentLevelInstance = this.helper.retrieveLevelInstance(existentLevelInstance.getId());
			this.bs.updateLevelValues(existentLevelInstance);
			existentLevelInstance.setParents(this.bs.getParents(existentLevelInstance));
			StructureLevel nextLevel = this.bs.retrieveNextLevel(existentLevelInstance.getLevel().getStructure(),
					existentLevelInstance.getLevel().getSequence() + 1);
			existentLevelInstance.setLevelSon(nextLevel);

			User responsible = this.userBS.existsByUser(userId);
			User manager = this.userBS.existsByUser(managerId);
			if (responsibleChanged) {
				this.helper.sendGoalNotification(
						responsible,
						this.domain.get(),
						existentLevelInstance,
						null,
						url,
						NotificationType.ATTRIBUTED_RESPONSIBLE);
			}
			if (isGoal) {
				if (managerChanged) {
					this.helper.sendGoalNotification(
							manager,
							this.domain.get(),
							existentLevelInstance,
							null,
							url,
							NotificationType.ATTRIBUTED_RESPONSIBLE);
				} else {
					StructureLevelInstance parent = this.helper
							.retrieveLevelInstance(existentLevelInstance.getParent());
					this.helper.sendGoalNotification(
							manager,
							this.domain.get(),
							existentLevelInstance,
							parent,
							url,
							NotificationType.GOAL_ATTRIBUTE_UPDATED);
				}
			}

			AttributeInstance attrInst;
			if (existentLevelInstance.getLevel().isIndicator())
				attrInst = this.attrHelper.retrievePolarityAttributeInstance(existentLevelInstance);
			else
				attrInst = this.attrHelper.retrievePolarityAttributeInstance(existentLevelInstance.getParent());
			if (attrInst != null)
				existentLevelInstance.setPolarity(attrInst.getValue());

			List<Attribute> attributeList = this.bs.retrieveLevelAttributes(existentLevelInstance.getLevel());
			attributeList = this.bs.setAttributesInstances(existentLevelInstance, attributeList);
			existentLevelInstance.getLevel().setAttributes(attributeList);
			return this.success(existentLevelInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Deletar uma instância de um level.
	 *
	 * @param levelInstance
	 *                      Instância a ser deletada.
	 * @return existentLevelInstance Instância que foi deletada.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance/delete")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteLevelAttributesInstance(@RequestBody StructureLevelInstanceDto dto,
			UserSession userSession) {
		try {
			StructureLevelInstance existentLevelInstance = this.helper
					.retrieveLevelInstance(dto.levelInstance().getId());

			if (existentLevelInstance == null) {
				return this.fail("Estrutura incorreta!");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !existentLevelInstance.getPlan().getParent().getCompany().getId()
						.equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, existentLevelInstance.getId(),
							existentLevelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(existentLevelInstance.getPlan().getParent().getCompany());

			StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo = bs
					.getStructureInstancePermissionInfo(existentLevelInstance);

			if (!permissionInfo.hasPermissionToAdd()) {
				return this.fail("Você não possui permissão de acesso");
			}

			StructureLevelInstance indicator = this.bs.isAggregating(existentLevelInstance);
			if (indicator != null) {
				return this.fail("Não pode ser excluído, está agregado ao indicador " + indicator.getName() + ".");
			}
			if (this.bs.checkHaveBudgetByLevel(existentLevelInstance.getLevel())) {
				List<Budget> budgetList = this.budgetBS.listBudgetByLevelInstance(existentLevelInstance);
				for (Budget budget : budgetList) {
					BudgetElement budgetElement = this.budgetBS
							.budgetElementExistsById(budget.getBudgetElement().getId());
					if (budgetElement != null) {
						double balanceAvailable = budgetElement.getBalanceAvailable();
						balanceAvailable += budget.getCommitted();
						budgetElement.setBalanceAvailable(balanceAvailable);
						budgetElement.setLinkedObjects(budgetElement.getLinkedObjects() - 1);
						this.budgetBS.update(budgetElement);
						this.fieldBs.deleteBudget(budget);
					}
				}
			}
			existentLevelInstance.setDeleted(true);
			this.bs.persist(existentLevelInstance);
			this.bs.updateLevelValues(existentLevelInstance);

			PaginatedList<FavoriteLevelInstance> favorites = this.bs
					.listFavoriteByLevelInstance(existentLevelInstance);
			for (FavoriteLevelInstance favorite : favorites.getList()) {
				favorite.setDeleted(true);
				this.bs.persist(favorite);
			}

			return this.success("Instância de nível excluída com sucesso.");
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar instâncias dos leveis filhos do level especificado.
	 *
	 * @param parentId
	 *                 Id do level pai.
	 * @return levelInstance Instância do level pai com uma lista de instâncias
	 *         dos leveis filhos, com a estrutura de atributos corrigida.
	 */
	@GetMapping(BASEPATH + "/structure/levelinstance/levelsons")
	public ResponseEntity<?> levelInstanceSons(
			@RequestParam Long parentId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			UserSession userSession) {

		try {
			StructureLevelInstance parentInstance = this.bs.retrieveLevelInstance(parentId);
			if (parentInstance == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !parentInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar filhos da Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, parentInstance.getId(),
							parentInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(parentInstance.getPlan().getParent().getCompany());

			PaginatedList<StructureLevelInstance> levelInstances = this.bs.retrieveLevelInstanceSons(parentId, page,
					pageSize);

			if (levelInstances.getList().isEmpty()) {
				StructureLevelInstance parentLevelInstance = this.bs.retrieveLevelInstance(parentId);
				parentLevelInstance.setSons(levelInstances);
				Long structureId = this.bs.retrieveStructureIdByLevel(parentLevelInstance.getLevel());
				Structure structure = new Structure();
				structure.setId(structureId);
				StructureLevel nextLevel = this.bs.retrieveNextLevel(structure,
						parentLevelInstance.getLevel().getSequence() + 1);
				parentLevelInstance.setLevelSon(nextLevel);
				return this.success(parentLevelInstance);
			}
			List<Attribute> masterAttributeList = this.bs
					.retrieveLevelAttributes(levelInstances.getList().get(0).getLevel());

			for (Attribute attr : masterAttributeList) {
				attr.setAttributeInstances(new ArrayList<>());
			}

			for (StructureLevelInstance goal : levelInstances.getList()) {
				for (Attribute masterAttr : masterAttributeList) {
					AttributeInstance attrInst = this.attrHelper.retrieveAttributeInstance(goal, masterAttr);
					if (attrInst != null) {
						masterAttr.getAttributeInstances().add(attrInst);
					} else {
						masterAttr.getAttributeInstances().add(new AttributeInstance());
					}
				}
			}

			for (StructureLevelInstance goal : levelInstances.getList()) {
				goal.getLevel().setAttributes(masterAttributeList);
			}

			boolean isGoalLevel = levelInstances.getList().get(0).getLevel().isGoal();
			if (isGoalLevel) {
				AttributeInstance attributeInstance = this.attrHelper.retrievePolarityAttributeInstance(parentId);
				String polarity = (attributeInstance != null) ? attributeInstance.getValue() : null;
				this.bs.setGoalStatus(levelInstances.getList(), polarity);
			}

			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(parentId);
			levelInstance.setSons(levelInstances);

			parentInstance.setSons(levelInstances);

			Long structureId = this.bs.retrieveStructureIdByLevel(parentInstance.getLevel());
			Structure structure = new Structure();
			structure.setId(structureId);
			StructureLevel nextLevel = this.bs.retrieveNextLevel(structure,
					parentInstance.getLevel().getSequence() + 1);
			parentInstance.setLevelSon(nextLevel);

			return this.success(parentInstance);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar metas a partir de filtros de pesquisa.
	 *
	 * @param term
	 *             Id da instância de um level pai.
	 * @return list Lista de instâncias dos leveis filhos.
	 */
	@GetMapping(BASEPATH + "/structure/filtergoals")
	public ResponseEntity<?> filterGoals(
			@RequestParam(required = false) Long parentId,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String attributesToFilter,
			@RequestParam(required = false) Integer progressStatusId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			UserSession userSession) {

		try {
			if (parentId != null) {
				StructureLevelInstance parentInstance = this.bs.retrieveLevelInstance(parentId);
				if (parentInstance == null) {
					return this.fail("O nível pai especificado no filtro não foi encontrado.");
				}

				// BOLA/IDOR Security Check
				boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
				if (!isSystemAdmin) {
					Long loggedInUserId = userSession.getAuthData().getId();
					Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
					if (userCompany == null
							|| !parentInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
						LOGGER.warn(String.format(
								"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou filtrar usando como pai a Instância ID %d da Empresa ID %d.",
								userSession.getAuthData().getName(), loggedInUserId, parentInstance.getId(),
								parentInstance.getPlan().getParent().getCompany().getId()));
						return this.fail("Acesso negado.");
					}
				}

				domain.validateTenant(parentInstance.getPlan().getParent().getCompany());
			}

			PaginatedList<StructureLevelInstance> goals = this.goalsFilterHelper.retrieveFilteredGoals(
					parentId, name, progressStatusId, attributesToFilter, page, pageSize);

			return this.success(goals);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Fechar ou reabrir a instância de um level meta.
	 *
	 * @param id
	 *                      Id da instância de um level meta.
	 * @param openCloseGoal
	 *                      Valor para fechar ou reabrir a meta.
	 * @return levelInstance Instância fechada ou reaberta.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance/closegoal")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> closeLevelGoal(@RequestBody UpdateGoalDto dto, UserSession userSession) {
		try {
			Long id = dto.id();
			Boolean openCloseGoal = dto.openCloseGoal();
			String url = dto.url();

			Long managerId = null;
			Long responsibleId = null;
			Long goalManagerId = null;

			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(id);
			if (levelInstance == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou fechar/abrir a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			levelInstance.setClosed(dto.openCloseGoal());
			levelInstance.setClosedDate(new Date());
			this.bs.persist(levelInstance);

			StructureLevelInstance levelInstanceParent = this.bs.retrieveLevelInstance(levelInstance.getParent());
			PaginatedList<Attribute> attributes = this.bs.listAttributes(levelInstanceParent.getLevel(), false);
			PaginatedList<Attribute> attributesGoal = this.bs.listAttributes(levelInstance.getLevel(), false);

			for (Attribute attr : attributes.getList()) {
				if (attr.getType().equals(ResponsibleField.class.getCanonicalName())
						|| attr.getType().equals(ManagerField.class.getCanonicalName())) {
					AttributeInstance attrInstance = this.attrHelper.retrieveAttributeInstance(levelInstanceParent,
							attr);
					if (attrInstance != null)
						managerId = Long.parseLong(attrInstance.getValue());
				}
			}
			for (Attribute attrGoal : attributesGoal.getList()) {
				if (attrGoal.getType().equals(ResponsibleField.class.getCanonicalName())) {
					AttributeInstance attrInstanceGoalResp = this.attrHelper.retrieveAttributeInstance(levelInstance,
							attrGoal);
					if (attrInstanceGoalResp != null)
						responsibleId = Long.parseLong(attrInstanceGoalResp.getValue());
				}
				if (attrGoal.getType().equals(ManagerField.class.getCanonicalName())) {
					AttributeInstance attrInstanceGoalMan = this.attrHelper.retrieveAttributeInstance(levelInstance,
							attrGoal);
					if (attrInstanceGoalMan != null)
						goalManagerId = Long.parseLong(attrInstanceGoalMan.getValue());
				}
			}

			User manager = this.userBS.existsByUser(managerId);
			User responsible = this.userBS.existsByUser(responsibleId);
			User goalManager = this.userBS.existsByUser(goalManagerId);
			String auxUrl = url.substring(0, url.lastIndexOf('/') + 1) + id;
			StructureLevelInstance parent = this.helper
					.retrieveLevelInstance(levelInstance.getParent());

			if (managerId != null) {
				this.helper.sendGoalNotification(
						manager,
						this.domain.get(),
						levelInstance,
						parent,
						auxUrl,
						openCloseGoal ? NotificationType.GOAL_CLOSED
								: NotificationType.GOAL_OPENED);
			}

			if (responsibleId != null) {
				if (responsible != null && !responsible.equals(manager)) {
					this.helper.sendGoalNotification(
							responsible,
							this.domain.get(),
							levelInstance,
							parent,
							auxUrl,
							openCloseGoal ? NotificationType.GOAL_CLOSED : NotificationType.GOAL_OPENED
					);
				}
			}

			if (goalManager != null) {
				if (!goalManager.equals(manager) && !goalManager.equals(responsible)) {
					this.helper.sendGoalNotification(
							goalManager,
							this.domain.get(),
							levelInstance,
							parent,
							auxUrl,
							openCloseGoal ? NotificationType.GOAL_CLOSED
									: NotificationType.GOAL_OPENED);
				}
			}

			return this.success(levelInstance);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Recupera informações sobre os niveis de um usuário
	 *
	 * @param userId
	 *               ID do usuário a ter os dados recuperados
	 * @return info informações sobre os objetivos
	 *
	 */
	@GetMapping(BASEPATH + "/structure/getuserlevels")
	public ResponseEntity<?> getUserLevels(@RequestParam Long userId, @ModelAttribute DefaultParams params) {
		try {
			if (!userBS.isLoggedUserOrHasAccess(userId, AccessLevels.COMPANY_ADMIN)) {
				return this.forbidden();
			}

			PaginatedList<StructureLevelInstance> list = this.bs.listLevelsByResponsible(userId, params);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Preencher os atributos "alcançado" e "justificativa" de uma instância de
	 * um level meta.
	 *
	 * @param id,
	 *                justification Id da instância de um level meta.
	 * @param reached
	 *                Valor do campo justificativa, valor do campo alcançado.
	 * @return structure Instância com os atributos preenchidos.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance/updategoal")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> updateLevelGoal(@RequestBody UpdateGoalDto dto, UserSession userSession) {
		boolean sucess = false;
		AttributeInstance alcancado;
		List<AttributeInstance> atributosInstance = new ArrayList<>();
		List<StructureLevelInstance> instances = new ArrayList<>();
		List<Attribute> attributeList;
		List<AttributeInstance> reacheadField = new ArrayList<>();

		try {
			String reached = dto.reached();

			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.id());
			if (levelInstance == null) {
				return this.fail("Estrutura incorreta!");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou atualizar a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			if (levelInstance.isClosed()) {
				return this.fail("Esta meta já foi concluída!");
			}

			attributeList = this.bs.retrieveLevelAttributes(levelInstance.getLevel());

			for (Attribute value : attributeList) {
				if (value.isReachedField()) {
					alcancado = this.attrHelper.retrieveAttributeInstance(levelInstance, value);
					if (alcancado == null) {
						AttributeInstance alcancadoEdit = new AttributeInstance();
						alcancadoEdit.setAttribute(value);
						alcancadoEdit.setLevelInstance(levelInstance);
						alcancadoEdit.setValue(reached);
						if (!reached.isEmpty()) {
							alcancadoEdit.setValueAsNumber(Double.parseDouble(reached));
						}
						value.setAttributeInstance(alcancadoEdit);
						reacheadField.add(alcancadoEdit);
						this.bs.persist(alcancadoEdit);
						atributosInstance.add(alcancadoEdit);
						sucess = true;

					} else {
						alcancado.setAttribute(value);
						alcancado.setLevelInstance(levelInstance);
						alcancado.setValue(reached);
						if (reached.isEmpty())
							alcancado.setValueAsNumber(null);
						else
							alcancado.setValueAsNumber(Double.parseDouble(reached));
						reacheadField.add(alcancado);
						this.bs.persist(alcancado);
						sucess = true;
					}
				}
			}

			StructureLevelInstance levelInstanceStatus = this.bs.retrieveLevelInstance(dto.id());
			levelInstanceStatus.getLevel()
					.setAttributes(this.bs.retrieveLevelAttributes(levelInstanceStatus.getLevel()));
			for (int i = 0; i < levelInstanceStatus.getLevel().getAttributes().size(); i++) {
				List<AttributeInstance> attrInstance = new ArrayList<>();
				attrInstance
						.add(this.attrHelper.retrieveAttributeInstance(levelInstanceStatus, attributeList.get(i)));
				if (attributeList.get(i).isReachedField()) {
					levelInstanceStatus.getLevel().getAttributes().get(i).setAttributeInstances(reacheadField);

				} else {
					levelInstanceStatus.getLevel().getAttributes().get(i).setAttributeInstances(attrInstance);
				}
			}

			instances.add(levelInstanceStatus);
			AttributeInstance polarityAttr = this.attrHelper
					.retrievePolarityAttributeInstance(levelInstance.getParent());
			if (polarityAttr != null)
				levelInstance.setPolarity(polarityAttr.getValue());
			else
				levelInstance.setPolarity(Polarity.BIGGER_BETTER.getValue());
			this.bs.setGoalStatus(instances, levelInstance.getPolarity());

			levelInstance.setProgressStatus(instances.get(0).getProgressStatus());
			List<Attribute> attributes = this.bs.retrieveLevelAttributes(levelInstance.getLevel());
			attributes = this.bs.setAttributesInstances(levelInstance, attributes);
			levelInstance.getLevel().setAttributes(attributes);
			if (sucess) {
				this.bs.updateLevelValues(levelInstance);
				return this.success("Meta atualizada com sucesso.");
			}
			return this.fail("O nível não é uma meta");
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar as instâncias do level indicador.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return list Lista de instâncias do level indicador.
	 */
	@GetMapping(BASEPATH + "/structure/indicators")
	public ResponseEntity<?> listIndicators() {
		try {
			PaginatedList<StructureLevelInstance> list = this.bs.listIndicators();
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as instâncias do level eixo.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return list Lista de instâncias do level eixo.
	 */
	@GetMapping(BASEPATH + "/structure/axes")
	public ResponseEntity<?> listAxes() {
		try {
			PaginatedList<StructureLevelInstance> list = this.bs.listAxes();
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as instâncias do level eixo.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return list Lista de instâncias do level eixo.
	 */
	@GetMapping(BASEPATH + "/structure/filter-axes")
	public ResponseEntity<?> listLinkAxesToLink(
			@RequestParam(required = false) List<Long> excludedIds,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String term) {
		try {
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterAxes(excludedIds, page,
					pageSize, term);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as instâncias do level indicador.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return list Lista de instâncias do level indicador.
	 */
	@GetMapping(BASEPATH + "/structure/filter-indicators")
	public ResponseEntity<?> listLinkIndicatorToLink(
			@RequestParam(required = false) List<Long> excludedIds,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String term) {

		try {
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterIndicators(excludedIds, page,
					pageSize, term);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as instâncias do level meta.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return list Lista de instâncias do level meta.
	 */
	@GetMapping(BASEPATH + "/structure/filter-goals")
	public ResponseEntity<?> listLinkGoalToLink(
			@RequestParam(required = false) List<Long> excludedIds,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String term) {

		try {
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterGoals(excludedIds, page,
					pageSize, term);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar instâncias do level objetivo pelo plano macro e/ou plano de metas.
	 *
	 * @param macroId
	 *                Id do plano macro.
	 * @param planId
	 *                Id do plano de metas.
	 * @return list Lista de instâncias do level objetivo.
	 */
	@GetMapping(BASEPATH + "/structure/objectives")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> listObjective(
			@RequestParam(required = false) Long macroId,
			@RequestParam(required = false) Long planId,
			UserSession userSession) {
		try {
			PlanMacro planMacro = null;
			if (macroId != null) {
				planMacro = this.planBS.retrievePlanMacroById(macroId);
			}

			Plan plan2 = null;
			if (planId != null) {
				plan2 = this.planBS.retrieveById(planId);
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
				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn("Tentativa de acesso indevido ao PlanMacro ID {}", macroId);
					return this.fail("Acesso negado.");
				}
				if (plan2 != null && !plan2.getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn("Tentativa de acesso indevido ao Plan ID {}", planId);
					return this.fail("Acesso negado.");
				}
			}

			PaginatedList<StructureLevelInstance> list = this.bs.listObjective(planMacro, plan2);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar instâncias do level objetivo pelo plano macro e/ou plano de metas para
	 * link com risco.
	 *
	 * @param macroId
	 *                Id do plano macro.
	 * @param planId
	 *                Id do plano de metas.
	 * @return list Lista de instâncias do level objetivo.
	 */
	@GetMapping(BASEPATH + "/structure/filter-objectives")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> listObjectivesToLink(
			@RequestParam(required = false) List<Long> excludedIds,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String term) {

		try {
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterObjectives(excludedIds, page,
					pageSize, term);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as instâncias de leveis vinculadas a riscos.
	 *
	 * @param ResponseEntity<?>
	 *
	 * @return list Lista de instâncias de leveis vinculadas a riscos.
	 */
	@GetMapping(BASEPATH + "/structure/pdi-linked-to-risk")
	public ResponseEntity<?> listPdiLinkedToRisks(@RequestParam Long unitId, UserSession userSession) {
		try {
			Unit unit = this.unitBS.exists(unitId, Unit.class);
			if (unit == null || unit.isDeleted()) {
				return this.fail("Unidade não encontrada");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !unit.getPlanRisk().getPolicy().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou acessar a Unit ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, unit.getId(),
							unit.getPlanRisk().getPolicy().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			List<StructureLevelInstance> list = structureInstanceFilterBS.listPdiLinkedToRisks(unit);
			return this.success(new ListWrapper<>(list));
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	@PostMapping(BASEPATH + "/structure/levelinstance/goalsgenerate")
	public ResponseEntity<?> goalsGenerate(@RequestBody GoalsGenerateDto dto, UserSession userSession) {
		try {
			String periodicity = null;
			String begin = null;
			String end = null;
			Date beginDate = null;
			Date endDate = null;

			StructureLevelInstance indicator = this.bs.retrieveLevelInstance(dto.indicatorId());
			if (indicator == null) {
				return this.fail("Indicador não encontrado.");
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !indicator.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou gerar metas para o Indicador ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, indicator.getId(),
							indicator.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(indicator.getPlan().getParent().getCompany());

			indicator.getLevel().setAttributes(this.bs.retrieveLevelAttributes(indicator.getLevel()));
			this.bs.setAttributesInstances(indicator, indicator.getLevel().getAttributes());
			for (Attribute attribute : indicator.getLevel().getAttributes()) {
				if (attribute.getAttributeInstance() != null) {
					if (attribute.isPeriodicityField()) {
						periodicity = attribute.getAttributeInstance().getValue();
					} else if (attribute.isBeginField()) {
						begin = attribute.getAttributeInstance().getValue();
					} else if (attribute.isEndField()) {
						end = attribute.getAttributeInstance().getValue();
					}
				}
			}
			String msgError = "";

			if (begin != null && end != null) {
				DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				beginDate = (Date) formatter.parse(begin + " 00:00:00");
				endDate = (Date) formatter.parse(end + " 00:00:00");
			} else {
				if (begin == null) {
					msgError = "Por favor, edite o indicador e insira os dados obrigatórios";
				}
				if (end == null) {
					msgError = "Por favor, edite o indicador e insira os dados obrigatórios";
				}
				return this.fail(msgError);
			}

			if (periodicity == null) {
				throw new IllegalStateException(
						"È necessário preencher a periodicidade do indicador antes de gerar as metas");
			}

			if (periodicity != null && beginDate != null && endDate != null && dto.name() != null) {
				Long parent = indicator.getId();
				Plan plan = indicator.getPlan();
				Long structureId = this.bs.retrieveStructureIdByLevel(indicator.getLevel());
				Structure structure = new Structure();
				structure.setId(structureId);
				StructureLevel level = this.bs.retrieveNextLevel(structure, indicator.getLevel().getSequence() + 1);
				level.setAttributes(this.bs.retrieveLevelAttributes(level));
				this.bs.goalsGenerateByPeriodicity(parent, plan, level, dto.name(), dto.manager(), dto.responsible(),
						dto.description(), dto.expected(), dto.minimum(), dto.maximum(), periodicity, beginDate,
						endDate);
			} else {
				return this.fail("Dados invalidos.");
			}

			return this.success("Metas geradas com sucesso.");

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Listar instâncias de um level indicador pelo plano macro e/ou plano de
	 * metas.
	 *
	 * @param macroId
	 *                Id do plano macro.
	 * @param planId
	 *                Id do plano de metas.
	 * @return list Lista de instâncias de um level indicador.
	 */
	@GetMapping(BASEPATH + "/structure/indicatorsByMacroAndPlan")
	@CommunityDashboard
	public ResponseEntity<?> listIndicatorsByMacroAndPlan(
			@RequestParam(required = false) Long macroId,
			@RequestParam(required = false) Long planId) {

		try {
			PlanMacro planMacro = null;
			if (macroId != null) {
				planMacro = this.planBS.retrievePlanMacroById(macroId);
				if (planMacro != null) {
					domain.validateTenant(planMacro.getCompany());
				}
			}

			Plan plan2 = null;
			if (planId != null) {
				plan2 = this.planBS.retrieveById(planId);
				if (plan2 != null) {
					domain.validateTenant(plan2.getParent().getCompany());
				}
			}

			PaginatedList<StructureLevelInstance> list = this.bs.listIndicatorsByMacroAndPlan(planMacro, plan2);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar instâncias dos leveis filhos de um level.
	 *
	 * @param planId
	 *               Id do plano de metas.
	 * @param parent
	 *               Id da instância de um level pai.
	 * @return list Lista de instâncias dos leveis filhos.
	 */
	@GetMapping(BASEPATH + "/structure/levelsonsfilter")
	@CommunityDashboard
	public ResponseEntity<?> listLevelSonsForFilter(
			@RequestParam(required = false) Long planId,
			@RequestParam(required = false) Long parent) {

		try {
			Plan plan = null;
			if (planId != null) {
				plan = this.planBS.retrieveById(planId);
				if (plan != null) {
					domain.validateTenant(plan.getParent().getCompany());
				}
			}

			if (parent != null) {
				StructureLevelInstance parentInstance = this.bs.retrieveLevelInstance(parent);
				if (parentInstance != null) {
					domain.validateTenant(parentInstance.getPlan().getParent().getCompany());
				}
			}

			PaginatedList<StructureLevelInstance> list = this.bs.retrieveLevelSonsForFilter(plan, parent);

			if (list.getTotal() > 0) {
				for (int i = 0; i < list.getList().size(); i++) {
					this.helper.fillIndicators(list.getList().get(i));
				}
			}
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Excluir uma lista de metas
	 *
	 * @param list,
	 *              lista dos IDs das metas.
	 */
	@PostMapping(BASEPATH + "/structure/deleteGoals")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteManyGoals(@RequestBody DeleteGoalsDto dto, UserSession userSession) {
		try {
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
			}

			var list = dto.list();
			for (int i = 0; i < list.getTotal(); i++) {
				Long id = list.getList().get(i).longValue();
				StructureLevelInstance goal = this.bs.retrieveLevelInstance(id);
				if (goal == null) {
					LOGGER.warn("Tentativa de exclusão de meta com ID inválido: {}", id);
					continue;
				}

				if (!isSystemAdmin) {
					if (!goal.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
						LOGGER.warn(String.format(
								"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou deletar a Meta ID %d da Empresa ID %d.",
								userSession.getAuthData().getName(), loggedInUserId, goal.getId(),
								goal.getPlan().getParent().getCompany().getId()));
						continue;
					}
				}

				domain.validateTenant(goal.getPlan().getParent().getCompany());

				goal.setDeleted(true);
				this.bs.persist(goal);

				PaginatedList<FavoriteLevelInstance> favorites = this.bs.listFavoriteByLevelInstance(goal);
				for (FavoriteLevelInstance favorite : favorites.getList()) {
					favorite.setDeleted(true);
					this.bs.persist(favorite);
				}
			}
			return this.success("Operação de exclusão de metas concluída.");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Adicionar um level aos favoritos
	 *
	 * @param levelInstanceId,
	 *                         Id de uma instância de um level.
	 */
	@PostMapping(BASEPATH + "/structure/savefavorite")
	public ResponseEntity<?> saveFavoriteLevelInstance(@RequestBody LevelInstanceIdDto dto, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.levelInstanceId());
			if (levelInstance == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou favoritar a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			PaginatedList<FavoriteLevelInstance> favorites = this.bs
					.listFavoriteLevelInstances(levelInstance.getPlan().getParent());

			if (favorites.getList().size() < 200) {
				FavoriteLevelInstance favorite = this.bs.retrieveFavoriteLevelInstance(levelInstance);
				if (favorite == null) {
					this.bs.saveFavoriteLevelInstance(levelInstance);
				} else {
					favorite.setDeleted(false);
					this.bs.persist(favorite);
				}
				return this.success("Adicionado aos favoritos com sucesso.");
			} else {
				return this.fail("Você já possui o número máximo de favoritos");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Remover um level dos favoritos
	 *
	 * @param levelInstanceId,
	 *                         Id de uma instância de um level.
	 */
	@PostMapping(BASEPATH + "/structure/removefavorite")
	public ResponseEntity<?> removeFavoriteLevelInstance(@RequestBody LevelInstanceIdDto dto, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.levelInstanceId());
			if (levelInstance == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou remover favorito da Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			FavoriteLevelInstance favorite = this.bs.retrieveFavoriteLevelInstance(levelInstance);
			if (favorite != null) {
				favorite.setDeleted(true);
				this.bs.persist(favorite);
			}
			return this.success("Removido dos favoritos com sucesso.");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar os leveis favoritos por usuário e empresa
	 *
	 * @param levelInstanceId,
	 *                         Id de uma instância de um level.
	 */
	@GetMapping(BASEPATH + "/structure/listfavorites")
	public ResponseEntity<?> listFavoriteLevelInstance(@RequestParam Long macroId, UserSession userSession) {
		try {
			PlanMacro macro = this.planBS.retrievePlanMacroById(macroId);
			if (macro == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !macro.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar favoritos do Plano Macro ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, macro.getId(),
							macro.getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(macro.getCompany());

			PaginatedList<FavoriteLevelInstance> favorites = this.bs.listFavoriteLevelInstances(macro);
			return this.success(favorites);
		} catch (Throwable ex) {
			LOGGER.error("Erro inesperado ao listar favoritos. macroId: {}", macroId, ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar o leveis indicadores que estão agregados a um level indicador
	 * agregado.
	 *
	 * @param aggregate
	 *                  Instância de um level indicador agregado.
	 *
	 */
	@GetMapping(BASEPATH + "/structure/listaggregates")
	public ResponseEntity<?> listAggregates(@RequestParam Long id, UserSession userSession) {
		try {
			StructureLevelInstance indicator = this.helper.retrieveLevelInstance(id);
			if (indicator == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !indicator.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou listar agregados da Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, indicator.getId(),
							indicator.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(indicator.getPlan().getParent().getCompany());

			if (indicator.isAggregate()) {
				PaginatedList<AggregateIndicator> list = new PaginatedList<AggregateIndicator>();
				list.setList(this.bs.listAggregateIndicatorsByAggregate(indicator));
				list.setTotal(Long.valueOf(this.bs.listAggregateIndicatorsByAggregate(indicator).size()));
				return this.success(list);
			} else {
				return this.fail("Indicador não agregado");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Atualiza status da instância de nível de estrutura para visualizada
	 *
	 * @param levelInstanceId
	 *                        Id da instância de nível.
	 *
	 */
	@PostMapping("/api/structure/levelinstance/setvisualized")
	public ResponseEntity<?> setVisualized(@RequestBody LevelInstanceIdDto dto, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.levelInstanceId());
			if (levelInstance == null) {
				return this.notFound();
			}

			// BOLA/IDOR Security Check
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
							"Tentativa de acesso indevido. Usuário '%s' (ID: %d) tentou marcar como visualizada a Instância ID %d da Empresa ID %d.",
							userSession.getAuthData().getName(), loggedInUserId, levelInstance.getId(),
							levelInstance.getPlan().getParent().getCompany().getId()));
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(levelInstance.getPlan().getParent().getCompany());

			levelInstance.setVisualized(true);
			this.bs.persist(levelInstance);
			return this.success("Status de visualização atualizado.");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}

	@GetMapping(BASEPATH + "/structure/infotable")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<?> InfoTable(@ModelAttribute PDIFilterParams filterParams, UserSession userSession) {
		try {
			filterParams.validateParams();

			CompanyDomain currentDomain = this.domain.get();
			Company domainCompany = currentDomain != null ? currentDomain.getCompany() : null;
			if (domainCompany == null) {
				LOGGER.warn("Tentativa de acesso à infotable sem domínio resolvido.");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
			}

			PlanMacro planMacro = null;
			if (filterParams.getMacro() != null) {
				planMacro = this.planBS.retrievePlanMacroById(filterParams.getMacro());
			}

			Plan planObj = null;
			if (filterParams.getPlan() != null) {
				planObj = this.planBS.retrieveById(filterParams.getPlan());
			}

			StructureLevelInstance levelInstance = null;
			Long levelInstanceId = filterParams.getLevelInstance();
			if (levelInstanceId != null) {
				levelInstance = this.bs.retrieveLevelInstance(levelInstanceId);
			}

			StructureLevelInstance goalInstance = null;
			Long goalId = filterParams.getGoalId();
			if (goalId != null) {
				goalInstance = this.bs.retrieveLevelInstance(goalId);
			}

			if (planMacro != null && !planMacro.getCompany().getId().equals(domainCompany.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
			}
			if (planObj != null && !planObj.getParent().getCompany().getId().equals(domainCompany.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
			}
			if (levelInstance != null
					&& !levelInstance.getPlan().getParent().getCompany().getId().equals(domainCompany.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
			}
			if (goalInstance != null
					&& !goalInstance.getPlan().getParent().getCompany().getId().equals(domainCompany.getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null) {
					LOGGER.error("Falha de segurança: Usuário com ID {} não está associado a nenhuma empresa.",
							loggedInUserId);
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Usuário sem empresa.");
				}
				if (!domainCompany.getId().equals(userCompany.getId())) {
					LOGGER.warn(
							"Tentativa de acesso cruzado à infotable. Usuário {} da empresa {} tentou acessar domínio da empresa {}.",
							loggedInUserId, userCompany.getId(), domainCompany.getId());
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Domínio inválido.");
				}
				if (planMacro != null && !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Usuário sem empresa.");
				}
				if (planObj != null && !planObj.getParent().getCompany().getId().equals(userCompany.getId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Usuário sem empresa.");
				}
				if (levelInstance != null
						&& !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Usuário sem empresa.");
				}
				if (goalInstance != null
						&& !goalInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Usuário sem empresa.");
				}
			}

			try {
				if (planMacro != null)
					domain.validateTenant(planMacro.getCompany());
				if (planObj != null)
					domain.validateTenant(planObj.getParent().getCompany());
				if (levelInstance != null)
					domain.validateTenant(levelInstance.getPlan().getParent().getCompany());
				if (goalInstance != null)
					domain.validateTenant(goalInstance.getPlan().getParent().getCompany());
			} catch (IllegalStateException ex) {
				LOGGER.warn("Violação de tenant na infotable: {}", ex.getMessage());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
			}

			String startDateStr = filterParams.getStartDate();
			String endDateStr = filterParams.getEndDate();
			Boolean goalStatus = filterParams.getGoalStatus();
			Integer progressStatus = filterParams.getProgressStatus();

			PaginatedList<GoalsInfoTable> goalsList = this.dashboardBS.getInfoTable(
					planMacro, planObj, levelInstanceId, goalId, filterParams.getPage(),
					filterParams.getPageSize(), startDateStr, endDateStr, goalStatus, progressStatus);

			return this.success(goalsList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@PostMapping(value = BASEPATH
    + "/structure/exportPDF", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	public ResponseEntity<Object> exportPDF(
			@RequestBody PDIFilterParams filterParams,
			@RequestParam(name = "async", required = false, defaultValue = "true") boolean async,
			UserSession userSession,
			HttpServletRequest request) {
		try {
			if (!async) {
				return ResponseEntity.badRequest().body("Modo síncrono não suportado. Use async=true.");
			}

			filterParams.validateParams();

			Plan planObj = planBS.retrieveById(filterParams.getPlan());
			if (planObj == null) {
				return new ResponseEntity<>("Plano não encontrado.", HttpStatus.BAD_REQUEST);
			}

			PlanMacro planMacro = planObj.getParent();
			if (planMacro == null) {
				return new ResponseEntity<>("Macro não encontrada.", HttpStatus.BAD_REQUEST);
			}

			StructureLevelInstance currentInstance = helper.retrieveLevelInstance(filterParams.getLevelInstance());
			if (currentInstance == null) {
				return new ResponseEntity<>("Nível não encontrado.", HttpStatus.BAD_REQUEST);
			}

			var dto = org.forpdi.planning.structure.dto.GoalsReportRequestDto
					.from(filterParams, planMacro.getCompany().getId());

			UUID jobId = structureBS.startReportJobFromRequest(dto);

			String ctx = request.getContextPath();
			Map<String, String> body = new HashMap<>();
			body.put("jobId", jobId.toString());
			body.put("statusUrl", ctx + BASEPATH + "/structure/exportPDF/status/" + jobId);

			return ResponseEntity.accepted()
					.header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
					.body(body);

		} catch (Exception ex) {
			LOGGER.error("Erro ao iniciar geração de PDF", ex);
			Map<String, String> errorBody = new HashMap<>();
			errorBody.put("error", "Erro interno ao gerar PDF");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
		}
	}

	@GetMapping(value = BASEPATH + "/structure/exportPDF", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<Object> exportPDFGetCompat(
			@RequestParam(name = "async", defaultValue = "true") boolean async,
			@RequestParam(name = "macro") Long macro,
			@RequestParam(name = "plan") Long plan,
			@RequestParam(name = "levelInstance", required = false) Long levelInstance,
			@RequestParam(name = "goalId", required = false) Long goalId,
			@RequestParam(name = "startDate", required = false) String startDate,
			@RequestParam(name = "endDate", required = false) String endDate,
			@RequestParam(name = "goalStatus", required = false) String goalStatus,
			@RequestParam(name = "progressStatus", required = false) Integer progressStatus,
			UserSession userSession,
			HttpServletRequest request) {
		try {
			PlanMacro planMacro = this.planBS.retrievePlanMacroById(macro);

			if (planMacro == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plano Macro não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !planMacro.getCompany().getId().equals(userCompany.getId())) {
					LOGGER.warn(String.format(
						"SECURITY ALERT: IDOR em exportPDF. User: %s (ID: %d) tentou acessar Macro %d da Empresa %d.",
						userSession.getAuthData().getName(),
						loggedInUserId,
						macro,
						planMacro.getCompany().getId()));

					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Este plano pertence a outra instituição.");
				}
			}

			PDIFilterParams filterParams = new PDIFilterParams();
			filterParams.setMacro(macro);
			filterParams.setPlan(plan);
			filterParams.setLevelInstance(levelInstance);
			filterParams.setGoalId(goalId);
			filterParams.setStartDate(startDate);
			filterParams.setEndDate(endDate);
			filterParams.setGoalStatus(parseGoalStatusCompat(goalStatus));
			filterParams.setProgressStatus(progressStatus);

			return this.exportPDF(filterParams, async, userSession, request);

		} catch (Exception e) {
			LOGGER.error("Erro ao exportar PDF", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno.");
		}
	}

	private static Boolean parseGoalStatusCompat(String raw) {
		if (raw == null)
			return null;
		String v = raw.trim().toLowerCase();
		if (v.isEmpty() || "-1".equals(v))
			return null;
		if ("true".equals(v) || "1".equals(v))
			return Boolean.TRUE;
		if ("false".equals(v) || "0".equals(v))
			return Boolean.FALSE;
		return null;
	}

	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	@GetMapping(BASEPATH + "/structure/exportPDF/status/{jobId}")
	public ResponseEntity<Object> reportStatus(@PathVariable String jobId, UserSession userSession) {
		try {
			UUID id = UUID.fromString(jobId);
			JobInfoEntity job = structureBS.getJobStatusEntity(id);

			if (job == null) {
				Map<String, String> errorBody = new HashMap<>();
				errorBody.put("error", "Job não encontrado");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
			}

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("status", job.getStatus());
			responseBody.put("stage", job.getStage());
			responseBody.put("progress", job.getProgress());
			responseBody.put("error", job.getError() != null ? job.getError().replace("\"", "'") : "");

			if ("SUCCEEDED".equalsIgnoreCase(job.getStatus()) && job.getArchiveId() != null) {
				Archive arc = archiveBS.exists(job.getArchiveId(), Archive.class);
				if (arc != null) {
					String signed = storageService.createPresignedGetUrl(
							arc.getFileLink(), java.time.Duration.ofMinutes(5));
					responseBody.put("downloadUrl",
							signed != null
									? signed
									: "/" + org.forpdi.core.utils.Consts.FILES_ENDPOINT_BASE_URL + "/"
											+ arc.getFileLink());
				}
			}

			return ResponseEntity.ok()
					.header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
					.body(responseBody);

		} catch (Exception e) {
			Map<String, String> errorBody = new HashMap<>();
			errorBody.put("error", "Falha ao consultar status");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
		}
	}

	@PreAuthorize(AccessLevels.HAS_ROLE_AUTENTICATED)
	@GetMapping(BASEPATH + "/structure/exportPDF/download/{jobId}")
	public ResponseEntity<Void> reportDownloadRedirect(@PathVariable String jobId, HttpServletRequest request) {
		try {
			UUID id = UUID.fromString(jobId);
			JobInfoEntity job = structureBS.getJobStatusEntity(id);

			if (job == null || job.getArchiveId() == null) {
				return ResponseEntity.notFound().build();
			}
			Archive arc = archiveBS.exists(job.getArchiveId(), Archive.class);
			if (arc == null) {
				return ResponseEntity.notFound().build();
			}

			String contextPath = request.getContextPath();
			String downloadUrl = contextPath + "/" + org.forpdi.core.utils.Consts.FILES_ENDPOINT_BASE_URL + "/"
					+ arc.getFileLink();

			return ResponseEntity.status(HttpStatus.FOUND).header("Location", downloadUrl).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Listar os objetivos a partir de um eixo temático.
	 *
	 * * @param thematicAxisId
	 *
	 * Id do eixo temático.
	 *
	 * @return list Lista de objetivos.
	 */
	@GetMapping(BASEPATH + "/structure/thematic-axis/{thematicAxisId}/objectives")
	public ResponseEntity<?> listObjectivesByThematicAxis(@PathVariable Long thematicAxisId, UserSession userSession) {
		try {
			StructureLevelInstance thematicAxis = this.bs.retrieveLevelInstance(thematicAxisId);
			if (thematicAxis == null) {
				return this.fail("Eixo temático não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !thematicAxis.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(thematicAxis.getPlan().getParent().getCompany());
			PaginatedList<StructureLevelInstance> list = this.bs.listObjectivesByThematicAxis(thematicAxis);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar os indicadores a partir de um objetivo.
	 *
	 * * @param objectiveId
	 *
	 * Id do objetivo.
	 *
	 * @return list Lista de indicadores.
	 */
	@GetMapping(BASEPATH + "/structure/objective/{objectiveId}/indicators")
	public ResponseEntity<?> listIndicatorsByObjective(@PathVariable Long objectiveId, UserSession userSession) {
		try {
			StructureLevelInstance objective = this.bs.retrieveLevelInstance(objectiveId);
			if (objective == null) {
				return this.fail("Objetivo não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null || !objective.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			domain.validateTenant(objective.getPlan().getParent().getCompany());
			PaginatedList<StructureLevelInstance> list = this.bs.listIndicatorsByObjective(objective);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(BASEPATH + "/structure/levelinstance/{id}/duplicate/options")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> duplicateOptions(@PathVariable Long id, UserSession userSession) {
		try {
			final StructureLevelInstance source = this.bs.retrieveLevelInstance(id);
			if (source == null || source.isDeleted())
				return this.notFound();

			final boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				final Long loggedInUserId = userSession.getAuthData().getId();
				final Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !source.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			domain.validateTenant(source.getPlan().getParent().getCompany());

			final boolean needsParent = (source.getParent() != null);
			final List<StructureLevelInstance> candidates = this.bs.listDuplicateDestinations(source);

			final List<org.forpdi.planning.structure.dto.DuplicateOptionItemDto> items = candidates.stream()
					.map(c -> new org.forpdi.planning.structure.dto.DuplicateOptionItemDto(
							c.getId(), c.getName(), this.bs.buildPath(c)))
					.toList();

			final org.forpdi.planning.structure.dto.DuplicateLevelInstanceOptionsDto dto = new org.forpdi.planning.structure.dto.DuplicateLevelInstanceOptionsDto(
					needsParent, items);

			return this.success(dto);

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@PostMapping(BASEPATH + "/structure/levelinstance/{id}/duplicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> duplicateLevelInstance(
			@PathVariable Long id,
			@RequestBody(required = false) org.forpdi.planning.structure.dto.DuplicateLevelInstanceRequestDto dto,
			UserSession userSession) {

		try {
			final StructureLevelInstance source = this.bs.retrieveLevelInstance(id);
			if (source == null || source.isDeleted())
				return this.notFound();

			final boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				final Long loggedInUserId = userSession.getAuthData().getId();
				final Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				if (userCompany == null
						|| !source.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			domain.validateTenant(source.getPlan().getParent().getCompany());

			final boolean needsParent = (source.getParent() != null);
			Long destinationParentId = (dto != null ? dto.destinationParentId() : null);

			if (needsParent && destinationParentId == null) {
				return this.fail("Selecione o destino.");
			}
			if (!needsParent && destinationParentId != null) {
				destinationParentId = null;
			}

			if (needsParent) {
				final StructureLevelInstance dest = this.bs.retrieveLevelInstance(destinationParentId);
				if (dest == null || dest.isDeleted())
					return this.fail("Destino inválido.");

				if (!dest.getPlan().getId().equals(source.getPlan().getId())) {
					return this.fail("Origem e destino devem pertencer ao mesmo Plano de Metas.");
				}

				final StructureLevelInstance currentParent = this.bs.retrieveLevelInstance(source.getParent());
				final boolean sameParentLevel = dest.getLevel() != null && currentParent != null
						&& currentParent.getLevel() != null
						&& (java.util.Objects.equals(dest.getLevel().getId(), currentParent.getLevel().getId()) ||
								java.util.Objects.equals(dest.getLevel().getSequence(),
										currentParent.getLevel().getSequence()));
				if (!sameParentLevel) {
					return this.fail("Destino não é um nível-pai válido para este item.");
				}
			}

			final StructureLevelInstance cloneRoot = this.bs.duplicateSubtree(source, destinationParentId);
			return this.success(new org.forpdi.core.bean.IdDto(cloneRoot.getId()));

		} catch (IllegalArgumentException iae) {
			return this.fail(iae.getMessage());
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
}
