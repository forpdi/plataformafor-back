package org.forpdi.core.common;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Selection;

public interface Projection {
	Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext);
}
