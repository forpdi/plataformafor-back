package org.forrisco.core.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldSubItemTest {

	@DisplayName("FieldSubItem Criação do objeto.")
	@Test
	public void testFieldSubItemCreationAndSettersGetters() {
		FieldSubItem fieldSubItem = new FieldSubItem();

		String testName = "Test FieldSubItem Name";
		String testDescription = "Test FieldSubItem Description";
		String testFileLink = "02.pdf";
		String testValue = "Test Value";
		boolean testIsText = true;
		SubItem testSubItem = new SubItem();

		fieldSubItem.setName(testName);
		fieldSubItem.setDescription(testDescription);
		fieldSubItem.setFileLink(testFileLink);
		fieldSubItem.setValue(testValue);
		fieldSubItem.setText(testIsText);
		fieldSubItem.setSubitem(testSubItem);

		assertEquals(testName, fieldSubItem.getName(), "O nome deveria ser igual ao definido");
		assertEquals(testDescription, fieldSubItem.getDescription(), "A descrição deveria ser igual à definida");
		assertEquals(testFileLink, fieldSubItem.getFileLink(), "O link do arquivo deveria ser igual ao definido");
		assertEquals(testValue, fieldSubItem.getValue(), "O valor deveria ser igual ao definido");
		assertTrue(fieldSubItem.isText(), "O campo isText deveria ser verdadeiro");
		assertEquals(testSubItem, fieldSubItem.getSubitem(), "O subitem deveria ser igual ao definido");
		fieldSubItem.setText(false);
		assertFalse(fieldSubItem.isText(), "O campo isText deveria ser falso após a alteração");
	}
}