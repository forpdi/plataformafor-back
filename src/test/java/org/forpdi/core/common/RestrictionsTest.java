package org.forpdi.core.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

class RestrictionsTest {

    @Test
    @DisplayName("Testa a restrição de igualdade (eq)")
    void testEq() {
        var propertyName = "field";
        var value = "value";
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.equal(path, value)).thenReturn(predicate);

        var criterion = Restrictions.eq(propertyName, value);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de desigualdade (ne)")
    void testNe() {
        var propertyName = "field";
        var value = "value";
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.notEqual(path, value)).thenReturn(predicate);

        var criterion = Restrictions.ne(propertyName, value);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de maior que (gt)")
    void testGt() {
        var propertyName = "field";
        var value = 10;
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.greaterThan(path, value)).thenReturn(predicate);

        var criterion = Restrictions.gt(propertyName, value);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de maior ou igual (ge)")
    void testGe() {
        var propertyName = "field";
        var value = 10;
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.greaterThanOrEqualTo(path, value)).thenReturn(predicate);

        var criterion = Restrictions.ge(propertyName, value);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de menor que (lt)")
    void testLt() {
        var propertyName = "field";
        var value = 10;
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.lessThan(path, value)).thenReturn(predicate);

        var criterion = Restrictions.lt(propertyName, value);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de menor ou igual (le)")
    void testLe() {
        var propertyName = "field";
        var value = 10;
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.lessThanOrEqualTo(path, value)).thenReturn(predicate);

        var criterion = Restrictions.le(propertyName, value);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de coleção (in)")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void testIn() {
        String propertyName = "field";
        List<String> values = Arrays.asList("value1", "value2");

        CriteriaPropertiesContext context = mock(CriteriaPropertiesContext.class);
        Path path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);

        Predicate expected = mock(Predicate.class);

        when(path.in(any(Collection.class))).thenReturn(expected);
        when(path.in(any(Object[].class))).thenReturn(expected);

        var criterion = Restrictions.in(propertyName, values);
        Predicate result = criterion.getPredicate(mock(CriteriaBuilder.class), context);

        assertSame(expected, result);
    }

    @Test
    @DisplayName("Testa a restrição de valor nulo (isNull)")
    void testIsNull() {
        var propertyName = "field";
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.isNull(path)).thenReturn(predicate);

        var criterion = Restrictions.isNull(propertyName);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de valor não nulo (isNotNull)")
    void testIsNotNull() {
        var propertyName = "field";
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.isNotNull(path)).thenReturn(predicate);

        var criterion = Restrictions.isNotNull(propertyName);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a restrição de intervalo (between)")
    void testBetween() {
        var propertyName = "field";
        var low = 1;
        var high = 10;
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var path = mock(Path.class);
        when(context.getPath(propertyName)).thenReturn(path);
        var predicate = mock(Predicate.class);
        when(builder.between(path, low, high)).thenReturn(predicate);

        var criterion = Restrictions.between(propertyName, low, high);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa a negação de uma restrição (not)")
    void testNot() {
        var innerCriterion = mock(Criterion.class);
        var builder = mock(CriteriaBuilder.class);
        var context = mock(CriteriaPropertiesContext.class);
        var innerPredicate = mock(Predicate.class);
        when(innerCriterion.getPredicate(builder, context)).thenReturn(innerPredicate);
        var predicate = mock(Predicate.class);
        when(builder.not(innerPredicate)).thenReturn(predicate);

        var criterion = Restrictions.not(innerCriterion);
        assertEquals(predicate, criterion.getPredicate(builder, context));
    }

    @Test
    @DisplayName("Testa o método or para criar uma disjunção com critérios")
    void testOr() {
        var criterion1 = Restrictions.eq("field1", "value1");
        var criterion2 = Restrictions.eq("field2", "value2");

        var disjunction = Restrictions.or(criterion1, criterion2);

        assertNotNull(disjunction);
        assertEquals(2, disjunction.getCriterions().size());
        assertTrue(disjunction.getCriterions().contains(criterion1));
        assertTrue(disjunction.getCriterions().contains(criterion2));
    }

    @Test
    @DisplayName("Testa o método disjunction para criar uma disjunção vazia")
    void testDisjunction() {
        var disjunction = Restrictions.disjunction();

        assertNotNull(disjunction);
        assertEquals(0, disjunction.getCriterions().size());
    }

    @Test
    @DisplayName("Testa o método and para criar uma conjunção com critérios")
    void testAnd() {
        var criterion1 = Restrictions.eq("field1", "value1");
        var criterion2 = Restrictions.eq("field2", "value2");

        var conjunction = Restrictions.and(criterion1, criterion2);

        assertNotNull(conjunction);
        assertEquals(2, conjunction.getCriterions().size());
        assertTrue(conjunction.getCriterions().contains(criterion1));
        assertTrue(conjunction.getCriterions().contains(criterion2));
    }

    @Test
    @DisplayName("Testa o método conjunction para criar uma conjunção vazia")
    void testConjunction() {
        var conjunction = Restrictions.conjunction();

        assertNotNull(conjunction);
        assertEquals(0, conjunction.getCriterions().size());
    }
}
