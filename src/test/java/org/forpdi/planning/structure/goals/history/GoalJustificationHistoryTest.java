package org.forpdi.planning.structure.goals.history;

import org.forpdi.core.user.User;
import org.forpdi.planning.jobs.GoalDTO;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GoalJustificationHistoryTest {
	private GoalJustificationHistory goalJustificationHistory;
	private StructureLevelInstance mockLevelInstance;
	private User mockUser;

	@BeforeEach
	void setUp() {
		mockLevelInstance = mock(StructureLevelInstance.class);
		mockUser = mock(User.class);

		goalJustificationHistory = new GoalJustificationHistory();
		goalJustificationHistory.setLevelInstance(mockLevelInstance);
		goalJustificationHistory.setUser(mockUser);
		goalJustificationHistory.setJustification("Test Justification");
		goalJustificationHistory.setReachedValue(90.5);
	}

	@Test
	@DisplayName("Deve atribuir e validar os parâmetros do objeto.")
	void testGettersAndSetters() {
		goalJustificationHistory.setId(1L);
		assertEquals(1L, goalJustificationHistory.getId());
		goalJustificationHistory.setJustification("New Justification");
		assertEquals("New Justification", goalJustificationHistory.getJustification());

		goalJustificationHistory.setReachedValue(100.0);
		assertEquals(100.0, goalJustificationHistory.getReachedValue());

		Date updatedAt = new Date();
		goalJustificationHistory.setUpdatedAt(updatedAt);
		assertEquals(updatedAt, goalJustificationHistory.getUpdatedAt());

		goalJustificationHistory.setLevelInstance(mockLevelInstance);
		assertEquals(mockLevelInstance, goalJustificationHistory.getLevelInstance());

		goalJustificationHistory.setUser(mockUser);
		assertEquals(mockUser, goalJustificationHistory.getUser());
	}

	@Test
	@DisplayName("Deve obter a data do último update.")
	void testDefaultUpdatedAt() {
		assertNotNull(goalJustificationHistory.getUpdatedAt());
	}

	@Test
	@DisplayName("Deve vincular a um objeto GoalJustificationHistory o status de alcançado e justificação.")
	void testJustificationAndReachedValue() {
		GoalJustificationHistory.JustificationAndReachedValue justificationAndReachedValue = new GoalJustificationHistory.JustificationAndReachedValue();

		justificationAndReachedValue.setValues("Test Justification", 75.0);
		assertEquals("Test Justification", justificationAndReachedValue.getJustification());
		assertEquals(75.0, justificationAndReachedValue.getReachedValue());

		assertFalse(justificationAndReachedValue.isEmpty());

		GoalJustificationHistory.JustificationAndReachedValue other = new GoalJustificationHistory.JustificationAndReachedValue();
		other.setValues("Test Justification", 75.0);
		assertEquals(justificationAndReachedValue, other);
		assertEquals(justificationAndReachedValue.hashCode(), other.hashCode());
	}

	@Test
	@DisplayName("Deve verificar os se o status e justificação são vazios.")
	void testJustificationAndReachedValueEmptyCheck() {
		GoalJustificationHistory.JustificationAndReachedValue justificationAndReachedValue = new GoalJustificationHistory.JustificationAndReachedValue();

		assertTrue(justificationAndReachedValue.isEmpty());

		justificationAndReachedValue.setValues("Justification", 50.0);
		assertFalse(justificationAndReachedValue.isEmpty());
	}

	@Test
	@DisplayName("Deve validar o uso do Equals e HashCode entre objetos.")
	void testEqualsAndHashCode() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(10.0);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification("Test");
		goalJustificationHistory2.setReachedValue(10.0);

		assertEquals(goalJustificationHistory1, goalJustificationHistory2);
		assertEquals(goalJustificationHistory1.hashCode(), goalJustificationHistory2.hashCode());
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Caso sejam exatamente os mesmos.")
	void testEqualsComparingTheSameObject() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(10.0);

		assertEquals(goalJustificationHistory1, goalJustificationHistory1);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Caso instâncias diferentes, porém com mesmos parâmetros.")
	void testNotEqualsJustificationsNotSame() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(10.0);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification("Different Test");
		goalJustificationHistory2.setReachedValue(10.0);

		assertNotEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Um objeto contém justificação nula.")
	void testNotEqualsOneNullJustification() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification(null);
		goalJustificationHistory1.setReachedValue(10.0);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification("Test");
		goalJustificationHistory2.setReachedValue(10.0);

		assertNotEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Todas as justificativas nulas.")
	void testNotEqualsAllNullJustification() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification(null);
		goalJustificationHistory1.setReachedValue(10.0);
		assertFalse(goalJustificationHistory1.isEmpty());


		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification(null);
		goalJustificationHistory2.setReachedValue(10.0);

		assertEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Parâmetros alcançados diferentes.")
	void testNotEqualsReachedNotSame() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(10.0);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification("Test");
		goalJustificationHistory2.setReachedValue(15.0);

		assertNotEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Um parâmetro alcançado nulo.")
	void testNotEqualsOneNullReached() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(null);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification("Test");
		goalJustificationHistory2.setReachedValue(15.0);

		assertNotEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Todos os parâmetros alcançados nulos.")
	void testNotEqualsAllNullReached() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(null);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory2.setJustification("Test");
		goalJustificationHistory2.setReachedValue(null);

		assertEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Um dos objetos é nulo.")
	void testNotEqualsOneObjectNull() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(10.0);

		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory2
			= null;

		assertNotEquals(goalJustificationHistory1, goalJustificationHistory2);
	}

	@Test
	@DisplayName("Deve validar a igualdade entre dois objetos. Um objeto é de outra classe.")
	void testNotEqualsOneObjectOfAnotherClass() {
		GoalJustificationHistory.JustificationAndReachedValue goalJustificationHistory1
			= new GoalJustificationHistory.JustificationAndReachedValue();
		goalJustificationHistory1.setJustification("Test");
		goalJustificationHistory1.setReachedValue(10.0);

		GoalDTO goal = new GoalDTO();

		assertNotEquals(goalJustificationHistory1, goal);
	}

}