package org.forpdi.core.bean;

import org.forpdi.core.utils.Consts;
import org.hibernate.criterion.Order;

public class DefaultParams {
	private int page = 1;
	private int pageSize = Consts.MIN_PAGE_SIZE;
	private String[] sortedBy = null;
	private String term = null;
	
	public static DefaultParams create(int page, int pageSize) {
		DefaultParams params = new DefaultParams();
		params.page = page;
		params.pageSize = pageSize;
		return params;
	}

	public static DefaultParams createWithMaxPageSize() {
		DefaultParams params = new DefaultParams();
		params.page = 1;
		params.pageSize = Integer.MAX_VALUE;
		return params;
	}

	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public void setSortedBy(String[]sortedBy) {
		this.sortedBy = sortedBy;
	}
	public boolean isSorting() {
		return sortedBy != null && sortedBy.length > 0;
	}
	public String[] getSortedBy() {
		return sortedBy;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public boolean hasTerm() {
		return term != null && !term.isBlank();
	}
	public Order getSortOrder() {
		if (!isSorting()) {
			throw new IllegalStateException("Trying to get sort order without sorting");
		}
		String field = sortedBy[0];
		String order = sortedBy[1];
		if (order.equals("asc")) {
			return Order.asc(field);
		}
		if (order.equals("desc")) {
			return Order.desc(field);
		}
		throw new IllegalArgumentException("Invalid sort order: " + order);
	}
}
