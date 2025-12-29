package org.forpdi.planning.attribute.types.enums;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculationTypeTest {


	@Test
	void test_enum_constants_return_correct_values() {
		CalculationType normalAvg = CalculationType.NORMAL_AVG;
		CalculationType weightedAvg = CalculationType.WEIGHTED_AVG;
		CalculationType sum = CalculationType.SUM;

		Integer normalValue = normalAvg.getValue();
		Integer weightedValue = weightedAvg.getValue();
		Integer sumValue = sum.getValue();

		assertEquals(0, normalValue);
		assertEquals(1, weightedValue);
		assertEquals(2, sumValue);
	}

	@Test
	void test_cannot_instantiate_enum_directly() {
		Class<CalculationType> enumClass = CalculationType.class;

		assertThrows(NoSuchMethodException.class, () -> {
			Constructor<CalculationType> constructor = enumClass.getDeclaredConstructor(Integer.class);
			constructor.setAccessible(true);
			constructor.newInstance(99);
		});
	}
}