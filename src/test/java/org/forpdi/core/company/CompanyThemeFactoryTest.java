package org.forpdi.core.company;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyThemeFactoryTest {

	@Test
	@DisplayName("Testa obter a instância singleton do CompanyThemeFactory")
	void test_getInstance_returnsSingletonInstance() {
		CompanyThemeFactory instance1 = CompanyThemeFactory.getInstance();
		CompanyThemeFactory instance2 = CompanyThemeFactory.getInstance();

		assertNotNull(instance1, "A instância do CompanyThemeFactory não deve ser nula");
		assertSame(instance1, instance2, "As instâncias retornadas devem ser as mesmas");
	}

	@Test
	@DisplayName("Testa obter o tema padrão do CompanyThemeFactory")
	void test_getDefaultTheme_returnsDefaultTheme() {
		CompanyTheme defaultTheme = CompanyThemeFactory.getDefaultTheme();

		assertNotNull(defaultTheme, "O tema padrão não deve ser nulo");
		assertEquals("Tema Default", defaultTheme.getDisplayName(), "O tema padrão deve ser 'Tema Default'");
		assertEquals("theme-default.css", defaultTheme.getCSSFile(), "O arquivo CSS do tema padrão deve ser 'theme-default.css'");
	}

	@Test
	@DisplayName("Testa obter a lista de temas no formato JSON")
	void test_toJSON_returnsValidJSON() {
		CompanyThemeFactory factory = CompanyThemeFactory.getInstance();
		String json = factory.toJSON();

		assertNotNull(json, "O JSON gerado não deve ser nulo");
		assertTrue(json.startsWith("[") && json.endsWith("]"), "O JSON deve ser um array válido");
		assertTrue(json.contains("\"css\":\"theme-default.css\""), "O JSON deve conter o arquivo CSS do tema padrão");
		assertTrue(json.contains("\"label\":\"Tema Default\""), "O JSON deve conter o label do tema padrão");
		assertTrue(json.contains("\"css\":\"theme-red.css\""), "O JSON deve conter o arquivo CSS do tema vermelho");
		assertTrue(json.contains("\"label\":\"Tema Vermelho\""), "O JSON deve conter o label do tema vermelho");
	}

	@Test
	@DisplayName("Testa o registro de temas adicionais")
	void test_register_addsAdditionalThemes() {
		CompanyThemeFactory factory = CompanyThemeFactory.getInstance();

		class CustomTheme extends CompanyTheme {
			@Override
			public String getDisplayName() {
				return "Tema Personalizado";
			}

			@Override
			public String getCSSFile() {
				return "theme-custom.css";
			}
		}

		CustomTheme customTheme = new CustomTheme();
		factory.register(customTheme);

		CompanyTheme retrievedTheme = factory.get(factory.size() - 1);

		assertNotNull(retrievedTheme, "O tema registrado não deve ser nulo");
		assertEquals("Tema Personalizado", retrievedTheme.getDisplayName(), "O tema registrado deve ser 'Tema Personalizado'");
		assertEquals("theme-custom.css", retrievedTheme.getCSSFile(), "O arquivo CSS do tema registrado deve ser 'theme-custom.css'");
	}
}
