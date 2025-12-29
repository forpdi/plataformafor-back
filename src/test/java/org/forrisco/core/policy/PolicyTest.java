package org.forrisco.core.policy;

import java.util.Calendar;
import java.util.Date;

import org.forpdi.core.company.Company;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolicyTest {
	private Policy policy;
	private Company company;

	@BeforeEach
	@DisplayName("Inicializa o objeto Policy e Company antes de cada teste")
	void setUp() {
		policy = new Policy();
		company = new Company();
	}

	@Test
	@DisplayName("Teste dos métodos get e set para atributos primitivos e objetos")
	void testGettersAndSetters() {

		String name = "Policy Test";
		String description = "Policy description test";
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.JANUARY, 1);
		Date validityBegin = calendar.getTime();
		calendar.set(2024, Calendar.DECEMBER, 31);
		Date validityEnd = calendar.getTime();
		int nline = 10;
		int ncolumn = 5;
		String probability = "High";
		String impact = "Severe";
		String matrix = "Matrix Example";
		String pidDescriptions = "Descriptions Example";
		boolean archived = true;
		boolean hasLinkedPlans = false;
		String[][] riskLevel = {{"Low", "Medium"}, {"High", "Critical"}};

		policy.setName(name);
		policy.setDescription(description);
		policy.setValidityBegin(validityBegin);
		policy.setValidityEnd(validityEnd);
		policy.setNline(nline);
		policy.setNcolumn(ncolumn);
		policy.setProbability(probability);
		policy.setImpact(impact);
		policy.setMatrix(matrix);
		policy.setPIDescriptions(pidDescriptions);
		policy.setArchived(archived);
		policy.setHasLinkedPlans(hasLinkedPlans);
		policy.setRisk_level(riskLevel);
		policy.setCompany(company);

		assertEquals(name, policy.getName(), "O valor de 'name' está incorreto.");
		assertEquals(description, policy.getDescription(), "O valor de 'description' está incorreto.");
		assertEquals(validityBegin, policy.getValidityBegin(), "O valor de 'validityBegin' está incorreto.");
		assertEquals(validityEnd, policy.getValidityEnd(), "O valor de 'validityEnd' está incorreto.");
		assertEquals(nline, policy.getNline(), "O valor de 'nline' está incorreto.");
		assertEquals(ncolumn, policy.getNcolumn(), "O valor de 'ncolumn' está incorreto.");
		assertEquals(probability, policy.getProbability(), "O valor de 'probability' está incorreto.");
		assertEquals(impact, policy.getImpact(), "O valor de 'impact' está incorreto.");
		assertEquals(matrix, policy.getMatrix(), "O valor de 'matrix' está incorreto.");
		assertEquals(pidDescriptions, policy.getPIDescriptions(), "O valor de 'PIDescriptions' está incorreto.");
		assertEquals(archived, policy.isArchived(), "O valor de 'archived' está incorreto.");
		assertEquals(hasLinkedPlans, policy.getHasLinkedPlans(), "O valor de 'hasLinkedPlans' está incorreto.");
		assertArrayEquals(riskLevel, policy.getRisk_level(), "O valor de 'riskLevel' está incorreto.");
		assertEquals(company, policy.getCompany(), "O valor de 'company' está incorreto.");
	}

	@Test
	@DisplayName("Teste do comportamento padrão ao inicializar com o construtor vazio")
	void testDefaultConstructor() {
		assertNull(policy.getName(), "O valor inicial de 'name' deve ser null.");
		assertNull(policy.getDescription(), "O valor inicial de 'description' deve ser null.");
		assertNull(policy.getValidityBegin(), "O valor inicial de 'validityBegin' deve ser null.");
		assertNull(policy.getValidityEnd(), "O valor inicial de 'validityEnd' deve ser null.");
		assertEquals(0, policy.getNline(), "O valor inicial de 'nline' deve ser 0.");
		assertEquals(0, policy.getNcolumn(), "O valor inicial de 'ncolumn' deve ser 0.");
		assertNull(policy.getProbability(), "O valor inicial de 'probability' deve ser null.");
		assertNull(policy.getImpact(), "O valor inicial de 'impact' deve ser null.");
		assertNull(policy.getMatrix(), "O valor inicial de 'matrix' deve ser null.");
		assertNull(policy.getPIDescriptions(), "O valor inicial de 'PIDescriptions' deve ser null.");
		assertFalse(policy.isArchived(), "O valor inicial de 'archived' deve ser false.");
		assertNull(policy.getRisk_level(), "O valor inicial de 'risk_level' deve ser null.");
		assertNull(policy.getCompany(), "O valor inicial de 'company' deve ser null.");
		assertFalse(policy.getHasLinkedPlans(), "O valor inicial de 'hasLinkedPlans' deve ser false.");
	}

	@Test
	@DisplayName("Teste do método getPIDescriptionAsObject()")
	void testGetPIDescriptionAsObject() {
		String json = """
			    {
			        "PIDescriptions": {
			            "idescriptions": [
			                { "description": "Description 1", "value": "Value 1" },
			                { "description": "Description 2", "value": "Value 2" }
			            ],
			            "pdescriptions": [
			                { "description": "PDescription 1", "value": "PValue 1" },
			                { "description": "PDescription 2", "value": "PValue 2" }
			            ]
			        }
			    }
			""";

		Policy policy = new Policy();
		policy.setPIDescriptions(json);

		PIDescriptions result = policy.getPIDescriptionAsObject();


		assertNotNull(result, "O objeto PIDescriptions retornado não deve ser nulo.");

		assertNotNull(result.getIdescriptions(), "O array 'idescriptions' não deve ser nulo.");
		assertEquals(2, result.getIdescriptions().length, "O tamanho do array 'idescriptions' está incorreto.");
		assertEquals("Description 1", result.getIdescriptions()[0].getDescription(), "Descrição 1 de 'idescriptions' está incorreta.");
		assertEquals("Value 1", result.getIdescriptions()[0].getValue(), "Valor 1 de 'idescriptions' está incorreto.");
		assertEquals("Description 2", result.getIdescriptions()[1].getDescription(), "Descrição 2 de 'idescriptions' está incorreta.");
		assertEquals("Value 2", result.getIdescriptions()[1].getValue(), "Valor 2 de 'idescriptions' está incorreto.");

		assertNotNull(result.getPdescriptions(), "O array 'pdescriptions' não deve ser nulo.");
		assertEquals(2, result.getPdescriptions().length, "O tamanho do array 'pdescriptions' está incorreto.");
		assertEquals("PDescription 1", result.getPdescriptions()[0].getDescription(), "Descrição 1 de 'pdescriptions' está incorreta.");
		assertEquals("PValue 1", result.getPdescriptions()[0].getValue(), "Valor 1 de 'pdescriptions' está incorreto.");
		assertEquals("PDescription 2", result.getPdescriptions()[1].getDescription(), "Descrição 2 de 'pdescriptions' está incorreta.");
		assertEquals("PValue 2", result.getPdescriptions()[1].getValue(), "Valor 2 de 'pdescriptions' está incorreto.");
	}

	@Test
	@DisplayName("Teste do método getMatrixLevels() com uma matriz válida")
	void testGetMatrixLevels_ValidMatrix() {
		String matrix = "[0,0]Low;[0,1]Medium;[1,0]High;[1,1]Critical";
		int nline = 1;
		int ncolumn = 1;

		Policy policy = new Policy();
		policy.setMatrix(matrix);
		policy.setNline(nline);
		policy.setNcolumn(ncolumn);

		String[][] result = policy.getMatrixLevels();

		assertNotNull(result, "O array retornado não deve ser nulo.");
		assertEquals(2, result.length, "O número de linhas do array está incorreto.");
		assertEquals(2, result[0].length, "O número de colunas do array na linha 0 está incorreto.");
		assertEquals(2, result[1].length, "O número de colunas do array na linha 1 está incorreto.");

		assertEquals("Low", result[0][0], "Valor da posição [0][0] está incorreto.");
		assertEquals("Medium", result[0][1], "Valor da posição [0][1] está incorreto.");
		assertEquals("High", result[1][0], "Valor da posição [1][0] está incorreto.");
		assertEquals("Critical", result[1][1], "Valor da posição [1][1] está incorreto.");
	}

	@Test
	@DisplayName("Teste do método getMatrixLevels() com formato inválido de matriz")
	void testGetMatrixLevels_InvalidMatrix() {
		String matrix = "[0,0]Low;[1,]Medium";
		int nline = 1;
		int ncolumn = 1;

		Policy policy = new Policy();
		policy.setMatrix(matrix);
		policy.setNline(nline);
		policy.setNcolumn(ncolumn);

		Exception exception = assertThrows(IllegalStateException.class, policy::getMatrixLevels);

		assertEquals("Invalid risk matrix format", exception.getMessage(), "Mensagem de erro incorreta.");
	}

	@Test
	@DisplayName("Teste do método getMatrixLevels() com coordenadas fora do limite")
	void testGetMatrixLevels_OutOfBounds() {
		String matrix = "[2,2]Invalid";
		int nline = 1;
		int ncolumn = 1;

		Policy policy = new Policy();
		policy.setMatrix(matrix);
		policy.setNline(nline);
		policy.setNcolumn(ncolumn);

		Exception exception = assertThrows(IllegalStateException.class, policy::getMatrixLevels);

		assertEquals("Invalid risk matrix format", exception.getMessage(), "Mensagem de erro incorreta.");
	}
}