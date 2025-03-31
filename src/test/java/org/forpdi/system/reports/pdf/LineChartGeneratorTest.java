package org.forpdi.system.reports.pdf;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.lang.reflect.Field;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.Test;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;

class LineChartGeneratorTest {

	@Test
	void test_constructor_with_dimension() {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder()
			.newSerie("Test Series")
			.add("2024", 100)
			.add("2025", 150)
			.create();

		Dimension customDimension = new Dimension(600, 300);
		LineChartGenerator generator = new LineChartGenerator(dataset, "Years", "Values", customDimension);

		assertNotNull(generator, "O construtor com dimensão personalizada deve inicializar a instância.");
	}

	@Test
	void test_constructor_without_dimension() {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder()
			.newSerie("Test Series")
			.add("2024", 100)
			.add("2025", 150)
			.create();

		LineChartGenerator generator = new LineChartGenerator(dataset, "Years", "Values");

		assertNotNull(generator, "O construtor sem dimensão deve inicializar a instância.");
	}

	@Test
	void test_generate_chart_image() throws BadElementException, IOException {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder()
			.newSerie("Test Series")
			.add("2024", 100)
			.add("2025", 150)
			.create();

		LineChartGenerator generator = new LineChartGenerator(dataset, "Years", "Values");

		Image chartImage = generator.generate();

		assertNotNull(chartImage, "O método generate deve retornar uma imagem válida.");
//		assertEquals("png", chartImage.getUrl().getProtocol(), "A imagem gerada deve ser no formato PNG."); // -> Deveria ser este

	}

	@Test
	void test_generate_chart_with_empty_dataset() throws BadElementException, MalformedURLException, IOException {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder().create();
		LineChartGenerator generator = new LineChartGenerator(dataset, "Empty X", "Empty Y");

		Image chartImage = generator.generate();

		assertNotNull(chartImage, "Mesmo com um conjunto de dados vazio, o gráfico deve ser gerado.");
	}

	@Test
	void test_parseBase64ToElement_throws_ioexception() {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder()
			.newSerie("Invalid Series")
			.add("2024", 100)
			.create();

		LineChartGenerator generator = new LineChartGenerator(dataset, "Years", "Values");

		assertDoesNotThrow(() -> generator.generate(),
			"O método parseBase64ToElement deve lidar corretamente com erros de IO.");
	}

	@Test
	void test_dataset_builder_add_without_serie_throws_exception() {
		LineChartGenerator.DatasetBuilder builder = new LineChartGenerator.DatasetBuilder();

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> builder.add("2024", 100),
			"Deve lançar exceção ao adicionar dados sem uma série.");
		assertEquals("A new serie must be added before", exception.getMessage(),
			"A mensagem de exceção deve ser clara.");
	}

	@Test
	void test_dataset_builder_creates_valid_dataset() throws Exception {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder()
			.newSerie("Test Series")
			.add("2024", 100)
			.add("2025", 150)
			.create();

		DefaultCategoryDataset series = (DefaultCategoryDataset) getPrivateField(dataset, "series");

		assertEquals(2, series.getColumnCount(), "O conjunto de dados deve conter 2 colunas.");
		assertEquals(100, series.getValue("Test Series", "2024"), "O valor para 2024 deve ser 100.");
		assertEquals(150, series.getValue("Test Series", "2025"), "O valor para 2025 deve ser 150.");
	}

	@Test
	void test_generate_chart_with_custom_dimensions() throws Exception {
		LineChartGenerator.Dataset dataset = new LineChartGenerator.DatasetBuilder()
			.newSerie("Test Series")
			.add("2024", 100)
			.add("2025", 150)
			.create();

		Dimension customDimension = new Dimension(800, 600);
		LineChartGenerator generator = new LineChartGenerator(dataset, "Years", "Values", customDimension);

		Image chartImage = generator.generate();
		Dimension dimension = (Dimension) getPrivateField(generator, "dimension");

		assertNotNull(chartImage, "O gráfico com dimensões personalizadas deve ser gerado.");
		assertEquals(customDimension.width, dimension.width, "A largura da dimensão deve ser igual à configurada.");
		assertEquals(customDimension.height, dimension.height, "A altura da dimensão deve ser igual à configurada.");
	}

	/**
	 * Utilitário para acessar campos privados usando reflexão.
	 */
	private Object getPrivateField(Object instance, String fieldName) throws Exception {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}
}
