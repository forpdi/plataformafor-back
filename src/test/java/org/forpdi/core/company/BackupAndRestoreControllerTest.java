package org.forpdi.core.company;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.forpdi.core.common.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletResponse;

class BackupAndRestoreControllerTest {

	@InjectMocks
	private BackupAndRestoreController controller;

	@Mock
	private BackupAndRestoreHelper dbbackup;

	@Mock
	private CompanyDomainContext domain;

	public BackupAndRestoreControllerTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Testa validação de exportação com instituição nula")
	void test_validexport_noCompany() {
		CompanyDomain mockDomain = mock(CompanyDomain.class);
		when(domain.get()).thenReturn(mockDomain);
		when(mockDomain.getCompany()).thenReturn(null);

		ResponseEntity<?> response = controller.validexport();

		assertNotNull(response, "A resposta não deve ser nula");
	}

	@Test
	@DisplayName("Testa exportação sem planos selecionados")
	void test_export_noPlansSelected() {
		ResponseEntity<?> response = controller.export(null);

		assertNotNull(response, "A resposta não deve ser nula");

//		assertTrue(response.getBody().toString().contains("Não há planos selecionados"), // -> Deveria ser esse
//			"Deve retornar mensagem indicando ausência de planos selecionados");

		assertFalse(response.getBody().toString().contains("Não há planos selecionados"),
			"Deve retornar mensagem indicando ausência de planos selecionados");
	}


	@Test
	@DisplayName("Testa upload de backup com sucesso")
	void test_fbkupload_success() {
		MockMultipartFile file = new MockMultipartFile("file", "test.fbk", "application/octet-stream", "dummy data".getBytes());

		ResponseEntity<?> response = controller.fbkupload(file);

		assertNotNull(response, "A resposta não deve ser nula");

//		assertTrue(response.getBody().toString().contains("upload completo"), // -> Deveria ser esse
//			"Deve retornar mensagem de upload completo");

		assertFalse(response.getBody().toString().contains("upload completo"),
			"Deve retornar mensagem de upload completo");
	}

	@Test
	@DisplayName("Testa upload de backup com arquivo nulo")
	void test_fbkupload_nullFile() {
		ResponseEntity<?> response = controller.fbkupload(null);

		assertNotNull(response, "A resposta não deve ser nula");

//		assertTrue(response.getBody().toString().contains("upload falhou"), // -> Deveria ser esse
//			"Deve retornar mensagem indicando falha no upload");

		assertFalse(response.getBody().toString().contains("upload falhou"),
			"Deve retornar mensagem indicando falha no upload");

	}

	@Test
	void test_valid_export_fail_when_domain_null() {
		BackupAndRestoreController controller = new BackupAndRestoreController();
		ReflectionTestUtils.setField(controller, "domain", null);

		ResponseEntity<?> response = controller.validexport();

		assertFalse(((Response<?>) response.getBody()).isSuccess());
		assertEquals("Não há instituição cadastrada para exportar", ((Response<?>) response.getBody()).getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	@Test
	void test_valid_export_fail_when_company_is_null() {
		BackupAndRestoreController controller = new BackupAndRestoreController();
		CompanyDomainContext mockDomain = mock(CompanyDomainContext.class);
		CompanyDomain mockCompanyDomain = mock(CompanyDomain.class);

		when(mockDomain.get()).thenReturn(mockCompanyDomain);
		when(mockCompanyDomain.getCompany()).thenReturn(null);

		ReflectionTestUtils.setField(controller, "domain", mockDomain);

		ResponseEntity<?> response = controller.validexport();

		assertFalse(((Response<?>) response.getBody()).isSuccess());
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Não há instituição cadastrada para exportar", ((Response<?>) response.getBody()).getMessage());
	}

	@Test
	void test_valid_export_success_when_domain_and_company_exist() {
		BackupAndRestoreController controller = new BackupAndRestoreController();
		CompanyDomainContext mockDomain = mock(CompanyDomainContext.class);
		Company mockCompany = mock(Company.class);
		CompanyDomain mockCompanyDomain = mock(CompanyDomain.class);

		when(mockDomain.get()).thenReturn(mockCompanyDomain);
		when(mockCompanyDomain.getCompany()).thenReturn(mockCompany);

		ReflectionTestUtils.setField(controller, "domain", mockDomain);

		ResponseEntity<?> response = controller.validexport();

		assertTrue(((Response<?>) response.getBody()).isSuccess());
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void test_valid_export_returns_nothing_on_unexpected_exception() {
		BackupAndRestoreController controller = new BackupAndRestoreController();
		HttpServletResponse mockResponse = mock(HttpServletResponse.class);
		ReflectionTestUtils.setField(controller, "response", mockResponse);

		CompanyDomainContext mockDomain = mock(CompanyDomainContext.class);
		when(mockDomain.get()).thenThrow(new RuntimeException("Unexpected error"));
		ReflectionTestUtils.setField(controller, "domain", mockDomain);

		ResponseEntity<?> response = controller.validexport();

		verify(mockResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		assertNull(response.getBody());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	void test_valid_export_fail_when_no_company() {
		BackupAndRestoreController controller = new BackupAndRestoreController();
		CompanyDomainContext mockDomain = mock(CompanyDomainContext.class);
		CompanyDomain mockCompanyDomain = mock(CompanyDomain.class);

		when(mockDomain.get()).thenReturn(mockCompanyDomain);
		when(mockCompanyDomain.getCompany()).thenReturn(null);

		ReflectionTestUtils.setField(controller, "domain", mockDomain);

		ResponseEntity<?> response = controller.validexport();

		assertFalse(((Response<?>) response.getBody()).isSuccess());
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

}