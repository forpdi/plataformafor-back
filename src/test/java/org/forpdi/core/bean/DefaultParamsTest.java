package org.forpdi.core.bean;

import org.hibernate.criterion.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultParamsTest {

	private DefaultParams defaultParams;

	@BeforeEach
	void setUp() {
		defaultParams = new DefaultParams();
	}

	@Test
	@DisplayName("Verificar se os parâmetros de página e tamanho de página são corretamente definidos")
	void testPageAndPageSize() {
		int page = 2;
		int pageSize = 10;

		defaultParams.setPage(page);
		defaultParams.setPageSize(pageSize);

		assertEquals(page, defaultParams.getPage(), "O valor da página deve ser o valor definido");
		assertEquals(pageSize, defaultParams.getPageSize(), "O valor do tamanho da página deve ser o valor definido");
	}

	@Test
	@DisplayName("Verificar criação com página e tamanho de página personalizados")
	void testCreateWithCustomValues() {
		int page = 3;
		int pageSize = 20;

		DefaultParams params = DefaultParams.create(page, pageSize);

		assertEquals(page, params.getPage(), "A página criada deve ser igual ao valor definido");
		assertEquals(pageSize, params.getPageSize(), "O tamanho da página criado deve ser igual ao valor definido");
	}

	@Test
	@DisplayName("Verificar criação com tamanho de página máximo")
	void testCreateWithMaxPageSize() {
		DefaultParams params = DefaultParams.createWithMaxPageSize();

		assertEquals(Integer.MAX_VALUE, params.getPageSize(), "O tamanho da página deve ser o valor máximo");
		assertEquals(1, params.getPage(), "A página deve ser 1");
	}

	@Test
	@DisplayName("Verificar comportamento de ordenação quando não há parâmetros de ordenação")
	void testIsSortingWithoutSorting() {
		assertFalse(defaultParams.isSorting(), "Deve retornar false quando não houver parâmetros de ordenação");
	}

	@Test
	@DisplayName("Verificar comportamento de ordenação com parâmetros de ordenação")
	void testIsSortingWithSorting() {
		defaultParams.setSortedBy(new String[] { "name", "asc" });

		assertTrue(defaultParams.isSorting(), "Deve retornar true quando houver parâmetros de ordenação");
	}

	@Test
	@DisplayName("Verificar comportamento de obtenção de ordenação sem definir parâmetros de ordenação")
	void testGetSortOrderWithoutSorting() {
		assertThrows(IllegalStateException.class, () -> defaultParams.getSortOrder(),
			"Deve lançar IllegalStateException quando tentar obter ordenação sem definir parâmetros de ordenação");
	}

	@Test
	@DisplayName("Verificar obtenção de ordenação com parâmetros de ordenação válidos")
	void testGetSortOrderWithValidSorting() {
		defaultParams.setSortedBy(new String[] { "name", "asc" });

		Order order = defaultParams.getSortOrder();

		assertNotNull(order, "A ordenação não pode ser nula");
		assertTrue(order.isAscending(), "A ordenação deve ser ascendente");
	}

	@Test
	@DisplayName("Verificar exceção com ordem de ordenação inválida")
	void testGetSortOrderWithInvalidOrder() {
		defaultParams.setSortedBy(new String[] { "name", "invalid" });

		assertThrows(IllegalArgumentException.class, () -> defaultParams.getSortOrder(),
			"Deve lançar IllegalArgumentException para ordem de ordenação inválida");
	}

	@Test
	@DisplayName("Verificar o comportamento de hasTerm com valor nulo")
	void testHasTermWithNullValue() {
		defaultParams.setTerm(null);

		assertFalse(defaultParams.hasTerm(), "O método deve retornar falso quando o termo é nulo");
	}

	@Test
	@DisplayName("Verificar o comportamento de hasTerm com valor em branco")
	void testHasTermWithBlankValue() {
		defaultParams.setTerm("   ");

		assertFalse(defaultParams.hasTerm(), "O método deve retornar falso quando o termo está em branco");
	}

	@Test
	@DisplayName("Verificar o comportamento de hasTerm com valor não nulo")
	void testHasTermWithNonNullValue() {
		defaultParams.setTerm("search term");

		assertTrue(defaultParams.hasTerm(), "O método deve retornar verdadeiro quando o termo não for nulo nem em branco");
	}
}
