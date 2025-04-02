package org.forpdi.planning.fields.budget;

import org.forpdi.core.common.SimpleLogicalDeletableEntity;

public class BudgetDTO extends SimpleLogicalDeletableEntity {
	private static final long serialVersionUID = 1L;
	
	private Budget budget;
	private Double budgetLoa;
	private Double balanceAvailable;
	
	public BudgetDTO(){
		// Construtor vazio necessário para frameworks como Hibernate
	}

	public Budget getBudget() {
		return budget;
	}

	public void setBudget(Budget budget) {
		this.budget = budget;
	}

	public Double getBudgetLoa() {
		return budgetLoa;
	}

	public void setBudgetLoa(Double budgetLoa) {
		this.budgetLoa = budgetLoa;
	}

	public Double getBalanceAvailable() {
		return balanceAvailable;
	}

	public void setBalanceAvailable(Double balanceAvailable) {
		this.balanceAvailable = balanceAvailable;
	}

	
	
}
