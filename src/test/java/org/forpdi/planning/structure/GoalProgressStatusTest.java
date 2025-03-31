package org.forpdi.planning.structure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GoalProgressStatusTest {

	@Test
	@DisplayName("Dado um id, quando procuramos o status correspondente, então o status correto é retornado")
	void testGetById_ValidId() {
		int validId = 1;

		GoalProgressStatus status = GoalProgressStatus.getById(validId);

		assertEquals(GoalProgressStatus.MINIMUM_BELOW, status, "O status retornado deve ser o correto para o id.");
	}

	@Test
	@DisplayName("Dado um id inválido, quando procuramos o status correspondente, então uma exceção é lançada")
	void testGetById_InvalidId() {
		int invalidId = 99;

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			GoalProgressStatus.getById(invalidId);
		});

		assertEquals("Status de progresso inválido: " + invalidId, exception.getMessage(), "A exceção lançada deve ter a mensagem correta.");
	}

	@Test
	@DisplayName("Dado um GoalProgressStatus, quando obtemos o id, então o id correto é retornado")
	void testGetId() {
		GoalProgressStatus status = GoalProgressStatus.ENOUGH_UP;

		int id = status.getId();

		assertEquals(3, id, "O id retornado deve ser 3 para o status 'ENOUGH_UP'.");
	}

	@Test
	@DisplayName("Dado um GoalProgressStatus, quando obtemos o label, então o label correto é retornado")
	void testGetLabel() {
		GoalProgressStatus status = GoalProgressStatus.MAXIMUM_UP;

		String label = status.getLabel();

		assertEquals("Acima do máximo", label, "O label retornado deve ser 'Acima do máximo' para o status 'MAXIMUM_UP'.");
	}

	@Test
	@DisplayName("Dado um GoalProgressStatus, quando definimos um novo id, então o id é atualizado corretamente")
	void testSetId() {
		GoalProgressStatus status = GoalProgressStatus.NOT_STARTED;

		status.setId(10);

		assertEquals(10, status.getId(), "O id deve ser atualizado corretamente.");
	}

	@Test
	@DisplayName("Dado um GoalProgressStatus, quando definimos um novo label, então o label é atualizado corretamente")
	void testSetLabel() {
		GoalProgressStatus status = GoalProgressStatus.NOT_FILLED;

		status.setLabel("Novo Label");

		assertEquals("Novo Label", status.getLabel(), "O label deve ser atualizado corretamente.");
	}
}
