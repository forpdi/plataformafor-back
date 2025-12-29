package org.forpdi.planning.structure;

import java.io.Serializable;

public class AttributeToFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	private long attributeId;

	private String type;
	
	private String value;

	public long getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(long attributeId) {
		this.attributeId = attributeId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (attributeId ^ (attributeId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeToFilter other = (AttributeToFilter) obj;
		if (attributeId != other.attributeId)
			return false;
		return true;
	}
}
