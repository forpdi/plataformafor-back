package org.forpdi.core.company.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.forpdi.core.company.CompanyDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyDomainDtoTest {

    @Test
    @DisplayName("Deve instanciar CompanyDomainDto corretamente")
    void testCompanyDomainDtoCreation() {
        CompanyDomain domain = new CompanyDomain();
        
        CompanyDomainDto dto = new CompanyDomainDto(domain);

        assertNotNull(dto, "O objeto CompanyDomainDto não deve ser nulo.");
        assertEquals(domain, dto.domain(), "O domínio no DTO deve ser igual ao fornecido.");
    }
}

