package org.forpdi.planning.plan;

import org.forpdi.core.company.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanMacroTest {

	@DisplayName("PlanMacro Validação do objeto por meio de Setters e Getters.")
	@Test
	void testPlanMacroObjectValidation() {
		PlanMacro planMacro = new PlanMacro();

		String name = "Plano Estratégico";
		String description = "Descrição detalhada do plano";
		Calendar date = Calendar.getInstance();
		date.set(2024, Calendar.JANUARY, 1);
		Date begin = date.getTime();
		date.set(2024, Calendar.DECEMBER, 31);
		Date end = date.getTime();
		Company company = new Company();
		boolean archived = true;
		boolean documented = false;
		boolean haveSons = true;
		List<PlanMacroField> fields = List.of(new PlanMacroField(), new PlanMacroField());

		planMacro.setName(name);
		planMacro.setDescription(description);
		planMacro.setBegin(begin);
		planMacro.setEnd(end);
		planMacro.setCompany(company);
		planMacro.setArchived(archived);
		planMacro.setDocumented(documented);
		planMacro.setHaveSons(haveSons);
		planMacro.setFields(fields);

		assertEquals(name, planMacro.getName(), "O nome do plano não foi definido corretamente.");
		assertEquals(description, planMacro.getDescription(), "A descrição do plano não foi definida corretamente.");
		assertEquals(begin, planMacro.getBegin(), "A data de início do plano não foi definida corretamente.");
		assertEquals(end, planMacro.getEnd(), "A data de término do plano não foi definida corretamente.");
		assertEquals(company, planMacro.getCompany(), "A empresa do plano não foi definida corretamente.");
		assertEquals(archived, planMacro.isArchived(), "O estado de arquivamento do plano não foi definido corretamente.");
		assertEquals(documented, planMacro.isDocumented(), "O estado de documentação do plano não foi definido corretamente.");
		assertEquals(haveSons, planMacro.isHaveSons(), "O estado 'haveSons' do plano não foi definido corretamente.");
		assertEquals(fields, planMacro.getFields(), "Os campos do plano não foram definidos corretamente.");
	}
}