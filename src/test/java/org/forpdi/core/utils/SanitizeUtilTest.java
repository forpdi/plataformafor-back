package org.forpdi.core.utils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SanitizeUtilTest {

	@Test
	@DisplayName("Sanitiza HTML contendo apenas tags permitidas")
	void testSanitizeHtmlWithOnlyAllowedTags() {
		String html = "<div><p>paragraph</p><a href='https://www.google.com/' /></div>";
		assertEquals("<div><p>paragraph</p></div>", SanitizeUtil.sanitize(html, List.of("div", "p")));
	}

	@Test
	@DisplayName("Sanitiza HTML com atributo onerror")
	void TestsanitizeHtmlWithOnerrorAttribute() {
		String html = "<img src=\"x\" onerror=\"alert('XSS')\">";

		String sanitizedHtml = SanitizeUtil.sanitize(html, List.of("img"));

		assertEquals("<img src=\"x\">", sanitizedHtml);
	}

	@Test
	@DisplayName("Verifica o tratamento de string vazia")
	void testHandleEmptyString() {
		String input = "";
		List<String> allowedTags = Arrays.asList("b", "i");
		String expectedOutput = "";
		String result = SanitizeUtil.sanitize(input, allowedTags);
		assertEquals(expectedOutput, result);
	}

	@Test
	@DisplayName("Sanitiza texto rico corretamente")
	void testSanitizeRichText() {
		String richTextHtml = "<p><strong>Testing, </strong><span class=\"ql-font-monospace\">Testing, </span><em>Testing, </em><u>Testing.</u></p>"
												+ "<p><u style=\"background-color: rgb(255, 255, 0); color: rgb(0, 138, 0);\">Testing</u></p>"
												+ "<p class=\"ql-indent-1\"><u style=\"background-color: rgb(255, 255, 0); color: rgb(0, 138, 0);\">Testing</u></p>"
												+ "<p><a href=\"https://www.google.com/\" rel=\"noopener noreferrer\" target=\"_blank\">https://www.google.com/</a></p>"
												+ "<blockquote>Testing</blockquote>"
												+ "<p>x<sup>2</sup></p>"
												+ "<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAB0VBMVEVHcEwojS==\" alt=\"Test image\"></p>";

		String expectedSanitizedHtml = richTextHtml;

		String sanitizedHtml = SanitizeUtil.sanitize(richTextHtml);

		assertEquals(expectedSanitizedHtml, sanitizedHtml);
	}
	
	@Test
	@DisplayName("Verifica a sanitização de texto rico com script malicioso")
	void testSanitizeRichTextWithScript() {
		String richTextHtml = "<ol><li>texto numerado</li><li>texto numerado</li><li>texto numerado</li></ol><ul><li>texto em tópicos</li><script>alert('Hello!')</script><li>texto em tópicos</li><li>texto em tópicos</li><li><strong>Negrito<span class=\"ql-cursor\">﻿</span></strong></li></ul>";

		String expectedSanitizedHtml = "<ol><li>texto numerado</li><li>texto numerado</li><li>texto numerado</li></ol><ul><li>texto em tópicos</li><li>texto em tópicos</li><li>texto em tópicos</li><li><strong>Negrito<span class=\"ql-cursor\">﻿</span></strong></li></ul>";

		String sanitizedHtml = SanitizeUtil.sanitize(richTextHtml);

		assertEquals(expectedSanitizedHtml, sanitizedHtml);
	}
	
	@Test
	@DisplayName("Sanitiza texto rico com evento mouseover malicioso")
	void testSanitizeRichTextWithMouseOver() {
		String richTextHtml = "<div><p onmouseover=\"alert('test')\">paragraph</p><a href=\"https://www.google.com/\">link</a></div>";

		String expectedSanitizedHtml = "<div><p>paragraph</p><a href=\"https://www.google.com/\">link</a></div>";

		String sanitizedHtml = SanitizeUtil.sanitize(richTextHtml);

		assertEquals(expectedSanitizedHtml, sanitizedHtml);
	}

	@Test
	@DisplayName("Sanitiza HTML com lista personalizada de tags permitidas")
	void test_sanitize_with_custom_allowed_tags() {
		String html = "<div>Hello <script>alert('xss')</script><p style='color:red'>World</p></div>";
		List<String> allowedTags = Arrays.asList("div", "p");

		String sanitized = SanitizeUtil.sanitize(html, allowedTags);

		assertEquals("<div>Hello <p style=\"color:red\">World</p></div>", sanitized);
	}

	@Test
	void test_sanitize_with_null_input() {
		String nullString = null;
		List<String> allowedTags = Arrays.asList("div", "p");

		String sanitized = SanitizeUtil.sanitize(nullString, allowedTags);

		assertNull(sanitized);
	}

	@Test
	public void test_sanitize_with_allowed_tags_returns_cleaned_string() {
		String input = "<p>Hello</p><script>alert('xss')</script><div>World</div>";
		List<String> allowedTags = Arrays.asList("p", "div");

		String result = SanitizeUtil.sanitize(input, allowedTags);

		assertEquals("<p>Hello</p><div>World</div>", result);
	}
}
