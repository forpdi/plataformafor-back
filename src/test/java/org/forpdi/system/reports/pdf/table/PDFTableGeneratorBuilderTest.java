package org.forpdi.system.reports.pdf.table;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

class PDFTableGeneratorBuilderTest {

	@Test
	@DisplayName("Dado um PDFTableGeneratorBuilder, quando adicionamos itens, então os itens são configurados corretamente")
	void testWithItems() {
		List<String> items = Arrays.asList("Item1", "Item2", "Item3");

		PDFTableGenerator<String> generator = new PDFTableGeneratorBuilder<String>()
			.withItems(items)
			.create();

		assertEquals(items, generator.items, "Os itens configurados devem ser os mesmos que foram passados.");
	}

	@Test
	@DisplayName("Dado um PDFTableGeneratorBuilder, quando o número de larguras relativas não corresponde ao número de colunas, então uma exceção é lançada")
	void testWithInvalidColumnWidths() {
		float[] widths = {1.5f, 2.0f};

		PDFTableGeneratorBuilder<String> builder = new PDFTableGeneratorBuilder<>();
		builder.withItems(Arrays.asList("Item1", "Item2", "Item3"));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			builder.withRelativeColumnWidths(widths).create();
		});
		assertEquals("Error to build a pdf table: relative colmuns widths should be equals to number of columns", exception.getMessage());
	}

	@Test
	@DisplayName("Dado um PDFTableGeneratorBuilder, quando os itens são nulos, então uma exceção é lançada")
	void testWithNullItems() {
		PDFTableGeneratorBuilder<String> builder = new PDFTableGeneratorBuilder<>();

		builder.withItems(null);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			builder.create();
		});
		assertEquals("Error to build a pdf table: items should not be null", exception.getMessage());
	}
}
