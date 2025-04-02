package org.forrisco.core.item.dto;

import org.forrisco.core.item.FieldItem;
import org.forrisco.core.item.FieldSubItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldItemDtoTest {

	@Test
	void test_create_field_item_dto_with_valid_field_item() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Test Field");

		FieldItemDto dto = new FieldItemDto(fieldItem);

		assertNotNull(dto);
	}

	@Test
	void test_access_field_item_through_accessor() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Test Field");
		FieldItemDto dto = new FieldItemDto(fieldItem);

		FieldItem retrievedItem = dto.fieldItem();

		assertEquals(fieldItem, retrievedItem);
	}

	@Test
	void test_record_component_matches_provided_field_item() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Test Component");

		FieldItemDto dto = new FieldItemDto(fieldItem);

		assertSame(fieldItem, dto.fieldItem());
	}

	@Test
	void test_equals_with_identical_field_items() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Test Equals");

		FieldItemDto dto1 = new FieldItemDto(fieldItem);
		FieldItemDto dto2 = new FieldItemDto(fieldItem);

		assertEquals(dto1, dto2);
	}

	@Test
	void test_hash_code_for_identical_objects() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Test Hash");

		FieldItemDto dto1 = new FieldItemDto(fieldItem);
		FieldItemDto dto2 = new FieldItemDto(fieldItem);

		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void test_compare_with_different_record_types() {
		FieldItem fieldItem = new FieldItem();
		FieldItemDto dto = new FieldItemDto(fieldItem);
		record OtherRecord(FieldItem fieldItem) {}
		OtherRecord other = new OtherRecord(fieldItem);

		assertNotEquals(dto, other);
	}

	@Test
	void test_compare_with_null() {
		FieldItem fieldItem = new FieldItem();
		FieldItemDto dto = new FieldItemDto(fieldItem);

		assertNotEquals(dto, null);
	}

	@Test
	void test_equals_with_same_id_different_properties() {
		FieldItem item1 = new FieldItem();
		item1.setId(1L);
		item1.setName("Name 1");

		FieldItem item2 = new FieldItem();
		item2.setId(1L);
		item2.setName("Name 2");

		FieldItemDto dto1 = new FieldItemDto(item1);
		FieldItemDto dto2 = new FieldItemDto(item2);

		assertNotEquals(dto1, dto2);
	}

	@Test
	void test_to_string_output() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Test ToString");
		FieldItemDto dto = new FieldItemDto(fieldItem);

		String result = dto.toString();

		assertTrue(result.contains("FieldItemDto"));
		assertTrue(result.contains("fieldItem"));
	}

	@Test
	void test_record_immutability() {
		FieldItem fieldItem = new FieldItem();
		fieldItem.setName("Original Name");
		FieldItemDto dto = new FieldItemDto(fieldItem);

		fieldItem.setName("Modified Name");

		assertEquals("Modified Name", dto.fieldItem().getName());
	}

	@DisplayName("FieldItemDto Criação do DTO a partir de um FieldSubItem")
	@Test
	void testFieldItemDtoCreation() {
		FieldSubItem fieldSubItem = new FieldSubItem();
		fieldSubItem.setName("Test Field");
		fieldSubItem.setDescription("Test Description");
		fieldSubItem.setText(true);

		FieldSubItemDto dto = new FieldSubItemDto(fieldSubItem);

		assertNotNull(dto);
		assertEquals(fieldSubItem, dto.fieldSubItem());
	}
}