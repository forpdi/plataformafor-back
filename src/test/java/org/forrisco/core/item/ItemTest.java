package org.forrisco.core.item;

import org.forrisco.core.policy.Policy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTest {
	@Test
	public void testItemObjectCreation() {
		Item item = new Item();

		String testName = "Test Item Name";
		String testDescription = "Test Item Description";
		Policy testPolicy = new Policy();
		List<FieldItem> testFields = new ArrayList<>();
		List<SubItem> testSubitems = new ArrayList<>();
		boolean testHasFile = true;
		boolean testHasText = false;

		item.setName(testName);
		item.setDescription(testDescription);
		item.setPolicy(testPolicy);
		item.setFieldItem(testFields);
		item.setSubitems(testSubitems);
		item.setHasFile(testHasFile);
		item.setHasText(testHasText);

		assertEquals(testName, item.getName(), "O nome deveria ser igual ao definido");
		assertEquals(testDescription, item.getDescription(), "A descrição deveria ser igual à definida");
		assertEquals(testPolicy, item.getPolicy(), "A política deveria ser igual à definida");
		assertEquals(testFields, item.getFieldItem(), "Os campos deveriam ser iguais aos definidos");
		assertEquals(testSubitems, item.getSubitems(), "Os subitens deveriam ser iguais aos definidos");
		assertTrue(item.hasFile(), "O campo hasFile deveria ser verdadeiro");
		assertEquals(testHasText, item.hasText(), "O campo hasText deveria ser igual ao definido");
	}

}