package org.forpdi.core.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ListWrapperTest {

	@Test
	void create_list_wrapper_with_non_empty_list() {
		List<String> stringList = Arrays.asList("test1", "test2", "test3");

		ListWrapper<String> wrapper = new ListWrapper<>(stringList);

		assertNotNull(wrapper.getList());
		assertEquals(3, wrapper.getList().size());
		assertEquals("test1", wrapper.getList().get(0));
		assertEquals("test2", wrapper.getList().get(1));
		assertEquals("test3", wrapper.getList().get(2));
	}

	@Test
	void create_list_wrapper_with_null_elements() {
		List<String> listWithNulls = Arrays.asList("test1", null, "test3");

		ListWrapper<String> wrapper = new ListWrapper<>(listWithNulls);

		assertNotNull(wrapper.getList());
		assertEquals(3, wrapper.getList().size());
		assertEquals("test1", wrapper.getList().get(0));
		assertNull(wrapper.getList().get(1));
		assertEquals("test3", wrapper.getList().get(2));
	}
}