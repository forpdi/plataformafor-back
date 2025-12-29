package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CriteriaPropertiesContextTest {

    @Test
    public void test_constructor_initializes_with_valid_input() {
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);

        // Aqui usamos o seu enum interno via nome completo (sem import):
        // org.forpdi.core.common.JoinType.INNER_JOIN
        LinkedHashMap<String, CriteriaPropertiesContext.PropertyAlias> aliasesMap = new LinkedHashMap<>();
        aliasesMap.put(
                "user",
                new CriteriaPropertiesContext.PropertyAlias(
                        "user",
                        org.forpdi.core.common.JoinType.INNER_JOIN));

        CriteriaPropertiesContext context = new CriteriaPropertiesContext(aliasesMap, root);

        assertNotNull(context);
        // Verifica que chame root.join("user", JoinType.INNER) exatamente uma vez
        verify(root, times(1)).join("user", JoinType.INNER);
    }

    @Test
    public void test_property_alias_parse_inner_join() {
        String associationPath = "department";

        // Construímos o PropertyAlias usando o enum interno
        // (org.forpdi.core.common.JoinType)
        CriteriaPropertiesContext.PropertyAlias alias = new CriteriaPropertiesContext.PropertyAlias(
                associationPath,
                org.forpdi.core.common.JoinType.INNER_JOIN);

        // Agora conferimos que getJoinType() devolve o JoinType do JPA
        assertEquals(JoinType.INNER, alias.getJoinType());
        assertEquals(associationPath, alias.getAssociationPath());
    }

    @Test
    public void test_constructor_with_empty_aliases_map() {
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        LinkedHashMap<String, CriteriaPropertiesContext.PropertyAlias> emptyMap = new LinkedHashMap<>();

        CriteriaPropertiesContext context = new CriteriaPropertiesContext(emptyMap, root);

        assertNotNull(context);
        // Como não há aliases, root.join nunca deve ser invocado
        verifyNoInteractions(root);
    }

    @Test
    public void test_generate_alias_paths_invalid_format() {
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        LinkedHashMap<String, CriteriaPropertiesContext.PropertyAlias> aliasesMap = new LinkedHashMap<>();
        // associationPath = "." gera split[0] = "" → getPathByAlias("") lança
        // HibernateException
        aliasesMap.put(
                "invalid",
                new CriteriaPropertiesContext.PropertyAlias(
                        ".",
                        org.forpdi.core.common.JoinType.INNER_JOIN));

        assertThrows(HibernateException.class, () -> {
            new CriteriaPropertiesContext(aliasesMap, root);
        });
    }

    @Test
    public void test_get_path_nonexistent_alias() {
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        CriteriaPropertiesContext context = new CriteriaPropertiesContext(new LinkedHashMap<>(), root);

        // Como não existe “nonexistent”, root.get("nonexistent") retorna null.
        // Depois tentamos fazer .get("property") sobre null → NullPointerException
        assertThrows(NullPointerException.class, () -> {
            context.getPath("nonexistent.property");
        });
    }

    @Test
    public void test_get_path_null_property() {
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        CriteriaPropertiesContext context = new CriteriaPropertiesContext(new LinkedHashMap<>(), root);

        // propertyName == null → propertyName.split(...) lança NullPointerException
        assertThrows(NullPointerException.class, () -> {
            context.getPath(null);
        });
    }

    @Test
    public void test_property_alias_unsupported_join_type() {
        String associationPath = "department";

        // FULL_JOIN não é tratado em parseJoinType() → lança IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new CriteriaPropertiesContext.PropertyAlias(
                    associationPath,
                    org.forpdi.core.common.JoinType.FULL_JOIN);
        });
    }

    @Test
    @DisplayName("Lançar NullPointerException ao acessar alias inexistente via getPathByAlias")
    void testGetPathByAliasThrowsExceptionForUnknownAlias() {
        @SuppressWarnings("unchecked")
        Root<Object> rootMock = mock(Root.class);
        CriteriaPropertiesContext context = new CriteriaPropertiesContext(new LinkedHashMap<>(), rootMock);

        // root.get("unknownAlias") retorna null → null.get("someProperty") lança
        // NullPointerException
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            context.getPath("unknownAlias.someProperty");
        });

        // Verificamos ao menos que a mensagem mencione “Cannot invoke”
        assertTrue(exception.getMessage().contains("Cannot invoke"));
    }
}
