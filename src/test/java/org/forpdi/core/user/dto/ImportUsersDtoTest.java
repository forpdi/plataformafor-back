package org.forpdi.core.user.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ImportUsersDtoTest {

    @Test
    void test_create_dto_with_valid_arrays() {
        String[] names = {"John Doe", "Jane Doe"};
        String[] emails = {"john@email.com", "jane@email.com"};
        Integer[] access = {1, 2};

        ImportUsersDto dto = new ImportUsersDto(names, emails, access);

        assertArrayEquals(names, dto.nameList(), "A lista de nomes não corresponde ao esperado.");
        assertArrayEquals(emails, dto.emailList(), "A lista de emails não corresponde ao esperado.");
        assertArrayEquals(access, dto.accessList(), "A lista de níveis de acesso não corresponde ao esperado.");
    }

    @Test
    void test_create_dto_with_empty_arrays() {
        String[] names = new String[0];
        String[] emails = new String[0];
        Integer[] access = new Integer[0];

        ImportUsersDto dto = new ImportUsersDto(names, emails, access);

        assertEquals(0, dto.nameList().length, "A lista de nomes deve estar vazia.");
        assertEquals(0, dto.emailList().length, "A lista de emails deve estar vazia.");
        assertEquals(0, dto.accessList().length, "A lista de níveis de acesso deve estar vazia.");
    }
}