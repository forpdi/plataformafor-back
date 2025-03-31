package org.forpdi.planning.fields;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.forpdi.core.bean.IdDto;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.DateUtil;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.attachment.Attachment;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetBS;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.fields.budget.BudgetElement;
import org.forpdi.planning.fields.dto.ActionPlanDto;
import org.forpdi.planning.fields.dto.AttachmentDto;
import org.forpdi.planning.fields.dto.AttachmentListDto;
import org.forpdi.planning.fields.dto.BudgetFieldDto;
import org.forpdi.planning.fields.dto.FieldBudgetUpdateDto;
import org.forpdi.planning.fields.dto.ScheduleDto;
import org.forpdi.planning.fields.dto.ScheduleToSaveDto;
import org.forpdi.planning.fields.dto.TableFieldsDto;
import org.forpdi.planning.fields.dto.TableFieldsToSaveDto;
import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.schedule.ScheduleInstance;
import org.forpdi.planning.fields.schedule.ScheduleStructure;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.fields.table.TableInstance;
import org.forpdi.planning.fields.table.TableStructure;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FieldsController extends AbstractController {

	@Autowired
	private FieldsBS bs;
	@Autowired
	private StructureBS structureBs;
	@Autowired
	private CompanyBS companyBs;
	@Autowired
	private BudgetBS budgetElementBs;
	@Autowired
	private UserBS userBs;

	/**
	 * Salvar um novo orçamento no banco de dados, relacionando a uma instancia
	 * de nível.
	 * 
	 * @param name,
	 *            nome do orçamento
	 * @param subAction,
	 *            sub ação orçamentaria
	 * @param instanceId,
	 *            id da instancia de nível
	 * 
	 * @return item, dto do orçamento salvo, com os valores conforme a sub ação
	 */
	@PostMapping(BASEPATH + "/field/budget")
	public ResponseEntity<?> save(@RequestBody BudgetFieldDto dto) {
		Double committedD = 0d;

		try {
			StructureLevelInstance instance = this.structureBs.retrieveLevelInstance(dto.instanceId());
			BudgetElement budgetElement = this.budgetElementBs.budgetElementExistsById(dto.subAction());

			if (instance == null) {
				return this.fail("Estrutura inválida!");
			}

			if (budgetElement == null) {
				return this.fail("Sub ação inválida!");
			}

			Budget budget = new Budget();
			budget.setSubAction(budgetElement.getSubAction());
			budget.setName(dto.name());

			if (dto.committed() != null) {
				String numberCommitted = dto.committed();
				String numberFormated = numberCommitted.replace(",", ".");
				committedD = Double.parseDouble(numberFormated);

				if (committedD > budgetElement.getBudgetLoa()) {
					return this.fail("Valor empenhado não pode ser maior que o valor do orçamento LOA!");
				} else {
					if (committedD > budgetElement.getBalanceAvailable()) {
						return this.fail("Valor do empenhado não pode ser maior que o saldo disponível!");
					} else {
						budget.setCommitted(committedD);
						double balanceAvailable = budgetElement.getBalanceAvailable();
						balanceAvailable -= committedD;
						budgetElement.setBalanceAvailable(balanceAvailable);
					}

				}
			}

			if (!GeneralUtils.isEmpty(dto.realized())) {
				String numberRealized = dto.realized();
				String numberFormated = numberRealized.replaceAll(",", ".");
				Double realizedD = Double.parseDouble(numberFormated);

				if (realizedD > committedD) {
					return this.fail("Valor do realizado não pode ser maior que o valor do empenhado!");
				} else {
					budget.setRealized(realizedD);
				}
			} else {
				budget.setRealized(0.0);
			}

			budget.setBudgetElement(budgetElement);
			budget.setLevelInstance(instance);

			Long linkedObjects = budgetElement.getLinkedObjects();
			linkedObjects += 1;
			budgetElement.setLinkedObjects(linkedObjects);

			this.budgetElementBs.update(budgetElement);

			this.bs.saveBudget(budget);

			BudgetDTO item = new BudgetDTO();
			item.setBudget(budget);
			item.setBudgetLoa(budgetElement.getBudgetLoa());
			item.setBalanceAvailable(budgetElement.getBalanceAvailable());

			return this.success(item);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualizar um orçamento existente no banco de dados.
	 * 
	 * @param name,
	 *            novo nome do orçamento
	 * @param subAction,
	 *            nova sub ação orçametaria
	 * @param id,
	 *            referente ao orçamento existente
	 * 
	 * @return item, dto do orçamento atualizado
	 */
	@PostMapping(BASEPATH + "/field/budget/update")
	public ResponseEntity<?> update(@RequestBody FieldBudgetUpdateDto dto) {
		try {
			Budget budget = this.bs.budgetExistsById(dto.id());
			BudgetElement budgetElement = this.budgetElementBs.budgetElementExistsById(dto.idBudgetElement());

			if (budget == null) {
				LOGGER.error("Orçamento inexistente para ser editado.");
				return this.fail("Orçamento inválido.");
			}

			if (budgetElement == null) {
				return this.fail("Sub ação inválida!");
			}

			budget.setName(dto.name());
			budget.setSubAction(budgetElement.getSubAction());

			Double committed = dto.committed();
			Double realized = dto.realized();
			
			double budgetBalanceAvailable = budgetElement.getBalanceAvailable();
			if (!budget.getBudgetElement().getId().equals(budgetElement.getId())) {
				BudgetElement budgetElementUpdate = budget.getBudgetElement();
				Double balanceAvaliableUpdate = budgetElementUpdate.getBalanceAvailable();
				Double newBudgetBalanceAvaliableUpdate = budgetElement.getBalanceAvailable();
				if (committed <= newBudgetBalanceAvaliableUpdate) {
					balanceAvaliableUpdate += budget.getCommitted();
					budgetElementUpdate.setBalanceAvailable(balanceAvaliableUpdate);
					Long linkedObjects = budgetElementUpdate.getLinkedObjects();
					linkedObjects -= 1;
					budgetElementUpdate.setLinkedObjects(linkedObjects);
					this.budgetElementBs.update(budgetElementUpdate);

					newBudgetBalanceAvaliableUpdate -= committed;
					budgetElement.setBalanceAvailable(newBudgetBalanceAvaliableUpdate);
					Long newLinkedObjects = budgetElement.getLinkedObjects();
					newLinkedObjects += 1;
					budgetElement.setLinkedObjects(newLinkedObjects);
					this.budgetElementBs.update(budgetElement);
				} else {
					return this.fail("Valor empenhado não pode ser maior que o valor do saldo disponível!");
				}
			} else if (committed != null && committed <= budgetBalanceAvailable + budget.getCommitted()) {
				LOGGER.info("committed: " + committed + " budgetBalanceAvailable + budget.getCommitted(): " + budgetBalanceAvailable + budget.getCommitted());
				budgetBalanceAvailable += budget.getCommitted();
				budgetBalanceAvailable -= committed;
				budgetElement.setBalanceAvailable(budgetBalanceAvailable);
				LOGGER.info("budgetBalanceAvailable: " + budgetBalanceAvailable);
				this.budgetElementBs.update(budgetElement);
			} else {
				return this.fail("Valor empenhado não pode ser maior que o valor do saldo disponível!");
			}

			budget.setCommitted(committed);
			budget.setBudgetElement(budgetElement);

			if (realized != null) {
				if (realized > committed) {
					return this.fail("Valor do realizado não pode ser maior que o valor do empenhado!");
				} else {
					budget.setRealized(realized);
				}

			}

			this.bs.update(budget);
			BudgetDTO item = new BudgetDTO();
			item.setBudget(budget);
			item.setBudgetLoa(budgetElement.getBudgetLoa());
			item.setBalanceAvailable(budgetElement.getBalanceAvailable());
			return this.success(item);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Excluir um orçamento existente no banco de dados
	 * 
	 * @param id,
	 *            referente ao orçamento a ser excluido
	 * @return budget, orçamento excluído
	 */
	@PostMapping(BASEPATH + "/field/budget/delete")
	public ResponseEntity<?> deleteBudget(@RequestBody FieldBudgetUpdateDto dto) {
		try {
			Budget budget = this.bs.budgetExistsById(dto.id());
			BudgetElement budgetElement = this.budgetElementBs.budgetElementExistsById(dto.idBudgetElement());

			if (dto.committed() != null) {
				double balanceAvailable = budgetElement.getBalanceAvailable();
				balanceAvailable += dto.committed();
				budgetElement.setBalanceAvailable(balanceAvailable);
				budgetElement.setLinkedObjects(budgetElement.getLinkedObjects() - 1);
				this.budgetElementBs.update(budgetElement);
			}

			this.bs.deleteBudget(budget);
			return this.success(budget);
		} catch (Throwable e) {
			LOGGER.error("Erro ao deletar o orçamento", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar um novo cronograma.
	 * 
	 * @param scheduleInstance,
	 *            instancia do cronograma a ser salva
	 * @param scheduleId,
	 *            id do atributo cronograma
	 * @param beginDate,
	 *            data de início
	 * @param endDate,
	 *            data de fim
	 * @return existentScheduleInstance, instancia do cronograma que foi salvo
	 */
	@PostMapping(BASEPATH + "/field/schedule")
	public ResponseEntity<?> saveSchedule(@RequestBody ScheduleDto dto) {
		try {
			ScheduleInstance scheduleInstance = dto.scheduleInstance();
			String beginDate = dto.beginDate();
			String endDate = dto.endDate();
			
			if (beginDate != null && endDate != null) {
				DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				scheduleInstance.setBegin((Date) formatter.parse(beginDate));
				scheduleInstance.setEnd((Date) formatter.parse(endDate));
			} else {
				return this.fail("As datas devem ser preenchidas!");
			}
			Schedule schedule = this.bs.retrieveSchedule(dto.scheduleId());
			if (schedule == null) {
				return this.fail("Cronograma inválido!");
			}
			ScheduleInstance existentScheduleInstance = new ScheduleInstance();
			if (scheduleInstance.getId() != null && scheduleInstance.getId() != 0)
				existentScheduleInstance = this.bs.retrieveScheduleInstance(scheduleInstance.getId());
			else
				existentScheduleInstance.setNumber(this.bs.getScheduleInstanceNumber(schedule));
			if (scheduleInstance.getDescription().length() > 4000) {
				return this.fail("Texto muito longo para descrição (Limite 4000 caracteres)");
			}
			existentScheduleInstance.setDescription(scheduleInstance.getDescription());
			existentScheduleInstance.setBegin(scheduleInstance.getBegin());
			existentScheduleInstance.setEnd(scheduleInstance.getEnd());
			existentScheduleInstance.setPeriodicity(scheduleInstance.getPeriodicity());
			existentScheduleInstance.setSchedule(schedule);
			this.bs.persist(existentScheduleInstance);
			this.bs.saveScheduleValues(existentScheduleInstance, scheduleInstance.getScheduleValues());
			existentScheduleInstance.setScheduleValues(scheduleInstance.getScheduleValues());
			return this.success(existentScheduleInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Deletar um cronograma do banco de dados
	 * 
	 * @param id,
	 *            referente ao cronograma a ser excluído
	 * @return scheduleInstance, que foi excluído do banco de dados
	 */
	@PostMapping(BASEPATH + "/field/schedule/delete")
	public ResponseEntity<?> deleteScheduleInstance(@RequestBody IdDto dto) {
		try {
			ScheduleInstance scheduleInstance = this.bs.retrieveScheduleInstance(dto.id());
			this.bs.deleteScheduleInstance(scheduleInstance);
			return this.success(scheduleInstance);
		} catch (Throwable e) {
			LOGGER.error("Erro ao deletar o orçamento", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar um novo campo de um dado atributo tabela
	 * 
	 * @param tableInstance,
	 *            instancia da tabela que será inserido o campo
	 * @param tableFieldsId,
	 *            id do campo da tabela
	 * @return existentTableInstance, instancia do campo da tabela que foi salvo
	 *         no banco
	 */
	@PostMapping(BASEPATH + "/field/tableFields")
	public ResponseEntity<?> saveTableFields(@RequestBody TableFieldsDto dto) {
		try {
			TableInstance tableInstance = dto.tableInstance();
			Long tableFieldsId = dto.tableFieldsId();
			TableFields tableFields = this.bs.retrieveTableFields(tableFieldsId);
			if (tableFields == null) {
				return this.fail("Tabela inválida!");
			}
			TableInstance existentTableInstance = new TableInstance();
			if (tableInstance.getId() != null && tableInstance.getId() != 0)
				existentTableInstance = this.bs.retrieveTableInstance(tableInstance.getId());
			existentTableInstance.setTableFields(tableFields);
			this.bs.persist(existentTableInstance);
			this.bs.saveTableValues(existentTableInstance, tableInstance.getTableValues());
			existentTableInstance.setTableValues(tableInstance.getTableValues());
			existentTableInstance.setTableFieldsId(tableFieldsId);
			return this.success(existentTableInstance);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar colunas de um atributo tabela
	 * 
	 * @param tableFields,
	 *            tabela a ser inserida essa coluna
	 * @return tableFields
	 */
	@PostMapping(BASEPATH + "/field/tableFields/structures")
	public ResponseEntity<?> saveTableColumns(@RequestBody TableFieldsToSaveDto dto) {
		try {
			TableFields tableFields = dto.tableFields();
			if (tableFields == null) {
				return this.fail("Tabela não existente.");
			}
			for (TableStructure structure : tableFields.getTableStructures()) {
				TableStructure structure2 = new TableStructure();
				structure2.setDeleted(false);
				structure2.setInTotal(structure.isInTotal());
				structure2.setLabel(structure.getLabel());
				structure2.setType(structure.getType());
				structure2.setTableFields(tableFields);
				this.bs.persist(structure2);
				structure.setId(structure2.getId());
			}
			return this.success(tableFields);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Excluir uma instancia da tabela
	 * 
	 * @param id,
	 *            referente a instance da tabela a ser excluida
	 * @return tableInstance, instancia da tabela que foi excluida
	 */
	@PostMapping(BASEPATH + "/field/tableFields/delete")
	public ResponseEntity<?> deleteTableInstance(@RequestBody IdDto dto) {
		try {
			TableInstance tableInstance = this.bs.retrieveTableInstance(dto.id());
			tableInstance.setTableFieldsId(tableInstance.getTableFields().getId());
			this.bs.deleteTableInstance(tableInstance);
			return this.success(tableInstance);
		} catch (Throwable e) {
			LOGGER.error("Erro ao deletar o orçamento", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salvar um novo plano de ação no banco de dados
	 * 
	 * @param actionPlan,
	 *            plano de ação
	 * @param levelInstanceId,
	 *            id da instancia do nível
	 * @param begin,
	 *            data de inicio em String
	 * @param end,
	 *            data de fim em String
	 * @return actionPlan
	 */
	@PostMapping(BASEPATH + "/field/actionplan")
	public ResponseEntity<?> saveAction(@RequestBody ActionPlanDto dto) {
		try {
			ActionPlan actionPlan = dto.actionPlan();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				actionPlan.setBegin(format.parse(dto.begin()));
				actionPlan.setEnd(format.parse(dto.end()));
			} catch (ParseException e1) {
				throw new IllegalArgumentException("Erro ao converter data do action plan", e1);
			}

			StructureLevelInstance levelInstance = this.structureBs.retrieveLevelInstance(dto.levelInstanceId());
			if (levelInstance == null) {
				return this.fail("Estrutura inválida!");
			}

			Plan planToVerifyDate = levelInstance.getPlan();

			Date planDateBegin = planToVerifyDate.getBegin();
			Date planDateEnd = planToVerifyDate.getEnd();

			if (!DateUtil.isBetween(actionPlan.getBegin(), planDateBegin, planDateEnd) || 
				!DateUtil.isBetween(actionPlan.getEnd(), planDateBegin, planDateEnd)) {
				return this.fail("O plano de ação deve estar dentro do intervalo de Início e Fim do Plano de metas.");
			}

			User user = this.userBs.existsByUser(actionPlan.getUser().getId());

			ActionPlan.ActionPlanPermissionInfo permissionInfo = bs.getActionPlanPermissionInfo(user, levelInstance);
			if (!permissionInfo.hasPermission()) {
				return this.fail("Usuário sem permissão de acesso");
			}

			if (!userBs.userIsLinkedToCurrentCompany(user)) {
				return this.fail("Usuário não encontrado na instituição");
			}

			actionPlan.setLevelInstance(levelInstance);

			this.bs.validateActionPlanStatus(actionPlan);

			this.bs.saveActionPlan(actionPlan);

			this.bs.sendUserLinkedToActionPlanNotification(actionPlan, user);

			return this.success(actionPlan);
		} catch (Throwable e) {
			LOGGER.error("Erro ao salvar o plano de ação no banco", e);
			return this.fail(e.getMessage());
		}
	}

	/**
	 * Excluir um plano de ação do banco de dados
	 * 
	 * @param id,
	 *            referente ao plano de ação a ser excluido
	 * @return actionPlan, plano de ação que foi excluido
	 */
	@PostMapping(BASEPATH + "/field/actionplan/delete")
	public ResponseEntity<?> deleteActionPlan(@RequestBody IdDto dto) {
		try {
			ActionPlan actionPlan = this.bs.actionPlanExistsById(dto.id());
			
			ActionPlan.ActionPlanPermissionInfo permissionInfo = bs.getActionPlanPermissionInfo(actionPlan);
			
			if (permissionInfo.hasPermission()) {
				this.bs.deleteActionPlan(actionPlan);
				return this.success(actionPlan);
			}
			else {
				return this.fail("Usuário sem permissão de acesso");
			}
		} catch (Throwable e) {
			LOGGER.error("Erro ao deletar o plano de ação", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualizar um plano de ação
	 * 
	 * @param id,
	 *            referente ao plano de ação a ser atualizado
	 * @param begin,
	 *            nova data de inicio
	 * @param end,
	 *            nova data de fim
	 * @param checked,
	 *            novo atributo marcado
	 * @param responsible,
	 *            novo responsável
	 * @param description,
	 *            nova descrição
	 * 
	 * @return exist, plano de ação que foi atualizado
	 */
	@PostMapping(BASEPATH + "/field/actionplan/update")
	public ResponseEntity<?> updateActionPlan(@RequestBody ActionPlanDto dto) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			ActionPlan exist = this.bs.actionPlanExistsById(dto.id());
			if (exist == null) {
				return this.fail("Não foi possível editar. Ação inexistente.");
			}

			Plan planToVerifyDate = exist.getLevelInstance().getPlan();

			Date planDateBegin = planToVerifyDate.getBegin();
			Date planDateEnd = planToVerifyDate.getEnd();

			Date actionPlanBegin = format.parse(dto.begin());
			Date actionPlanEnd = format.parse(dto.end());

			if (!DateUtil.isBetween(actionPlanBegin, planDateBegin, planDateEnd) || 
				!DateUtil.isBetween(actionPlanEnd, planDateBegin, planDateEnd)) {
				return this.fail("O plano de ação deve estar dentro do intervalo de Início e Fim do Plano de metas.");
			}

			ActionPlan.ActionPlanPermissionInfo permissionInfo = bs.getActionPlanPermissionInfo(exist);

			if (permissionInfo.hasPermission()) {
				exist.setId(dto.id());
				exist.setBegin(actionPlanBegin);
				exist.setChecked(dto.checked());
				exist.setNotChecked(dto.notChecked());
				exist.setDescription(dto.description());
				exist.setEnd(actionPlanEnd);
				
				this.bs.validateActionPlanStatus(exist);

				boolean userChanged = false;

				if (permissionInfo.hasPermissionToUpdateResponsible()) {
					User user = this.userBs.existsByUser(dto.userId());
					if (!userBs.userIsLinkedToCurrentCompany(user)) {
						return this.fail("Usuário não encontrado na instituição");
					}

					exist.setUser(user);

					Long responsibleId = exist.getUser() != null ? exist.getUser().getId() : null;
					userChanged = responsibleId != null && !responsibleId.equals(dto.userId());
				}
				
				this.bs.saveActionPlan(exist);
				
				if (userChanged) {
					this.bs.sendUserLinkedToActionPlanNotification(exist, exist.getUser());
				}

				return this.success(exist);
			} else {
				return this.fail("Usuário sem permissão de acesso");
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Cria ligação entre um plano de ação e uma meta
	 * 
	 * @param id,
	 *            referente ao plano de ação a ser atualizado
	 * 
	 * @return exist, plano de ação que foi atualizado
	 */
	@PostMapping(BASEPATH + "/field/actionplan/linkGoal")
	public ResponseEntity<?> linkGoalActionPlan(@RequestBody ActionPlanDto dto) {

		try {
			ActionPlan exist = this.bs.actionPlanExistsById(dto.id());
			if (exist == null) {
				return this.fail("Não foi possível editar. Ação inexistente.");
			}

			StructureLevelInstance linkedGoal = this.structureBs.retrieveLevelInstanceNoDeleted(dto.linkedGoalId());
			if (linkedGoal != null && !linkedGoal.getLevel().isGoal()) {
				throw new IllegalArgumentException("Level instance isn't a goal");
			}
			
			exist.setLinkedGoal(linkedGoal);	
			this.bs.saveActionPlan(exist);
			return this.success(exist);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}
	
	/**
	 * Atualizar o atributo checado do plano de ação
	 * 
	 * @param id,
	 *            referente ao plano de ação
	 * @param checked,
	 *            atributo checado
	 * 
	 * @return exist, plano de ação atualizado
	 */
	@PostMapping(BASEPATH + "/field/actionplan/update-status")
	public ResponseEntity<?> updateStatus(@RequestBody ActionPlanDto dto) {
		try {
			ActionPlan exist = this.bs.actionPlanExistsById(dto.id());
			
			exist.setChecked(dto.checked());
			exist.setNotChecked(dto.notChecked());

			ActionPlan.ActionPlanPermissionInfo permissionInfo = bs.getActionPlanPermissionInfo(exist);
			
			if (permissionInfo.hasPermission()) {
				this.bs.validateActionPlanStatus(exist);
				
				this.bs.saveActionPlan(exist);
				return this.success(exist);
			} else {
				return this.fail("Usuário sem permissão de acesso");
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um ao tentar atualizar o checkbox: " + e.getMessage());
		}
	}
	
	/**
	 * Listar todos os orçamentos de simulação
	 * 
	 * @return list, todos as simulações de orçamento
	 */
	@GetMapping(BASEPATH + "/field/budget/budget")
	public ResponseEntity<?> listBudgetAction(@RequestParam Long companyId) {
		try {
			Company company = this.companyBs.exists(companyId, Company.class);
			if (company == null) {
				return this.fail("Empresa inválida!");
			}

			PaginatedList<BudgetElement> list = this.bs.listBudget(company);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Listar orçamentos referentes à uma instancia de nível
	 * 
	 * @param levelId,
	 *            id da instancia de nível que possui os orçamentos
	 * @return list, lista com os orçamentos daquele instancia de nível
	 */
	@GetMapping(BASEPATH + "/field/budget")
	public ResponseEntity<?> listBudgetByLevelInstance(@RequestParam Long levelId) {
		try {
			StructureLevelInstance levelInstance = this.structureBs.retrieveLevelInstance(levelId);
			PaginatedList<Budget> list = this.bs.listBudgetsByInstance(levelInstance);
			return this.success(list);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Salvar estrutura de cronograma
	 * 
	 * @param schedule,
	 *            cronograma a ser referido a estrutura
	 * @return schedule
	 */
	@PostMapping(BASEPATH + "/field/schedule/structures")
	public ResponseEntity<?> saveScheduleStructures(@RequestBody ScheduleToSaveDto dto) {
		try {
			Schedule schedule = dto.schedule();
			for (ScheduleStructure structure : schedule.getScheduleStructures()) {
				ScheduleStructure structure2 = new ScheduleStructure();
				structure2.setDeleted(false);
				structure2.setLabel(structure.getLabel());
				structure2.setSchedule(schedule);
				structure2.setType(structure.getType());
				this.bs.persist(structure2);
			}
			return this.success(schedule);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Lista com os atributos do plano de ação para serem mostrados
	 * 
	 * @param id
	 *            Id do plano
	 * @param page
	 *            Número da página
	 * @param pageSize
	 *            Tamanho da página (quantidade de registros por busca)
	 */
	@GetMapping(BASEPATH + "/field/actionplan/listActionPlanAttribute")
	public ResponseEntity<?> listActionPlanAttribute(
			@RequestParam Long id,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String description,
			@RequestParam(required = false) Boolean checked,
			@RequestParam(required = false) Boolean notChecked,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false, value = "sortedBy[]")  List<String> sortedBy) {

		PaginatedList<ActionPlan> actionPlansPaginated = new PaginatedList<ActionPlan>();
		ArrayList<ActionPlan> actionPlans = new ArrayList<>();

		try {

			StructureLevelInstance levelInstance = this.structureBs.retrieveLevelInstance(id);
			if (levelInstance == null) {
				return this.fail("Estrutura incorreta!");
			} else {
				List<Attribute> attributeList = this.structureBs.retrieveLevelAttributes(levelInstance.getLevel());
				PaginatedList<Attribute> attributeListPagined = new PaginatedList<Attribute>();
				attributeListPagined.setList(attributeList);

				attributeListPagined = this.structureBs.setActionPlansAttributes(levelInstance, attributeListPagined, page,
					pageSize, description, checked, notChecked, year, sortedBy);

				for (Attribute attribute : attributeListPagined.getList()) {
					if (attribute.getActionPlans() != null) {
						for (ActionPlan actionPlan : attribute.getActionPlans()) {
							actionPlans.add(actionPlan);
						}
					}
				}

				actionPlansPaginated.setList(actionPlans);
				actionPlansPaginated.setTotal(attributeListPagined.getTotal());

				return this.success(actionPlansPaginated.getList(), actionPlansPaginated.getTotal());

			}

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Retorna um único plano de ação pelo id
	 * 
	 * @param id
	 *            Id do plano de ação
	 * @return ActionPlan Retorna o plano de ação de acordo com o id passado.
	 */
	@GetMapping(BASEPATH + "/field/actionplan/{id}")
		public ResponseEntity<?> actionPlanId(@PathVariable Long id) {
		try {
			ActionPlan actionPlan = this.bs.actionPlanExistsById(id);
			if (actionPlan == null) {
				return this.fail("O plano de ação não foi encontrado");
			} else {
				return this.success(actionPlan);
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + e.getMessage());
		}
	}

	/**
	 * Salvar anexo
	 * 
	 * @param attachment,
	 *            anexo a ser salvo
	 */
	@PostMapping(BASEPATH + "/attachment")
	public ResponseEntity<?> saveAttachment(@RequestBody Attachment attachment) {
		try {
			attachment.setFileLink(attachment.getFileLink().replace("https://", "http://"));
			this.bs.saveAttachment(attachment);
			return this.success(attachment);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Listar anexos de um dado nível
	 * 
	 * @param levelInstance
	 */
	@GetMapping(BASEPATH + "/attachment")
	public ResponseEntity<?> listAttachmentByLevelInstance(
			@RequestParam Long id,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer pageSize) {
		try {
			StructureLevelInstance levelInstance = this.structureBs.retrieveLevelInstance(id);
			PaginatedList<Attachment> result = this.bs.listAllAttachment(levelInstance, page, pageSize);
			return this.success(result);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Deletar anexo de um dado nível
	 * 
	 * @param id,
	 *            referente ao anexo a ser excluido
	 */
	@PostMapping(BASEPATH + "/attachment/delete")
	public ResponseEntity<?> deleteAttachment(@RequestBody IdDto dto) {
		try {
			Attachment attachment = this.bs.retrieveById(dto.id());
			if (this.bs.deleteAttachment(attachment)) {				
				return this.success(attachment);
			} else {
				return this.fail("Não foi possível excluir o anexo.");
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualizar o anexo
	 * 
	 * @param attachement,
	 *            anexo a ser atualizado.
	 */
	@PostMapping(BASEPATH + "/attachment/update")
	public ResponseEntity<?> updateAttachment(AttachmentDto dto) {
		try {
			if (this.bs.updateAttachment(dto.attachment())) {
				return this.success(dto.attachment());
			} else {
				return this.fail("Não foi possível atualizar o anexo.");
			}
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Deletar lista de anexos
	 * 
	 * @param attachmentList
	 */
	@PostMapping(BASEPATH + "/attachment/deleteList")
	public ResponseEntity<?> deleteListOfAttachment(@RequestBody AttachmentListDto dto) {
		try {
			var attachmentList = dto.attachmentList();
			List<Attachment> list = new ArrayList<>();
			for (Double id : attachmentList.getList()) {
				Attachment attachment = this.bs.retrieveById(id.longValue());
				list.add(attachment);
			}
			this.bs.deleteAttachmentList(list);
			return this.success(attachmentList);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

}
