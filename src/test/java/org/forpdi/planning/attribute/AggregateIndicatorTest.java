package org.forpdi.planning.attribute;

import org.forpdi.planning.structure.StructureLevelInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregateIndicatorTest {

	@Test
	@DisplayName("Deve definir e obter o indicador corretamente")
	void testSetAndGetIndicator() {
		AggregateIndicator aggregateIndicator = new AggregateIndicator();
		StructureLevelInstance indicator = new StructureLevelInstance();
		indicator.setId(2L);
		aggregateIndicator.setIndicator(indicator);

		assertEquals(indicator, aggregateIndicator.getIndicator(), "O indicador deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter o agregado corretamente")
	void testSetAndGetAggregate() {
		AggregateIndicator aggregateIndicator = new AggregateIndicator();
		StructureLevelInstance aggregate = new StructureLevelInstance();
		aggregate.setId(1L);
		aggregateIndicator.setAggregate(aggregate);

		assertEquals(aggregate, aggregateIndicator.getAggregate(), "O agregado deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter a porcentagem corretamente")
	void testSetAndGetPercentage() {
		AggregateIndicator aggregateIndicator = new AggregateIndicator();
		Double percentage = 75.5;
		aggregateIndicator.setPercentage(percentage);

		assertEquals(percentage, aggregateIndicator.getPercentage(), "A porcentagem deve ser configurada e recuperada corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter o exportAggregateId corretamente")
	void testSetAndGetExportAggregateId() {
		AggregateIndicator aggregateIndicator = new AggregateIndicator();
		Long exportAggregateId = 123L;
		aggregateIndicator.setExportAggregateId(exportAggregateId);

		assertEquals(exportAggregateId, aggregateIndicator.getExportAggregateId(), "O exportAggregateId deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve definir e obter o exportIndicatorId corretamente")
	void testSetAndGetExportIndicatorId() {
		AggregateIndicator aggregateIndicator = new AggregateIndicator();
		Long exportIndicatorId = 456L;
		aggregateIndicator.setExportIndicatorId(exportIndicatorId);

		assertEquals(exportIndicatorId, aggregateIndicator.getExportIndicatorId(), "O exportIndicatorId deve ser configurado e recuperado corretamente");
	}

	@Test
	@DisplayName("Deve gerar uma string representativa corretamente")
	void testToString() {
		AggregateIndicator aggregateIndicator = new AggregateIndicator();
		StructureLevelInstance indicator = new StructureLevelInstance();
		StructureLevelInstance aggregate = new StructureLevelInstance();

		indicator.setId(1L);
		aggregate.setId(2L);

		aggregateIndicator.setIndicator(indicator);
		aggregateIndicator.setAggregate(aggregate);
		aggregateIndicator.setPercentage(50.0);

		String expected = "AggregateIndicator [indicator=" + indicator.toString() +
			", aggregate=" + aggregate.toString() +
			", percentage=50.0]";
		assertEquals(expected, aggregateIndicator.toString(), "O método toString deve gerar a representação correta");
	}
}
