package org.forpdi.core.company;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyThemeTest {

    @Test
    @DisplayName("Verifica a implementação do método abstrato getCSSFile")
    void testGetCSSFile_AbstractMethod() {
        class TestCompanyTheme extends CompanyTheme {
            @Override
            public String getCSSFile() {
                return "test-theme.css";
            }

			@Override
			public String getDisplayName() {
				return "";
			}
		}

        CompanyTheme theme = new TestCompanyTheme();

        String cssFile = theme.getCSSFile();

        assertEquals("test-theme.css", cssFile, "O método getCSSFile deve retornar o valor esperado.");
    }
}

