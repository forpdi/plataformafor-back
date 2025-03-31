package org.forrisco.core.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldItemTest {

	@DisplayName("FieldItem Criação do objeto.")
	@Test
	public void testFieldItemCreation() {
		FieldItem fieldItem = new FieldItem();

		String testName = "Test Field Name";
		String testDescription = "Test Field Description";
		String testFileLink = "01.png";
		String testValue = "Test Value";
		boolean testIsText = true;
		Item testItem = new Item();

		fieldItem.setName(testName);
		fieldItem.setDescription(testDescription);
		fieldItem.setFileLink(testFileLink);
		fieldItem.setValue(testValue);
		fieldItem.setText(testIsText);
		fieldItem.setItem(testItem);

		assertEquals(testName, fieldItem.getName(), "O nome deveria ser igual ao definido");
		assertEquals(testDescription, fieldItem.getDescription(), "A descrição deveria ser igual à definida");
		assertEquals(testFileLink, fieldItem.getFileLink(), "O link do arquivo deveria ser igual ao definido");
		assertEquals(testValue, fieldItem.getValue(), "O valor deveria ser igual ao definido");
		assertTrue(fieldItem.isText(), "O campo isText deveria ser verdadeiro");
		assertEquals(testItem, fieldItem.getItem(), "O item deveria ser igual ao definido");
		fieldItem.setText(false);
		assertFalse(fieldItem.isText(), "O campo isText deveria ser falso após a alteração");
	}
}