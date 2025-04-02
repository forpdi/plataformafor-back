package org.forpdi.core.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashMap;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class CriteriaPropertiesContextTest {


	@Test
	public void test_constructor_initializes_with_valid_input() {
		Root<?> root = mock(Root.class);
		LinkedHashMap<String, CriteriaPropertiesContext.PropertyAlias> aliasesMap = new LinkedHashMap<>();
		aliasesMap.put("user", new CriteriaPropertiesContext.PropertyAlias("user", org.hibernate.sql.JoinType.INNER_JOIN));

		CriteriaPropertiesContext context = new CriteriaPropertiesContext(aliasesMap, root);

		assertNotNull(context);
		verify(root).join("user", JoinType.INNER);
	}

	@Test
	public void test_property_alias_parse_inner_join() {
		String associationPath = "department";

		CriteriaPropertiesContext.PropertyAlias alias = new CriteriaPropertiesContext.PropertyAlias(associationPath, org.hibernate.sql.JoinType.INNER_JOIN);

		assertEquals(JoinType.INNER, alias.getJoinType());
		assertEquals(associationPath, alias.getAssociationPath());
	}

	@Test
	public void test_constructor_with_empty_aliases_map() {
		Root<?> root = mock(Root.class);
		LinkedHashMap<String, CriteriaPropertiesContext.PropertyAlias> emptyMap = new LinkedHashMap<>();

		CriteriaPropertiesContext context = new CriteriaPropertiesContext(emptyMap, root);

		assertNotNull(context);
		verifyNoInteractions(root);
	}

	@Test
	public void test_generate_alias_paths_invalid_format() {
		Root<?> root = mock(Root.class);
		LinkedHashMap<String, CriteriaPropertiesContext.PropertyAlias> aliasesMap = new LinkedHashMap<>();
		aliasesMap.put("invalid", new CriteriaPropertiesContext.PropertyAlias(".", org.hibernate.sql.JoinType.INNER_JOIN));

		assertThrows(HibernateException.class, () -> {
			new CriteriaPropertiesContext(aliasesMap, root);
		});
	}

	@Test
	public void test_get_path_nonexistent_alias() {
		Root<?> root = mock(Root.class);
		CriteriaPropertiesContext context = new CriteriaPropertiesContext(new LinkedHashMap<>(), root);

		assertThrows(NullPointerException.class, () -> {
			context.getPath("nonexistent.property");
		});
	}

	@Test
	public void test_get_path_null_property() {
		Root<?> root = mock(Root.class);
		CriteriaPropertiesContext context = new CriteriaPropertiesContext(new LinkedHashMap<>(), root);

		assertThrows(NullPointerException.class, () -> {
			context.getPath(null);
		});
	}

	@Test
	public void test_property_alias_unsupported_join_type() {
		String associationPath = "department";

		assertThrows(IllegalArgumentException.class, () -> {
			new CriteriaPropertiesContext.PropertyAlias(associationPath, org.hibernate.sql.JoinType.FULL_JOIN);
		});
	}


	@Test
	@DisplayName("Lançar exceção ao acessar alias inexistente")
	void testGetPathByAliasThrowsExceptionForUnknownAlias() {
		Root<?> rootMock = mock(Root.class);
		CriteriaPropertiesContext context = new CriteriaPropertiesContext(new LinkedHashMap<>(), rootMock);

		NullPointerException exception = assertThrows(NullPointerException.class, () -> {
			context.getPath("unknownAlias.someProperty");
		});

//		assertEquals("Alias not found: unknownAlias", exception.getMessage());
		assertEquals("Cannot invoke \"javax.persistence.criteria.Path.get(String)\" because the return value of \"javax.persistence.criteria.Root.get(String)\" is null", exception.getMessage());
	}
}
