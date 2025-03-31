package org.forpdi.core.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;

public final class CriteriaPropertiesContext {
	private Root<?> root;
	
	private final Map<String, Join<?, ?>> aliasPathsMap;

	public CriteriaPropertiesContext(LinkedHashMap<String, PropertyAlias> aliasesMap, Root<?> root) {
		this.root = root;
		this.aliasPathsMap = new HashMap<>();
		generateAliasPaths(aliasesMap);
	}

	private void generateAliasPaths(LinkedHashMap<String, PropertyAlias> aliasesMap) {
		for (var entry : aliasesMap.entrySet()) {
			PropertyAlias alias = entry.getValue();
			String associationPath = alias.getAssociationPath();
			JoinType joinType = alias.getJoinType();
			String[] split = associationPath.split("\\.", 2);
			Join<?, ?> join;
			if (split.length > 1) {
				join = getPathByAlias(split[0]).join(split[1], joinType);
			} else {
				join = root.join(split[0], joinType);
			}
			
			aliasPathsMap.put(entry.getKey(), join);
		}
	}
	
	public Path<?> getPath(String propertyName) {
		String[] split = propertyName.split("\\.", 2);
		if (split.length > 1) {
			if (aliasPathsMap.containsKey(split[0])) {
				return aliasPathsMap.get(split[0]).get(split[1]);
			}
			return root.get(split[0]).get(split[1]);
		} else {
			if (aliasPathsMap.containsKey(split[0])) {
				return getPathByAlias(split[0]);
			}
			return root.get(split[0]);
		}
	}
	
	private Join<?, ?> getPathByAlias(String alias) {
		if (aliasPathsMap.containsKey(alias)) {
			return aliasPathsMap.get(alias);
		}
		
		throw new HibernateException("Alias not found: " + alias);
	}
	
	public static final class PropertyAlias {
		private final String associationPath;
		private final JoinType joinType;

		public PropertyAlias(String associationPath, org.hibernate.sql.JoinType hibernateJoinType) {
			this.associationPath = associationPath;
			this.joinType = parseJoinType(hibernateJoinType);
		}

		private JoinType parseJoinType(org.hibernate.sql.JoinType hibernateJoinType) {
			switch (hibernateJoinType) {
			case INNER_JOIN: {
				return JoinType.INNER;
			}
			case LEFT_OUTER_JOIN: {
				return JoinType.LEFT;
			}
			case RIGHT_OUTER_JOIN: {
				return JoinType.RIGHT;
			}
			default:
				throw new IllegalArgumentException("Unsupperted join type: " + hibernateJoinType);
			}
		}

		public String getAssociationPath() {
			return associationPath;
		}

		public JoinType getJoinType() {
			return joinType;
		}
	}
}
