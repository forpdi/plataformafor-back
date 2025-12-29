package org.forrisco.core.item;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SubItemTest {

	@Test
	public void testSettersAndGetters() {

		SubItem subItem = new SubItem();

		String testName = "SubItem Test";
		String testDescription = "This is a test description for SubItem.";
		Item mockItem = new Item();
		List<FieldSubItem> testFields = List.of(mock(FieldSubItem.class), mock(FieldSubItem.class));
		Long testItemId = 123L;
		String testValue = "Test Value";
		boolean testHasFile = true;
		boolean testHasText = false;

		subItem.setName(testName);
		subItem.setDescription(testDescription);
		subItem.setItem(mockItem);
		subItem.setFieldSubItem(testFields);
		subItem.setItemId(testItemId);
		subItem.setValue(testValue);
		subItem.setHasFile(testHasFile);
		subItem.setHasText(testHasText);

		assertEquals(testName, subItem.getName(), "O nome deveria ser igual ao definido.");
		assertEquals(testDescription, subItem.getDescription(), "A descrição deveria ser igual à definida.");
		assertEquals(mockItem, subItem.getItem(), "O objeto Item deveria ser igual ao mock definido.");
		assertEquals(testFields, subItem.getFieldSubItem(), "Os campos deveriam corresponder à lista definida.");
		assertEquals(testItemId, subItem.getItemId(), "O ID do item deveria ser igual ao definido.");
		assertEquals(testValue, subItem.getValue(), "O valor deveria ser igual ao definido.");
		assertTrue(subItem.hasFile(), "O valor de hasFile deveria ser verdadeiro.");
		assertEquals(testHasText, subItem.hasText(), "O valor de hasText deveria corresponder ao definido.");
	}
}