package org.forpdi.planning.structure.goals.history;

import java.util.List;

import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.system.ErrorMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GoalJustificationHistoryController extends AbstractController {

	private static final String PATH = BASEPATH + "/goals-justification";
	
	@Autowired
	private GoalJustificationHistoryBS bs;
	
	@GetMapping(PATH + "/list/{levelInstanceId}")
	public ResponseEntity<?> listByLevelInstance(@PathVariable long levelInstanceId) {
		try {
			List<GoalJustificationHistory> list = bs.listByLevelInstance(levelInstanceId);
			return this.success(new ListWrapper<GoalJustificationHistory>(list));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
}
