package org.forpdi.core.common;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JunctionTest {

	static class MockJunction extends Junction {
		public MockJunction(List<Criterion> criterions) {
			super(criterions);
		}

		@Override
		public Predicate getPredicate(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
			return null;
		}
	}

	@Test
	@DisplayName("Deve adicionar critérios e retornar os critérios agrupados corretamente")
	void testAddAndGetGroupedPredicates() {
		List<Criterion> initialCriterions = new ArrayList<>();
		Junction junction = new MockJunction(initialCriterions);

		Criterion criterion1 = mock(Criterion.class);
		Criterion criterion2 = mock(Criterion.class);
		CriteriaBuilder builder = mock(CriteriaBuilder.class);
		CriteriaPropertiesContext context = mock(CriteriaPropertiesContext.class);
		Predicate predicate1 = mock(Predicate.class);
		Predicate predicate2 = mock(Predicate.class);

		when(criterion1.getPredicate(builder, context)).thenReturn(predicate1);
		when(criterion2.getPredicate(builder, context)).thenReturn(predicate2);

		junction.add(criterion1).add(criterion2);
		Predicate[] groupedPredicates = junction.getGroupedPredicates(builder, context);

		assertNotNull(groupedPredicates);
		assertEquals(2, groupedPredicates.length);
		assertEquals(predicate1, groupedPredicates[0]);
		assertEquals(predicate2, groupedPredicates[1]);

		verify(criterion1).getPredicate(builder, context);
		verify(criterion2).getPredicate(builder, context);
	}
}
