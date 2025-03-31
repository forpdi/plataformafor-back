package org.forpdi.planning.attribute;

import org.forpdi.core.common.Criteria;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.Restrictions;
import org.forpdi.planning.attribute.types.enums.FormatValue;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Classe com implementações de métodos para auxílios nas classes de negócio e
 * para as jobs assíncronas. Contempla regras relacionadas aos atributos.
 * 
 * @author Renato R. R. de Oliveira
 *
 */
@Service
public class AttributeHelper {

	@Autowired
	private HibernateDAO dao;

	/**
	 * Buscar a instância de um atributo.
	 * 
	 */
	public AttributeInstance retrieveAttributeInstance(StructureLevelInstance levelInstance, Attribute attribute) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		// .setProjection(Projections.property("id"));
		criteria.add(Restrictions.eq("levelInstance", levelInstance));
		criteria.add(Restrictions.eq("attribute", attribute));
		criteria.setMaxResults(1);
		// Logger.getLogger(this.getClass()).error("stop "+criteria.list().toString());


		AttributeInstance attributeInstance = (AttributeInstance) criteria.uniqueResult();
		if ((attribute.isExpectedField() || attribute.isMaximumField() || attribute.isMinimumField()
				|| attribute.isReachedField() || attribute.isReferenceField()) && attributeInstance != null) {
			AttributeInstance attr = this.retrieveFormatAttributeInstance(levelInstance.getParent());
			FormatValue formatValue = FormatValue.forAttributeInstance(attr);
			attributeInstance.setFormattedValue(attributeInstance.getValueAsNumber() != null
					? formatValue.format(attributeInstance.getValueAsNumber().toString())
					: "");
		}
		return attributeInstance;
	}

	/** Busca o atributo que define a formatação do valor de um nível. */
	public AttributeInstance retrieveFormatAttributeInstance(Long levelInstanceId) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class)
				.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
				.add(Restrictions.eq("attribute.formatField", true))
				.add(Restrictions.eq("levelInstance.id", levelInstanceId));
		criteria.setMaxResults(1);
		return (AttributeInstance) criteria.uniqueResult();
	}

	public AttributeInstance retrieveFormatAttributeInstance(StructureLevelInstance levelInstance) {
		return this.retrieveFormatAttributeInstance(levelInstance.getId());
	}

	/** Busca o atributo que define a polaridade do valor de um nível. */
	public AttributeInstance retrievePolarityAttributeInstance(Long levelInstanceId) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("levelInstance.id", levelInstanceId));
		criteria.add(Restrictions.eq("attribute.polarityField", true));
		criteria.setMaxResults(1);
		AttributeInstance attributeInstance = (AttributeInstance) criteria.uniqueResult();
		return attributeInstance;
	}

	public AttributeInstance retrievePolarityAttributeInstance(StructureLevelInstance levelInstance) {
		return this.retrievePolarityAttributeInstance(levelInstance.getId());
	}
	
	/**
	 * Recupera uma instancia de atributo pela classe do tipo do atributo
	 */
	public AttributeInstance retrieveAttributeInstance(StructureLevelInstance levelInstance,
			Class<? extends AttributeType> attributeType) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
				.add(Restrictions.eq("levelInstance", levelInstance))
				.add(Restrictions.eq("attribute.type", attributeType.getCanonicalName()));
		criteria.setMaxResults(1);	

		return (AttributeInstance) criteria.uniqueResult();
	}

	
	/**
	 * Recupera os campos especiais de meta de uma instância de nível.
	 * 
	 * @param levelInstance
	 *            Instância de nível que deseja recuperar o campo especial.
	 * @param boolField
	 *            O nome do campo booleano da tabela Attribute que indica qual campo
	 *            especial aquele campo é.
	 * @return Instância do atributo especial de meta dessa instância de nível.
	 */
	protected AttributeInstance retrieveSpecialAttribute(StructureLevelInstance levelInstance, String boolField) {
		Criteria criteria = this.dao.newCriteria(AttributeInstance.class);
		criteria.createAlias("attribute", "attribute", JoinType.INNER_JOIN)
				.add(Restrictions.eq("levelInstance", levelInstance))
				.add(Restrictions.eq("attribute." + boolField, true));
		criteria.setMaxResults(1);
		return (AttributeInstance) criteria.uniqueResult();
	}

	public AttributeInstance retrieveExpectedFieldAttribute(StructureLevelInstance levelInstance) {
		return this.retrieveSpecialAttribute(levelInstance, "expectedField");
	}

	public AttributeInstance retrieveMinimumFieldAttribute(StructureLevelInstance levelInstance) {
		return this.retrieveSpecialAttribute(levelInstance, "minimumField");
	}

	public AttributeInstance retrieveMaximumFieldAttribute(StructureLevelInstance levelInstance) {
		return this.retrieveSpecialAttribute(levelInstance, "maximumField");
	}

	public AttributeInstance retrieveReachedFieldAttribute(StructureLevelInstance levelInstance) {
		return this.retrieveSpecialAttribute(levelInstance, "reachedField");
	}

	public AttributeInstance retrieveFinishDateFieldAttribute(StructureLevelInstance levelInstance) {
		return this.retrieveSpecialAttribute(levelInstance, "finishDate");
	}

}
