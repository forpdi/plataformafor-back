package org.forpdi.planning.structure;

import org.forpdi.core.company.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructureTest {

	@Test
	@DisplayName("Deve definir e obter o nome da estrutura corretamente")
	void testSetAndGetName() {
		Structure structure = new Structure();
		String name = "Estrutura de Teste";
		structure.setName(name);

		assertEquals(name, structure.getName(), "O nome da estrutura deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter a descrição da estrutura corretamente")
	void testSetAndGetDescription() {
		Structure structure = new Structure();
		String description = "Descrição de Teste para a Estrutura";
		structure.setDescription(description);

		assertEquals(description, structure.getDescription(), "A descrição da estrutura deve ser configurada e recuperada corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter a empresa associada corretamente")
	void testSetAndGetCompany() {
		Structure structure = new Structure();
		Company company = new Company();
		company.setId(1L);
		company.setName("Empresa Teste");
		structure.setCompany(company);

		assertEquals(company, structure.getCompany(), "A empresa associada deve ser configurada e recuperada corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter os níveis associados corretamente")
	void testSetAndGetLevels() {
		Structure structure = new Structure();
		StructureLevel level1 = new StructureLevel();
		StructureLevel level2 = new StructureLevel();
		List<StructureLevel> levels = List.of(level1, level2);
		structure.setLevels(levels);

		assertEquals(levels, structure.getLevels(), "Os níveis associados devem ser configurados e recuperados corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter o ID de exportação da empresa corretamente")
	void testSetAndGetExportCompanyId() {
		Structure structure = new Structure();
		Long exportCompanyId = 12345L;
		structure.setExportCompanyId(exportCompanyId);

		assertEquals(exportCompanyId, structure.getExportCompanyId(), "O ID de exportação da empresa deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve criar um objeto Structure com valores padrão")
	void testDefaultValues() {
		Structure structure = new Structure();

		assertNull(structure.getName(), "O nome da estrutura deve ser nulo por padrão");
		assertNull(structure.getDescription(), "A descrição da estrutura deve ser nula por padrão");
		assertNull(structure.getCompany(), "A empresa associada deve ser nula por padrão");
		assertNull(structure.getLevels(), "Os níveis associados devem ser nulos por padrão");
		assertNull(structure.getExportCompanyId(), "O ID de exportação deve ser nulo por padrão");
	}
}
