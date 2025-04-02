package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

public class ConjunctionTest {


    @Test
    @DisplayName("Deve criar uma Conjunction com lista não vazia quando recebe múltiplos Criterion")
    void test_constructor_with_multiple_criterions() {
        Criterion criterion1 = new Criterion() {
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return null;
			}
		};
        Criterion criterion2 = new Criterion() {
			@Override
			public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
				return null;
			}
		};

        Conjunction conjunction = new Conjunction(criterion1, criterion2);

        assertNotNull(conjunction, "O objeto Conjunction não deve ser nulo");
        assertFalse(conjunction.getCriterions().isEmpty(), "A lista de critérios não deve estar vazia");
        assertEquals(2, conjunction.getCriterions().size(), "A lista deve conter exatamente 2 critérios");
        assertTrue(conjunction.getCriterions().contains(criterion1), "A lista deve conter o primeiro critério");
        assertTrue(conjunction.getCriterions().contains(criterion2), "A lista deve conter o segundo critério");
    }
}
