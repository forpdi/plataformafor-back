package org.forpdi.planning.structure.goals.history;

import java.util.List;

import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
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
	@Autowired
    private StructureBS structureBS;
    @Autowired
    private CompanyDomainContext domain;
	@Autowired
	private CompanyBS companyBS;

	@GetMapping(PATH + "/list/{levelInstanceId}")
	public ResponseEntity<?> listByLevelInstance(@PathVariable long levelInstanceId, UserSession userSession) {
		try {
			StructureLevelInstance levelInstance = this.structureBS.retrieveLevelInstance(levelInstanceId);
			if (levelInstance == null) {
				return this.fail("O nível solicitado não foi encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			List<GoalJustificationHistory> list = bs.listByLevelInstance(levelInstanceId);
			return this.success(new ListWrapper<GoalJustificationHistory>(list));

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}
}
