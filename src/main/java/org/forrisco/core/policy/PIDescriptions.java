package org.forrisco.core.policy;

import java.io.Serializable;

public class PIDescriptions implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Description[] idescriptions;
	private Description[] pdescriptions;
	
	public Description[] getIdescriptions() {
		return idescriptions;
	}

	public void setIdescriptions(Description[] idescriptions) {
		this.idescriptions = idescriptions;
	}

	public Description[] getPdescriptions() {
		return pdescriptions;
	}

	public void setPdescriptions(Description[] pdescriptions) {
		this.pdescriptions = pdescriptions;
	}
	
	public static class PIDescriptionsWrapper implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public PIDescriptions PIDescriptions;
	}

	public static class Description implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String description;
		private String value;
		
		public Description(String description, String value) {
			this.description = description;
			this.value = value;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}

}
