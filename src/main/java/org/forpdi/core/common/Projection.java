package org.forpdi.core.common;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

public interface Projection {
	Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext);
}
