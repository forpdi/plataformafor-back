package org.forpdi.planning.document.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DocumentSectionToSaveDtoTest {


    @Test
    @DisplayName("Deve criar um DocumentAttributeDto com valores válidos")
    void test_document_attribute_dto_creation() {
        Long section = 123L;
        String name = "Atributo Teste";
        String type = "text";
        Boolean periodicity = true;

        DocumentAttributeDto dto = new DocumentAttributeDto(section, name, type, periodicity);

        assertNotNull(dto, "O objeto DocumentAttributeDto não deve ser nulo");
        assertEquals(section, dto.section(), "A seção deve ser igual à fornecida");
        assertEquals(name, dto.name(), "O nome deve ser igual ao fornecido");
        assertEquals(type, dto.type(), "O tipo deve ser igual ao fornecido");
        assertEquals(periodicity, dto.periodicity(), "A periodicidade deve ser igual à fornecida");
    }

	@Test
	void create_dto_with_all_fields_populated() {
		String name = "Section 1";
		Long parentId = 123L;
		Long id = 456L;
		String value = "Section content";

		DocumentSectionToSaveDto dto = new DocumentSectionToSaveDto(name, parentId, id, value);

		assertEquals("Section 1", dto.name());
		assertEquals(123L, dto.parentId());
		assertEquals(456L, dto.id());
		assertEquals("Section content", dto.value());
	}
}
