package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class UnimplementedMethodExceptionTest {

	@Test
	public void test_throw_with_default_constructor() {
		assertThrows(UnimplementedMethodException.class, () -> {
			throw new UnimplementedMethodException();
		});
	}

	@Test
	public void test_throw_with_null_message() {
		UnimplementedMethodException exception = assertThrows(UnimplementedMethodException.class, () -> {
			throw new UnimplementedMethodException();
		});
		assertNull(exception.getMessage());
	}
}