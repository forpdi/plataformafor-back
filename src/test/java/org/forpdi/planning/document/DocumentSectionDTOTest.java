package org.forpdi.planning.document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentSectionDTOTest {

	private DocumentSectionDTO dto;

	private DocumentSection mockSection;
	private DocumentBS mockBS;

	@BeforeEach
	void setup() {
		mockSection = mock(DocumentSection.class);
		mockBS = mock(DocumentBS.class);

		Document mockDocument = mock(Document.class);
		when(mockDocument.getId()).thenReturn(100L);
		when(mockSection.getDocument()).thenReturn(mockDocument);
	}

	@Test
	@DisplayName("Deve inicializar DocumentSectionDTO com valores corretos via construtor")
	void testConstructorInitialization() {
		when(mockSection.getId()).thenReturn(1L);
		when(mockSection.getSequence()).thenReturn(10);
		when(mockSection.getName()).thenReturn("Test Section");
		when(mockSection.isPreTextSection()).thenReturn(true);
		when(mockBS.countAttributesPerSection(mockSection)).thenReturn(5L);

		DocumentSection mockParent = mock(DocumentSection.class);
		when(mockParent.getId()).thenReturn(2L);
		when(mockSection.getParent()).thenReturn(mockParent);
		when(mockBS.listSectionDTOsByDocument(any(), anyLong())).thenReturn(List.of());

		dto = new DocumentSectionDTO(mockSection, mockBS);

		assertEquals(1L, dto.getId());
		assertEquals(100L, dto.getDocumentId());
		assertEquals(10, dto.getSequence());
		assertEquals("Test Section", dto.getName());
		assertTrue(dto.isPreTextSection());
		assertEquals(5L, dto.getAttributesAmount());
		assertEquals(2L, dto.getParentId());
		assertTrue(dto.isLeaf());
//		assertNotNull(dto.getChildren());
//		assertTrue(dto.getChildren().isEmpty());
	}

	@Test
	@DisplayName("Deve inicializar DocumentSectionDTO com valores corretos via construtor")
	void testConstructorTryInitialization() {
		when(mockSection.getId()).thenReturn(1L);
		when(mockSection.getSequence()).thenReturn(10);
		when(mockSection.getName()).thenReturn("Test Section");
		when(mockSection.isPreTextSection()).thenReturn(true);
		when(mockBS.countAttributesPerSection(mockSection)).thenReturn(5L);

		Document mockDocument = mock(Document.class);
		when(mockDocument.getId()).thenReturn(100L);
		when(mockSection.getDocument()).thenReturn(mockDocument);

		DocumentSection mockParent = mock(DocumentSection.class);
		when(mockParent.getId()).thenReturn(2L);
		when(mockSection.getParent()).thenReturn(mockParent);
		when(mockBS.listSectionDTOsByDocument(mockDocument, 1L)).thenReturn(List.of());

		dto = new DocumentSectionDTO(mockSection, mockBS);

		assertEquals(1L, dto.getId());
		assertEquals(100L
, dto.getDocumentId());
		assertEquals(10, dto.getSequence());
		assertEquals("Test Section", dto.getName());
		assertTrue(dto.isPreTextSection());
		assertEquals(5L, dto.getAttributesAmount());
		assertEquals(2L, dto.getParentId());
		assertTrue(dto.isLeaf());
//		assertNotNull(dto.getChildren());
//		assertTrue(dto.getChildren().isEmpty());
	}

	@Test
	@DisplayName("Deve inicializar como seção raiz quando não houver parent")
	void testConstructorInitializationWithoutParent() {
		when(mockSection.getId()).thenReturn(1L);
		when(mockSection.getSequence()).thenReturn(5);
		when(mockSection.getName()).thenReturn("Root Section");
		when(mockSection.isPreTextSection()).thenReturn(false);
		when(mockBS.countAttributesPerSection(mockSection)).thenReturn(10L);

		Document mockDocument = mock(Document.class);
		when(mockDocument.getId()).thenReturn(200L);
		when(mockSection.getDocument()).thenReturn(mockDocument);

		when(mockSection.getParent()).thenReturn(null);

		DocumentSectionDTO child = mock(DocumentSectionDTO.class);
		when(mockBS.listSectionDTOsByDocument(mockDocument, 1L)).thenReturn(List.of(child));

		dto = new DocumentSectionDTO(mockSection, mockBS);

		assertEquals(1L, dto.getId());
		assertEquals(200L, dto.getDocumentId());
		assertEquals(5, dto.getSequence());
		assertEquals("Root Section", dto.getName());
		assertFalse(dto.isPreTextSection());
		assertEquals(10L, dto.getAttributesAmount());
		assertNull(dto.getParentId());
		assertFalse(dto.isLeaf());
		assertNotNull(dto.getChildren());
		assertEquals(1, dto.getChildren().size());
	}

	@Test
	@DisplayName("Deve permitir alterações de valores via setters")
	void testSetters() {
		dto = new DocumentSectionDTO(mockSection, mockBS);

		dto.setId(123L);
		dto.setDocumentId(456L);
		dto.setParentId(789L);
		dto.setSequence(3);
		dto.setAttributesAmount(20L);
		dto.setLeaf(false);
		dto.setName("Updated Name");
		dto.setChildren(List.of());
		dto.setPreTextSection(true);

		assertEquals(123L, dto.getId());
		assertEquals(456L, dto.getDocumentId());
		assertEquals(789L, dto.getParentId());
		assertEquals(3, dto.getSequence());
		assertEquals(20L, dto.getAttributesAmount());
		assertFalse(dto.isLeaf());
		assertEquals("Updated Name", dto.getName());
		assertNotNull(dto.getChildren());
		assertTrue(dto.getChildren().isEmpty());
		assertTrue(dto.isPreTextSection());
	}
}
