package org.forpdi.core.utils;

import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UtilTest {
	@Test
	public void testGetDownloadFilesURLWithTrailingSlash() {
		String fileLink = "example-file.txt";
		String baseUrl = "http://example.com/";

		String expected = "http://example.com/forpdi/api/file/example-file.txt";
		String actual = Util.getDownloadFilesURL(fileLink, baseUrl);

		assertEquals(expected, actual, "The URL should be formatted correctly when baseUrl ends with a slash.");
	}

	@Test
	public void testGetDownloadFilesURLWithoutTrailingSlash() {
		String fileLink = "example-file.txt";
		String baseUrl = "http://example.com";

		String expected = "http://example.com/forpdi/api/file/example-file.txt";
		String actual = Util.getDownloadFilesURL(fileLink, baseUrl);

		assertEquals(expected, actual, "The URL should be formatted correctly when baseUrl does not end with a slash.");
	}

	@Test
	public void testGetDownloadFilesURLEmptyFileLink() {
		String fileLink = "";
		String baseUrl = "http://example.com/";

		String expected = "http://example.com/forpdi/api/file/";
		String actual = Util.getDownloadFilesURL(fileLink, baseUrl);

		assertEquals(expected, actual, "The URL should handle an empty fileLink correctly.");
	}

	@Test
	public void testGetDownloadFilesURLEmptyBaseUrl() {
		String fileLink = "example-file.txt";
		String baseUrl = "";

		String expected = "/forpdi/api/file/example-file.txt";
		String actual = Util.getDownloadFilesURL(fileLink, baseUrl);

		assertEquals(expected, actual, "The URL should handle an empty baseUrl correctly.");
	}

	@Test
	public void testCloseFile_withNonNullCloseable() throws IOException {
		Closeable closeable = mock(Closeable.class);

		assertDoesNotThrow(() -> Util.closeFile(closeable), "closeFile should not throw an exception for valid Closeable.");

		verify(closeable, times(1)).close();
	}

	@Test
	public void testCloseFile_withNullCloseable() {
		assertDoesNotThrow(() -> Util.closeFile(null), "closeFile should not throw an exception for null Closeable.");
	}

	@Test
	public void testCloseFile_withIOException() throws IOException {
		Closeable closeable = mock(Closeable.class);
		doThrow(new IOException("Test exception")).when(closeable).close();

		assertDoesNotThrow(() -> Util.closeFile(closeable), "closeFile should not propagate IOException.");

		verify(closeable, times(1)).close();
	}

	@Test
	public void testReplace() {
		StringBuilder builder = new StringBuilder("Hello World");
		String from = "World";
		String to = "Java";

		Util.replace(builder, from, to);

		assertEquals("Hello Java", builder.toString(), "The method should replace 'World' with 'Java'.");
	}

	@Test
	public void testReplaceAll_withValidInputs() {
		StringBuilder builder = new StringBuilder("abc def abc def abc");
		String from = "abc";
		String to = "xyz";

		Util.replaceAll(builder, from, to);

		assertEquals("xyz def xyz def xyz", builder.toString(), "The method should replace all occurrences of 'abc' with 'xyz'.");
	}

}