package org.forpdi.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

	@Test
	@DisplayName("Deve inicializar o contador com valor padrão (zero)")
	void givenDefaultConstructor_whenInstantiated_thenCounterShouldBeZero() {
		Counter counter = new Counter();

		assertEquals(0, counter.getCounter(), "O contador deveria ser inicializado com valor zero.");
	}

	@Test
	@DisplayName("Deve inicializar o contador com o valor fornecido no construtor")
	void givenParameterizedConstructor_whenInstantiated_thenCounterShouldMatchProvidedValue() {
		int initialValue = 10;

		Counter counter = new Counter(initialValue);

		assertEquals(initialValue, counter.getCounter(), "O contador deveria ser inicializado com o valor fornecido.");
	}

	@Test
	@DisplayName("Deve adicionar valor ao contador corretamente")
	void givenCounter_whenAddCalled_thenValueShouldBeAdded() {
		Counter counter = new Counter(5);
		int valueToAdd = 3;

		counter.add(valueToAdd);

		assertEquals(8, counter.getCounter(), "O valor deveria ser somado corretamente ao contador.");
	}

	@Test
	@DisplayName("Deve incrementar o contador em 1")
	void givenCounter_whenIncrementCalled_thenCounterShouldIncreaseByOne() {
		Counter counter = new Counter(5);

		counter.increment();

		assertEquals(6, counter.getCounter(), "O contador deveria ser incrementado em 1.");
	}

	@Test
	@DisplayName("Deve decrementar o contador em 1")
	void givenCounter_whenDecrementCalled_thenCounterShouldDecreaseByOne() {
		Counter counter = new Counter(5);

		counter.decrement();

		assertEquals(4, counter.getCounter(), "O contador deveria ser decrementado em 1.");
	}

	@Test
	@DisplayName("Deve retornar o valor atual do contador como string")
	void givenCounter_whenToStringCalled_thenShouldReturnCounterValueAsString() {
		Counter counter = new Counter(7);

		String result = counter.toString();

		assertEquals("7", result, "O método toString deveria retornar o valor atual do contador como string.");
	}
}
