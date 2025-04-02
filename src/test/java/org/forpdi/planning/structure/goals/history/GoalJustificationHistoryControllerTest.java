package org.forpdi.planning.structure.goals.history;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.forpdi.core.common.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.forpdi.core.bean.ListWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class GoalJustificationHistoryControllerTest {


	@Test
	void test_list_by_level_instance_returns_history() {
		long levelInstanceId = 1L;
		GoalJustificationHistoryController controller = new GoalJustificationHistoryController();
		GoalJustificationHistoryBS bs = mock(GoalJustificationHistoryBS.class);
		ReflectionTestUtils.setField(controller, "bs", bs);

		List<GoalJustificationHistory> historyList = new ArrayList<>();
		historyList.add(new GoalJustificationHistory());
		when(bs.listByLevelInstance(levelInstanceId)).thenReturn(historyList);

		ResponseEntity<?> response = controller.listByLevelInstance(levelInstanceId);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Response<?> body = (Response<?>) response.getBody();
		assertTrue(body.isSuccess());
		ListWrapper<?> wrapper = (ListWrapper<?>) body.getData();
		assertEquals(1, wrapper.getList().size());
	}

	@Test
	void test_list_by_level_instance_with_negative_id() {
		long levelInstanceId = -1L;
		GoalJustificationHistoryController controller = new GoalJustificationHistoryController();
		GoalJustificationHistoryBS bs = mock(GoalJustificationHistoryBS.class);
		ReflectionTestUtils.setField(controller, "bs", bs);

		when(bs.listByLevelInstance(levelInstanceId)).thenReturn(new ArrayList<>());

		ResponseEntity<?> response = controller.listByLevelInstance(levelInstanceId);

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Response<?> body = (Response<?>) response.getBody();
		assertTrue(body.isSuccess());
		ListWrapper<?> wrapper = (ListWrapper<?>) body.getData();
		assertTrue(wrapper.getList().isEmpty());
	}
}