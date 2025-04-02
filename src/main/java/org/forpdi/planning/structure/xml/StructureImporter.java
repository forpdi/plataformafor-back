package org.forpdi.planning.structure.xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.AbstractAttribute;
import org.forpdi.core.common.HibernateDAO;
import org.forpdi.core.common.HibernateDAO.TransactionalOperation;
import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.StaticAttributeLabels;
import org.forpdi.planning.attribute.types.ResponsibleField;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.fields.schedule.Schedule;
import org.forpdi.planning.fields.schedule.ScheduleStructure;
import org.forpdi.planning.structure.Structure;
import org.forpdi.planning.structure.StructureLevel;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;


public class StructureImporter implements TransactionalOperation {

	private static final Logger LOG = LoggerFactory.getLogger(StructureImporter.class);

	private final SAXReader saxReader;

	protected StructureBeans.Structure readedStructure;
	private Structure savedStructure;
	
	/**
	 * Importa estrutura do plano (arquivo xml)
	 * @param xmlIS
	 * 		Arquivo que contem a estrutura
	 * @param schema
	 * 			Schema para validação
	 * @throws Exception
	 */
	public StructureImporter(InputStream xmlIS, String schema) throws Exception {
		StringBuffer xmlFile = new StringBuffer();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(xmlIS, Charset.forName("UTF-8")));
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			xmlFile.append(line).append("\n");
		}
		String xml = xmlFile.toString();

		/*
		 * TODO Publicar schema para validação. Validator validator =
		 * SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
		 * .newSchema(new StreamSource(new
		 * CharArrayReader(schema.toCharArray()))).newValidator(); Source source
		 * = new StreamSource(new CharArrayReader(xml.toCharArray()));
		 * validator.validate(source);
		 */

		
		this.saxReader = createSaxReader();
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes())) {
			this.readedStructure = parseInternal(bais);
		} catch (DocumentException e) {
			throw new Exception("Falha ao processar o arquivo de estruturas. (Obs: Entidades externas não são permitidas)");
		}
	}

	private SAXReader createSaxReader()
			throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();

		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);


		factory.setXIncludeAware(false);
		factory.setNamespaceAware(true);

		SAXParser parser = factory.newSAXParser();
		SAXReader saxReader = new SAXReader(parser.getXMLReader());

		saxReader.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException ex) throws SAXException {
				LOG.warn(ex.getMessage(), ex);
			}

			@Override
			public void fatalError(SAXParseException ex) throws SAXException {
				LOG.error(ex.getMessage(), ex);
			}

			@Override
			public void error(SAXParseException ex) throws SAXException {
				LOG.error(ex.getMessage(), ex);
			}
		});
		
		return saxReader;
	}

	/**
	 * Inicia a sessão
	 * @param dao 
	 * 			Sessão que será iniciada
	 */
	@Override
	public void execute(HibernateDAO dao) throws HibernateException {
		AttributeTypeMap types = new AttributeTypeMap();
		Structure structure = new Structure();
		structure.setDeleted(false);
		structure.setDescription(this.readedStructure.description);
		structure.setName(this.readedStructure.name);
		dao.persist(structure);

		for (StructureBeans.StructureLevel rawLevel : this.readedStructure.levels) {
			StructureLevel level = new StructureLevel();
			level.setDeleted(false);
			level.setDescription(rawLevel.description);
			level.setName(rawLevel.name);
			level.setSequence(rawLevel.sequence);
			level.setLeaf(this.readedStructure.levels.size() == (rawLevel.sequence + 1));
			level.setStructure(structure);
			level.setGoal(rawLevel.goal);
			level.setObjective(rawLevel.objective);
			level.setIndicator(rawLevel.indicator);
			dao.persist(level);

			for (StructureBeans.Attribute rawAttr : rawLevel.attributes) {
				Attribute attr = new Attribute();
				attr.setDeleted(false);
				attr.setDescription(rawAttr.description);
				attr.setLabel(rawAttr.label);
				attr.setRequired(rawAttr.required);
				attr.setVisibleInTables(rawAttr.visibleInTables);
				attr.setFinishDate(rawAttr.finishDate);
				attr.setExpectedField(rawAttr.expectedField);
				attr.setJustificationField(rawAttr.justificationField);
				attr.setMinimumField(rawAttr.minimumField);
				attr.setMaximumField(rawAttr.maximumField);
				attr.setReachedField(rawAttr.reachedField);
				attr.setReferenceField(rawAttr.referenceField);
				attr.setPolarityField(rawAttr.polarityField);
				attr.setFormatField(rawAttr.formatField);
				attr.setPeriodicityField(rawAttr.periodicityField);
				attr.setBeginField(rawAttr.beginField);
				attr.setEndField(rawAttr.endField);
				attr.setBscField(rawAttr.bscField);
				attr.setLevel(level);
				attr.setType(types.get(rawAttr.type));
				dao.persist(attr);

				if (attr.getType().equals("org.forpdi.planning.attribute.types.ScheduleField")) {
					Schedule schedule = new Schedule();
					schedule.setDeleted(false);
					schedule.setAttributeId(attr.getId());
					schedule.setIsDocument(false);
					schedule.setPeriodicityEnable(rawAttr.periodicity);
					dao.persist(schedule);
					if (rawAttr.scheduleValues.size() > 0) {
						for (int i = 0; i < rawAttr.scheduleValues.size(); i++) {
							ScheduleStructure scheduleStructure = new ScheduleStructure();
							scheduleStructure.setDeleted(false);
							scheduleStructure.setSchedule(schedule);
							scheduleStructure.setLabel(rawAttr.scheduleValues.get(i).label);
							scheduleStructure.setType(rawAttr.scheduleValues.get(i).type);
							dao.persist(scheduleStructure);
						}
					}
				}

				if (attr.getType().equals("org.forpdi.planning.attribute.types.SelectField")) {
					if (rawAttr.optionsField.size() > 0) {
						for (int i = 0; i < rawAttr.optionsField.size(); i++) {
							OptionsField optionsField = new OptionsField();
							optionsField.setDeleted(false);
							optionsField.setAttributeId(attr.getId());
							optionsField.setCreation(new Date());
							optionsField.setDocument(false);
							optionsField.setLabel(rawAttr.optionsField.get(i).label);
							dao.persist(optionsField);
						}
					}
				}
			}
		}

		this.savedStructure = structure;
	}

	/**
	 * Retorna o arquivo que contém a estrutura do plano
	 * @return Structure savedStructure
	 * 				Estrutura do plano
	 */
	public Structure getImportedStructure() {
		return this.savedStructure;
	}
	
	public StructureBeans.Structure getReadedStructure() {
		return this.readedStructure;
	}

	/**
	 * Monta a estrutura pelo arquivo do xml
	 * @param is
	 * 		Manipulador de arquivo
	 * @return
	 * @throws DocumentException
	 */
	public StructureBeans.Structure parseInternal(InputStream is) throws DocumentException {
		StructureBeans.Structure struct = new StructureBeans.Structure();
		Document document = this.saxReader.read(new InputStreamReader(is, Charset.forName("UTF-8")));

		Element root = document.getRootElement();
		if (!root.getQName().getName().equals("structure")) {
			throw new RuntimeException("Wrong element: " + root.getQName());
		}

		Iterator<Element> children = root.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("structureName")) {
				struct.name = ((Element) n).getTextTrim();
			} else if (childName.equals("structureDescription")) {
				struct.description = ((Element) n).getTextTrim();
			} else if (childName.equals("structureLevels")) {
				struct.levels = this.parseLevels((Element) n);
			}
		}
		return struct;
	}
	/**
	 * Monta a estrutura dos levels
	 * @param el
	 * 			Elemento que contém estrutura
	 * @return
	 */
	private List<StructureBeans.StructureLevel> parseLevels(Element el) {
		List<StructureBeans.StructureLevel> levels = new LinkedList<StructureBeans.StructureLevel>();

		Iterator<Element> children = el.elementIterator();
		int sequence = 0;
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("level")) {
				StructureBeans.StructureLevel level;
				do {
					level = this.parseLevel((Element) n);
					level.sequence = sequence;
					levels.add(level);
					sequence++;
					n = level.child;
				} while (!level.leaf);
			}
		}

		return levels;
	}
	
	/**
	 * Monta a estrutura das metas
	 * @param el
	 * 		Elemento que contém estrutura	
	 * @return
	 */
	private StructureBeans.StructureLevel parseLevel(Element el) {
		StructureBeans.StructureLevel level = new StructureBeans.StructureLevel();
		level.leaf = true;

		Iterator<Element> children = el.elementIterator();
		while (children.hasNext() && level.leaf) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("name")) {
				level.name = ((Element) n).getTextTrim();
			} else if (childName.equals("description")) {
				level.description = ((Element) n).getTextTrim();
			} else if (childName.equals("attributes")) {
				level.attributes = this.parseAttributes((Element) n);
				if (level.goal) {
					updateForciblyModifiedAttribute(level.attributes);
				}
			} else if (childName.equals("goal")) {
				level.goal = ((Element) n).getTextTrim().equals("true");
			} else if (childName.equals("objective")) {
				level.objective = ((Element) n).getTextTrim().equals("true");
			} else if (childName.equals("indicator")) {
				level.indicator = ((Element) n).getTextTrim().equals("true");
			} else if (childName.equals("level")) {
				level.leaf = false;
				level.child = (Element) n;
			}
		}

		return level;
	}
	
	/**
	 * Monta a estrutura dos atributos
	 * @param el
	 * 		Elemento que contém a estrutura
	 * @return
	 */
	private List<StructureBeans.Attribute> parseAttributes(Element el) {
		List<StructureBeans.Attribute> attrs = new LinkedList<StructureBeans.Attribute>();

		Iterator<Element> children = el.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("attribute")) {
				StructureBeans.Attribute attr = this.parseAttribute((Element) n);
				attrs.add(attr);
			}
		}

		return attrs;
	}
	/**
	 * Monta a estrutura dos atributos
	 * @param el
	 * 		Elemento que contém a estrutura
	 * @return
	 */
	private StructureBeans.Attribute parseAttribute(Element el) {
		StructureBeans.Attribute attr = new StructureBeans.Attribute();

		Iterator<org.dom4j.Attribute> xmlAttrs = el.attributeIterator();
		while (xmlAttrs.hasNext()) {
			Node n = xmlAttrs.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("required")) {
				attr.required = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("visibleInTables")) {
				attr.visibleInTables = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("finishDate")) {
				attr.finishDate = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("expectedField")) {
				attr.expectedField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("justificationField")) {
				attr.justificationField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("minimumField")) {
				attr.minimumField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("maximumField")) {
				attr.maximumField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("reachedField")) {
				attr.reachedField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("referenceField")) {
				attr.referenceField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("polarityField")) {
				attr.polarityField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("formatField")) {
				attr.formatField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("periodicityField")) {
				attr.periodicityField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("beginField")) {
				attr.beginField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("endField")) {
				attr.endField = ((AbstractAttribute) n).getValue().equals("true");
			} else if (childName.equals("bscField")) {
				attr.bscField = ((AbstractAttribute) n).getValue().equals("true");
			}
		}
		Iterator<Element> children = el.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("label")) {
				attr.label = ((Element) n).getTextTrim();
			} else if (childName.equals("description")) {
				attr.description = ((Element) n).getTextTrim();
			} else if (childName.equals("type")) {
				attr.type = ((Element) n).getTextTrim();
			} else if (childName.equals("scheduleOptions")) {
				attr.periodicity = this.getAttributePeriodicity((Element) n);
				attr.scheduleValues = this.parseScheduleValues((Element) n);
			} else if (childName.equals("optionsField")) {
				attr.optionsField = this.parseOptionsField((Element) n);
			}
		}

		return attr;
	}
	/**
	 * Retorna atributo referente a periodicidade
	 * @param el
	 * 		Elemento que contém a estrutura
	 * @return
	 */
	private boolean getAttributePeriodicity(Element el) {
		Iterator<Element> children = el.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			if (childName.equals("periodicity")) {
				if (((Element) n).getTextTrim().equals("true")) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 *	Monta a estrutura do cronograma
	 * @param el
	 * 		Elemento que contém a estrutura
	 * @return
	 */
	private List<StructureBeans.ScheduleValue> parseScheduleValues(Element el) {
		List<StructureBeans.ScheduleValue> values = new LinkedList<StructureBeans.ScheduleValue>();

		Iterator<Element> children = el.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			else if (childName.equals("scheduleValues")) {
				Element element = (Element) n;
				Iterator<Element> childrenValue = element.elementIterator();
				while (childrenValue.hasNext()) {
					Node node = childrenValue.next();
					String childValueName = node.getName();
					if (childValueName == null)
						continue;
					else if (childValueName.equals("scheduleValue")) {
						StructureBeans.ScheduleValue value = this.parseScheduleValue((Element) node);
						values.add(value);
					}
				}
			}
		}

		return values;
	}
	/**
	 * Monta a estrutura do cronograma
	 * @param el
	 * 			Elemento que contém a estrutura
	 * @return
	 */
	private StructureBeans.ScheduleValue parseScheduleValue(Element el) {
		StructureBeans.ScheduleValue value = new StructureBeans.ScheduleValue();

		Iterator<Element> children = el.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();
			if (childName == null)
				continue;
			else if (childName.equals("valueLabel")) {
				value.label = ((Element) n).getTextTrim();
			} else if (childName.equals("valueType")) {
				value.type = ((Element) n).getTextTrim();
			}
		}

		return value;
	}
	/**
	 * Monta a estrutura dos pontos que possuem opções
	 * @param el
	 * 		Elemento que contém a estrutura das opções
	 * @return
	 */
	private List<StructureBeans.OptionsField> parseOptionsField(Element el) {
		List<StructureBeans.OptionsField> options = new LinkedList<>();

		Iterator<Element> children = el.elementIterator();
		while (children.hasNext()) {
			Node n = children.next();
			String childName = n.getName();

			if (childName == null)
				continue;
			else if (childName.equals("optionLabel")) {
				Element element = (Element) n;
				Iterator<Element> childrenValue = element.elementIterator();

				while (childrenValue.hasNext()) {
					Node node = childrenValue.next();
					String childValueName = node.getName();
					if (childValueName == null)
						continue;
					else if (childValueName.equals("label")) {
						String valueLabel = ((Element) node).getTextTrim();
						StructureBeans.OptionsField value = new StructureBeans.OptionsField();
						value.label = valueLabel;
						options.add(value);
					}
				}
			}
		}

		return options;
	}
	
	
	/*
	 * "Workaround" para forçar que o nome de responsável por uma meta seja sempre responsável técnico 
	 */
	private void updateForciblyModifiedAttribute(List<StructureBeans.Attribute> attributes) {
		for (StructureBeans.Attribute attribute : attributes) {
			if (attribute.type.equals(ResponsibleField.class.getSimpleName())) {
				attribute.label = StaticAttributeLabels.GOAL_RESPONSIBLE_LABEL;
			}
		}
	}
}
