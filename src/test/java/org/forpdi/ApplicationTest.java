package org.forpdi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import static org.mockito.Mockito.*;

class ApplicationTest {

	@Test
	@DisplayName("Deve inicializar a aplicação sem erros")
	void testMainMethodInitialization() {
		String[] args = {};

		try (var mockStatic = mockStatic(SpringApplication.class)) {
			Application.main(args);

			mockStatic.verify(() -> SpringApplication.run(Application.class, args), times(1));
		}
	}
}
