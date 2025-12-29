package org.forpdi.core.company;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompanyTypeTest {


	@Test
	void test_university_id_value() {
		CompanyType university = CompanyType.UNIVERSITY;

		assertEquals(5, university.getId());
	}

	@Test
	void test_enum_values_immutable() {
		CompanyType university = CompanyType.UNIVERSITY;
		CompanyType institute = CompanyType.INSTITUTE;

		assertEquals(5, university.getId());
		assertEquals("Universidade", university.getLabel());
		assertEquals(10, institute.getId());
		assertEquals("Instituto Federal", institute.getLabel());

		assertEquals(5, university.getId());
		assertEquals("Universidade", university.getLabel());
	}
}