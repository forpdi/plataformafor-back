package org.forrisco.core.process;

import org.forpdi.core.company.Company;
import org.forpdi.core.storage.file.Archive;
import org.forrisco.core.unit.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessTest {

	@Test
	@DisplayName("Criação e validação do objeto por meio dos getters e setters")
	void testObjectCreation() {
		Process process = new Process();

		Company company = new Company();
		Archive file = new Archive();
		Unit unitCreator = new Unit();
		Unit unit = new Unit();
		String name = "Process 1";
		String objective = "Objective of process 1";
		String fileLink = "01.pdf";

		process.setCompany(company);
		process.setFile(file);
		process.setFileLink(fileLink);
		process.setUnitCreator(unitCreator);
		process.setUnit(unit);
		process.setName(name);
		process.setObjective(objective);

		assertEquals(company, process.getCompany(), "O valor retornado não foi o esperado.");
		assertEquals(file, process.getFile(), "O valor retornado não foi o esperado.");
		assertEquals(fileLink, process.getFileLink(), "O valor retornado não foi o esperado.");
		assertEquals(unitCreator, process.getUnitCreator(), "O valor retornado não foi o esperado.");
		assertEquals(unit, process.getUnit(), "O valor retornado não foi o esperado.");
		assertEquals(name, process.getName(), "O valor retornado não foi o esperado.");
		assertEquals(objective, process.getObjective(), "O valor retornado não foi o esperado.");

	}

	@Test
	@DisplayName("Deve retornar os nomes das unidades relacionadas corretamente")
	public void testGetRelatedUnitNames() {

		Process process = new Process();
		Unit unit1 = new Unit();
		unit1.setName("Financeiro");
		Unit unit2 = new Unit();
		unit2.setName("RH");
		process.setRelatedUnits(Arrays.asList(unit1, unit2));

		List<String> relatedUnitNames = process.getRelatedUnitNames();

		assertNotNull(process.getRelatedUnits(), "A lista de unidades relacionadas não deveria ser nula.");
		assertEquals(2, relatedUnitNames.size(), "O tamanho da lista retornada deveria de 2.");
		assertTrue(relatedUnitNames.contains("Financeiro"),
			"A lista de unidades relacionadas deveria conter 'Financeiro'");
		assertTrue(relatedUnitNames.contains("RH"), "A lista de unidades relacionadas deveria conter 'RH'");
	}

	@Test
	@DisplayName("Deve retornar uma lista vazia quando não há unidades relacionadas")
	public void testGetRelatedUnitNamesEmpty() {
		Process process = new Process();
		process.setRelatedUnits(null);

		List<String> relatedUnitNames = process.getRelatedUnitNames();

		assertTrue(relatedUnitNames.isEmpty(), "A lista retornada deveria ser vazia.");
	}

	@Test
	@DisplayName("Deve retornar as descrições dos objetivos corretamente")
	public void testGetProcessObjectivesDescriptions() {
		Process process = new Process();
		ProcessObjective objective1 = new ProcessObjective();
		objective1.setDescription("Aumentar a eficiência");
		ProcessObjective objective2 = new ProcessObjective();
		objective2.setDescription("Reduzir custos");
		process.setAllObjectives(Arrays.asList(objective1, objective2));

		List<String> descriptions = process.getProcessObjectivesDescriptions();

		assertNotNull(process.getAllObjectives(), "A lista de objetivos não deveria ser nula.");
		assertEquals(2, descriptions.size(), "O tamanho da lista retornada deveria de 2.");
		assertTrue(descriptions.contains("Aumentar a eficiência"),
			"A lista de unidades relacionadas deveria conter 'Aumentar a eficiência'");
		assertTrue(descriptions.contains("Reduzir custos"),
			"A lista de unidades relacionadas deveria conter 'Reduzir custos'");
	}

	@Test
	@DisplayName("Deve retornar uma lista vazia quando não há objetivos definidos")
	public void testGetProcessObjectivesDescriptions_Empty() {
		Process process = new Process();
		process.setAllObjectives(null);

		List<String> descriptions = process.getProcessObjectivesDescriptions();

		assertTrue(descriptions.isEmpty(), "A lista retornada deveria ser vazia.");
	}

	@Test
	@DisplayName("Deve retornar as descrições dos objetivos como string separada por vírgulas")
	public void testGetProcessObjectivesDescriptionsString() {
		Process process = new Process();
		ProcessObjective objective1 = new ProcessObjective();
		objective1.setDescription("Melhorar qualidade");
		ProcessObjective objective2 = new ProcessObjective();
		objective2.setDescription("Aumentar produtividade");
		process.setAllObjectives(Arrays.asList(objective1, objective2));

		String descriptionsString = process.getProcessObjectivesDescriptionsString();

		assertEquals("Melhorar qualidade, Aumentar produtividade", descriptionsString,
			"A descrição retornada não é a esperada.");
	}

	@Test
	@DisplayName("Deve retornar uma string vazia quando não há descrições de objetivos")
	public void testGetProcessObjectivesDescriptionsString_Empty() {
		Process process = new Process();
		process.setAllObjectives(null);

		String descriptionsString = process.getProcessObjectivesDescriptionsString();

		assertEquals("", descriptionsString, "A descrição retornada deveria ser uma string vazia.");
	}

}