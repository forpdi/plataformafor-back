package org.forrisco.core.item;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.forrisco.core.policy.Policy;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = Item.TABLE)
@Table(name = Item.TABLE)

public class Item extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_item";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length=400)
	private String name;
	
	@Column(nullable = true, length=10000)
	private String description;

	@ManyToOne(targetEntity=Policy.class, optional=false, fetch=FetchType.EAGER)
	private Policy policy;
	
	@Transient
	private List<FieldItem> fields;
	
	@Transient
	private List<SubItem> subitems;

	@Transient
	private boolean hasFile;

	@Transient
	private boolean hasText;

	public List<FieldItem> getFieldItem() {
		return fields;
	}

	public void setFieldItem(List<FieldItem> fieldItem) {
		this.fields = fieldItem;
	}
	
	public List<SubItem> getSubitems() {
		return subitems;
	}

	public void setSubitems(List<SubItem> subitems) {
		this.subitems = subitems;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}
	
	public void setHasText(boolean hasText) {
		this.hasText = hasText;
	}

	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}
	
	public boolean hasFile() {
		return hasFile;
	}
	
	public boolean hasText() {
		return hasText;
	}

}