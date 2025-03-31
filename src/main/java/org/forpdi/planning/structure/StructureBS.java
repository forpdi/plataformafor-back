package org.forpdi.planning.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.UserToSelect;
import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.Disjunction;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.HibernateBusiness;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.common.Projections;
import org.forpdi.core.common.Restrictions;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.user.User;
import org.forpdi.core.user.UserBS;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.manager.LevelInstanceHistory;
import org.forpdi.planning.attribute.AggregateIndicator;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.AttributeHelper;
import org.forpdi.planning.attribute.AttributeInstance;
import org.forpdi.planning.attribute.AttributeTypeFactory;
import org.forpdi.planning.attribute.AttributeTypeWrapper;
import org.forpdi.planning.attribute.types.ActionPlanField;
import org.forpdi.planning.attribute.types.BudgetField;
import org.forpdi.planning.attribute.types.ManagerField;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.attribute.types.SelectField;
import org.forpdi.planning.attribute.types.TableField;
import org.forpdi.planning.attribute.types.enums.Periodicity;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.fields.actionplan.ActionPlan;
import org.forpdi.planning.fields.attachment.Attachment;
import org.forpdi.planning.fields.budget.Budget;
import org.forpdi.planning.fields.budget.BudgetDTO;
import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.table.TableFields;
import org.forpdi.planning.jobs.GoalDTO;
import org.forpdi.planning.jobs.GoalsGenerationTask;
import org.forpdi.planning.jobs.OnLevelInstanceUpdateTask;
import org.forpdi.planning.plan.Plan;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.xml.StructureImporter;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.CriteriaCompanyFilter;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;


/**
 *
 * @author Renato R. R. de Oliveira
 *
 */
@Service
public class StructureBS extends HibernateBusiness {
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private FieldsBS fieldsBS;
	@Autowired
	private UserSession userSession;
	@Autowired
	private UserBS userBS;
	@Autowired
	StructureHelper helper;
	@Autowired
	AttributeHelper attrHelper;
	@Autowired
	private CriteriaCompanyFilter filter;
	@Autowired
	private GoalsGenerationTask goalTask;
	@Autowired
	private OnLevelInstanceUpdateTask onLevelInstanceUpdateTask;

	private static final int PAGESIZE = 5;

	public static final String SCHEMA;
	static {
		try {
			InputStream xsd = StructureBS.class.getResourceAsStream("/forpdi_structure_1.0.xsd");
			if (xsd == null) {
				throw new RuntimeException("The import Schema couldn't be opened.");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(xsd, Charset.forName("UTF-8")));
			String line = null;
			StringBuilder str = new StringBuilder();
			while ((line = in.readLine()) != null) {
				str.append(line).append("\n");
			}
			SCHEMA = str.toString();
			xsd.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Retorna um importer de estrutura.
	 *
	 * @param xmlFile
	 *            Arquivo xml.
	 * @return structure Estrutura importada.
	 * @throws Exception 
	 */
	public StructureImporter getStructureImporter(File xmlFile) throws Exception {
		try (InputStream xml = new FileInputStream(xmlFile)) {
			StructureImporter importer = new StructureImporter(xml, SCHEMA);
			return importer;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Importar o xml da estrutura dos planos.
	 *
	 * @param xmlFile
	 *            Arquivo xml.
	 * @return structure Estrutura importada.
	 */
	public Structure importStructure(StructureImporter importer) throws Exception {
		this.dao.execute(importer);
		return importer.getImportedStructure();
	}

	/**
	 * Listar as estruturas.
	 * @return results Lista de estruturas.
	 */
	public PaginatedList<Structure> list() {
		PaginatedList<Structure> results = new PaginatedList<Structure>();

		if(this.domain == null) {
			results.setList(new ArrayList<>());
			results.setTotal((long) 0);
			return results;
		}

		Criteria criteria = this.dao.newCriteria(Structure.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("company", this.domain.get().getCompany())).addOrder(Order.asc("name"));

		/*Criteria criteria = this.dao.newCriteria(Structure.class).setFirstResult(page * PAGESIZE)
				.setMaxResults(PAGESIZE).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("company", this.domain.get().getCompany())).addOrder(Order.asc("name")); */

		Criteria counting = this.dao.newCriteria(Structure.class).setProjection(Projections.countDistinct("id"))
				.add(Restrictions.eq("deleted", false)).add(Restrictions.eq("company", this.domain.get().getCompany()));
		results.setList(this.dao.findByCriteria(criteria, Structure.class));
		results.setTotal((Long) counting.uniqueResult());
		return results;
	}

	/**
	 * Listar os leveis de uma estrutura.
	 *
	 * @param structure
	 *            Estrutura para buscar os leveis.
	 * @return results Lista de leveis.
	 */
	public PaginatedList<StructureLevel> listLevels(Structure structure) {
		PaginatedList<StructureLevel> results = new PaginatedList<StructureLevel>();
		Criteria criteria = this.dao.newCriteria(StructureLevel.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("structure", structure)).addOrder(Order.asc("sequence"));
		results.setList(this.dao.findByCriteria(criteria, StructureLevel.class));
		results.setTotal((long) results.getList().size());

		return results;
	}

	/**
	 * Listar os leveis de uma estrutura.
	 *
	 * @param structure
	 *            Estrutura para buscar os leveis.
	 * @return results Lista de leveis.
	 */
	public List<StructureLevel> listStructureLevels(Structure structure) {
		Criteria criteria = this.dao.newCriteria(StructureLevel.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("structure", structure)).addOrder(Order.asc("sequence"));

		return this.dao.findByCriteria(criteria, StructureLevel.class);
	}

	public List<StructureLevel> listAllStructuresLevels(List<Structure> structures) {
		if (GeneralUtils.isEmpty(structures)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(StructureLevel.class)
			.add(Restrictions.in("structure", structures))
		;
		return this.dao.findByCriteria(criteria, StructureLevel.class);
	}
	public List<Attribute> listAllAttributes(List<StructureLevel> structureLevels) {
		if (GeneralUtils.isEmpty(structureLevels)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(Attribute.class)
			.add(Restrictions.in("level", structureLevels))
		;
		return this.dao.findByCriteria(criteria, Attribute.class);
	}

	/**
	 * Listar atributos de um level.
	 *
	 * @param level
	 *            Level de uma estrutura.
	 * @return results Lista de atributos.
	 */
	public PaginatedList<Attribute> listAttributes(StructureLevel level, boolean deleted) {
		PaginatedList<Attribute> results = new PaginatedList<Attribute>();
		Criteria criteria = this.dao.newCriteria(Attribute.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("level", level)).addOrder(Order.asc("label"));
		results.setList(this.dao.findByCriteria(criteria, Attribute.class));
		results.setTotal((long) results.getList().size());
		return results;
	}
	public PaginatedList<Attribute> listAttributes(StructureLevel level) {
		PaginatedList<Attribute> results = new PaginatedList<Attribute>();
		Criteria criteria = this.dao.newCriteria(Attribute.class)//.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("level", level)).addOrder(Order.asc("label"));
		results.setList(this.dao.findByCriteria(criteria, Attribute.class));
		results.setTotal((long) results.getList().size());
		return results;
	}

	/**
	 * Listar atributos de um level para o pdf.
	 *
	 * @param level
	 *            Level de uma estrutura.
	 * @return results Lista de atributos.
	 */
	public List<Attribute> listAttributesPDF(StructureLevel level) {
		Criteria criteria = this.dao.newCriteria(Attribute.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("level", level)).addOrder(Order.asc("label"));
		List<Attribute> results = this.dao.findByCriteria(criteria, Attribute.class);
		return results;
	}

	/**
	 * Listar as instâncias de um level.
	 *
	 * @param structureLevel
	 *            Level de uma estrutura.
	 * @param plan
	 *            Plano de metas.
	 * @param parentId
	 *            Id do level pai.
	 * @return results Lista de instâncias de um level.
	 */
	public List<StructureLevelInstance> listLevelsInstance(StructureLevel structureLevel, Plan plan,
			Long parentId) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("level", structureLevel)).add(Restrictions.eq("plan", plan));

		if (parentId != null)
			criteria.add(Restrictions.eq("parent", parentId));

		return this.dao.findByCriteria(criteria, StructureLevelInstance.class);
	}


	/**
	 * Listar levels de uma instância
	 * @param plan
	 * 		Plano de Metas
	 * @param parentId
	 * 		Id do Plano Macro
	 * @return
	 */
	public PaginatedList<StructureLevelInstance> listLevelsInstance(Plan plan, Long parentId) {
		PaginatedList<StructureLevelInstance> results = new PaginatedList<StructureLevelInstance>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("plan", plan))
			.addOrder(Order.asc("name"))
		;
		if (parentId != null)
			criteria.add(Restrictions.eq("parent", parentId));
		else
			criteria.add(Restrictions.isNull("parent"));

		results.setList(this.dao.findByCriteria(criteria, StructureLevelInstance.class));
		results.setTotal((long) results.getList().size());
		return results;
	}

	/**
	 * Listar as instâncias dos leveis pelo termo da busca.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param terms
	 *            Termmo da busca.
	 * @param subPlansSelect
	 *            Lista de planos de metas.
	 * @param levelsSelect
	 *            Lista de leveis.
	 * @param ordResult
	 *            Ordem.
	 * @return results Lista de instâncias dos leveis.
	 */
	public List<StructureLevelInstance> listLevelsInstanceTerms(PlanMacro macro, String terms, Long subPlansSelect[],
			Long levelsSelect[], int ordResult) {
		List<StructureLevelInstance> results = new ArrayList<StructureLevelInstance>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("plan", "plan", JoinType.INNER_JOIN).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("plan.parent", macro));

		if (terms != null && !terms.isEmpty()) {
			criteria.add(Restrictions.like("name", "%" + terms + "%").ignoreCase());
		}

		if (subPlansSelect != null) {
			Disjunction or = Restrictions.disjunction();
			for (int i = 0; i < subPlansSelect.length; i++) {
				or.add(Restrictions.eq("plan.id", subPlansSelect[i]));
			}
			criteria.add(or);
		}
		if (levelsSelect != null) {
			criteria.createAlias("level", "level", JoinType.INNER_JOIN);
			Disjunction or = Restrictions.disjunction();
			for (int i = 0; i < levelsSelect.length; i++) {
				or.add(Restrictions.eq("level.id", levelsSelect[i]));
			}
			criteria.add(or);
		}
		if (ordResult == 1) {
			criteria.addOrder(Order.asc("creation"));
		} else if (ordResult == 2) {
			criteria.addOrder(Order.desc("creation"));
		}
		results = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		return results;
	}

	/**
	 * Listar as instâncias dos atributos pelo termo da busca.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param terms
	 *            Termmo da busca.
	 * @param subPlansSelect
	 *            Lista de planos de metas.
	 * @param levelsSelect
	 *            Lista de leveis.
	 * @param ordResult
	 *            Ordem.
	 * @return results Lista de instâncias dos atributos.
	 */
	public List<AttributeInstance> listAttributesTerms(PlanMacro macro, String terms, Long subPlansSelect[],
			Long levelsSelect[], int ordResult) {
		List<AttributeInstance> results = new ArrayList<AttributeInstance>();
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class)
				.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN)
				.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("plan.parent", macro))
				.add(Restrictions.eq("levelInstance.deleted", false));

		if (terms != null && !terms.isEmpty()) {
			criteria.add(Restrictions.like("value", "%" + terms + "%").ignoreCase());
		}

		if (subPlansSelect != null) {
			Disjunction or = Restrictions.disjunction();
			for (int i = 0; i < subPlansSelect.length; i++) {
				or.add(Restrictions.eq("plan.id", subPlansSelect[i]));
			}
			criteria.add(or);
		}
		if (levelsSelect != null) {
			criteria.createAlias("levelInstance.level", "level", JoinType.INNER_JOIN);
			Disjunction or = Restrictions.disjunction();
			for (int i = 0; i < levelsSelect.length; i++) {
				or.add(Restrictions.eq("level.id", levelsSelect[i]));
			}
			criteria.add(or);
		}
		if (ordResult == 1) {
			criteria.addOrder(Order.asc("levelInstance.creation"));
		} else if (ordResult == 2) {
			criteria.addOrder(Order.desc("levelInstance.creation"));
		}

		results = this.dao.findByCriteria(criteria, AttributeInstance.class);

		return results;
	}

	/**
	 * Listar as instâncias dos leveis pelo responsável.
	 *
	 * @param users
	 *            Lista de usuários.
	 * @return results Lista de instâncias dos leveis.
	 */
	public List<StructureLevelInstance> listLevelInstancesByResponsible(List<User> users) {
		List<StructureLevelInstance> results = new ArrayList<StructureLevelInstance>();
		if (users.size() > 0) {
			Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
			criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);

			Disjunction or = Restrictions.disjunction();
			for (User user : users) {
				or.add(Restrictions.eq("value", String.valueOf(user.getId())));
			}
			criteria.add(or);

			criteria.add(Restrictions.eq("deleted", false));
			criteria.add(Restrictions.eq("attribute.deleted", false));
			criteria.add(Restrictions.eq("attribute.type", ResponsibleField.class.getCanonicalName()))
				.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN)
				.add(Restrictions.eq("levelInstance.deleted", false))
				.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN)
				.createAlias("plan.parent", "planMacro", JoinType.INNER_JOIN)
				.add(Restrictions.eq("planMacro.company", domain.get().getCompany()));

			List<AttributeInstance> attrInsts = this.dao.findByCriteria(criteria, AttributeInstance.class);

			for (AttributeInstance attrInst : attrInsts) {
				boolean exists = false;
				for (StructureLevelInstance levelInst : results) {
					if (levelInst.getId().equals(attrInst.getLevelInstance().getId()))
						exists = true;
				}
				if (!exists)
					results.add(attrInst.getLevelInstance());
			}
		}

		return results;
	}

	public Structure retrieveById(Long id) {
		Criteria criteria = this.dao.newCriteria(Structure.class)
				.add(Restrictions.eq("id", id))
				.add(Restrictions.eq("deleted", false));
		return (Structure) criteria.uniqueResult();
	}

	/**
	 * Buscar um nível pelo id.
	 */
	public StructureLevel retrieveStructureLevelById(Long id) {
		return this.dao.exists(id, StructureLevel.class);
	}

	/**
	 * FIXME Remover em versões futuras. Buscar uma instância de um level pelo
	 * id.
	 *
	 * @deprecated Utilizar o método da helper.
	 */
	@Deprecated
	public StructureLevelInstance retrieveLevelInstance(Long id) {
		return this.helper.retrieveLevelInstance(id);
	}

	/**
	 * Buscar uma instância de um level pelo
	 * id.
	 *
	 */
	public StructureLevelInstance retrieveLevelInstanceNoDeleted(Long id) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.add(Restrictions.eq("deleted", false));
		StructureLevelInstance levelInstance = (StructureLevelInstance) criteria.uniqueResult();
		return levelInstance;
	}

	/**
	 * Buscar uma instância de um level para o pdf.
	 *
	 */
	public StructureLevelInstance retrieveLevelInstancePDF(Long id) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.addOrder(Order.asc("id"));
		StructureLevelInstance levelInstance = (StructureLevelInstance) criteria.uniqueResult();
		return levelInstance;
	}

	/**
	 * Listar atributos de um level.
	 *
	 */
	public List<Attribute> retrieveLevelAttributes(StructureLevel level) {
		Criteria criteria = this.dao.newCriteria(Attribute.class);
		criteria.add(Restrictions.eq("level", level));
		List<Attribute> attributes = this.dao.findByCriteria(criteria, Attribute.class);
		return attributes;
	}
	
	/**
	 * Listar atributos de um level que tem o nome na lista
	 *
	 */
	public List<Attribute> retrieveLevelAttributes(StructureLevel level, List<String> attributeLabels) {
		Criteria criteria = this.dao.newCriteria(Attribute.class);
		criteria.add(Restrictions.eq("level", level));
		
		if (attributeLabels != null) {
			criteria.add(Restrictions.in("label", attributeLabels));			
		}

		List<Attribute> attributes = this.dao.findByCriteria(criteria, Attribute.class);
		return attributes;
	}

	/**
	 * Listar atributos de um level filho.
	 *
	 */
	public List<Attribute> retrieveLevelSonsAttributes(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(Attribute.class);
		criteria.add(Restrictions.eq("level", levelInstance.getLevel()));
		List<Attribute> attributes = this.dao.findByCriteria(criteria, Attribute.class);
		for (Attribute attribute : attributes) {
			// if (attribute.isVisibleInTables()) {
			if (attribute.getAttributeInstances() == null)
				attribute.setAttributeInstances(new ArrayList<AttributeInstance>());
			attribute.getAttributeInstances().add(attrHelper.retrieveAttributeInstance(levelInstance, attribute));
			// }
		}
		return attributes;
	}

	/**
	 * Setar os status das instâncias dos leveis.
	 *
	 * @param levelInstances
	 *            Lista de instâncias dos leveis.
	 * @param polarity
	 *            Polaridade para cálculo dos status.
	 *
	 * @throws ParseException
	 */
	public void setGoalStatus(List<StructureLevelInstance> levelInstances, String polarity) throws ParseException {
		for (StructureLevelInstance instance : levelInstances) {
			Number expectedValue = null;
			Number minimumValue = null;
			Number maximumValue = null;
			Number reachedValue = null;
			if (polarity == null)
				polarity = "";
			List<AttributeInstance> attrs = this.listAttributeInstanceByLevel(instance,false);
			for (AttributeInstance attribute : attrs) {
				if (attribute.getAttribute().isFinishDate()) {
					if (instance.isClosed()) {
						DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
						Date date = (Date) formatter.parse(attribute.getValue());
						if (instance.getClosedDate().after(date)) {
							instance.setDeadlineStatus(1); // concluído atrasado
						} else {
							instance.setDeadlineStatus(2); // concluído no prazo
						}
					} else {
						Date datenow = new Date();
						/*
						 * removendo hora, minuto, segundo e milissegundos /* da
						 * data atual para comparação
						 */
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(datenow);
						calendar.set(Calendar.HOUR_OF_DAY, 0);
						calendar.set(Calendar.MINUTE, 0);
						calendar.set(Calendar.SECOND, 0);
						calendar.set(Calendar.MILLISECOND, 0);
						datenow = calendar.getTime();
						DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
						Date date = (Date) formatter.parse(attribute.getValue());
						if (datenow.after(date)) {
							instance.setDeadlineStatus(3); // atrasado
						} else {
							instance.setDeadlineStatus(4); // em dia
						}
					}
				} else {
					if (attribute.getAttribute().isExpectedField()) {
						expectedValue = attribute.getValueAsNumber();
					} else if (attribute.getAttribute().isMinimumField()) {
						minimumValue = attribute.getValueAsNumber();
					} else if (attribute.getAttribute().isMaximumField()) {
						maximumValue = attribute.getValueAsNumber();
					} else if (attribute.getAttribute().isReachedField()) {
						reachedValue = attribute.getValueAsNumber();
					}
				}

			}

			if (minimumValue != null && maximumValue != null) {
				if (reachedValue == null && instance.getDeadlineStatus() != 3) {
					instance.setProgressStatus(GoalProgressStatus.NOT_STARTED.getId()); // não iniciado
					continue;
				}
				if (polarity.equals(Polarity.SMALLER_BETTER.getValue())) {
					if (reachedValue == null || reachedValue.doubleValue() > minimumValue.doubleValue()) {
						instance.setProgressStatus(GoalProgressStatus.MINIMUM_BELOW.getId()); // abaixo do mínimo
					} else if (reachedValue.doubleValue() <= minimumValue.doubleValue()
							&& reachedValue.doubleValue() > expectedValue.doubleValue()) {
						instance.setProgressStatus(GoalProgressStatus.MINIMUM.getId()); // mínimo até o esperado
					} else {
						if (reachedValue.doubleValue() <= expectedValue.doubleValue()
								&& reachedValue.doubleValue() >= maximumValue.doubleValue()) {
							instance.setProgressStatus(GoalProgressStatus.ENOUGH_UP.getId()); // suficiente até o
															// máximo
						} else {
							if (reachedValue.doubleValue() < maximumValue.doubleValue()) {
								instance.setProgressStatus(GoalProgressStatus.MAXIMUM_UP.getId()); // máximo pra
																// cima
							}
						}
					}
				} else {
					if (reachedValue == null || reachedValue.doubleValue() < minimumValue.doubleValue()) {
						instance.setProgressStatus(GoalProgressStatus.MINIMUM_BELOW.getId()); // abaixo do mínimo
					} else if (reachedValue.doubleValue() >= minimumValue.doubleValue()
							&& reachedValue.doubleValue() < expectedValue.doubleValue()) {
						instance.setProgressStatus(GoalProgressStatus.MINIMUM.getId()); // mínimo até o esperado
					} else {
						if (reachedValue.doubleValue() >= expectedValue.doubleValue()
								&& reachedValue.doubleValue() <= maximumValue.doubleValue()) {
							instance.setProgressStatus(GoalProgressStatus.ENOUGH_UP.getId()); // suficiente até o
															// máximo
						} else {
							if (reachedValue.doubleValue() > maximumValue.doubleValue()) {
								instance.setProgressStatus(GoalProgressStatus.MAXIMUM_UP.getId()); // máximo pra
																// cima
							}
						}
					}
				}
			} else {
				instance.setProgressStatus(GoalProgressStatus.NOT_FILLED.getId());
			}
		}
	}

	/**
	 * Seta atributos no plano de ação com ordenação 
	 * @param levelInstance
	 * 			Level Instance
	 * @param attributes
	 * 			Lista de Atributos
	 * @param page
	 * 			Número da página
	 * @param pageSize
	 * 			Tamanho da página
	 * @return
	 */
	public PaginatedList<Attribute> setActionPlansAttributes(StructureLevelInstance levelInstance,
			PaginatedList<Attribute> attributes, Integer page, Integer pageSize, String description,
			Boolean checked, Boolean notChecked, Integer year, List<String> sortedBy) throws ParseException {

		Long total = 0L;

		for (Attribute attribute : attributes.getList()) {
			if (attribute.getType().equals(ActionPlanField.class.getCanonicalName())) {
					PaginatedList<ActionPlan> actionPlans = this.fieldsBS.listActionPlansByInstancePaginedAndOrdered(levelInstance,
						page, pageSize, description, checked, notChecked, year, sortedBy);
					
					attribute.setActionPlans(actionPlans.getList());
					total = total + actionPlans.getTotal();
			}
		}
		attributes.setTotal(total);

		return attributes;
	}

	/**
	 * Seta atributos no plano de ação
	 * @param levelInstance
	 * 			Level Instance
	 * @param attributes
	 * 			Lista de Atributos
	 * @param page
	 * 			Número da página
	 * @param pageSize
	 * 			Tamanho da página
	 * @return
	 */
	public PaginatedList<Attribute> setActionPlansAttributes(StructureLevelInstance levelInstance,
			PaginatedList<Attribute> attributes, Integer page, Integer pageSize, String description,
			Boolean checked, Boolean notChecked, Integer year) throws ParseException {

		Long total = 0L;

		for (Attribute attribute : attributes.getList()) {
			if (attribute.getType().equals(ActionPlanField.class.getCanonicalName())) {
				PaginatedList<ActionPlan> actionPlans = this.fieldsBS.listActionPlansByInstancePagined(levelInstance,
						page, pageSize, description, checked, notChecked, year);
				attribute.setActionPlans(actionPlans.getList());
				total = total + actionPlans.getTotal();
			}
		}

		attributes.setTotal(total);
		return attributes;
	}

	/**
	 * Setar as instâncias dos atributos na instância de um level.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @param attributes
	 *            Lista de atributos.
	 * @return attributes Lista de atributos.
	 */
	public List<Attribute> setAttributesInstances(StructureLevelInstance levelInstance, List<Attribute> attributes) {
		for (int i = 0; i < attributes.size(); i++) {
			Attribute attribute = attributes.get(i);
			
			if (attribute.getType().equals(ManagerField.class.getCanonicalName())) {
				attributes.remove(i);
				attributes.add(0, attribute);
			}
			
			if (attribute.isJustificationField()) {
				attributes.remove(i);
				attributes.add(5, attribute);
			}
			
			if (attribute.getType().equals(SelectField.class.getCanonicalName())) {
				PaginatedList<OptionsField> options = this.fieldsBS.getOptionsField(attribute.getId(),false);
				List<OptionsField> optionLabels = options.getList();

				if (attribute.isPeriodicityField()) {
					OptionsField trienalPeriodicity = new OptionsField();
					trienalPeriodicity.setLabel("Trienal");
					trienalPeriodicity.setAttributeId(attribute.getId());
					optionLabels.add(trienalPeriodicity);
				}

				attribute.setOptionLabels(optionLabels);
			} else if (attribute.getType().equals(ResponsibleField.class.getCanonicalName())) {
				List<UserToSelect> users = this.userBS.listUsersToSelect(this.domain.get().getCompany(), null, null);
				attribute.setUsers(UserToSelect.toUserList(users));
			} else if (attribute.getType().equals(ManagerField.class.getCanonicalName())) {
				int minAccessLevel = AccessLevels.MANAGER.getLevel();
				int maxAccessLevel = AccessLevels.COMPANY_ADMIN.getLevel();
				List<UserToSelect> managers = this.userBS.listUsersToSelect(this.domain.get().getCompany(), minAccessLevel, maxAccessLevel);
				attribute.setUsers(UserToSelect.toUserList(managers));
			}
			if (attribute.getType().equals(BudgetField.class.getCanonicalName())) {
				List<BudgetDTO> budgetList = this.fieldsBS.getBudgets(levelInstance);
				attribute.setBudgets(budgetList);
			} else if (attribute.getType().equals(ActionPlanField.class.getCanonicalName())) {
				PaginatedList<ActionPlan> actionPlans = this.fieldsBS.listActionPlansByInstance(levelInstance);
				attribute.setActionPlans(actionPlans.getList());
			} else if (attribute.getType().equals(Schedule.class.getCanonicalName())) {
				Schedule schedule = this.fieldsBS.scheduleByAttribute(attribute.getId(), false);
				attribute.setSchedule(schedule);
			} else if (attribute.getType().equals(TableField.class.getCanonicalName())) {
				TableFields tableFields = this.fieldsBS.tableFieldsByAttribute(attribute.getId(), false, false);
				attribute.setTableFields(tableFields);
			} else {
				AttributeInstance attributeInstance = attrHelper.retrieveAttributeInstance(levelInstance, attribute);
				if (attributeInstance != null) {
					AttributeTypeWrapper attributeTypeWrapper = AttributeTypeFactory.getInstance()
							.get(attribute.getType()).getWrapper();
					if (attributeInstance.getValueAsDate() != null)
						attributeInstance
								.setValue(attributeTypeWrapper.fromDatabaseDate(attributeInstance.getValueAsDate()));
					else if (attributeInstance.getValueAsNumber() != null)
						attributeInstance.setValue(
								attributeTypeWrapper.fromDatabaseNumerical(attributeInstance.getValueAsNumber()));
					else
						attributeInstance.setValue(attributeTypeWrapper.fromDatabase(attributeInstance.getValue()));
					attribute.setAttributeInstance(attributeInstance);
				}
			}
		}
		return attributes;
	}

	/**
	 * Buscar um atributo.
	 *
	 * @param id
	 *            Id do atributo.
	 * @return attribute Atributo.
	 */
	public Attribute retrieveAttribute(Long id) {
		Criteria criteria = this.dao.newCriteria(Attribute.class);
		criteria.add(Restrictions.eq("id", id));
		Attribute attribute = (Attribute) criteria.uniqueResult();
		return attribute;
	}

	/**
	 * Converter valor no formato string para o tipo do atributo.
	 *
	 * @param attribute
	 *            Atributo.
	 * @param value
	 *            Valor em string.
	 * @return attributeInstance Instância de um atributo.
	 */
	public AttributeInstance valueStringToValueType(Attribute attribute, String value) {
		AttributeInstance attributeInstance = new AttributeInstance();
		AttributeTypeWrapper attributeTypeWrapper = AttributeTypeFactory.getInstance().get(attribute.getType())
				.getWrapper();
		if (value == null) {
			attributeInstance.setValue(null);
			attributeInstance.setValueAsDate(null);
			attributeInstance.setValueAsNumber(null);
		} else {
			attributeInstance.setValue(attributeTypeWrapper.toDatabase(value));

			if (attributeTypeWrapper.isNumerical())
				attributeInstance.setValueAsNumber(attributeTypeWrapper.toDatabaseNumerical(value));
			if (attributeTypeWrapper.isDate()) {
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(attributeTypeWrapper.toDatabaseDate(value));
				gc.add(Calendar.MONTH, -1);
				attributeInstance.setValueAsDate(gc.getTime());
			}
		}
		return attributeInstance;
	}

	/**
	 * Listar instâncias do level filho de um level, por página.
	 *
	 * @param parentId
	 *            Id da instância do level pai.
	 * @param page
	 *            Número da página desejada.
	 * @return levelInstanceList Lista de instâncias de um level.
	 */
	public PaginatedList<StructureLevelInstance> retrieveLevelInstanceSons(Long parentId, Integer page,
			Integer pageSize) {
		if (page == null || page < 1) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PAGESIZE;
		}
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("parent", parentId));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.addOrder(Order.asc("visualized"));
		criteria.addOrder(Order.desc("creation"));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		Criteria counting = this.dao.newCriteria(StructureLevelInstance.class);
		counting.setProjection(Projections.countDistinct("id"));
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("parent", parentId));
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();

		result.setList(this.dao.findByCriteria(criteria, StructureLevelInstance.class));
		result.setTotal((Long) counting.uniqueResult());
		return result;
	}

	/**
	 * Listar instâncias do level filho de um level.
	 *
	 * @param parentId
	 *            Id da instância do level pai.
	 * @return levelInstanceList Lista de instâncias de um level.
	 */
	public List<StructureLevelInstance> retrieveLevelInstanceSons(Long parentId) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("parent", parentId));
		criteria.add(Restrictions.eq("deleted", false));
		List<StructureLevelInstance> levelInstanceList = this.dao.findByCriteria(criteria,
				StructureLevelInstance.class);
		return levelInstanceList;
	}

	/**
	 * Listar instâncias dos leveis pelo plano.
	 *
	 * @param plan
	 *            Plano de metas.
	 * @return list Lista de instâncias dos leveis.
	 */
	public List<StructureLevelInstance> listLevelInstanceByPlan(Plan plan) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("plan", plan));
		criteria.add(Restrictions.eq("deleted", false));
		List<StructureLevelInstance> list = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		return list;
	}

	public List<StructureLevelInstance> listAllLevelInstanceByPlans(List<Plan> plans) {
		if (GeneralUtils.isEmpty(plans)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.in("plan", plans))
				.addOrder(Order.asc("parent"));

		return this.dao.findByCriteria(criteria, StructureLevelInstance.class);
	}

	/**
	 * Listar instâncias dos leveis pelo plano
	 *
	 * @param plan
	 *            Plano de metas.
	 * @return list Lista de instâncias dos leveis.
	 */
	public List<StructureLevelInstance> listAllLevelInstanceByPlan(Plan plan, Boolean deleted) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("plan", plan))
			.add(Restrictions.eq("deleted", deleted != null ? deleted : true));
		return this.dao.findByCriteria(criteria, StructureLevelInstance.class);
	}

	/**
	 * Listar as instâncias do level principal.
	 *
	 * @param plan
	 *            Plano de metas.
	 * @return list Lista de instâncias.
	 */
	public List<StructureLevelInstance> listRootLevelInstanceByPlan(Plan plan) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.add(Restrictions.eq("plan", plan));
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.isNull("parent"));
		List<StructureLevelInstance> list = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		return list;
	}

	/**
	 * Listar instâncias dos atributos de um level.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return list Lista de instâncias dos atributos.
	 */
	public List<AttributeInstance> listAttributeInstanceByLevel(StructureLevelInstance levelInstance, boolean deleted) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("deleted", deleted));
		List<AttributeInstance> list = this.dao.findByCriteria(criteria, AttributeInstance.class);
		return list;
	}
	public AttributeInstance listResponsibleAttributeByLevel(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("attribute.label", "Responsável"));
		List<AttributeInstance> list = this.dao.findByCriteria(criteria, AttributeInstance.class);
		return GeneralUtils.isEmpty(list) ? null : list.get(0);
	}

	/**
	 * Listar instâncias dos leveis eixos.
	 *
	 * @param void
	 *
	 * @return result Lista de instâncias.
	 */
	public PaginatedList<StructureLevelInstance> listAxes() {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("sequence", 0));
		criteria.add(Restrictions.eq("objective", false));
		criteria.add(Restrictions.eq("indicator", false));
		criteria.add(Restrictions.eq("goal", false));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);

		List<StructureLevelInstance> list = new ArrayList<>();
		for (StructureLevel level : levelList) {
			Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
			criteria2.createAlias("plan", "plan", JoinType.INNER_JOIN);
			criteria2.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
			criteria2.add(Restrictions.eq("deleted", false));
			criteria2.add(Restrictions.eq("level", level));
			criteria2.add(Restrictions.eq("aggregate", false));
			criteria2.add(Restrictions.eq("macro.archived", false));
			list.addAll(this.filter.filterAndList(criteria2, StructureLevelInstance.class, "macro.company"));
		}

		result.setList(list);
		result.setTotal((long) list.size());



		return result;
	}
	
	/**
	 * Listar instâncias dos leveis indicador.
	 *
	 * @param void
	 *
	 * @return result Lista de instâncias.
	 */
	public PaginatedList<StructureLevelInstance> listIndicators() {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("indicator", true));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);

		List<StructureLevelInstance> list = new ArrayList<>();
		for (StructureLevel level : levelList) {
			Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
			criteria2.createAlias("plan", "plan", JoinType.INNER_JOIN);
			criteria2.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
			criteria2.add(Restrictions.eq("deleted", false));
			criteria2.add(Restrictions.eq("level", level));
			criteria2.add(Restrictions.eq("aggregate", false));
			criteria2.add(Restrictions.eq("macro.archived", false));
			list.addAll(this.filter.filterAndList(criteria2, StructureLevelInstance.class, "macro.company"));
		}

		result.setList(list);
		result.setTotal((long) list.size());



		return result;
	}

	/**
	 * Listar instâncias dos leveis objetivo.
	 *
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias.
	 */
	public PaginatedList<StructureLevelInstance> listObjective(Plan plan) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("objective", true));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);

		List<StructureLevelInstance> list = new ArrayList<>();
		for (StructureLevel level : levelList) {
			Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
			criteria2.createAlias("plan", "plan", JoinType.INNER_JOIN);
			criteria2.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
			criteria2.add(Restrictions.eq("deleted", false));
			criteria2.add(Restrictions.eq("level", level));
			criteria2.add(Restrictions.eq("plan", plan));
			list.addAll(this.filter.filterAndList(criteria2, StructureLevelInstance.class, "macro.company"));
		}

		result.setList(list);
		result.setTotal((long) list.size());

		return result;
	}

	/**
	 * Listar instâncias dos leveis objetivo pelo plano macro e/ou plano de
	 * metas.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias do level objetivo.
	 */
	public PaginatedList<StructureLevelInstance> listObjective(PlanMacro macro, Plan plan) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = createLevelInstanceFilterCriteria(macro, plan, "objective");

		List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");

		result.setList(list);
		result.setTotal((long) list.size());
		return result;
	}
	
	public Long countObjective(PlanMacro macro, Plan plan) {
		Criteria criteria = createLevelInstanceFilterCriteria(macro, plan, "objective");
		criteria.setProjection(Projections.countDistinct("id"));
		
		return (Long) criteria.uniqueResult();
	}

	public long countPlans(PlanMacro macro, Plan plan) {
    Criteria criteria = createPlanFilterCriteria(macro, plan);
    criteria.setProjection(Projections.countDistinct("id"));
    
    Long result = (Long) criteria.uniqueResult();
    
    return result;
}

private Criteria createPlanFilterCriteria(PlanMacro macro, Plan plan) {
	Criteria criteria = this.dao.newCriteria(Plan.class);
	criteria.createAlias("parent", "macro", JoinType.INNER_JOIN);
	criteria.add(Restrictions.eq("deleted", false));
	criteria.add(Restrictions.eq("archived", false));
	criteria.add(Restrictions.eq("macro.deleted", false));
	criteria.add(Restrictions.eq("macro.archived", false));
	criteria.add(Restrictions.eq("macro.company", domain.get().getCompany()));

	if (plan != null) {
			criteria.add(Restrictions.eq("id", plan.getId()));
	} else if (macro != null) {
			criteria.add(Restrictions.eq("parent", macro));
	}
	return criteria;
}

	public Criteria createLevelInstanceFilterCriteria(String structureLevelName) {
		return createLevelInstanceFilterCriteria(null, null, structureLevelName);
	}
	
	private Criteria createLevelInstanceFilterCriteria(PlanMacro macro, Plan plan, String structureLevelName) {
		String structureLevelPropery = "structure_level." + structureLevelName;
		
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("level", "structure_level", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("structure_level.deleted", false));
		criteria.add(Restrictions.eq(structureLevelPropery, true));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.add(Restrictions.eq("macro.company", domain.get().getCompany()));

		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}
		return criteria;
	}

	/**
	 * Listar instâncias dos leveis objetivo pelos planos de metas.
	 *
	 * @param plans
	 *            Lista de planos de metas.
	 * @return result Lista de instâncias dos leveis objetivos.
	 */
	public PaginatedList<StructureLevelInstance> listObjective(PaginatedList<Plan> plans) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("objective", true));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);

		List<StructureLevelInstance> list = new ArrayList<>();
		for (StructureLevel level : levelList) {
			Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
			criteria2.createAlias("plan", "plan", JoinType.INNER_JOIN);
			criteria2.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
			criteria2.add(Restrictions.eq("deleted", false));
			criteria2.add(Restrictions.eq("level", level));
			criteria.add(Restrictions.eq("macro.archived", false));
			if (plans != null) {

				List<Plan> planParam = new ArrayList<Plan>();
				for (int i = 0; i < plans.getList().size(); i++) {
					planParam.add(plans.getList().get(i));
				}
				criteria2.add(Restrictions.in("plan", planParam));
			}
			list.addAll(this.filter.filterAndList(criteria2, StructureLevelInstance.class, "macro.company"));
		}

		result.setList(list);
		result.setTotal((long) list.size());

		return result;
	}

	/**
	 * Listar instâncias dos leveis objetivo pelo plano macro.
	 *
	 * @param macro
	 *            Plano macro.
	 * @return result Lista de instâncias dos leveis objetivos.
	 */
	public PaginatedList<StructureLevelInstance> listObjective(PlanMacro macro) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(Plan.class);
		criteria.createAlias("parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("parent", macro));
		criteria.add(Restrictions.eq("macro.archived", false));
		List<Plan> plans = this.filter.filterAndList(criteria, Plan.class, "macro.company");

		List<StructureLevelInstance> list = new ArrayList<>();
		for (Plan plan : plans) {
			list.addAll(this.listIndicators(plan).getList());
		}

		result.setList(list);
		result.setTotal((long) list.size());

		return result;
	}

	/**
	 * Duplicar plano de metas.
	 *
	 * @param plan
	 *            Plano de metas.
	 * @param clone
	 *            Copia do plano de metas.
	 * @param keepContent
	 *            Indicação se é para manter o conteúdo do plano.
	 * @return boolean Retorna verdadeiro se for duplicado.
	 */
	public boolean duplicatePlanLevelInstance(Plan plan, Plan clone, Boolean keepContent) {
		try {
			List<StructureLevelInstance> levels = this.listLevelInstanceByPlan(plan);
			Map<Long, Long> map = new HashMap<>(); // map para referenciar qual
													// level original o
													// duplicado representa
			for (StructureLevelInstance instance : levels) {
				StructureLevelInstance instanceClone = new StructureLevelInstance();
				instanceClone.setLevel(instance.getLevel());
				instanceClone.setName(instance.getName());
				instanceClone.setCreation(new Date());
				instanceClone.setParent(map.get(instance.getParent()));
				instanceClone.setPlan(clone);
				instanceClone.setSons(instance.getSons());
				instanceClone.setId(null);
				instanceClone.setAggregate(instance.isAggregate());
				instanceClone.setCalculation(instance.getCalculation());

				if (keepContent) // para não duplicar o levelValue caso não ative a opção manter conteúdo
					instanceClone.setLevelValue(instance.getLevelValue());

				this.persist(instanceClone);
				map.put(instance.getId(), instanceClone.getId());
				if (keepContent) {
					if (this.duplicateAttributeInstance(instance, instanceClone)) {
						//LOGGER.info("Conteúdo do nível " + instance.getName() + " duplicado com sucesso");
					} else {
						LOGGER.info("Não foi possivel duplicar o conteúdo do nível " + instance.getName());
					}
				}
				if (instanceClone.isAggregate()) {
					if (this.duplicateAggregateIndicator(instance, instanceClone)) {
						//LOGGER.info("Nível agregado " + instance.getName() + " duplicado com sucesso");
					} else {
						LOGGER.info("Não foi possível duplicar nível agregado " + instance.getName());
					}
				}
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("Unexpected runtime error", e);
			return false;
		}
	}

	/**
	 * Duplicar o indicador agregado.
	 *
	 * @param instance
	 *            Instância de um level indicador.
	 * @param clone
	 *            Copia da instância do level indicador.
	 * @return boolean Retorna verdadeiro se o indicador agregado foi duplicado.
	 */
	public boolean duplicateAggregateIndicator(StructureLevelInstance instance, StructureLevelInstance clone) {
		try {
			List<AggregateIndicator> indicators = this.helper.listIndicators(instance);
			for (AggregateIndicator indicator : indicators) {
				AggregateIndicator indicatorCopy = new AggregateIndicator();
				indicatorCopy.setIndicator(clone);
				indicatorCopy.setDeleted(false);
				indicatorCopy.setPercentage(indicator.getPercentage());
				indicatorCopy.setAggregate(indicator.getAggregate());
				this.persist(indicatorCopy);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("Unexpected runtime error", e);
			return false;
		}
	}

	/**
	 * Duplicar as instâncias dos atributos.
	 *
	 * @param instance
	 *            Instância de um level.
	 * @param instanceClone
	 *            Copia da instância do level.
	 * @return boolean Retorna verdadeiro se as instâncias dos atributos foram
	 *         duplicadas.
	 */
	public boolean duplicateAttributeInstance(StructureLevelInstance instance, StructureLevelInstance instanceClone) {
		try {
			List<AttributeInstance> attrList = this.listAttributeInstanceByLevel(instance,false);
			for (AttributeInstance attr : attrList) {
				AttributeInstance attrCopy = new AttributeInstance();
				attrCopy.setAttribute(attr.getAttribute());
				attrCopy.setCreation(new Date());
				attrCopy.setDeleted(false);
				attrCopy.setId(null);
				attrCopy.setLevelInstance(instanceClone);
				attrCopy.setValue(attr.getValue());
				attrCopy.setValueAsDate(attr.getValueAsDate());
				attrCopy.setValueAsNumber(attr.getValueAsNumber());
				this.persist(attrCopy);
			}
			PaginatedList<Attribute> attributes = this.listAttributes(instance.getLevel());
			for (Attribute attribute : attributes.getList()) {
				if (attribute.getType().equals(BudgetField.class.getCanonicalName())) {
					PaginatedList<Budget> budgets = this.fieldsBS.listBudgetsByInstance(instance);
					for (Budget budget : budgets.getList()) {
						Budget budgetCopy = new Budget();
						budgetCopy.setCreation(new Date());
						budgetCopy.setDeleted(false);
						budgetCopy.setLevelInstance(instanceClone);
						budgetCopy.setName(budget.getName());
						budgetCopy.setSubAction(budget.getSubAction());
						budgetCopy.setBudgetElement(budget.getBudgetElement());
						budgetCopy.setCommitted(budget.getCommitted());
						budgetCopy.setRealized(budget.getRealized());
						this.persist(budgetCopy);
					}
				} else if (attribute.getType().equals(ActionPlanField.class.getCanonicalName())) {
					PaginatedList<ActionPlan> list = this.fieldsBS.listActionPlansByInstance(instance);
					for (ActionPlan actionPlan : list.getList()) {
						ActionPlan actionPlanCopy = new ActionPlan();
						actionPlanCopy.setBegin(actionPlan.getBegin());
						actionPlanCopy.setChecked(actionPlan.isChecked());
						actionPlanCopy.setNotChecked(actionPlan.isNotChecked());
						actionPlanCopy.setCreation(new Date());
						actionPlanCopy.setDeleted(false);
						actionPlanCopy.setDescription(actionPlan.getDescription());
						actionPlanCopy.setEnd(actionPlan.getEnd());
						actionPlanCopy.setLevelInstance(instanceClone);
						actionPlanCopy.setResponsible(actionPlan.getResponsible());
						actionPlanCopy.setUser(actionPlan.getUser());
						this.persist(actionPlanCopy);
					}
				}
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("Unexpected runtime error", e);
			return false;
		}
	}

	/**
	 * Buscar o id da estrutura pelo level.
	 *
	 * @param level
	 *            Level para buscar o id da estrutura.
	 * @return id Id da estrutura encontrada.
	 */
	public BigInteger retrieveStructureIdByLevel(StructureLevel level) {
		String sql = String.format("SELECT structure_id AS id FROM fpdi_structure_level WHERE id=%d", level.getId());
		Query query = this.dao.newSQLQuery(sql);
		return (BigInteger) query.getSingleResult();
	}

	/**
	 * Buscar o próximo level de uma estrutura.
	 *
	 * @param structure
	 *            Estrutura.
	 * @param sequence
	 *            Número de sequência.
	 * @return structureLevel Próximo level da estrutura encontrado.
	 */
	public StructureLevel retrieveNextLevel(Structure structure, int sequence) {
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("structure", structure));
		criteria.add(Restrictions.eq("sequence", sequence));
		return (StructureLevel) criteria.uniqueResult();
	}

	/**
	 * Gerar instâncias de um level meta.
	 *
	 * @param parent
	 *            Id da instância do level pai.
	 * @param plan
	 *            Plano de metas.
	 * @param level
	 *            Level.
	 * @param name
	 *            Nome.
	 * @param responsible
	 *            Responsável.
	 * @param description
	 *            Descrição.
	 * @param expected
	 *            Valor esperado.
	 * @param minimun
	 *            Valor mínimo.
	 * @param maximum
	 *            Valor máximo.
	 * @param periodicity
	 *            Periodicidade.
	 * @param beginDate
	 *            Data de inicio.
	 * @param endDate
	 *            Data de fim.
	 * @return void
	 *
	 */
	public void goalsGenerateByPeriodicity(Long parent, Plan plan, StructureLevel level, String name, String manager,
			String responsible, String description, double expected, double minimum, double maximum, String periodicity,
			Date beginDate, Date endDate) {
		GoalDTO dto = new GoalDTO(parent, plan, level, name, manager, responsible, description, expected, minimum, maximum,
				periodicity, beginDate, endDate);
		goalTask.add(dto);
	}

	/**
	 * Enfileira a instância de nível para atualização das médias e agregações.
	 */
	public void updateLevelValues(StructureLevelInstance levelInstance) {
		this.onLevelInstanceUpdateTask.add(levelInstance);
	}

	/**
	 * Salvar as instâncias dos leveis indicador que são agregados a um level
	 * indicador agregado.
	 */
	public void saveIndicators(List<AggregateIndicator> indicators) {
		for (AggregateIndicator indicator : indicators) {
			AggregateIndicator existent = this.retrieveAggregateIndicator(indicator.getIndicator(),
					indicator.getAggregate());
			if (existent == null) {
				indicator.setId(null);
				indicator.setAggregate(this.retrieveLevelInstance(indicator.getAggregate().getId()));
				this.persist(indicator);
			} else {
				existent.setPercentage(indicator.getPercentage());
				existent.setDeleted(indicator.isDeleted());
				this.persist(existent);
			}

		}
	}

	/**
	 * Buscar instância de um level indicador agregado.
	 *
	 * @param indicator
	 *            Instância de um level indicador.
	 * @param aggregate
	 *            Instância de um level indicador agregado.
	 * @return aggregateIndicator Instância do level encontrado.
	 */
	public AggregateIndicator retrieveAggregateIndicator(StructureLevelInstance indicator,
			StructureLevelInstance aggregate) {
		Criteria criteria = this.dao.newCriteria(AggregateIndicator.class);
		criteria.add(Restrictions.eq("indicator", indicator));
		criteria.add(Restrictions.eq("aggregate", aggregate));
		return (AggregateIndicator) criteria.uniqueResult();
	}

	/**
	 * Listar instâncias do level eixo estratégico.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias do level encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listStrategicAxis(PlanMacro macro, Plan plan, Integer page,
			Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PAGESIZE;
		}
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.isNull("parent"));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		Criteria counting = this.dao.newCriteria(StructureLevelInstance.class);
		counting.createAlias("plan", "plan", JoinType.INNER_JOIN);
		counting.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.isNull("parent"));
		counting.add(Restrictions.eq("macro.archived", false));
		counting.setProjection(Projections.countDistinct("id"));

		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
			counting.add(Restrictions.eq("plan", plan));
		}
		if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
			counting.add(Restrictions.eq("plan.parent", macro));
		}

		List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");
		Long total = this.filter.filterAndFind(counting, "macro.company");
		result.setList(list);
		result.setTotal(total);

		return result;
	}

	public long countStrategicAxis(PlanMacro macro, Plan plan) {
		
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.isNull("parent"));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.add(Restrictions.eq("macro.company", domain.get().getCompany()));
		criteria.setProjection(Projections.countDistinct("id"));

		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		}
		if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}
		
		return (long) criteria.uniqueResult();
	}

	/**
	 * Listar instâncias dos leveis meta.
	 *
	 * @param void
	 *
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listGoals() {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("level", "level", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("level.goal", true));
		criteria.add(Restrictions.eq("deleted", false));

		List<StructureLevelInstance> list = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		result.setList(list);
		result.setTotal((long) list.size());
		return result;
	}

	/**
	 * Listar metas, de acordo a um plano macro, plano, pagina e tamanho de
	 * pagina
	 *
	 * @param macro,
	 *            plano macro das metas desejadas
	 * @param plan,
	 *            plano das metas desejadas
	 * @param indicator,
	 *            indicador das metas desejadas
	 * @param page,
	 *            número da página das metas
	 * @param pageSize,
	 *            tamanho da página desejada
	 * @return
	 */
	public PaginatedList<StructureLevelInstance> listGoals(PlanMacro macro, Plan plan, StructureLevelInstance indicator,
			Integer page, Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PAGESIZE;
		}
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.filterByResponsibleCriteria(this.userSession.getUser().getId());
		criteria.add(Restrictions.eq("level.goal", true));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.setProjection(Projections.property("levelInstance"));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		Criteria counting = this.filterByResponsibleCriteria(this.userSession.getUser().getId());
		counting.add(Restrictions.eq("level.goal", true));
		counting.add(Restrictions.eq("macro.archived", false));
		counting.setProjection(Projections.countDistinct("levelInstance.id"));

		if (plan != null) {
			criteria.add(Restrictions.eq("levelInstance.plan", plan));
			counting.add(Restrictions.eq("levelInstance.plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
			counting.add(Restrictions.eq("plan.parent", macro));
		}
		if (indicator != null) {
			criteria.add(Restrictions.eq("levelInstance.parent", indicator.getId()));
			counting.add(Restrictions.eq("levelInstance.parent", indicator.getId()));
		}

		List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");
		Long total = this.filter.filterAndFind(counting, "macro.company");
		result.setList(list);
		result.setTotal(total);
		return result;
	}
		
	/**
	 * Listar instâncias dos levels meta pelo plano macro e/ou plano de metas considerando o
	 * responsável logado
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @param indicator
	 *            Instância de um level indicador.
	 * @return result Lista de instâncias dos levels encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listGoalsByResponsible(PlanMacro macro, Plan plan) {
		Set<Long> structurelevelIds = this.retrieveChildResponsibleIds();
		if (structurelevelIds.isEmpty()) {
			return new PaginatedList<>(new ArrayList<>(0), 0L);
		}
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
			.createAlias("level", "level", JoinType.INNER_JOIN)
			.createAlias("plan", "plan", JoinType.INNER_JOIN)
			.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
			.add(Restrictions.in("id", structurelevelIds))
			.add(Restrictions.eq("level.goal", true));
		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}
		List<StructureLevelInstance> structureInstances = this.filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company");
		return new PaginatedList<>(structureInstances, (long) structureInstances.size());
	}

	/**
	 * Listar instâncias dos levels meta pelo plano de metas.
	 * 
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias dos levels encontradas.
	 */
	public List<StructureLevelInstance> listGoalsByPlan(Plan plan) {
		if(plan == null){
			throw new NullPointerException("The goals plan cannot be null");
		}
			Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("level", "level", JoinType.INNER_JOIN)
				.createAlias("plan", "plan", JoinType.INNER_JOIN)
				.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
				.add(Restrictions.eq("level.goal", true))
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("macro.archived", false));
			criteria.add(Restrictions.eq("plan", plan));

			List<StructureLevelInstance> structureInstances = this.filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company");
			return structureInstances;
	}

	/**
	 * Listar instâncias dos levels meta pelo plano macro e/ou plano de metas considerando um 
	 * id de usuário responsável
	 *
	 * @param userId
	 *            Instância de um responsável.
	 * @return result Lista de instâncias dos levels encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listLevelsByResponsible(Long userId, DefaultParams params) {
		Criteria criteria = this.filterByResponsibleCriteria(userId);

		if (params.isSorting()) {
			criteria.addOrder(params.getSortOrder());
		} else {
			criteria.addOrder(Order.asc("level.name"));
		}

		int page = params.getPage();
		int pageSize = params.getPageSize();
		criteria.setFirstResult((page - 1) * pageSize)
				.setMaxResults(pageSize);

		Criteria counting = this.filterByResponsibleCriteria(userId);
		counting.setProjection(Projections.countDistinct("levelInstance.id"));
		
		List<AttributeInstance> attributeInstances = this.filter.filterAndList(criteria, AttributeInstance.class, "macro.company");
		Long total = this.filter.filterAndFind(counting, "macro.company");
		
		List<StructureLevelInstance> structureLevelInstances = new ArrayList<StructureLevelInstance>(attributeInstances.size());
		
		for (AttributeInstance ai : attributeInstances) {
			structureLevelInstances.add(ai.getLevelInstance());
		}
		
		return new PaginatedList<>(structureLevelInstances, total);
	}

	/**
	 * Cria uma criteria com uma query para filtrar se o usuário de certo id é o
	 * responsável pelos níveis.
	 *
	 * @return criteria
	 */
	public Criteria filterByResponsibleCriteria(Long userId) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.add(Restrictions.eq("levelInstance.deleted", false));
		criteria.add(Restrictions.eq("attribute.type", ResponsibleField.class.getCanonicalName()));
		criteria.add(Restrictions.eq("value", userId.toString()));

		return criteria;
	}

	/**
	 * Retorna uma lista dos IDs dos níveis nos quais o usuario logado eh responsavel e seus filhos (se houver)
	 *
	 * @return allIds, lista de IDs dos níveis
	 */
	public Set<Long> retrieveChildResponsibleIds() {
		Set<Long> allIds = new HashSet<>();

		Criteria criteriaLvl1 = this.filterByResponsibleCriteria(this.userSession.getUser().getId());

		criteriaLvl1.setProjection(Projections.property("levelInstance.id"));
		List<Long> idsLevel1 = this.filter.filterAndList(criteriaLvl1, Long.class, "macro.company");
		if (idsLevel1.isEmpty()) {
			return allIds;
		}
		allIds.addAll(idsLevel1);

		Criteria criteriaLvl2 = this.dao.newCriteria(StructureLevelInstance.class);
		criteriaLvl2.add(Restrictions.in("parent", idsLevel1))
			.add(Restrictions.eq("deleted", false))
			.setProjection(Projections.property("id"));
		List<Long> idsLevel2 = this.dao.findByCriteria(criteriaLvl2, Long.class);
		if (idsLevel2.isEmpty()) {
			return allIds;
		}
		allIds.addAll(idsLevel2);

		Criteria criteriaLvl3 = this.dao.newCriteria(StructureLevelInstance.class);
		criteriaLvl3.add(Restrictions.in("parent", idsLevel2))
			.add(Restrictions.eq("deleted", false))
			.setProjection(Projections.property("id"));
		List<Long> idsLevel3 = this.dao.findByCriteria(criteriaLvl3, Long.class);
		if (idsLevel3.isEmpty()) {
			return allIds;
		}
		allIds.addAll(idsLevel3);

		Criteria criteriaLvl4 = this.dao.newCriteria(StructureLevelInstance.class);
		criteriaLvl4.add(Restrictions.in("parent", idsLevel3))
			.add(Restrictions.eq("deleted", false))
			.setProjection(Projections.property("id"));
		List<Long> idsLevel4 = this.dao.findByCriteria(criteriaLvl4, Long.class);
		allIds.addAll(idsLevel4);

		return allIds;
	}

	/**
	 * Listar instâncias dos leveis meta pelo indicador sem filtrar pelo
	 * responsável.
	 *
	 * @param indicator
	 *            Instância de um level indicador.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listGoalsByIndicatorWithoutResponsible(
			StructureLevelInstance indicator) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("level.goal", true));

		if (indicator != null) {
			criteria.add(Restrictions.eq("parent", indicator.getId()));
		}

		List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");
		result.setList(list);
		result.setTotal((long) list.size());
		return result;
	}

	/**
	 * Listar instâncias dos leveis meta pelo indicador sem filtrar pelo
	 * responsável.
	 *
	 * @param indicator
	 *            Instância de um level indicador.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listGoalsByIndicatorWithoutResponsible(
			StructureLevelInstance indicator, Integer page, Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PAGESIZE;
		}
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		/*Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("level.goal", true));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		*/

		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.level", "level", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("attribute.finishDate", true));
		criteria.add(Restrictions.eq("levelInstance.deleted", false));
		criteria.add(Restrictions.eq("level.goal", true));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		criteria.addOrder(Order.asc("valueAsDate"));



		Criteria counting = this.dao.newCriteria(StructureLevelInstance.class);
		counting.createAlias("level", "level", JoinType.INNER_JOIN);
		counting.createAlias("plan", "plan", JoinType.INNER_JOIN);
		counting.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("level.goal", true));
		counting.setProjection(Projections.countDistinct("id"));

		if (indicator != null) {
			criteria.add(Restrictions.eq("levelInstance.parent", indicator.getId()));
			counting.add(Restrictions.eq("parent", indicator.getId()));
		}

		List<AttributeInstance> attList = this.dao.findByCriteria(criteria, AttributeInstance.class);
		List<StructureLevelInstance> list = new ArrayList<StructureLevelInstance>();
		for(AttributeInstance attribute : attList){
			list.add(attribute.getLevelInstance());
		}

		/*List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");*/
		Long total = this.filter.filterAndFind(counting, "macro.company");
		result.setList(list);
		result.setTotal(total);
		return result;
	}

	/**
	 * Listar instâncias dos leveis indicador.
	 *
	 * @param void
	 *
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listAllIndicators() {
		Criteria criteria = this.dao.newCriteria(PlanMacro.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("archived", false));
		List<PlanMacro> plansmacro = this.filter.filterAndList(criteria, PlanMacro.class);
		
		List<StructureLevelInstance> indicators = new ArrayList<>();
		
		for (PlanMacro planMacro : plansmacro) {
			indicators.addAll(listIndicators(planMacro).getList());
		}

		return new PaginatedList<>(indicators, (long) indicators.size());
	}

	 public Map<String, Long> countPlansAndIndicators() {
        Criteria criteria = this.dao.newCriteria(PlanMacro.class)
                .add(Restrictions.eq("deleted", false))
                .add(Restrictions.eq("archived", false));
        List<PlanMacro> plansmacro = this.filter.filterAndList(criteria, PlanMacro.class);

        long indicatorCounter = 0;
        long planCounter = plansmacro.size();

        for (PlanMacro planMacro : plansmacro) {
            indicatorCounter += countIndicators(planMacro);
        }

        Map<String, Long> result = new HashMap<>();
        result.put("planCount", planCounter);
        result.put("indicatorCount", indicatorCounter);

        return result;
    }
	
	public long countAllIndicators() {
		Criteria criteria = this.dao.newCriteria(PlanMacro.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("archived", false));
		List<PlanMacro> plansmacro = this.filter.filterAndList(criteria, PlanMacro.class);

		long counter = 0;
		for (PlanMacro planMacro : plansmacro) {
			counter += countIndicators(planMacro);
		}

		return counter;
	}

	/**
	 * Listar instâncias dos leveis indicador pelo plano macro.
	 *
	 * @param macro
	 *            Plano macro.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listIndicators(PlanMacro macro) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(Plan.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("parent", macro));

		List<Plan> plans = this.dao.findByCriteria(criteria, Plan.class);

		List<StructureLevelInstance> list = new ArrayList<>();
		for (Plan plan : plans) {
			list.addAll(this.listIndicators(plan).getList());
		}

		result.setList(list);
		result.setTotal((long) list.size());

		return result;
	}

	public Long countIndicators(PlanMacro macro) {
		Criteria criteria = this.dao.newCriteria(Plan.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("parent", macro));

		List<Plan> plans = this.dao.findByCriteria(criteria, Plan.class);

		long counter = 0;
		for (Plan plan : plans) {
			counter += countIndicators(plan);
		}

		return counter;
	}

	/**
	 * Listar instâncias dos orçamentos pelo plano macro e/ou plano de metas.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias dos orçamentos encontrados.
	 */
	public Long countBudgets(PlanMacro macro, Plan plan) {
		Criteria criteria = this.dao.newCriteria(Budget.class)
				.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN)
				.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false))
				.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
				.add(Restrictions.eq("macro.archived", false));
		if (plan != null) {
			criteria.add(Restrictions.eq("levelInstance.plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		} else {
			criteria.add(Restrictions.eq("macro.company", domain.get().getCompany()));
		}
		
		criteria.setProjection(Projections.countDistinct("id"));

		return (Long) criteria.uniqueResult();
	}

	
	/**
	 * Listar instâncias dos leveis indicador pelo plano de metas.
	 *
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listIndicators(Plan plan) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("indicator", true));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);


		List<StructureLevelInstance> list = new ArrayList<>();
		for (StructureLevel level : levelList) {
			/*Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class)
			.createAlias("plan", "plan", JoinType.INNER_JOIN)
			.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
			.add(Restrictions.eq("macro.archived", false))
			.add(Restrictions.eq("deleted", false))
			.add(Restrictions.eq("level", level))
			.add(Restrictions.eq("plan", plan));*/
			Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
					criteria2.createAlias("plan", "plan", JoinType.INNER_JOIN);
					criteria2.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
					criteria2.add(Restrictions.eq("macro.archived", false));
					criteria2.add(Restrictions.eq("deleted", false));
					criteria2.add(Restrictions.eq("level", level));
					criteria2.add(Restrictions.eq("plan", plan));
			list.addAll(this.dao.findByCriteria(criteria2, StructureLevelInstance.class));
		}

		result.setList(list);
		result.setTotal((long) list.size());

		return result;
	}

	public long countIndicators(Plan plan) {
		Criteria criteria = this.dao.newCriteria(StructureLevel.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("indicator", true));
		List<StructureLevel> levelList = this.dao.findByCriteria(criteria, StructureLevel.class);
		if (levelList.isEmpty()) {
			return 0;
		}

		List<Long> levelIds = levelList.stream().map(l -> l.getId()).collect(Collectors.toList());
		
		Criteria criteria2 = this.dao.newCriteria(StructureLevelInstance.class);
				criteria2.createAlias("plan", "plan", JoinType.INNER_JOIN);
				criteria2.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
				criteria2.add(Restrictions.eq("macro.archived", false));
				criteria2.add(Restrictions.eq("deleted", false));
				criteria2.add(Restrictions.in("level.id", levelIds));
				criteria2.add(Restrictions.eq("plan", plan));
				criteria2.setProjection(Projections.countDistinct("id"));

		return (long) criteria2.uniqueResult();
	}

	/**
	 * Listar instâncias dos levels indicador pelo plano macro e/ou plan.
	 *
	 * @param macro
	 *            Plano de macro.
	 * @param plan
	 *            Plano de metas.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listIndicatorsByMacroAndPlan(PlanMacro macro, Plan plan) {
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
			.createAlias("plan", "plan", JoinType.INNER_JOIN)
			.createAlias("plan.parent", "macro", JoinType.INNER_JOIN)
			.createAlias("level", "level", JoinType.INNER_JOIN)
			.add(Restrictions.eq("level.indicator", true))
			.add(Restrictions.eq("macro.archived", false))
			.add(Restrictions.eq("deleted", false));
		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}
		List<StructureLevelInstance> structureInstances = this.filter.filterAndList(criteria,
				StructureLevelInstance.class, "macro.company");
		return new PaginatedList<>(structureInstances, (long) structureInstances.size());
	}

	/**
	 * Listar instâncias dos leveis indicador pelo plano macro e/ou plano de
	 * metas e/ou instância de um level ibjetivo.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @param objective
	 *            Instância de um level objetivo.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listIndicators(PlanMacro macro, Plan plan,
			StructureLevelInstance objective) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("plan", "plan", JoinType.INNER_JOIN).createAlias("level", "level", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false)).add(Restrictions.eq("level.indicator", true));
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("macro.archived", false));
		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}
		if (objective != null) {
			criteria.add(Restrictions.eq("parent", objective.getId()));
		}

		List<StructureLevelInstance> indicatorsList = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		indicatorsList = this.filterByResponsible(indicatorsList);
		result.setList(indicatorsList);
		result.setTotal((long) indicatorsList.size());
		return result;
	}

	/**
	 * Listar instâncias dos leveis indicador pelo plano macro e/ou plano de
	 * metas e/ou instância de um level ibjetivo.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @param objective
	 *            Instância de um level objetivo.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listIndicators(PlanMacro macro, Plan plan,
			StructureLevelInstance objective, Integer page, Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PAGESIZE;
		}
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.filterByResponsibleCriteria(this.userSession.getUser().getId());
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("level.indicator", true));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		criteria.setProjection(Projections.property("levelInstance"));
		Criteria counting = this.filterByResponsibleCriteria(this.userSession.getUser().getId());
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("level.indicator", true));
		counting.add(Restrictions.eq("macro.archived", false));
		counting.setProjection(Projections.countDistinct("levelInstance.id"));

		if (plan != null) {
			criteria.add(Restrictions.eq("levelInstance.plan", plan));
			counting.add(Restrictions.eq("levelInstance.plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
			counting.add(Restrictions.eq("plan.parent", macro));
		}
		if (objective != null) {
			criteria.add(Restrictions.eq("levelInstance.parent", objective.getId()));
			counting.add(Restrictions.eq("levelInstance.parent", objective.getId()));
		}

		result.setList(this.dao.findByCriteria(criteria, StructureLevelInstance.class));
		result.setTotal((long) counting.uniqueResult());
		return result;
	}

	/**
	 * Listar objetivos dos leveis eixo temáticos pelo plano macro e/ou plano de
	 * metas e/ou instância de um level eixo tématico.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @param objective
	 *            Instância de um level objetivo.
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listObjectivesByThematicAxis(PlanMacro macro, Plan plan,
			StructureLevelInstance thematicAxis) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("plan", "plan", JoinType.INNER_JOIN).createAlias("level", "level", JoinType.INNER_JOIN)
				.add(Restrictions.eq("deleted", false)).add(Restrictions.eq("level.objective", true));
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("macro.archived", false));
		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}
		if (thematicAxis != null) {
			criteria.add(Restrictions.eq("parent", thematicAxis.getId()));
		}

		List<StructureLevelInstance> objectiveList = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		objectiveList = this.filterByResponsible(objectiveList);
		result.setList(objectiveList);
		result.setTotal((long) objectiveList.size());
		return result;
	}

	/**
	 * Listar instâncias dos leveis objetivo pelo plano macro e/ou plano de
	 * metas.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas
	 * @return result Lista de instâncias dos leveis encontradas.
	 */
	public PaginatedList<StructureLevelInstance> listObjectives(PlanMacro macro, Plan plan) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.createAlias("plan", "plan", JoinType.INNER_JOIN)
				.createAlias("level", "level", JoinType.INNER_JOIN)
				.add(Restrictions.eq("level.objective", true));

		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
		} else if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
		}else {
			criteria.createAlias("plan.parent", "planMacro", JoinType.INNER_JOIN);
			criteria.add(Restrictions.eq("planMacro.company",this.domain.get().getCompany()));
		}


		List<StructureLevelInstance> objectivesList = this.dao.findByCriteria(criteria, StructureLevelInstance.class);
		objectivesList = this.filterByResponsible(objectivesList);
		result.setList(objectivesList);
		result.setTotal((long) objectivesList.size());
		return result;
	}

	/**
	 * Listar as instâncias pais de uma instância de um level.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return parents Lista de instâncias dos leveis encontradas.
	 */
	public List<StructureLevelInstance> getParents(StructureLevelInstance levelInstance) {
		List<StructureLevelInstance> parents = new ArrayList<StructureLevelInstance>();
		boolean haveParent = false;
		if (levelInstance.getParent() != null) {
			haveParent = true;
			while (haveParent) {
				levelInstance = this.retrieveLevelInstance(levelInstance.getParent());
				levelInstance.setResponsibleId(getLevelInstanceResponsibleId(levelInstance));
				parents.add(levelInstance);
				if (levelInstance.getParent() == null)
					haveParent = false;
			}
		}
		return Lists.reverse(parents);
	}
	
	public Long getLevelInstanceResponsibleId(StructureLevelInstance levelInstance) {
		AttributeInstance attributeInstance = attrHelper.retrieveAttributeInstance(levelInstance, ResponsibleField.class);
		if (attributeInstance != null) {
			String value = attributeInstance.getValue();
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				LOGGER.error("Fail to parse user id to long value: attributeInstance.id = " + attributeInstance.getId());
			}
		}

		return null;
	}

	/**
	 * Retornar a periodicidade pela instância de um level.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return periodicity Periodicidade.
	 */
	public Periodicity getPeriodicityByInstance(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class)
				.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
				.add(Restrictions.eq("attribute.periodicityField", true))
				.add(Restrictions.eq("levelInstance", levelInstance));
		AttributeInstance attr = (AttributeInstance) criteria.uniqueResult();

		if (attr == null) {
			return null;
		} else {
			switch (attr.getValue()) {
			case "Diária":
				return Periodicity.DAILY;
			case "Semanal":
				return Periodicity.WEEKLY;
			case "Quinzenal":
				return Periodicity.FORTNIGHTLY;
			case "Mensal":
				return Periodicity.MONTHLY;
			case "Bimestral":
				return Periodicity.BIMONTHLY;
			case "Trimestral":
				return Periodicity.QUARTERLY;
			case "Semestral":
				return Periodicity.SEMIANNUAL;
			case "Anual":
				return Periodicity.ANNUAL;
			case "Bienal":
				return Periodicity.BIENNIAL;
			case "Trienal":
				return Periodicity.TRIENNIAL;
			default:
				return null;
			}
		}
	}

	/**
	 * Filtrar consultas pelo responsável pelas instâncias de um level.
	 *
	 * @param levelInstances
	 *            Lista de instâncias de um level.
	 * @return levelInstances Lista de instâncias dos leveis após filtro.
	 */
	public List<StructureLevelInstance> filterByResponsible(List<StructureLevelInstance> levelInstances) {
		if (this.userSession.getAccessLevel() < AccessLevels.COMPANY_ADMIN.getLevel()) {

			if(levelInstances.size()==0) {
				return levelInstances;
			}

			List<Long> idList= new ArrayList<>();
			List<StructureLevelInstance> list2 = new ArrayList<StructureLevelInstance>();

			for (StructureLevelInstance levelInst : levelInstances) {
				idList.add(levelInst.getId());
			}

			//pegar todos os filhos de uma vez para evitar muitos acessos ao banco
			Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
			.add(Restrictions.in("parent", idList))
			.add(Restrictions.eq("deleted", false));
			List<StructureLevelInstance> sonList = this.dao.findByCriteria(criteria, StructureLevelInstance.class);

			//pegar todos os attributeInstance de uma vez para evitar muitos acessos ao banco
			criteria = this.dao.newCriteria(AttributeInstance.class)
			.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
			.add(Restrictions.eq("attribute.type", ResponsibleField.class.getCanonicalName()))
			.add(Restrictions.eq("value",String.valueOf(this.userSession.getUser().getId())))
			.addOrder(Order.asc("levelInstance.id"));
			List<AttributeInstance> attrList = this.dao.findByCriteria(criteria, AttributeInstance.class);

			Map<Long, AttributeInstance> attrMap = new HashMap<>();
			for (AttributeInstance attr : attrList) {
				attrMap.put(attr.getLevelInstance().getId(), attr);
			}

			for (StructureLevelInstance levelInst : levelInstances) {
				boolean lvlAdd = false;
				StructureLevelInstance lvlI = levelInst;

				if(attrMap.get(levelInst.getId()) !=null) {
					list2.add(levelInst);
					lvlAdd = true;
					continue;
				}

				for (StructureLevelInstance stLvInst : sonList) {
					if(attrMap.get(stLvInst.getId()) !=null) {
						list2.add(stLvInst);
						lvlAdd = true;
						break;
					}
				}

				while (!lvlAdd && lvlI.getParent() != null) {
					lvlI = this.retrieveLevelInstance(lvlI.getParent());
					if(attrMap.get(lvlI.getId()) !=null) {
						list2.add(lvlI);
						lvlAdd=true;
					}
				}
			}
			levelInstances = list2;
		}
		return levelInstances;
	}

	/**
	 * Verificar se o usuário é responsável por algum level em um instituição.
	 *
	 * @param id
	 *            Id do usuário.
	 * @param company
	 *            Instituição.
	 * @return boolean Retorna verdadeiro se o usuário for responsável por algum
	 *         level.
	 */
	public boolean isUserResponsibleForSomeLevel(Long id, Company company) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance", "levelInstance", JoinType.INNER_JOIN);
		criteria.createAlias("levelInstance.plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "planMacro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("planMacro.company", company));
		criteria.add(Restrictions.eq("planMacro.deleted", false));
		criteria.add(Restrictions.eq("attribute.type", ResponsibleField.class.getCanonicalName()));
		criteria.add(Restrictions.eq("value", String.valueOf(id)));

		List<AttributeInstance> attributes = this.dao.findByCriteria(criteria, AttributeInstance.class);

		return attributes.isEmpty() ? false : true;
	}

	/**
	 * Listar instâncias dos leveis filhos para o filtro.
	 *
	 * @param plan
	 *            Plano de metas.
	 * @param parent
	 *            Id da instância do level pai
	 * @return result Lista de instâncias dos leveis filhos encontradas.
	 */
	public PaginatedList<StructureLevelInstance> retrieveLevelSonsForFilter(Plan plan, Long parent) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("macro.archived", false));
		if (plan != null)
			criteria.add(Restrictions.eq("plan", plan));
		if (parent != null)
			criteria.add(Restrictions.eq("parent", parent));
		else
			criteria.add(Restrictions.isNull("parent"));

		List<StructureLevelInstance> list = this.filter.filterAndList(criteria, StructureLevelInstance.class,
				"macro.company");
		result.setList(list);
		result.setTotal((long) list.size());
		return result;
	}

	/**
	 * Listar instâncias dos leveis filhos para o gráfico.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @param levelInstance
	 *            Id instância de um level.
	 * @return result Lista de instâncias dos leveis filhos encontradas.
	 */
	public PaginatedList<StructureLevelInstance> retrieveLevelSonsForGraph(PlanMacro macro, Plan plan,
			Long levelInstance) {
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("macro.archived", false));

		if (levelInstance == null)
			criteria.add(Restrictions.isNull("parent"));
		else
			criteria.add(Restrictions.eq("parent", levelInstance));

		if (plan != null)
			criteria.add(Restrictions.eq("plan", plan));
		if (macro != null)
			criteria.add(Restrictions.eq("plan.parent", macro));

		result.setList(this.filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"));
		result.setTotal(
				(long) this.filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company").size());

		return result;
	}

	/**
	 * Listar instâncias dos leveis filhos para o gráfico.
	 *
	 * @param macro
	 *            Plano macro.
	 * @param plan
	 *            Plano de metas.
	 * @param levelInstance
	 *            Id instância de um level.
	 * @return result Lista de instâncias dos leveis filhos encontradas.
	 */
	public PaginatedList<StructureLevelInstance> retrieveLevelSonsForGraph(PlanMacro macro, Plan plan,
			Long levelInstance, Integer page, Integer pageSize) {
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PAGESIZE;
		}
		PaginatedList<StructureLevelInstance> result = new PaginatedList<>();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class);
		criteria.createAlias("plan", "plan", JoinType.INNER_JOIN);
		criteria.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("macro.archived", false));
		criteria.setFirstResult((page - 1) * pageSize);
		criteria.setMaxResults(pageSize);
		Criteria counting = this.dao.newCriteria(StructureLevelInstance.class);
		counting.createAlias("plan", "plan", JoinType.INNER_JOIN);
		counting.createAlias("plan.parent", "macro", JoinType.INNER_JOIN);
		counting.add(Restrictions.eq("deleted", false));
		counting.add(Restrictions.eq("macro.archived", false));
		counting.setProjection(Projections.countDistinct("id"));

		if (levelInstance == null) {
			criteria.add(Restrictions.isNull("parent"));
			counting.add(Restrictions.isNull("parent"));
		} else {
			criteria.add(Restrictions.eq("parent", levelInstance));
			counting.add(Restrictions.eq("parent", levelInstance));
		}

		if (plan != null) {
			criteria.add(Restrictions.eq("plan", plan));
			counting.add(Restrictions.eq("plan", plan));
		}
		if (macro != null) {
			criteria.add(Restrictions.eq("plan.parent", macro));
			counting.add(Restrictions.eq("plan.parent", macro));
		}

		result.setList(this.filter.filterAndList(criteria, StructureLevelInstance.class, "macro.company"));
		result.setTotal((long) this.filter.filterAndFind(counting, "macro.company"));

		return result;
	}

	/**
	 * Verificar se uma instância de um level indicador é agregado a outro.
	 *
	 * @param aggregate
	 *            Instância de um level indicador agregado.
	 * @return indicator Instância de um level indicador.
	 */
	public StructureLevelInstance isAggregating(StructureLevelInstance aggregated) {
		List<AggregateIndicator> list = this.helper.getAggregatedToIndicators(aggregated);
		if (list.isEmpty())
			return null;

		return list.get(0).getIndicator();
	}

	/**
	 * Buscar um level nos favoritos por usuário e empresa.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return favorite Level adicionado como favorito.
	 */
	public FavoriteLevelInstance retrieveFavoriteLevelInstance(StructureLevelInstance levelInstance) {
		CompanyUser companyUser = this.userBS.retrieveCompanyUser(this.userSession.getUser(), this.domain.get().getCompany());
		Criteria criteria = this.dao.newCriteria(FavoriteLevelInstance.class);
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("companyUser", companyUser));

		FavoriteLevelInstance favorite = (FavoriteLevelInstance) criteria.uniqueResult();
		return favorite;
	}

	/**
	 * Adicionar um level aos favoritos.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return favorite Level adicionado como favorito.
	 */
	public FavoriteLevelInstance saveFavoriteLevelInstance(StructureLevelInstance levelInstance) {
		FavoriteLevelInstance favorite = new FavoriteLevelInstance();
		CompanyUser companyUser = this.userBS.retrieveCompanyUser(this.userSession.getUser(), this.domain.get().getCompany());
		if (companyUser != null) {
			favorite.setLevelInstance(levelInstance);
			favorite.setCompanyUser(companyUser);
			favorite.setDeleted(false);
			this.persist(favorite);
		}

		return favorite;
	}

	/**
	 * Buscar os favoritos por usuário e empresa.
	 *
	 * @param macro
	 *            Plano macro.
	 * @return favorites Lista de favoritos.
	 */
	public PaginatedList<FavoriteLevelInstance> listFavoriteLevelInstances(PlanMacro macro) {
		CompanyUser companyUser = this.userBS.retrieveCompanyUser(this.userSession.getUser(), this.domain.get().getCompany());
		Criteria criteria = this.dao.newCriteria(FavoriteLevelInstance.class);
		criteria.createAlias("levelInstance", "levelInstance");
		criteria.createAlias("levelInstance.plan", "plan");
		criteria.add(Restrictions.eq("plan.parent", macro));
		criteria.add(Restrictions.eq("companyUser", companyUser));
		criteria.add(Restrictions.eq("deleted", false));

		PaginatedList<FavoriteLevelInstance> favorites = new PaginatedList<FavoriteLevelInstance>();
		favorites.setList(this.dao.findByCriteria(criteria, FavoriteLevelInstance.class));
		if (favorites.getList() == null)
			favorites.setList(new ArrayList<FavoriteLevelInstance>());
		return favorites;
	}

	/**
	 * Buscar os favoritos por uma instância de um level.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return favorites Lista de favoritos.
	 */
	public PaginatedList<FavoriteLevelInstance> listFavoriteByLevelInstance(StructureLevelInstance levelInstance) {
		Criteria criteria = this.dao.newCriteria(FavoriteLevelInstance.class);
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("deleted", false));

		PaginatedList<FavoriteLevelInstance> favorites = new PaginatedList<FavoriteLevelInstance>();
		favorites.setList(this.dao.findByCriteria(criteria, FavoriteLevelInstance.class));
		if (favorites.getList() == null)
			favorites.setList(new ArrayList<FavoriteLevelInstance>());
		return favorites;
	}

	/**
	 * Listar o leveis indicadores que estão agregados a um level
	 * indicador agregado.
	 *
	 * @param aggregate
	 *            Instância de um level indicador agregado.
	 * @return query Lista dos leveis indicadores.
	 */
	public List<AggregateIndicator> listAggregateIndicatorsByAggregate(StructureLevelInstance aggregate) {
		Criteria criteria = this.dao.newCriteria(AggregateIndicator.class);
		criteria.add(Restrictions.eq("indicator", aggregate));
		criteria.add(Restrictions.eq("deleted", false));
		return this.dao.findByCriteria(criteria, AggregateIndicator.class);
	}

	/**
	 * Listar as instãncias dos leveis detalhadas
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return query Lista das intâncias dos leveis.
	 */
	public List<StructureLevelInstanceDetailed> listLevelInstanceDetailed(StructureLevelInstance levelInstance) {
		int year = LocalDate.now().getYear();
		Criteria criteria = this.dao.newCriteria(StructureLevelInstanceDetailed.class);
		criteria.add(Restrictions.eq("deleted", false));
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("year", year));

		return this.dao.findByCriteria(criteria, StructureLevelInstanceDetailed.class);
	}

	/**
	 * Listar as instãncias dos leveis detalhadas. Inclusive deletados.
	 *
	 * @param levelInstance
	 *            Instância de um level.
	 * @return query Lista das intâncias dos leveis.
	 */
	public List<StructureLevelInstanceDetailed> listAllLevelInstancesDetailedByLevelInstances(List<StructureLevelInstance> levelInstances) {
		if (GeneralUtils.isEmpty(levelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(StructureLevelInstanceDetailed.class);
		criteria.add(Restrictions.in("levelInstance", levelInstances));
		return this.dao.findByCriteria(criteria, StructureLevelInstanceDetailed.class);
	}

	public List<ActionPlan> listAllActionPlansByLevelInstances(List<StructureLevelInstance> levelInstances) {
		if (GeneralUtils.isEmpty(levelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(ActionPlan.class);
		criteria.add(Restrictions.in("levelInstance", levelInstances));
		return this.dao.findByCriteria(criteria, ActionPlan.class);
	}

	public List<AggregateIndicator> listAllAggregateIndicatorsByLevelInstances(List<StructureLevelInstance> levelInstances) {
		if (GeneralUtils.isEmpty(levelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(AggregateIndicator.class);
		criteria.add(Restrictions.in("aggregate", levelInstances));
		criteria.add(Restrictions.in("indicator", levelInstances));
		return this.dao.findByCriteria(criteria, AggregateIndicator.class);
	}

	public List<Attachment> listAllAttachmentsByLevelInstances(List<StructureLevelInstance> levelInstances) {
		if (GeneralUtils.isEmpty(levelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(Attachment.class);
		criteria.add(Restrictions.in("levelInstance", levelInstances));
		return this.dao.findByCriteria(criteria, Attachment.class);
	}

	public List<LevelInstanceHistory> listAllLevelInstancesHistoryByLevelInstances(List<StructureLevelInstance> levelInstances) {
		if (GeneralUtils.isEmpty(levelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(LevelInstanceHistory.class);
		criteria.add(Restrictions.in("levelInstance", levelInstances));
		return this.dao.findByCriteria(criteria, LevelInstanceHistory.class);
	}

	public boolean checkHaveBudgetByLevel(StructureLevel level){
		PaginatedList<Attribute> attributeList = this.listAttributes(level);
		boolean haveBudget = false;
		for (Attribute atr : attributeList.getList()) {
			if (atr.getType().matches("org.forpdi.planning.attribute.types.BudgetField")) {
				haveBudget = true;
			}
		}
		return haveBudget;
	}

	public AttributeInstance getAttributeInstance(StructureLevelInstance levelInstance, Attribute attribute) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("attribute", attribute));
		criteria.add(Restrictions.eq("deleted", false));

		List<AttributeInstance> attributeInstance = this.dao.findByCriteria(criteria, AttributeInstance.class);
		return GeneralUtils.isEmpty(attributeInstance) ? null : attributeInstance.get(0);
	}
	
	public List<AttributeInstance> getAttributeInstacesValues(List<StructureLevelInstance> levelInstances, List<Attribute> attributes) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.add(Restrictions.in("levelInstance", levelInstances));
		criteria.add(Restrictions.in("attribute", attributes));
		criteria.add(Restrictions.eq("deleted", false));

		List<AttributeInstance> result = this.dao.findByCriteria(criteria, AttributeInstance.class);
		return result;
	}
	
	public List<AttributeInstance> listAttributeInstancesByIds(List<Long> ids) {
		if (GeneralUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("id", ids));
		
		return this.dao.findByCriteria(criteria, AttributeInstance.class);
	}
	
	public List<StructureLevelInstance> listStructureLevelInstancesByIds(List<Long> ids) {
		if (GeneralUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		
		Criteria criteria = this.dao.newCriteria(StructureLevelInstance.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.in("id", ids));
		
		return this.dao.findByCriteria(criteria, StructureLevelInstance.class);
	}
	
	public StructureLevelInstance.StructureLevelInstancePermissionInfo getStructureInstancePermissionInfo(
			StructureLevelInstance levelInstance) {
		Long userSessionId = userSession.getUser().getId();
		
		boolean isResponsibleForLevelInstance = false;
		if (levelInstance.getId() != null) {
			Long responsibleId = getLevelInstanceResponsibleId(levelInstance);
			isResponsibleForLevelInstance = userSessionId.equals(responsibleId);
		}

		boolean isResponsibleForSomeParent = hasPermissionToManageStructureInstanceParents(levelInstance);
	
		return new StructureLevelInstance.StructureLevelInstancePermissionInfo(userSession.getAccessLevel(),
				isResponsibleForLevelInstance, isResponsibleForSomeParent, levelInstance.getLevel().isGoal());
	}
	
	private boolean hasPermissionToManageStructureInstanceParents(StructureLevelInstance levelInstance) {
		if (userSession.getAccessLevel() >= AccessLevels.COMPANY_ADMIN.getLevel()) {
			return true;
		}
		
		Long userSessionId = userSession.getUser().getId();
		List<StructureLevelInstance> parents = getParents(levelInstance);

		for (StructureLevelInstance sli : parents) {
			if (userSessionId.equals(sli.getResponsibleId())) {
				return true;
			}
		}
		
		return false;
	}
}
