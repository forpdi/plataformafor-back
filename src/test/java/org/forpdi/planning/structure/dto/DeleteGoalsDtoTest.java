package org.forpdi.planning.structure.dto;

import org.forpdi.core.common.PaginatedList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeleteGoalsDtoTest {

	@Test
	@DisplayName("Cria DeleteGoalsDto com PaginatedList válido contendo valores Double")
	void test_create_delete_goals_dto_with_valid_paginated_list() {
		List<Double> values = Arrays.asList(1.0, 2.0, 3.0);
		PaginatedList<Double> paginatedList = new PaginatedList<>(values, 3L);

		DeleteGoalsDto deleteGoalsDto = new DeleteGoalsDto(paginatedList);

		assertNotNull(deleteGoalsDto);
		assertEquals(paginatedList, deleteGoalsDto.list());
		assertEquals(3, deleteGoalsDto.list().getList().size());
		assertEquals(3L, deleteGoalsDto.list().getTotal());
	}
	@Test
	void test_create_delete_goals_dto_with_null_paginated_list() {
		DeleteGoalsDto deleteGoalsDto = new DeleteGoalsDto(null);

		assertNotNull(deleteGoalsDto);
		assertNull(deleteGoalsDto.list());
	}
}
