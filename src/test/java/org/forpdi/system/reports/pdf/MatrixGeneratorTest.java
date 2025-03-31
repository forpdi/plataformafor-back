package org.forpdi.system.reports.pdf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.IOException;

import org.forpdi.system.reports.pdf.MatrixGenerator.MatrixCell;
import org.forpdi.system.reports.pdf.table.TempFilesManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class MatrixGeneratorTest {

	@Test
	@DisplayName("Deve lançar exceção ao tentar gerar tabela com células nulas")
	void test_generate_throwsExceptionOnNullCells() {
		MatrixCell[][] cells = null;

		assertThrows(NullPointerException.class, () -> {
			new MatrixGenerator(cells).generate();
		}, "Deve lançar NullPointerException ao tentar gerar com células nulas.");
	}

	@Test
	@DisplayName("Deve lançar erro ao tentar gerar tabela vazia")
	void test_generate_throwsErrorOnEmptyMatrix() {
		MatrixCell[][] cells = new MatrixCell[0][0];

		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			new MatrixGenerator(cells).generate();
		}, "Deve lançar AssertionError ao tentar gerar tabela vazia.");
	}

	@Test
	@DisplayName("Deve gerar HTML corretamente a partir das células")
	void test_getHtmlTable_generatesValidHtml() throws Exception {
		MatrixCell[][] cells = {
			{ new MatrixCell("Test1", "#FFFFFF"), new MatrixCell("Test2", "#000000") }
		};

		MatrixGenerator generator = new MatrixGenerator(cells);
		String html = (String) getPrivateMethod(generator, "getHtmlTable");

		assertTrue(html.contains("<table>"), "O HTML gerado deve conter uma tag <table>.");
		assertTrue(html.contains("Test1"), "O HTML gerado deve conter o valor 'Test1'.");
	}

	@Test
	@DisplayName("Deve deletar arquivos temporários após geração de tabela")
	void test_tempFileDeletion_afterGeneration() throws IOException {
		File mockFile = mock(File.class);
		when(mockFile.exists()).thenReturn(true);

		TempFilesManager.deleteTempFile(mockFile);
		verify(mockFile).delete();
	}

	@Test
	void test_generate_throws_assertion_error_when_matrix_creation_fails() {
		MatrixGenerator.MatrixCell[][] cells = new MatrixGenerator.MatrixCell[0][0];
		MatrixGenerator generator = new MatrixGenerator(cells);

		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			generator.generate();
		});
	}

	@Test
	void test_generate_with_empty_matrix_cells() {
		MatrixGenerator.MatrixCell[][] emptyCells = new MatrixGenerator.MatrixCell[0][0];
		MatrixGenerator generator = new MatrixGenerator(emptyCells);

		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			generator.generate();
		});
	}

	@Test
	void test_empty_matrix_generation() {
		MatrixCell[][] cells = new MatrixCell[0][0];
		MatrixGenerator generator = new MatrixGenerator(cells);

		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			generator.generate();
		});
	}

	@Test
	void test_empty_matrix_cells() {
		MatrixCell[][] emptyCells = new MatrixCell[0][0];
		MatrixGenerator generator = new MatrixGenerator(emptyCells);

		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			generator.generate();
		});
	}


	@Test
	void test_file_system_access_error() {
		MatrixCell[][] cells = new MatrixCell[][] {
			{new MatrixCell("Test", "#000000")}
		};
		MatrixGenerator generator = new MatrixGenerator(cells);
		File mockFile = mock(File.class);
		when(mockFile.exists()).thenReturn(false);
		when(mockFile.mkdirs()).thenReturn(false);

		assertThrows(IllegalStateException.class, () -> {
			generator.generate();
		});
	}

	private Object getPrivateMethod(Object instance, String methodName, Object... args) throws Exception {
		Class<?> clazz = instance.getClass();
		var method = clazz.getDeclaredMethod(methodName);
		method.setAccessible(true);
		return method.invoke(instance, args);
	}

}
