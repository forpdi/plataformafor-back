package org.forpdi.core.company.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.forpdi.core.company.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyDtoTest {

    @Test
    @DisplayName("Deve instanciar CompanyDto corretamente")
    void testCompanyDtoCreation() {
        Company company = new Company();

        CompanyDto dto = new CompanyDto(company);

        assertNotNull(dto, "O objeto CompanyDto não deve ser nulo.");
        assertEquals(company, dto.company(), "A empresa no DTO deve ser igual à fornecida.");
    }
}

