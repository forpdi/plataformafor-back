package org.forpdi.planning.fields.table;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


@Entity(name = TableInstance.TABLE)
@Table(name = TableInstance.TABLE)
public class TableInstance extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_table_instance";
	private static final long serialVersionUID = 1L;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date creation = new Date();
	
	@SkipSerialization
	@ManyToOne(targetEntity=TableFields.class, optional=false, fetch=FetchType.EAGER)
	private TableFields tableFields;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="tableInstance", targetEntity=TableValues.class)
	@Cascade(CascadeType.ALL)
	private List<TableValues> tableValues;
	
	@Transient
	private Long tableFieldsId;
	
	@Transient
	private Long exportTableFieldsId;

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public TableFields getTableFields() {
		return tableFields;
	}

	public void setTableFields(TableFields tableFields) {
		this.tableFields = tableFields;
	}

	public List<TableValues> getTableValues() {
		return tableValues;
	}

	public void setTableValues(List<TableValues> tableValues) {
		this.tableValues = tableValues;
	}

	public Long getTableFieldsId() {
		return tableFieldsId;
	}

	public void setTableFieldsId(Long tableFieldsId) {
		this.tableFieldsId = tableFieldsId;
	}
	
	public Long getExportTableFieldsId() {
		return exportTableFieldsId;
	}

	public void setExportTableFieldsId(Long exportTableFieldsId) {
		this.exportTableFieldsId = exportTableFieldsId;
	}
	
}
