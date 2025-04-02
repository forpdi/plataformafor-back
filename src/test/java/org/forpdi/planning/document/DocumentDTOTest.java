package org.forpdi.planning.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
 class DocumentDTOTest {

	@Mock
	DocumentBS documentBS;

	@Test
	void test_get_set_document_with_valid_data() {
		DocumentDTO documentDTO = new DocumentDTO();
		Document document = new Document();
		document.setTitle("Test Document");
		document.setDescription("Test Description");

		documentDTO.setDocument(document);
		Document retrievedDocument = documentDTO.getDocument();

		assertNotNull(retrievedDocument);
		assertEquals("Test Document", retrievedDocument.getTitle());
		assertEquals("Test Description", retrievedDocument.getDescription());
	}

	@Test
	void test_set_document_to_null() {
		DocumentDTO documentDTO = new DocumentDTO();
		Document document = new Document();
		documentDTO.setDocument(document);

		documentDTO.setDocument(null);

		assertNull(documentDTO.getDocument());
	}

	@Test
	void test_get_set_sections_with_valid_data() {
		DocumentSection documentSection = new DocumentSection();
		documentSection.setId(1L);
		Document document = new Document();
		document.setId(2L);

		documentSection.setDocument(document);
		DocumentDTO documentDTO = new DocumentDTO();
		DocumentSectionDTO section1 = new DocumentSectionDTO(documentSection, documentBS);
		section1.setName("Section 1");
		DocumentSectionDTO section2 = new DocumentSectionDTO(documentSection, documentBS);
		section2.setName("Section 2");
		List<DocumentSectionDTO> sections = Arrays.asList(section1, section2);
		documentDTO.setSections(sections);

		List<DocumentSectionDTO> retrievedSections = documentDTO.getSections();

		assertNotNull(retrievedSections);
		assertEquals(2, retrievedSections.size());
		assertEquals("Section 1", retrievedSections.get(0).getName());
		assertEquals("Section 2", retrievedSections.get(1).getName());
	}

	@Test
	void test_set_sections_to_null_after_initialization() {
		DocumentDTO documentDTO = new DocumentDTO();

		documentDTO.setSections(null);
		List<DocumentSectionDTO> sections = documentDTO.getSections();

		assertNull(sections);
	}
}