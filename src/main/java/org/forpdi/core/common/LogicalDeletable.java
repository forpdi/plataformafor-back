package org.forpdi.core.common;

import java.io.Serializable;

public interface LogicalDeletable extends Serializable {

	public boolean isDeleted();
	public void setDeleted(boolean deleted);
	
}
