package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LikeCriterionTest {

	@Test
	@DisplayName("Deve criar o predicado LIKE sensível a maiúsculas/minúsculas corretamente")
	void testGetPredicateWithCaseSensitive() {
		var propertyName = "nome";
		var pattern = "%valor%";
		var criteriaBuilder = mock(CriteriaBuilder.class);
		var path = mock(Path.class);
		var propertiesContext = mock(CriteriaPropertiesContext.class);
		var predicate = mock(Predicate.class);
		var pathAsString = mock(Path.class);

		when(propertiesContext.getPath(propertyName)).thenReturn(path);
		when(path.as(String.class)).thenReturn(pathAsString);
		when(criteriaBuilder.like(pathAsString, pattern)).thenReturn(predicate);

		var likeCriterion = new LikeCriterion(propertyName, pattern);

		var result = likeCriterion.getPredicate(criteriaBuilder, propertiesContext);

		assertNotNull(result, "O predicado não deve ser nulo.");
		assertEquals(predicate, result, "O predicado retornado deve ser o esperado.");
		verify(criteriaBuilder).like(pathAsString, pattern);
	}

	@Test
	@DisplayName("Deve criar o predicado LIKE ignorando maiúsculas/minúsculas corretamente")
	void testGetPredicateWithIgnoreCase() {
		var propertyName = "descricao";
		var pattern = "%VALOR%";
		var criteriaBuilder = mock(CriteriaBuilder.class);
		var path = mock(Path.class);
		var propertiesContext = mock(CriteriaPropertiesContext.class);
		var predicate = mock(Predicate.class);
		var pathAsString = mock(Path.class);

		when(propertiesContext.getPath(propertyName)).thenReturn(path);
		when(path.as(String.class)).thenReturn(pathAsString);
		when(criteriaBuilder.like(any(), eq(pattern))).thenReturn(predicate);

		var likeCriterion = new LikeCriterion(propertyName, pattern).ignoreCase();

		var result = likeCriterion.getPredicate(criteriaBuilder, propertiesContext);

		assertNotNull(result, "O predicado não deve ser nulo.");
		assertEquals(predicate, result, "O predicado retornado deve ser o esperado.");
		verify(criteriaBuilder).like(any(), eq(pattern.toUpperCase()));
	}

	@Test
	@DisplayName("Deve retornar 'true' para isIgnoreCase após configurar ignoreCase")
	void testIsIgnoreCase() {
		var likeCriterion = new LikeCriterion("teste", "%padrao%");

		likeCriterion.ignoreCase();

		assertTrue(likeCriterion.isIgnoreCase(), "O atributo 'ignoreCase' deve ser verdadeiro após chamar ignoreCase().");
	}

	@Test
	@DisplayName("Deve retornar 'false' para isIgnoreCase por padrão")
	void testDefaultIgnoreCase() {
		var likeCriterion = new LikeCriterion("teste", "%padrao%");

		assertFalse(likeCriterion.isIgnoreCase(), "O atributo 'ignoreCase' deve ser falso por padrão.");
	}
}
