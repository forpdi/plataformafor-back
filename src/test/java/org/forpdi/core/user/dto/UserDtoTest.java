package org.forpdi.core.user.dto;

import java.util.Date;

import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyUser;
import org.forpdi.core.user.User;
import org.forpdi.core.utils.Util;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserDtoTest {

    @Test
    @DisplayName("Converte usuário em DTO sem censurar CPF")
    public void testFromUserWithoutCensorCpf() {

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setCpf("12345678901");
        user.setName("John Doe");
        user.setCellphone("123456789");
        user.setPhone("987654321");
        user.setDepartment("IT");
        user.setCreation(new Date());
        user.setTermsAcceptance(new Date());
        user.setActive(true);
        user.setAccessLevel(2);
        user.setDeleted(false);
        
        UserDto dto = UserDto.from(user, false);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getCpf(), dto.getCpf());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getCellphone(), dto.getCellphone());
        assertEquals(user.getPhone(), dto.getPhone());
        assertEquals(user.getDepartment(), dto.getDepartment());
        assertEquals(user.getCreation(), dto.getCreation());
        assertEquals(user.getTermsAcceptance(), dto.getTermsAcceptance());
        assertEquals(user.isActive(), dto.isActive());
        assertEquals(user.getAccessLevel(), dto.getAccessLevel());
        assertEquals(user.isDeleted(), dto.isDeleted());
    }

    @Test
    @DisplayName("Converte usuário em DTO com CPF censurado")
    public void testFromUserWithCensorCpf() {

        User user = new User();
        user.setId(1L);
        user.setCpf("12345678901");

        UserDto dto = UserDto.from(user, true);

        assertEquals(Util.censorCPF(user.getCpf()), dto.getCpf());
    }

    @Test
    @DisplayName("Converte usuário de empresa em DTO")
    public void testFromCompanyUser() {

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setCpf("12345678901");

        Company company = new Company();
        CompanyUser companyUser = new CompanyUser();
        companyUser.setUser(user);
        companyUser.setBlocked(true);
        companyUser.setNotificationSetting(1);
        companyUser.setCompany(company);

        UserDto dto = UserDto.from(companyUser, false);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getCpf(), dto.getCpf());
        assertEquals(companyUser.isBlocked(), dto.isBlocked());
        assertEquals(companyUser.getNotificationSetting(), dto.getNotificationSettings());
        assertEquals(companyUser.getCompany(), dto.getCompany());
    }
}

