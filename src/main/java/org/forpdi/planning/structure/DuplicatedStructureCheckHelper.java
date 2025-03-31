package org.forpdi.planning.structure;

import java.util.List;
import java.util.stream.Collectors;

import org.forpdi.planning.attribute.Attribute;
import org.forpdi.planning.attribute.StaticAttributeLabels;
import org.forpdi.planning.fields.FieldsBS;
import org.forpdi.planning.fields.OptionsField;
import org.forpdi.planning.structure.xml.StructureBeans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DuplicatedStructureCheckHelper {
	@Autowired private StructureBS structureBS;
	
	@Autowired private FieldsBS fieldsBS;
	
	public Structure retrieveDuplicatedStructure(StructureBeans.Structure readedStructure) {
		StructureBeans.Structure readedStructureCopy = readedStructure.deepClone();

		List<Structure> structureList = structureBS.list().getList();
		
		Structure duplicatedStructure = retrieveWithNameDuplicated(readedStructureCopy, structureList);
		
		if (duplicatedStructure != null) {
			return duplicatedStructure;
		}
		
		duplicatedStructure = retrieveWithLevelStructuresDuplicated(readedStructureCopy, structureList);
		if (duplicatedStructure != null) {
			return duplicatedStructure;
		}
		
		return null;
	}
	
	private Structure retrieveWithNameDuplicated(StructureBeans.Structure readedStructure, List<Structure> structureList) {
		for (Structure structure : structureList) {
			if (structure.getName().equals(readedStructure.name)) {
				return structure;
			}
		}
		
		return null;
	}
	
	private Structure retrieveWithLevelStructuresDuplicated(StructureBeans.Structure readedStructure, List<Structure> structureList) {
		for (Structure structure : structureList) {
			List<StructureLevel> levels = structureBS.listStructureLevels(structure);

			if (this.hasDifferentLevelStructures(readedStructure.levels, levels)) {
				continue;
			}
			
			return structure;
		}
		
		return null;
	}

	private boolean hasDifferentLevelStructures(List<StructureBeans.StructureLevel> readedLevels,
			List<StructureLevel> levels) {
		
		if (readedLevels.size() != levels.size()) {
			return true;
		}
		
		readedLevels.sort((l1, l2) -> l1.name.compareTo(l2.name));
		levels.sort((l1, l2) -> l1.getName().compareTo(l2.getName()));
				
		for (int i = 0; i < readedLevels.size(); i++) {
			StructureBeans.StructureLevel readedLevel = readedLevels.get(i);
			StructureLevel level = levels.get(i);
			
			boolean nameOrDescriptionDifferent = !readedLevel.name.equals(level.getName())
					|| !readedLevel.description.equals(level.getDescription());
			
			if (nameOrDescriptionDifferent || hasDifferentAttributes(readedLevel, level)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasDifferentAttributes(StructureBeans.StructureLevel readedLevel,
			StructureLevel levels) {
		
		List<StructureBeans.Attribute> readedAttributes = readedLevel.attributes;
		
		List<Attribute> attributes = structureBS.listAttributes(levels).getList();
		
		List<Attribute> filteredAttributes = getNonStaticAttributes(attributes);
		
		if (readedAttributes.size() != filteredAttributes.size()) {
			return true;
		}
		
		readedAttributes.sort((a1, a2) -> a1.label.compareTo(a2.label));
		filteredAttributes.sort((a1, a2) -> a1.getLabel().compareTo(a2.getLabel()));
		
		for (int i = 0; i < readedAttributes.size(); i++) {
			StructureBeans.Attribute readedAttribute = readedAttributes.get(i);
			Attribute attribute = filteredAttributes.get(i);
						
			boolean labelDescriptionOrRequirementDifferent = !readedAttribute.label.equals(attribute.getLabel())
					|| !readedAttribute.description.equals(attribute.getDescription())
					|| readedAttribute.required != attribute.isRequired();
			
			if (labelDescriptionOrRequirementDifferent || hasDifferentOptionFields(readedAttribute, attribute)) {
				return true;
			}
		}
		
		return false;
	}

	private List<Attribute> getNonStaticAttributes(List<Attribute> attributes) {
		return attributes.stream()
				.filter(a -> isNonStaticAttribute(a))
				.collect(Collectors.toList());
	}
	
	private boolean isNonStaticAttribute(Attribute attribute) {
		if (attribute.getLevel().isGoal()) {
			if (attribute.getLabel().equals(StaticAttributeLabels.GOAL_MANAGER_LABEL)) {
				return false;
			}
			
			if (attribute.getLabel().equals(StaticAttributeLabels.GOAL_ATTACHMENT_LABEL)) {
				return false;
			}
		} else if (attribute.getLevel().isIndicator()) {
			if (attribute.isJustificationField()
					&& attribute.getLabel().equals(StaticAttributeLabels.INDICATOR_JUSTIFICATION_LABEL)) {
				return false;
			}
		}

		return true;
	}
	
	private boolean hasDifferentOptionFields(StructureBeans.Attribute readedAttribute, Attribute attribute) {
		List<StructureBeans.OptionsField> readedOptionFields = readedAttribute.optionsField;
		
		List<OptionsField> optionsFields = fieldsBS.getOptionsField(attribute.getId(), false).getList();
		
		List<OptionsField> filteredOptionsFields = optionsFields.stream()
				.filter(of -> !of.isDocument())
				.collect(Collectors.toList());
		
		readedOptionFields.sort((of1, of2) -> of1.label.compareTo(of2.label));
		filteredOptionsFields.sort((of1, of2) -> of1.getLabel().compareTo(of2.getLabel()));
		
		if (readedOptionFields.size() != filteredOptionsFields.size()) {
			return true;
		}
		
		for (int i = 0; i < readedOptionFields.size(); i++) {
			StructureBeans.OptionsField readedOptionField = readedOptionFields.get(i);
			OptionsField optionField = filteredOptionsFields.get(i);
			
			if (!readedOptionField.label.equals(optionField.getLabel())) {
				return true;
			}
		}
		
		return false;
	}
}
