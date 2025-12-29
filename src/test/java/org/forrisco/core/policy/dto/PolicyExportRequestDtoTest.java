package org.forrisco.core.policy.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para PolicyExportRequestDto")
class PolicyExportRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve criar o DTO com sucesso e verificar os valores")
    void shouldCreateDtoWithValidData() {
        Long policyId = 1L;
        String title = "Política de Segurança da Informação";
        String itens = "item1,item2";
        String subitens = "subitemA,subitemB";

        PolicyExportRequestDto dto = new PolicyExportRequestDto(policyId, title, itens, subitens);

        assertNotNull(dto);
        assertEquals(policyId, dto.policyId());
        assertEquals(title, dto.title());
        assertEquals(itens, dto.itens());
        assertEquals(subitens, dto.subitens());
    }

    @Test
    @DisplayName("Deve falhar a validação quando policyId for nulo")
    void shouldFailValidationWhenPolicyIdIsNull() {
        PolicyExportRequestDto dtoWithNullId = new PolicyExportRequestDto(null, "Título Válido", "itens", "subitens");

        Set<ConstraintViolation<PolicyExportRequestDto>> violations = validator.validate(dtoWithNullId);

        assertFalse(violations.isEmpty(), "Deveria haver violações de validação");
        assertEquals(1, violations.size(), "Deveria haver exatamente uma violação");

        ConstraintViolation<PolicyExportRequestDto> violation = violations.iterator().next();
        assertEquals("policyId", violation.getPropertyPath().toString());
        assertEquals("must not be null", violation.getMessage());
    }

    @Test
    @DisplayName("Deve falhar a validação quando title for nulo")
    void shouldFailValidationWhenTitleIsNull() {
        PolicyExportRequestDto dtoWithNullTitle = new PolicyExportRequestDto(1L, null, "itens", "subitens");

        Set<ConstraintViolation<PolicyExportRequestDto>> violations = validator.validate(dtoWithNullTitle);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        ConstraintViolation<PolicyExportRequestDto> violation = violations.iterator().next();
        assertEquals("title", violation.getPropertyPath().toString());
        assertEquals("must not be null", violation.getMessage());
    }

    @Test
    @DisplayName("Não deve falhar a validação quando itens e subitens forem nulos")
    void shouldNotFailValidationWhenOptionalFieldsAreNull() {
        PolicyExportRequestDto dtoWithNullOptionals = new PolicyExportRequestDto(1L, "Título Válido", null, null);

        Set<ConstraintViolation<PolicyExportRequestDto>> violations = validator.validate(dtoWithNullOptionals);

        assertTrue(violations.isEmpty(), "Não deveria haver violações para campos opcionais nulos");
    }
}