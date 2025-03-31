package org.forpdi.core.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Selection;

import static org.junit.jupiter.api.Assertions.*;

public class AggregateProjectionTest {

	private static final String PROPERTY_NAME = "testProperty";
	private static final Class<?> RESULT_CLASS = String.class;

	private TestAggregateProjection projection;

	@BeforeEach
	public void setUp() {
		projection = new TestAggregateProjection(PROPERTY_NAME, RESULT_CLASS);
	}

	@Test
	public void testConstructorWithPropertyName() {
		TestAggregateProjection projectionWithDefaultClass = new TestAggregateProjection(PROPERTY_NAME);
		assertEquals(PROPERTY_NAME, projectionWithDefaultClass.getPropertyName());
		assertEquals(Object.class, projectionWithDefaultClass.getResultClass());
	}

	@Test
	public void testConstructorWithPropertyNameAndResultClass() {
		assertEquals(PROPERTY_NAME, projection.getPropertyName());
		assertEquals(RESULT_CLASS, projection.getResultClass());
	}

	@Test
	public void testGetResultClass() {
		assertEquals(RESULT_CLASS, projection.getResultClass());
	}

	private static class TestAggregateProjection extends AggregateProjection {
		public TestAggregateProjection(String propertyName) {
			super(propertyName);
		}

		public TestAggregateProjection(String propertyName, Class<?> resultClass) {
			super(propertyName, resultClass);
		}

		@Override
		public Selection<?>[] getSelection(CriteriaBuilder builder, CriteriaPropertiesContext propertiesContext) {
			return new Selection[0];
		}
	}
}