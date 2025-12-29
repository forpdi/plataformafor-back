package org.forpdi.system.reports.pdf.htmlparser;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TagStylesTest {

	@Test
	@DisplayName("Deve criar uma instância de TagStyles com a tag fornecida")
	void testConstructor() {
		String tag = "h1";

		TagStyles tagStyles = new TagStyles(tag);

		assertNotNull(tagStyles);
		assertEquals(tag, tagStyles.getTag());
	}

	@Test
	@DisplayName("Deve retornar as propriedades como um mapa vazio inicialmente")
	void testGetPropertiesInitiallyEmpty() {
		TagStyles tagStyles = new TagStyles("h1");

		Map<String, String> properties = tagStyles.getProperties();

		assertNotNull(properties);
		assertTrue(properties.isEmpty());
	}

	@Test
	@DisplayName("Deve adicionar uma propriedade e recuperá-la corretamente")
	void testAddAndRetrieveProperty() {
		TagStyles tagStyles = new TagStyles("h1");
		String propertyName = "font-size";
		String propertyValue = "12px";

		tagStyles.addProperty(propertyName, propertyValue);

		assertEquals(propertyValue, tagStyles.getPropertyValue(propertyName));
	}

	@Test
	@DisplayName("Deve sobrescrever o valor de uma propriedade existente")
	void testOverwriteProperty() {
		TagStyles tagStyles = new TagStyles("h1");
		String propertyName = "color";
		tagStyles.addProperty(propertyName, "red");

		tagStyles.addProperty(propertyName, "blue");

		assertEquals("blue", tagStyles.getPropertyValue(propertyName));
	}

	@Test
	@DisplayName("Deve retornar null ao tentar recuperar uma propriedade inexistente")
	void testGetNonExistentProperty() {
		TagStyles tagStyles = new TagStyles("h1");

		String value = tagStyles.getPropertyValue("background");

		assertNull(value);
	}

	@Test
	@DisplayName("Deve retornar um novo mapa de propriedades para evitar mutações externas")
	void testGetPropertiesReturnsCopy() {
		TagStyles tagStyles = new TagStyles("h1");
		tagStyles.addProperty("margin", "10px");

		Map<String, String> properties = tagStyles.getProperties();

		properties.put("padding", "5px");
		assertNull(tagStyles.getPropertyValue("padding"));
	}
}
