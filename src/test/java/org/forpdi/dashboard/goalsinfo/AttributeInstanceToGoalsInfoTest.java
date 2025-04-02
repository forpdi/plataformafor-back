package org.forpdi.dashboard.goalsinfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AttributeInstanceToGoalsInfoTest {

	AttributeInstanceToGoalsInfo attrIGoalsInfoExpected;

	@BeforeEach()
	public void setup(){

		attrIGoalsInfoExpected = new AttributeInstanceToGoalsInfo(1D, new Date(), false,
			false, false, false, false, 1L);
	}

	@DisplayName("AttributeInstanceToGoalsInfo Criação do objeto com o construtor completo.")
	@Test
	void AttributeInstanceToGoalsInfoCreationWithFullConstructor(){

		AttributeInstanceToGoalsInfo expected = attrIGoalsInfoExpected;

		AttributeInstanceToGoalsInfo createdByConstrucor = new AttributeInstanceToGoalsInfo(expected.getValueAsNumber(),
			expected.getValueAsDate(), expected.isFinishDate(), expected.isExpectedField(), expected.isReachedField(),
			expected.isMaximumField() ,expected.isMinimumField(), expected.getLevelInstanceId());

		assertNotNull(createdByConstrucor, "O objeto instânciado não deve ser nulo.");
		assertEquals(expected.getValueAsNumber(), createdByConstrucor.getValueAsNumber(),
			"Os valores não correspondem");
		assertEquals(expected.getValueAsDate(), createdByConstrucor.getValueAsDate(),
			"As data são divergentes.");
		assertEquals(expected.isFinishDate(), createdByConstrucor.isFinishDate(),
			"Os status de data final não corresponde ao esperado.");
		assertEquals(expected.isExpectedField(), createdByConstrucor.isExpectedField(),
			"O valor de campo esperado é divergente do esperado.");
		assertEquals(expected.isReachedField(), createdByConstrucor.isReachedField(),
			"O valor de ReachedField é divergente do esperado.");
		assertEquals(expected.isMaximumField(), createdByConstrucor.isMaximumField(),
			"O valor de MaximumField é divergente do esperado.");
		assertEquals(expected.isMinimumField(), createdByConstrucor.isMinimumField(),
			"O valor de MinimumField é divergente do esperado.");
		assertEquals(expected.getLevelInstanceId(), createdByConstrucor.getLevelInstanceId(),
			"O id da instância é divergente do esperado.");
	}

	@DisplayName("AttributeInstanceToGoalsInfo Criação do objeto com o construtor vazio.")
	@Test
	void AttributeInstanceToGoalsInfoCreationWithEmptyConstructor(){

		AttributeInstanceToGoalsInfo attrGoals = new AttributeInstanceToGoalsInfo();

		assertNotNull(attrGoals, "O objeto não deve ser nulo.");
	}

	@DisplayName("AttributeInstanceToGoalsInfo Criação do objeto e setando os parâmetros.")
	@Test
	void AttributeInstanceToGoalsInfoSettingParams(){
		AttributeInstanceToGoalsInfo attrToChange = attrIGoalsInfoExpected;

		attrToChange.setValueAsNumber(2D);
		attrToChange.setValueAsDate(new Date());
		attrToChange.setFinishDate(true);
		attrToChange.setExpectedField(true);
		attrToChange.setReachedField(true);
		attrToChange.setMinimumField(true);
		attrToChange.setMaximumField(true);
		attrToChange.setLevelInstanceId(3L);

		assertNotNull(attrToChange, "O objeto não deve ser nulo");
		assertEquals(2D, attrToChange.getValueAsNumber(),
			"O valor atualizado não corresponde ao esperado.");
		assertEquals(attrIGoalsInfoExpected.getValueAsDate(), attrToChange.getValueAsDate(),
			"As data atualizada é diferente da esperada.");
		assertTrue(attrToChange.isFinishDate(),
			"Os status de data final não corresponde ao esperado.");
		assertTrue(attrToChange.isExpectedField(),
			"O valor de campo esperado é divergente do esperado.");
		assertTrue(attrToChange.isReachedField(),
			"O valor de ReachedField é divergente do esperado.");
		assertTrue(attrToChange.isMaximumField(),
			"O valor de MaximumField é divergente do esperado.");
		assertTrue(attrToChange.isMinimumField(),
			"O valor de MinimumField é divergente do esperado.");
		assertEquals(3L, attrToChange.getLevelInstanceId(),
			"O id da instância é divergente do esperado.");
	}
}