package org.forpdi.planning.structure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.notification.NotificationType;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.DashboardBS;
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
import org.forpdi.planning.structure.dto.CreateLevelInstanceDto;
import org.forpdi.planning.structure.dto.DeleteGoalsDto;
import org.forpdi.planning.structure.dto.GoalsGenerateDto;
import org.forpdi.planning.structure.dto.LevelInstanceIdDto;
import org.forpdi.planning.structure.dto.SaveAttributeInstanceDto;
import org.forpdi.planning.structure.dto.StructureLevelInstanceDto;
import org.forpdi.planning.structure.dto.UpdateGoalDto;
import org.forpdi.planning.structure.goals.history.GoalJustificationHistory.JustificationAndReachedValue;
import org.forpdi.planning.structure.goals.history.GoalJustificationHistoryBS;
import org.forpdi.planning.structure.xml.StructureBeans;
import org.forpdi.planning.structure.xml.StructureImporter;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.fpdi.GoalsReportGenerator;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.forrisco.core.unit.Unit;
import org.forrisco.core.unit.UnitBS;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;


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
	 *            Arquivo xml.
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
					Structure duplicatedStructure = duplicatedStructureCheckHelper.retrieveDuplicatedStructure(readedStructure);
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
	 *            Estrutura a se salva.
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
	 *            Estrutura a ser atualizada.
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
			existent.setDescription(structure.getDescription());
			existent.setName(structure.getName());
			this.bs.persist(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Buscar a estrutura pelo id.
	 * 
	 * @param id
	 *            Id da estrutura a ser buscada.
	 * @return structure Estrutura encontrada.
	 */
	@GetMapping(BASEPATH + "/structure/{id}")
	public ResponseEntity<?> retrieveStructure(@PathVariable Long id) {
		try {
			Structure structure = this.bs.exists(id, Structure.class);
			if (structure == null) {
				return this.fail("A empresa solicitada não foi encontrada.");
			} else {
				structure.setLevels(this.bs.listLevels(structure).getList());
				for (StructureLevel level : structure.getLevels()) {
					level.setAttributes(this.bs.listAttributes(level,false).getList());
				}
				return this.success(structure);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar as estruturas por página.
	 * 
	 * @param page
	 *            Número da página a ser listada.
	 * @return structures Lista de estruturas.
	 */
	@GetMapping(BASEPATH + "/structure")
	public ResponseEntity<?> listStructures() {
		try {
			PaginatedList<Structure> structures = this.bs.list();
			for (Structure structure : structures.getList()) {
				structure.setLevels(this.bs.listLevels(structure).getList());
				for (StructureLevel level : structure.getLevels()) {
					level.setAttributes(this.bs.listAttributes(level,false).getList());
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
	 *            Id da estrutura a ser deletada.
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
	 *            Id do level.
	 * @param planId
	 *            Id do plano de metas.
	 * @param parentId
	 *            Id do level pai.
	 * @return list Lista de instâncias de um level.
	 */
	@GetMapping(BASEPATH + "/structure/levelInstancelist")
	public ResponseEntity<?> listLevelInstance(
			@RequestParam Long sequence,
			@RequestParam Long planId,
			@RequestParam(required = false) Long parentId) {

		try {
			Plan plan = this.planBS.retrieveById(planId);
			StructureLevel level = this.bs.retrieveNextLevel(plan.getStructure(), sequence.intValue());

			if (parentId == 0)
				parentId = null;
			boolean haveBudget = false;
			PaginatedList<StructureLevelInstance> dto = new PaginatedList<>();
			List<StructureLevelInstance> list = this.bs.listLevelsInstance(level, plan, parentId);
			dto.setList(list);
			dto.setTotal((long) list.size());
						
			List<StructureLevelInstance> structureList = dto.getList();
			for (int count = 0; count < structureList.size(); count++) {
				List<Budget> budgetList = this.budgetBS.listBudgetByLevelInstance(structureList.get(count));
				if(!budgetList.isEmpty())
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
			@RequestParam(required = false) Long parentId) {

		try {
			Plan plan = this.planBS.retrieveById(planId);
			if (parentId == 0)
				parentId = null;
			PaginatedList<StructureLevelInstance> list = this.bs.listLevelsInstance(plan, parentId);
			List<StructureLevelInstanceDetailed> levelInstanceDetailedList = new ArrayList<StructureLevelInstanceDetailed>();
			for (StructureLevelInstance levelInstance : list.getList()) {
				levelInstanceDetailedList = this.bs.listLevelInstanceDetailed(levelInstance);
				levelInstance.setLevelInstanceDetailedList(new ArrayList<StructureLevelInstanceDetailed>());
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
	 *            Id do plano.
	 * @param levelId
	 *            Id do level.
	 * @param instanceName
	 *            Nome do levelInstance.
	 * @param parentId
	 *            Id do level pai.
	 * @return levelInstance Retorna a instância criada.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> createLevelInstance(@RequestBody CreateLevelInstanceDto dto) {
		
		if (Util.hasOnlySpecialCharactersString(dto.instanceName())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}
		
		try {
			Long parentId = dto.parentId();
			Plan plan = this.planBS.retrieveById(dto.planId());
			StructureLevel level = this.bs.retrieveStructureLevelById(dto.levelId());
			if (parentId != null) {
				StructureLevelInstance parent = this.bs.retrieveLevelInstance(parentId);
				if (parent.isAggregate()) {
					return this.fail("Indicadores agregados não podem ter metas");
				}
			}

			if (plan == null || level == null) {
				return this.fail("Plano ou estrutura incorretos!");
			} else {
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
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Buscar uma instância de um level pelo id
	 * 
	 * @param levelInstanceId
	 *            Id da instância.
	 * @return levelInstance Instância encontrada.
	 */
	@GetMapping(BASEPATH + "/structure/levelinstance")
	public ResponseEntity<?> retrieveLevelInstance(@RequestParam Long levelInstanceId) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(levelInstanceId);
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
	 *            Id da instância de um level.
	 * @return levelInstance Instância de um level com a lista de atributos.
	 */
	@GetMapping(BASEPATH + "/structure/levelattributes")
	public ResponseEntity<?> retrieveLevelAttributes(@RequestParam Long id) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstanceNoDeleted(id);

			
			if (levelInstance == null) {
				return this.fail("Estrutura incorreta!");
			} else {
				List<Attribute> attributeList = this.bs.retrieveLevelAttributes(levelInstance.getLevel());
				attributeList = this.bs.setAttributesInstances(levelInstance, attributeList);

				// Hard coded campo de Gestor, Anexar Arquivos de uma meta
				// e label "Responsável" para Responsável técnico
				StructureLevel structureLevel = levelInstance.getLevel();
				if (structureLevel.isGoal()) {
					Boolean hasManagerField = false;
					Boolean hasAttachmentField = false;
					for (int i = 0; i < attributeList.size(); i++) {					
						Attribute attribute = attributeList.get(i);
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

				//Hard coded campo de Justificativa de Formato (Indicadores)
				if(structureLevel.isIndicator()) {
					Boolean hasJustificationField = false;
					for (int i = 0; i < attributeList.size(); i++) {					
						if (attributeList.get(i).isJustificationField()) {
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

				AttributeInstance attributeInstance = null;
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
				for (int i = 0; i < favorites.getList().size(); i++) {
					if (favorites.getList().get(i).getLevelInstance().getId().equals(levelInstance.getId()))
						favoriteExistent = true;
				}
				levelInstance.setFavoriteExistent(favoriteExistent);
				levelInstance.setFavoriteTotal(favorites.getList().size());
				levelInstance.setIndicatorList(this.bs.listAggregateIndicatorsByAggregate(levelInstance));

				return this.success(levelInstance);
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar as instâncias dos atributos de um level.
	 * 
	 * @param levelInstance
	 *            Instância de um level com a lista de atributos e seus valores.
	 * @return existentLevelInstance Instância com a lista de atributos e seus
	 *         valores salvos.
	 */
	@PostMapping(BASEPATH + "/structure/levelattributes")
	public ResponseEntity<?> saveLevelAttributesInstance(@RequestBody SaveAttributeInstanceDto dto) {
		
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

			String mainUrl[] = url.split("\\?"); // remoção de parâmetro na url
			if (mainUrl.length > 0) {
				url = mainUrl[0];
			}

			StructureLevelInstance existentLevelInstance = this.bs.retrieveLevelInstance(levelInstance.getId());
			if (existentLevelInstance == null) {
				return this.fail("Estrutura incorreta!");
			} else if (existentLevelInstance.isClosed()) {
				return this.fail("Esta meta já foi concluída!");
			}
			
			boolean isGoal = existentLevelInstance.getLevel().isGoal();
			
			StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo = bs.getStructureInstancePermissionInfo(existentLevelInstance);
			
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
						userId = Long.parseLong(attInst.getValue());
						if (attributeInstance.getValue() == null
								|| !attributeInstance.getValue().equals(attInst.getValue())) {
							responsibleChanged = true;
						}
					}
					if (attribute.getType().equals(ManagerField.class.getCanonicalName())) {
						managerId = Long.parseLong(attInst.getValue());
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
						} else 	if (isReachedField) {
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
					NotificationType.ATTRIBUTED_RESPONSIBLE
				);
			}
			if (isGoal) {
				if (managerChanged) {
					this.helper.sendGoalNotification(
						manager,
						this.domain.get(),
						existentLevelInstance,
						null,
						url,
						NotificationType.ATTRIBUTED_RESPONSIBLE
					);
				} else {
					StructureLevelInstance parent = this.helper
						.retrieveLevelInstance(existentLevelInstance.getParent());
					this.helper.sendGoalNotification(
						manager,
						this.domain.get(),
						existentLevelInstance,
						parent,
						url,
						NotificationType.GOAL_ATTRIBUTE_UPDATED
					);
				}	
			}

			AttributeInstance attrInst = null;
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
		} catch (

		Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Deletar uma instância de um level.
	 * 
	 * @param levelInstance
	 *            Instância a ser deletada.
	 * @return existentLevelInstance Instância que foi deletada.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance/delete")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteLevelAttributesInstance(@RequestBody StructureLevelInstanceDto dto) {
		try {
			StructureLevelInstance existentLevelInstance = this.helper.retrieveLevelInstance(dto.levelInstance().getId());

			if (existentLevelInstance == null) {
				return this.fail("Estrutura incorreta!");
			}

			StructureLevelInstance.StructureLevelInstancePermissionInfo permissionInfo = bs
					.getStructureInstancePermissionInfo(existentLevelInstance);

			if (!permissionInfo.hasPermissionToAdd()) {
				return this.fail("Você não possui permissão de acesso");
			}
			
			StructureLevelInstance indicator = this.bs.isAggregating(existentLevelInstance);
			if (indicator != null) {
				return this.fail("Não pode ser excluído, está agregado ao indicador " + indicator.getName() + ".");
			}
			if(this.bs.checkHaveBudgetByLevel(existentLevelInstance.getLevel())){
				List<Budget> budgetList = this.budgetBS.listBudgetByLevelInstance(existentLevelInstance);
				for (Budget budget : budgetList) {
					BudgetElement budgetElement = this.budgetBS.budgetElementExistsById(budget.getBudgetElement().getId());
					if(budgetElement != null){
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
			return this.success(existentLevelInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar instâncias dos leveis filhos do level especificado.
	 * 
	 * @param parentId
	 *            Id do level pai.
	 * @return levelInstance Instância do level pai com uma lista de instâncias
	 *         dos leveis filhos.
	 */
	@GetMapping(BASEPATH + "/structure/levelinstance/levelsons")
	public ResponseEntity<?> levelInstanceSons(
			@RequestParam Long parentId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {

		try {
			PaginatedList<StructureLevelInstance> levelInstances = this.bs.retrieveLevelInstanceSons(parentId, page,
					pageSize);
			boolean goal = false;
			for (int i = 0; i < levelInstances.getList().size(); i++) {
				goal = levelInstances.getList().get(i).getLevel().isGoal();
				levelInstances.getList().get(i).getLevel()
						.setAttributes(this.bs.retrieveLevelSonsAttributes(levelInstances.getList().get(i)));
			}
			if (goal) {
				AttributeInstance attributeInstance = this.attrHelper.retrievePolarityAttributeInstance(parentId);
				String polarity = null;
				if (attributeInstance != null)
					polarity = attributeInstance.getValue();
				this.bs.setGoalStatus(levelInstances.getList(), polarity);
			}
			StructureLevelInstance levelInstance = new StructureLevelInstance();
			levelInstance = this.bs.retrieveLevelInstance(parentId);
			levelInstance.setSons(levelInstances);

			BigInteger structureId = this.bs.retrieveStructureIdByLevel(levelInstance.getLevel());
			Structure structure = new Structure();
			structure.setId(structureId.longValue());
			StructureLevel nextLevel = this.bs.retrieveNextLevel(structure, levelInstance.getLevel().getSequence() + 1);
			levelInstance.setLevelSon(nextLevel);

			return this.success(levelInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Listar metas a partir de filtros de pesquisa.
	 * @param term
	 *            Id da instância de um level pai.
	 * @return list Lista de instâncias dos leveis filhos.
	 */
	@GetMapping(BASEPATH + "/structure/filtergoals")
	public ResponseEntity<?> filterGoals(
			@RequestParam(required = false) Long parentId,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String attributesToFilter,
			@RequestParam(required = false) Integer progressStatusId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {

		try {
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
	 *            Id da instância de um level meta.
	 * @param openCloseGoal
	 *            Valor para fechar ou reabrir a meta.
	 * @return levelInstance Instância fechada ou reaberta.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance/closegoal")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> closeLevelGoal(@RequestBody UpdateGoalDto dto) {
		try {
			Long id = dto.id();
			Boolean openCloseGoal = dto.openCloseGoal();
			String url = dto.url();
			
			Long managerId = null;
			Long responsibleId = null;
			Long goalManagerId = null;
			
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(id);
			levelInstance.setClosed(dto.openCloseGoal());
			levelInstance.setClosedDate(new Date());
			this.bs.persist(levelInstance);
			
			StructureLevelInstance levelInstanceParent = this.bs.retrieveLevelInstance(levelInstance.getParent());
			PaginatedList<Attribute> attributes = this.bs.listAttributes(levelInstanceParent.getLevel(),false);
			PaginatedList<Attribute> attributesGoal = this.bs.listAttributes(levelInstance.getLevel(),false);
			
			for (Attribute attr : attributes.getList()) {
				if (attr.getType().equals(ResponsibleField.class.getCanonicalName()) || attr.getType().equals(ManagerField.class.getCanonicalName())) {
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
							:NotificationType.GOAL_OPENED);
			}
			
			if (responsibleId != null) {
				if(!responsible.equals(manager)) {
					this.helper.sendGoalNotification(
							responsible,
							this.domain.get(),
							levelInstance,
							parent,
							auxUrl,
							openCloseGoal ? NotificationType.GOAL_CLOSED
								:NotificationType.GOAL_OPENED);
				}
			}	
			
			if (goalManager != null) {
				if(!goalManager.equals(manager) && !goalManager.equals(responsible)) {
					this.helper.sendGoalNotification(
							goalManager,
							this.domain.get(),
							levelInstance,
							parent,
							auxUrl,
							openCloseGoal ? NotificationType.GOAL_CLOSED
								:NotificationType.GOAL_OPENED);
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
	 *            ID do usuário a ter os dados recuperados
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
	 *            justification Id da instância de um level meta.
	 * @param reached
	 *            Valor do campo justificativa, valor do campo alcançado.
	 * @return structure Instância com os atributos preenchidos.
	 */
	@PostMapping(BASEPATH + "/structure/levelinstance/updategoal")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> updateLevelGoal(@RequestBody UpdateGoalDto dto) {
		// String justification
		boolean sucess = false;
		// AttributeInstance justificativa = null;
		AttributeInstance alcancado = null;
		List<AttributeInstance> atributosInstance = new ArrayList<AttributeInstance>();
		List<Attribute> attr = new ArrayList<Attribute>();
		List<StructureLevelInstance> instances = new ArrayList<StructureLevelInstance>();
		Attribute atribute = new Attribute();
		// atribute = new Attribute();
		atribute.setId(1l);
		attr.add(atribute);
		List<Attribute> attributeList = new ArrayList<Attribute>();
		List<AttributeInstance> reacheadField = new ArrayList<AttributeInstance>();

		try {
			String reached = dto.reached();
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.id());

			if (levelInstance == null) {
				return this.fail("Estrutura incorreta!");
			} else if (levelInstance.isClosed()) {
				return this.fail("Esta meta já foi concluída!");
			} else {
				attributeList = this.bs.retrieveLevelAttributes(levelInstance.getLevel());

				for (int i = 0; i < attributeList.size(); i++) {
					/*
					 * if (attributeList.get(i).isJustificationField()) {
					 * 
					 * justificativa =
					 * this.attrHelper.retrieveAttributeInstance(levelInstance,
					 * attributeList.get(i));
					 * 
					 * if (justificativa == null) { AttributeInstance
					 * justificativaEdit = new AttributeInstance();
					 * justificativaEdit.setAttribute(attributeList.get(i));
					 * justificativaEdit.setLevelInstance(levelInstance);
					 * justificativaEdit.setValue(justification);
					 * this.bs.persist(justificativaEdit);
					 * atributosInstance.add(justificativaEdit); sucess = true;
					 * } else {
					 * justificativa.setAttribute(attributeList.get(i));
					 * justificativa.setLevelInstance(levelInstance);
					 * justificativa.setValue(justification);
					 * attributeList.get(i).setAttributeInstance(justificativa);
					 * this.bs.persist(justificativa); sucess = true; }
					 * 
					 * }
					 */

					if (attributeList.get(i).isReachedField()) {
						alcancado = this.attrHelper.retrieveAttributeInstance(levelInstance, attributeList.get(i));
						if (alcancado == null) {
							AttributeInstance alcancadoEdit = new AttributeInstance();
							alcancadoEdit.setAttribute(attributeList.get(i));
							alcancadoEdit.setLevelInstance(levelInstance);
							alcancadoEdit.setValue(reached);
							if (!reached.isEmpty()) {
								alcancadoEdit.setValueAsNumber(Double.parseDouble(reached));
							}
							attributeList.get(i).setAttributeInstance(alcancadoEdit);
							reacheadField.add(alcancadoEdit);
							this.bs.persist(alcancadoEdit);
							atributosInstance.add(alcancadoEdit);
							sucess = true;

						} else {
							alcancado.setAttribute(attributeList.get(i));
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
					List<AttributeInstance> attrInstance = new ArrayList<AttributeInstance>();
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
				// levelInstance.getLevel().setAttributes(attr);
				// levelInstance.getLevel().getAttributes().get(0).setAttributeInstances(atributosInstance);
				List<Attribute> attributes = this.bs.retrieveLevelAttributes(levelInstance.getLevel());
				attributes = this.bs.setAttributesInstances(levelInstance, attributes);
				levelInstance.getLevel().setAttributes(attributes);
				if (sucess) {
					this.bs.updateLevelValues(levelInstance); // erro do
																// arredondamento
																// aquig

					return this.success(levelInstance);
				}
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
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterAxes(excludedIds, page, pageSize, term);
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
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterIndicators(excludedIds, page, pageSize, term);
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
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterGoals(excludedIds, page, pageSize, term);
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
	 *            Id do plano macro.
	 * @param planId
	 *            Id do plano de metas.
	 * @return list Lista de instâncias do level objetivo.
	 */
	@GetMapping(BASEPATH + "/structure/objectives")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> listObjective(
			@RequestParam(required = false) Long macroId,
			@RequestParam(required = false) Long planId) {

		PlanMacro planMacro = null;
		Plan plan2 = null;
		try {
			planMacro = this.planBS.retrievePlanMacroById(macroId);
			plan2 = this.planBS.retrieveById(planId);
			PaginatedList<StructureLevelInstance> list = this.bs.listObjective(planMacro, plan2);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	/**
	 * Listar instâncias do level objetivo pelo plano macro e/ou plano de metas para link com risco.
	 * 
	 * @param macroId
	 *            Id do plano macro.
	 * @param planId
	 *            Id do plano de metas.
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
			PaginatedList<StructureLevelInstance> list = structureInstanceFilterBS.filterObjectives(excludedIds, page, pageSize, term);
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
	public ResponseEntity<?> listPdiLinkedToRisks(@RequestParam Long unitId) {
		try {
			Unit unit = this.unitBS.exists(unitId, Unit.class);
			if (unit == null || unit.isDeleted()) {
				return this.fail("Unidade não encontrada");
			}
			
			List<StructureLevelInstance> list = structureInstanceFilterBS.listPdiLinkedToRisks(unit);
			
			return this.success(new ListWrapper<StructureLevelInstance>(list));
		} catch (Throwable ex) {
			LOGGER.error("", ex);
			return this.fail(ex.getMessage());
		}
	}

	@PostMapping(BASEPATH + "/structure/levelinstance/goalsgenerate")
	public ResponseEntity<?> goalsGenerate(@RequestBody GoalsGenerateDto dto) {
		try {
			Plan returnList = new Plan();
			String periodicity = null;
			String begin = null;
			String end = null;
			Date beginDate = null;
			Date endDate = null;
			StructureLevelInstance indicator = this.bs.retrieveLevelInstance(dto.indicatorId());
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
				throw new IllegalStateException("È necessário preencher a periodicidade do indicador antes de gerar as metas");
			}
			
			if (periodicity != null && beginDate != null && endDate != null && dto.name() != null) {
				Long parent = indicator.getId();
				Plan plan = indicator.getPlan();
				BigInteger structureId = this.bs.retrieveStructureIdByLevel(indicator.getLevel());
				Structure structure = new Structure();
				structure.setId(structureId.longValue());
				StructureLevel level = this.bs.retrieveNextLevel(structure, indicator.getLevel().getSequence() + 1);
				level.setAttributes(this.bs.retrieveLevelAttributes(level));
				this.bs.goalsGenerateByPeriodicity(parent, plan, level, dto.name(), dto.manager(), dto.responsible(),
						dto.description(), dto.expected(), dto.minimum(), dto.maximum(), periodicity, beginDate, endDate);
			} else {
				return this.fail("Dados invalidos.");
			}
			return this.success(returnList);
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
	 *            Id do plano macro.
	 * @param planId
	 *            Id do plano de metas.
	 * @return list Lista de instâncias de um level indicador.
	 */
	@GetMapping(BASEPATH + "/structure/indicatorsByMacroAndPlan")
	@CommunityDashboard
	public ResponseEntity<?> listIndicatorsByMacroAndPlan(
			@RequestParam(required = false) Long macroId,
			@RequestParam(required = false) Long planId) {

		PlanMacro planMacro = null;
		Plan plan2 = null;
		try {
			planMacro = this.planBS.retrievePlanMacroById(macroId);
			plan2 = this.planBS.retrieveById(planId);
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
	 *            Id do plano de metas.
	 * @param parent
	 *            Id da instância de um level pai.
	 * @return list Lista de instâncias dos leveis filhos.
	 */
	@GetMapping(BASEPATH + "/structure/levelsonsfilter")
	@CommunityDashboard
	public ResponseEntity<?> listLevelSonsForFilter(
			@RequestParam(required = false) Long planId,
			@RequestParam(required = false) Long parent) {

		Plan plan = null;
		try {
			if (planId != null)
				plan = this.planBS.retrieveById(planId);
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
	 *            lista dos IDs das metas.
	 */
	@PostMapping(BASEPATH + "/structure/deleteGoals")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteManyGoals(@RequestBody DeleteGoalsDto dto) {

		try {
			var list = dto.list();
			for (int i = 0; i < list.getTotal(); i++) {
				Long id = list.getList().get(i).longValue();
				StructureLevelInstance goal = this.bs.retrieveLevelInstance(id);
				if (goal == null) {
					return this.fail("Meta com id " + id + " inválido.");
				}
				goal.setDeleted(true);
				this.bs.persist(goal);

				PaginatedList<FavoriteLevelInstance> favorites = this.bs.listFavoriteByLevelInstance(goal);
				for (FavoriteLevelInstance favorite : favorites.getList()) {
					favorite.setDeleted(true);
					this.bs.persist(favorite);
				}
			}
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Adicionar um level aos favoritos
	 * 
	 * @param levelInstanceId,
	 *            Id de uma instância de um level.
	 */
	@PostMapping(BASEPATH + "/structure/savefavorite")
	public ResponseEntity<?> saveFavoriteLevelInstance(@RequestBody LevelInstanceIdDto dto) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.levelInstanceId());
			PaginatedList<FavoriteLevelInstance> favorites = this.bs
					.listFavoriteLevelInstances(levelInstance.getPlan().getParent());
			if (favorites.getList().size() < 200) {
				FavoriteLevelInstance favorite = this.bs.retrieveFavoriteLevelInstance(levelInstance);
				if (favorite == null) {
					favorite = this.bs.saveFavoriteLevelInstance(levelInstance);
				} else {
					favorite.setDeleted(false);
					this.bs.persist(favorite);
				}
				return this.success(favorite);
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
	 *            Id de uma instância de um level.
	 */
	@PostMapping(BASEPATH + "/structure/removefavorite")
	public ResponseEntity<?> removeFavoriteLevelInstance(@RequestBody LevelInstanceIdDto dto) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.levelInstanceId());
			FavoriteLevelInstance favorite = this.bs.retrieveFavoriteLevelInstance(levelInstance);
			if (favorite != null) {
				favorite.setDeleted(true);
				this.bs.persist(favorite);
			}
			return this.success(favorite);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar os leveis favoritos por usuário e empresa
	 * 
	 * @param levelInstanceId,
	 *            Id de uma instância de um level.
	 */
	@GetMapping(BASEPATH + "/structure/listfavorites")
	public ResponseEntity<?> listFavoriteLevelInstance(@RequestParam Long macroId) {
		try {
			PlanMacro macro = this.planBS.retrievePlanMacroById(macroId);
			PaginatedList<FavoriteLevelInstance> favorites = this.bs.listFavoriteLevelInstances(macro);
			return this.success(favorites);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar o leveis indicadores que estão agregados a um level indicador
	 * agregado.
	 * 
	 * @param aggregate
	 *            Instância de um level indicador agregado.
	 * 
	 */
	@GetMapping(BASEPATH + "/structure/listaggregates")
	public ResponseEntity<?> listAggregates(@RequestParam Long id) {
		try {
			StructureLevelInstance indicator = this.helper.retrieveLevelInstance(id);
			if (indicator.isAggregate()) {
				PaginatedList<AggregateIndicator> list = new PaginatedList<AggregateIndicator>();
				list.setList(this.bs.listAggregateIndicatorsByAggregate(indicator));
				list.setTotal(new Long(this.bs.listAggregateIndicatorsByAggregate(indicator).size()));
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
	 *            Id da instância de nível.
	 *            
	 */
	@PostMapping("/api/structure/levelinstance/setvisualized")
	public ResponseEntity<?> setVisualized(@RequestBody LevelInstanceIdDto dto) {
		try {
			StructureLevelInstance levelInstance = this.bs.retrieveLevelInstance(dto.levelInstanceId());
			
			levelInstance.setVisualized(true);
			
			this.bs.persist(levelInstance);
			return this.success(levelInstance);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
	
	@GetMapping(BASEPATH + "/structure/infotable")
	public ResponseEntity<?> InfoTable(@ModelAttribute PDIFilterParams filterParams) {
		try {
			filterParams.validateParams();

			PlanMacro planMacro = filterParams.getMacro() != null
					? this.planBS.retrievePlanMacroById(filterParams.getMacro())
					: null;
			Plan planObj = filterParams.getPlan() != null
					? this.planBS.retrieveById(filterParams.getPlan())
					: null;
			Long levelInstance = filterParams.getLevelInstance();
			Long goalId = filterParams.getGoalId();
			String startDateStr = filterParams.getStartDate();
			String endDateStr = filterParams.getEndDate();
			Boolean goalStatus = filterParams.getGoalStatus();
			Integer progressStatus = filterParams.getProgressStatus();

			PaginatedList<GoalsInfoTable> goalsList = this.dashboardBS.getInfoTable(
					planMacro,
					planObj,
					levelInstance,
					goalId,
					filterParams.getPage(),
					filterParams.getPageSize(),
					startDateStr,
					endDateStr,
					goalStatus,
					progressStatus);

			return this.success(goalsList);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
	
	@GetMapping(BASEPATH + "/structure/exportPDF")
	public ResponseEntity<?> exportPDF(@ModelAttribute PDIFilterParams filterParams) {

		filterParams.validateParams();

		OutputStream outputStream = null;
		InputStream inputStream = null;

		try {
			String planName = "";
			String subplanName = "";
			String thematicAxisName = "Não preenchido";

			Plan planObj = this.planBS.retrieveById(filterParams.getPlan());
			planName = planObj.getName();

			PlanMacro planMacro = planObj.getParent();

			if (planObj.getParent() != null) {
				subplanName = planObj.getParent().getName();
			} else {
				subplanName = "Sem Subplano";
			}

			StructureLevelInstance currentInstance = this.helper.retrieveLevelInstance(filterParams.getLevelInstance());

			if (currentInstance == null) {
				return this.fail("O nível especificado não foi encontrado.");
			}

			StructureLevelInstance goalInstance = null;
			StructureLevelInstance indicatorInstance = null;
			StructureLevelInstance objectiveInstance = null;
			StructureLevelInstance thematicAxisInstance = null;

			StructureLevelInstance tempInstance = currentInstance;

			while (tempInstance != null) {
				StructureLevel level = tempInstance.getLevel();

				if (level.isGoal()) {
					goalInstance = tempInstance;
				} else if (level.isIndicator()) {
					indicatorInstance = tempInstance;
				} else if (level.isObjective()) {
					objectiveInstance = tempInstance;
				} else {
					thematicAxisInstance = tempInstance;
				}

				if (tempInstance.getParent() != null) {
					tempInstance = this.helper.retrieveLevelInstance(tempInstance.getParent());
				} else {
					tempInstance = null;
				}
			}

			if (thematicAxisInstance == null) {
				return this.fail("Eixo Temático não encontrado na hierarquia.");
			}

			thematicAxisName = thematicAxisInstance.getName();

			PaginatedList<GoalsInfoTable> goalsLists = this.dashboardBS.getCommunityInfoTable(null, planObj,
					currentInstance.getId(), 1,
					Integer.MAX_VALUE);

			inputStream = goalsReportGenerator.generateGoalsReport(
					"Relatório Plano de Desenvolvimento Institucional",
					goalsLists.getList(),
					planMacro,
					planObj,
					thematicAxisInstance,
					objectiveInstance,
					indicatorInstance,
					goalInstance,
					planName,
					subplanName,
					thematicAxisName);

			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"Relatório_de_metas.pdf\"");
			outputStream = this.response.getOutputStream();

			IOUtils.copy(inputStream, outputStream);
			outputStream.flush();

			return this.nothing();

		} catch (Exception ex) {
			LOGGER.error("Erro ao gerar o relatório em PDF.", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		} finally {
			Util.closeFile(inputStream);
			Util.closeFile(outputStream);
		}
	}
}
