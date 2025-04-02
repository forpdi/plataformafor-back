package org.forpdi.planning.fields.table;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.forpdi.core.SkipSerialization;
import org.forpdi.core.common.SimpleLogicalDeletableEntity;


@Entity(name = TableValues.TABLE)
@Table(name = TableValues.TABLE)
public class TableValues extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "fpdi_table_values";
	private static final long serialVersionUID = 1L;
	
	@Column(nullable = true, length=4000)
	private String value;
	
	@Column(nullable = true)
	private Double valueAsNumber;

	@Column(nullable = true)
	private Date valueAsDate;
	
	@SkipSerialization
	@ManyToOne(targetEntity=TableInstance.class, optional=false, fetch=FetchType.EAGER)
	private TableInstance tableInstance;
	
	@ManyToOne(targetEntity=TableStructure.class, optional=false, fetch=FetchType.EAGER)
	private TableStructure tableStructure;

	@Transient
	private Long exportTableStructureId;

	@Transient
	private Long exportTableInstanceId;
	
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Double getValueAsNumber() {
		return valueAsNumber;
	}

	public void setValueAsNumber(Double valueAsNumber) {
		this.valueAsNumber = valueAsNumber;
	}

	public Date getValueAsDate() {
		return valueAsDate;
	}

	public void setValueAsDate(Date valueAsDate) {
		this.valueAsDate = valueAsDate;
	}

	public TableInstance getTableInstance() {
		return tableInstance;
	}

	public void setTableInstance(TableInstance tableInstance) {
		this.tableInstance = tableInstance;
	}

	public TableStructure getTableStructure() {
		return tableStructure;
	}

	public void setTableStructure(TableStructure tableStructure) {
		this.tableStructure = tableStructure;
	}
	public Long getExportTableStructureId() {
		return exportTableStructureId;
	}

	public void setExportTableStructureId(Long exportTableStructureId) {
		this.exportTableStructureId = exportTableStructureId;
	}

	public Long getExportTableInstanceId() {
		return exportTableInstanceId;
	}

	public void setExportTableInstanceId(Long exportTableInstanceId) {
		this.exportTableInstanceId = exportTableInstanceId;
	}
	
}
