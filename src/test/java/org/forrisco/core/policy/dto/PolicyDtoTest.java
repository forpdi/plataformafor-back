package org.forrisco.core.policy.dto;

import org.forrisco.core.policy.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PolicyDtoTest {

	@DisplayName("PolicyDto Criação do DTO a partir de uma Policy.")
	@Test
	void testPolicyDTOCreation() {
		Policy policy = new Policy();
		policy.setName("Test Policy");
		policy.setDescription("Test Description");
		policy.setNline(3);
		policy.setNcolumn(3);
		policy.setProbability("High");
		policy.setImpact("Medium");
		policy.setMatrix("test matrix");

		PolicyDto policyDto = new PolicyDto(policy);

		assertNotNull(policyDto);
		assertEquals(policy, policyDto.policy());
	}
}