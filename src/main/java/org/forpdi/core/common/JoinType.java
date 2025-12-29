package org.forpdi.core.common;

public enum JoinType {
	INNER_JOIN(0),
	LEFT_OUTER_JOIN(1),
	RIGHT_OUTER_JOIN(2),
	FULL_JOIN(4);

	private final int joinTypeValue;

	JoinType(int joinTypeValue) {
		this.joinTypeValue = joinTypeValue;
	}

	public int getJoinTypeValue() {
		return joinTypeValue;
	}
}
