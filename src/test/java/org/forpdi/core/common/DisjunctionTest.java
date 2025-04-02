package org.forpdi.core.common;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DisjunctionTest {

	@Test
	public void test_constructor_creates_empty_list() {
		Disjunction disjunction = new Disjunction();

		List<Criterion> criterions = disjunction.getCriterions();

		assertNotNull(criterions);
		assertTrue(criterions.isEmpty());
	}

	@Test
	void test_disjunction_throws_exception_with_null_criterion_array() {
		Criterion[] nullCriterions = null;

		assertThrows(NullPointerException.class, () -> {
			new Disjunction(nullCriterions);
		});
	}

	@Test
	public void test_disjunction_creates_or_predicate() {
		CriteriaBuilder builder = mock(CriteriaBuilder.class);
		CriteriaPropertiesContext context = mock(CriteriaPropertiesContext.class);
		Criterion criterion1 = mock(Criterion.class);
		Criterion criterion2 = mock(Criterion.class);

		Predicate predicate1 = mock(Predicate.class);
		Predicate predicate2 = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);

		when(criterion1.getPredicate(builder, context)).thenReturn(predicate1);
		when(criterion2.getPredicate(builder, context)).thenReturn(predicate2);
		when(builder.or(new Predicate[]{predicate1, predicate2})).thenReturn(orPredicate);

		Disjunction disjunction = new Disjunction(criterion1, criterion2);

		Predicate result = disjunction.getPredicate(builder, context);

		verify(builder).or(new Predicate[]{predicate1, predicate2});
		assertEquals(orPredicate, result);
	}
}