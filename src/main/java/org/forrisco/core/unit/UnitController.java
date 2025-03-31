package org.forrisco.core.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.dashboard.interceptor.CommunityDashboard;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.ErrorMessages;
import org.forpdi.system.reports.frisco.UnitReportGenerator;
import org.forpdi.system.reports.frisco.UnitReportGenerator.Params;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.process.Process;
import org.forrisco.core.process.ProcessBS;
import org.forrisco.core.unit.dto.DuplicateUnitDto;
import org.forrisco.core.unit.dto.UnitDto;
import org.forrisco.risk.RiskFilterParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.DocumentException;

/**
 * @author Matheus Nascimento
 */
@RestController
public class UnitController extends AbstractController {

	@Autowired
	private UnitBS unitBS;
	@Autowired
	private ProcessBS processBS;
	@Autowired
	private UnitReportGenerator unitReportGenerator;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private UnitReplicationBS unitReplicationBS;

	public static final String PATH = BASEPATH + "/unit";

	/**
	 * Salvar unidade
	 *
	 * @param Unit
	 *            unidade a ser salva
	 */
	@PostMapping(PATH + "/new")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> save(@RequestBody UnitDto dto) {
		try {
			Unit unit = dto.unit();

			unit.setDescription(SanitizeUtil.sanitize(unit.getDescription()));
			
			PlanRisk planRisk = this.unitBS.exists(unit.getPlanRisk().getId(), PlanRisk.class);
			if (planRisk == null) {
				return this.fail("Unidade não possui Plano de Risco");
			}
			unit.setId(null);
			unit.setPlanRisk(planRisk);
			this.unitBS.save(unit);
			return this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Busca todas as unidades não deletadas
	 *
	 * @param page
	 *            pagina atual
	 * @param pageSize
	 *            quantidade de resultados por pagina
	 * return ResponseEntity<?>
	 */
	@GetMapping( PATH + "/units")
	public ResponseEntity<?> listAllUnits(
			@RequestParam(required = false) Long planRiskId,
			@ModelAttribute DefaultParams params) {
		try {
			if (this.domain != null) {
			PaginatedList<Unit> units = this.unitBS.listUnits(this.domain.get().getCompany(), planRiskId, params);
				return this.success(units);
			} else {
				return this.fail("Não possui domínio!");
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Salvar subunidade
	 *
	 * @param Unit
	 *            subunidade a ser salva
	 */
	@PostMapping(PATH + "/subnew")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> saveSub(@RequestBody UnitDto dto) {
		try {
			Unit unit = dto.unit();

			if (unit.getPlanRisk() == null) {
				return this.fail("Unidade não possui Plano de Risco");
			}

			if (unit.getParent() == null) {
				return this.fail("Unidade não possui unidade pai");
			}

			unit.setId(null);
			this.unitBS.save(unit);
			return this.success(unit);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}


	/**
	 * Duplica unidades
	 *
	 *  @Param List<Unit> lista de unidade com o id da unidade original
	 *  				 e o id no plano a ser salvo a unidade duplicado
	 * @return ResponseEntity<?>
	 */
	@PostMapping(PATH + "/duplicate")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> duplicateUnit(@RequestBody DuplicateUnitDto dto) {
		try {
			List<Unit> units = dto.units();
			PlanRisk planRisk = dto.planRisk();

			PlanRisk plan = this.unitBS.exists(planRisk.getId(), PlanRisk.class);
			if (plan == null || plan.isDeleted()) {
				return this.fail("Plano de Risco não encontrado");
			}

			List<Unit> allunits = new ArrayList<>();
			Map<Long, Long> duplicatesUnitsId= new HashMap <Long, Long>();

			//duplicar unidades/subunidades
			for(Unit unit: units) {

				unit = this.unitBS.exists(unit.getId(), Unit.class);
				if (unit == null || unit.isDeleted()) {
					//return this.fail("Unidade não possui Plano de Risco");
					continue;
				}

				Unit newUnit= new Unit(unit);
				newUnit.setPlanRisk(plan);
				this.unitBS.save(newUnit);
				duplicatesUnitsId.put(unit.getId(), newUnit.getId());
				allunits.add(newUnit);

				PaginatedList<Unit> subunits = this.unitBS.listSubunitByUnit(unit);

				for(Unit subunit: subunits.getList()) {
					Unit newSubunit= new Unit(subunit);
					newSubunit.setParent(newUnit);
					newSubunit.setPlanRisk(plan);
					this.unitBS.save(newSubunit);
					duplicatesUnitsId.put(subunit.getId(), newSubunit.getId());
					allunits.add(newSubunit);
				}
			}

			for(Unit unit: allunits) {
				unitReplicationBS.replicateProcess(unit, duplicatesUnitsId);
				unitReplicationBS.replicateUnitRisk(duplicatesUnitsId, unit);
			}

			return this.success("successfully duplicated plan risk");
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna unidades.
	 *
	 * @param PLanRisk
	 *            Id do plano de risco.
	 * @return <PaginatedList> Unit
	 */
	@GetMapping(PATH + "")
	public ResponseEntity<?> listUnits(@RequestParam Long planId) {
		try {
			PlanRisk plan = this.unitBS.exists(planId, PlanRisk.class);

			if (plan== null) {
				return this.fail("O Plano de Risco não foi encontrado");
			}

			PaginatedList<Unit> units = this.unitBS.listOnlyUnitsbyPlanRisk(plan);

			//PaginatedList<Unit> units =this.unitBS.listUnitsbyPlanRisk(plan);
			return this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna unidade.
	 *
	 * @param id
	 *            Id da unidade a ser retornado.
	 * @return Unit Retorna a unidade de acordo com o id passado.
	 */
	@GetMapping(PATH + "/{id}")
	public ResponseEntity<?> getUnit(@PathVariable Long id) {
		try {
			Unit unit = this.unitBS.retrieveUnitById(id);
			if (unit == null) {
				return this.fail("A unidade solicitada não foi encontrado.");
			} else {
				return this.success(unit);
			}
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna subunidades.
	 *
	 * @param unitId
	 *            Id da unidade parent.
	 * @return <PaginatedList> Subunidades filhas da unidade passada
	 */
	@GetMapping(PATH + "/listsub/{unitId}")
	public ResponseEntity<?> listSubunits(@PathVariable Long unitId, @ModelAttribute DefaultParams params) {
		try {
			Unit unit = this.unitBS.exists(unitId, Unit.class);

			if (unit == null) {
				return this.fail("A unidade não foi encontrada");
			}

			PaginatedList<Unit> subunits = this.unitBS.listSubunitByUnit(unit, params);
			return this.success(subunits);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}


	/**
	 * Retorna todas subunidades do plano.
	 *
	 * @param planId
	 *            Id do plano de risco
	 * @return <PaginatedList> Subunidades
	 */
	@GetMapping(PATH + "/listsub")
	public ResponseEntity<?> listSubunitsByPlan(@RequestParam Long planId) {
		try {
			PlanRisk plan =this.unitBS.exists(planId, PlanRisk.class);

			if (plan == null || plan.isDeleted()) {
				return this.fail("O Plano de risco não foi encontrado");
			}

			PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan);
			List<Unit> list = new ArrayList<>();

			for(Unit unit : units.getList()) {
				PaginatedList<Unit> subunits = this.unitBS.listSubunitByUnit(unit);
				list.addAll(subunits.getList());
			}

			PaginatedList<Unit> result = new PaginatedList<Unit>();

			result.setList(list);
			result.setTotal((long) list.size());
			return this.success(result);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna todas unidades e subunidades do plano.
	 *
	 * @param planId
	 *            Id do plano de risco
	 * @return <PaginatedList>Unit
	 */
	@GetMapping(PATH + "/allByPlan")
	@CommunityDashboard
	public ResponseEntity<?> listAllUnitsByPlan(@RequestParam Long planId) {
		try {
			PlanRisk plan =this.unitBS.exists(planId, PlanRisk.class);

			if (plan == null || plan.isDeleted()) {
				return this.fail("O Plano de risco não foi encontrado");
			}

			PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan);

			return this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Retorna todas unidades e subunidades do plano.
	 *
	 * @param planId
	 *            Id do plano de risco
	 * @return <PaginatedList>Unit
	 */
	@GetMapping(PATH + "/allByPlanPaginated")
	public ResponseEntity<?> listAllUnitsByPlanPaginated(
			@RequestParam Long planRiskId,
			@ModelAttribute DefaultParams params) {
		try {
			PlanRisk plan =this.unitBS.exists(planRiskId, PlanRisk.class);

			if (plan == null || plan.isDeleted()) {
				return this.fail("O Plano de risco não foi encontrado");
			}

			PaginatedList<Unit> units = this.unitBS.listUnitsbyPlanRisk(plan, params);

			return this.success(units);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	@GetMapping(PATH + "/list-to-select")
	public ResponseEntity<?> listToSelect(@RequestParam(required = false) Long planRiskId) {
		try {
			List<UnitToSelect> units = this.unitBS.listToSelect(planRiskId);

			return this.success(new ListWrapper<UnitToSelect>(units));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ErrorMessages.UNEXPECTED_ERROR + ex.getMessage());
		}
	}

	/**
	 * Exclui unidade.
	 *
	 * @param id
	 *            Id da unidade a ser excluído.
	 *
	 */
	@DeleteMapping(PATH + "/{id}")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_COMPANY_ADMIN)
	public ResponseEntity<?> deleteUnit(@PathVariable Long id) {
		try {

			Unit unit = this.unitBS.exists(id, Unit.class);
			if (GeneralUtils.isInvalid(unit)) {
				return this.notFound();
			}

			if (this.unitBS.deletableUnit(unit)) {
				//deletar processos desta unidade
				PaginatedList<Process> processes = this.processBS.listProcessByUnit(unit);
				for(Process process :processes.getList()) {
					this.processBS.deleteProcess(process);
				}
				this.unitBS.delete(unit);
				return this.success(unit);
			} else {
				return this.fail("A unidade possuiu riscos ou subunidades, portanto não pode ser excluida." );
			}

		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}



	/**
	 * Atualiza unidade.
	 *
	 * @param unit
	 *            Unidade a ser atualizada.
	 *
	 */
	@PutMapping(PATH)
    @PreAuthorize(value = AccessLevels.HAS_ROLE_COLABORATOR)
	public ResponseEntity<?> updateUnit(@RequestBody UnitDto dto) {
		try {
			Unit unit = dto.unit();
			unit.setDescription(SanitizeUtil.sanitize(unit.getDescription()));
			return this.success(unitBS.updateUnit(unit));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail("Ocorreu um erro inesperado: " + ex.getMessage());
		}
	}


	/**
	 * Cria arquivo pdf  para exportar relatório
	 *
	 *
	 * @param title
	 * @param author
	 * @param pre
	 * @param item
	 * @param subitem
	 * @throws DocumentException
	 * @throws IOException
	 *
	 */
	@GetMapping(PATH + "/exportUnitReport")
	public ResponseEntity<?> exportreport(
			@RequestParam Long planRiskId,
			@RequestParam String title,
			@RequestParam String units,
			@RequestParam(required = false) String subunits,
			@RequestParam(required = false) String riskFilters,
			@RequestParam Integer selectedYear) {

		OutputStream outputStream = null;
		InputStream inputStream = null;

		try {
			RiskFilterParams riskFilterParams = RiskFilterParams.fromJson(riskFilters); 
			if (subunits != null && subunits.isEmpty()) {
				subunits = null; 
			}																			
			Params params = new Params(planRiskId, title, units, subunits, selectedYear, riskFilterParams);
			inputStream = unitReportGenerator.generateReport(params);
			//adicionar os arquivos anexos aos processos?
			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"" + title + ".pdf\"");
			outputStream = this.response.getOutputStream();

			IOUtils.copy(inputStream, outputStream);
			return this.nothing();

		} catch (Throwable ex) {
			LOGGER.error("Error while proxying the file upload.", ex);
			return this.fail(ex.getMessage());
	} finally {
			Util.closeFile(outputStream);
			Util.closeFile(inputStream);
		}
	}
}
