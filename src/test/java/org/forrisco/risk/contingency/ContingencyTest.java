package org.forrisco.risk.contingency;

import org.forpdi.core.user.User;
import org.forrisco.risk.Risk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContingencyTest {

	private Contingency contingency;
	private User mockUser;
	private User mockManager;
	private Risk mockRisk;

	@BeforeEach
	public void setUp() {
		mockUser = Mockito.mock(User.class);
		mockManager = Mockito.mock(User.class);
		mockRisk = Mockito.mock(Risk.class);
		contingency = new Contingency();
	}

	@DisplayName("Contingency Validação da Classe por meio dos Getters and Setters.")
	@Test
	public void testContingencyGettersAndSetters() {
		contingency.setUser(mockUser);
		contingency.setManager(mockManager);
		contingency.setRisk(mockRisk);
		contingency.setAction("Test Action");
		Date beginDate = new Date();
		contingency.setValidityBegin(beginDate);
		Date endDate = new Date();
		contingency.setValidityEnd(endDate);

		assertEquals(mockUser, contingency.getUser(), "Os usuários deveriam corresponder");
		assertEquals(mockManager, contingency.getManager(), "Os gestores deveriam corresponder");
		assertEquals(mockRisk, contingency.getRisk(), "Os riscos deveriam corresponder");
		assertEquals("Test Action", contingency.getAction(), "A ação deveriam corresponder");
		assertEquals(beginDate, contingency.getValidityBegin(), "As datas de início deveriam ser iguais");
		assertEquals(endDate, contingency.getValidityEnd(), "As datas finais deveriam ser iguais");
	}

	@DisplayName("Contingency Validação do construtor vazio.")
	@Test
	public void testContingencyEmptyConstructor() {
		assertNotNull(contingency);
	}

	@DisplayName("Contingency Validação do construtor com parâmetro.")
	@Test
	public void testContingencyFullConstructor() {
		contingency.setUser(mockUser);
		contingency.setManager(mockManager);
		contingency.setRisk(mockRisk);
		contingency.setAction("Test Action");
		Date beginDate = new Date();
		contingency.setValidityBegin(beginDate);
		Date endDate = new Date();
		contingency.setValidityEnd(endDate);

		Contingency newContingency = new Contingency(contingency);

		assertEquals(mockUser, newContingency.getUser(), "Os usuários deveriam corresponder");
		assertEquals("Test Action", newContingency.getAction(), "A ação deveriam corresponder");
		assertEquals(beginDate, newContingency.getValidityBegin(), "As datas de início deveriam ser iguais");
		assertEquals(endDate, newContingency.getValidityEnd(), "As datas finais deveriam ser iguais");
	}

}