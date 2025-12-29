package org.forpdi.planning.document;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.forpdi.core.bean.IdDto;
import org.forpdi.core.common.AbstractController;
import org.forpdi.core.common.GeneralUtils;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.utils.SanitizeUtil;
import org.forpdi.core.utils.Util;
import org.forpdi.planning.document.dto.DocumentAttributeDto;
import org.forpdi.planning.document.dto.DocumentSectionToSaveDto;
import org.forpdi.planning.document.dto.SectionIdDto;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.reports.fpdi.LevelAttributesReportGenerator;
import org.forpdi.system.reports.fpdi.PDIDocumentReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentController extends AbstractController {

	@Autowired
	private DocumentBS bs;
	@Autowired
	private PlanBS planBs;
	@Autowired
	private StructureBS structureBS;
	@Autowired
	private PDIDocumentReportGenerator pdiDocumentReportGenerator;
	@Autowired
	private LevelAttributesReportGenerator levelAttributesReportGenerator;
	@Autowired
	private CompanyDomainContext domain;
	@Autowired
	private CompanyBS companyBS;

	/**
	 * Retorna documento com suas seções para exibição no frontend.
	 * 
	 * @param planId
	 *            Id do plano macro.
	 * @return DocumentDTO Documento com suas seções para exibição no frontend
	 */
	@GetMapping(BASEPATH + "/document/{planId}")
	public ResponseEntity<?> retrieveByPlan(@PathVariable Long planId, UserSession userSession) {
		try {
			PlanMacro plan = this.planBs.retrievePlanMacroById(planId);
			if (plan == null) {
				return this.fail("Plano não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !plan.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			DocumentDTO dto = new DocumentDTO();
			Document document = this.bs.retrieveByPlan(plan);
			dto.setDocument(document);
			dto.setSections(this.bs.listSectionDTOsByDocument(document, null));
			return this.success(dto);
			
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Lista atributos de uma seção.
	 * 
	 * @param sectionId
	 *            Id da seção onde estão os atributos.
	 * @param planId
	 *            Id do plano de metas.
	 * @return DocumentSection Seção do documento com seus atributos.
	 */
	@GetMapping(BASEPATH + "/document/sectionattributes")
	public ResponseEntity<?> getSectionAttributes(
			@RequestParam Long sectionId,
			@RequestParam(required = false) Long planId,
			UserSession userSession) {

		try {
			DocumentSection documentSection = this.bs.retrieveSectionById(sectionId);
			if (documentSection == null) {
				return this.fail("Seção inexistente!");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !documentSection.getDocument().getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			
			List<DocumentAttribute> list = this.bs.listAttributesBySection(documentSection, planId);
			documentSection.setDocumentAttributes(list);
			return this.success(documentSection);

		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Salva atributos de uma seção do documento.
	 * 
	 * @param documentSection
	 *            Seção do documento com seus atributos.
	 * @return DocumentSection Seção do documento com seus atributos.
	 */
	@PostMapping(BASEPATH + "/document/sectionattributes")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> saveSectionAttributes(@RequestBody DocumentSection documentSection, UserSession userSession) {

		String sanitizedName = Util.htmlToText(documentSection.getName());

		if (Util.hasOnlySpecialCharactersString(sanitizedName)) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}

		try {
			DocumentSection section = this.bs.retrieveSectionById(documentSection.getId());

			if (section == null) {
				return this.fail("Seção não encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !section.getDocument().getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			if (!sanitizedName.isEmpty()) {
				section.setName(sanitizedName);
				List<DocumentAttribute> sanitizedAttributes = documentSection.getDocumentAttributes().stream()
						.map(attr -> {
							attr.setValue(SanitizeUtil.sanitize(attr.getValue()));
							return attr;
						})
						.collect(Collectors.toList());

				section.setDocumentAttributes(this.bs.saveAttributes(sanitizedAttributes));
				this.bs.persist(section);
			}
			return this.success(documentSection);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Cria seção no documento
	 * 
	 * @param documentId
	 *            Id documento no qual se deseja inserir a seção.
	 * @param name
	 *            O nome da seção a ser adicionada.
	 * @param parentId
	 *            O id da seção pai, caso haja. Caso contrário deve-se passar
	 *            null.
	 * @return DocumentSectionDTO A seção do documento para exibição no
	 *         frontend.
	 */
	@PostMapping(BASEPATH + "/document/{documentId}/section")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> createSection(
			@PathVariable Long documentId,
			@RequestBody DocumentSectionToSaveDto dto,
			UserSession userSession) {
		
		if (Util.hasOnlySpecialCharactersString(dto.name())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}
		
		try {
			Document doc = this.bs.exists(documentId, Document.class);
			if (doc == null) {
				return this.fail("Documento não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !doc.getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			DocumentSection sec = this.bs.createSection(doc, dto.name(), dto.parentId());
			return this.success(new DocumentSectionDTO(sec, this.bs));
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Deletar seção do documento;
	 * 
	 * @param sectionId
	 *            Id da seção a ser excluída.
	 * @return DocumentSection Seção do documento que foi excluída.
	 */
	@PostMapping(BASEPATH + "/document/section/delete")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteSection(@RequestBody SectionIdDto dto, UserSession userSession) {
		try {
			DocumentSection section = this.bs.retrieveSectionById(dto.sectionId());
			if (section == null) {
				return this.fail("Seção não encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !section.getDocument().getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			section.setDeleted(true);
			this.bs.persist(section);
			if (section.getParent() != null) {
				DocumentSection parent = this.bs.retrieveSectionById(section.getParent().getId());
				if (this.bs.listSectionChild(parent).size() <= 0) {
					parent.setLeaf(true);
					this.bs.persist(parent);
				}
			}
			return this.success(section);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Cria um atributo em uma seção do documento.
	 * 
	 * @param sectionId
	 *            Id da seção na qual se deseja adicionar o atributo.
	 * @param name
	 *            O nome do atributo.
	 * @param type
	 *            O tipo do atributo.
	 * @param periodicity
	 *            A periodicidade do atributo, se houver.
	 * @return DocumentAttribute O atributo criado.
	 */
	@PostMapping(BASEPATH + "/document/section/{sectionId}/attribute")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> createAttribute(@RequestBody DocumentAttributeDto dto, UserSession userSession) {
		if (Util.hasOnlySpecialCharactersString(dto.name())) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}
		
		try {
			DocumentSection sec = this.bs.exists(dto.section(), DocumentSection.class);
			if (sec == null) {
				return this.fail("Seção não encontrada.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !sec.getDocument().getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			DocumentAttribute attr = this.bs.createAttribute(sec, dto.name(), dto.type(), dto.periodicity());
			return this.success(attr);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Lista seções do documento diferenciando se está preenchido ou não pelo
	 * atributo filled.
	 * 
	 * @param planId
	 *            Id do plano macro referente ao documento.
	 * @return PaginatedList<DocumentSection> Lista das seções com o atributo
	 *         filled preenchido.
	 */
	@GetMapping(BASEPATH + "/document/{planId}/filledsections")
	public ResponseEntity<?> retrieveFilledSectionsByPlan(@PathVariable Long planId, UserSession userSession) {
		try {
			PlanMacro planMacro = this.planBs.retrievePlanMacroById(planId);
			if (planMacro == null) {
				return this.fail("Plano macro não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !planMacro.getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			Document document = this.bs.retrieveDocumentByPlan(planMacro);
			PaginatedList<DocumentSection> sections = this.bs.listRootSectionsByDocument(document);
			this.bs.setSectionsFilled(sections.getList(), planId);
			return this.success(sections);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Exporta documento PDF
	 * 
	 * @param author
	 *            Autor do documento.
	 * @param title
	 *            Título do documento.
	 * @param lista
	 *            Lista de seções a serem exportadas.
	 * @return OutputStream Arquivo PDF.
	 */
	@GetMapping(BASEPATH + "/document/exportdocument")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> exportDocument(@RequestParam String title, @RequestParam String lista, UserSession userSession) {
		try {
			boolean isSystemAdmin = (userSession.getAccessLevel() == AccessLevels.SYSTEM_ADMIN.getLevel());
			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);
				Company domainCompany = this.domain.get().getCompany();
				if (userCompany == null || domainCompany == null
					|| !userCompany.getId().equals(domainCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}
			InputStream in = pdiDocumentReportGenerator.generateReport(new PDIDocumentReportGenerator.Params(title, lista));
			OutputStream out;

			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"" + title + ".pdf\"");
			out = this.response.getOutputStream();
			IOUtils.copy(in, out);
			out.close();
			in.close();
			return this.nothing();
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}


	/**
	 * Exportar para pdf os atributos de um level
	 * @param levelId
	 * 			Id o level
	 */
	@GetMapping(BASEPATH + "/document/exportLevelAttributes")
	public ResponseEntity<?> exportLevelAttributes(@RequestParam Long levelId, UserSession userSession) {
		InputStream in = null;
		OutputStream out = null;
		try {
			StructureLevelInstance levelInstance = this.structureBS.retrieveLevelInstance(levelId);
			if (levelInstance == null) {
				return this.fail("Nível da estrutura não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !levelInstance.getPlan().getParent().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			in = levelAttributesReportGenerator.generateReport(new LevelAttributesReportGenerator.Params(levelId));

			this.response.reset();
			this.response.setHeader("Content-Type", "application/pdf");
			this.response.setHeader("Content-Disposition", "inline; filename=\"exportLevel" + levelId + ".pdf\"");
			out = this.response.getOutputStream();
			IOUtils.copy(in, out);
			return this.nothing();
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		} finally {
			Util.closeFile(in);
			Util.closeFile(out);
		}
	}

	/**
	 * Deletar um atributo da seção
	 * 
	 * @param id
	 *            Id do atributo a ser excluído.
	 * @return DocumentAttribute Atributo excluído.
	 */
	@PostMapping(BASEPATH + "/document/sectionattribute/delete")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> deleteSectionAttribute(@RequestBody IdDto dto, UserSession userSession) {
		try {
			DocumentAttribute attr = this.bs.retrieveDocumentAttribute(dto.id());
			if (attr == null) {
				return this.fail("Atributo não encontrado.");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !attr.getSection().getDocument().getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			if (attr.getSection().isPreTextSection()) {
				return this.fail("O campo dessa seção não pode ser deletado");
			} else {
				attr.setDeleted(true);
				this.bs.persist(attr);
				return this.success(attr);
			}
			
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Editar atributo da seção.
	 * 
	 * @param id
	 *            Id do atributo do documento a ser editado.
	 * @param name
	 *            Nome do atributo.
	 * @return DocumentAttribute Atributo editado.
	 */
	@PostMapping(BASEPATH + "/document/sectionattribute/edit")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER)
	public ResponseEntity<?> editSectionAttribute(@RequestBody DocumentSectionToSaveDto dto, UserSession userSession) {
		String sanitizedName = SanitizeUtil.sanitize(dto.name());
		String sanitizedValue = SanitizeUtil.sanitize(dto.value());

		if (Util.hasOnlySpecialCharactersString(sanitizedName)) {
			return this.fail("O nome não pode possuir somente caracteres especiais");
		}

		if (GeneralUtils.isEmpty(sanitizedName)) {
			return this.fail("O nome não pode ser vazio");
		}

		if (GeneralUtils.isEmpty(sanitizedValue)) {
			return this.fail("O valor não pode ser vazio");
		}

		try {
			DocumentAttribute exist = this.bs.retrieveDocumentAttribute(dto.id());
			if (exist == null) {
				return this.fail("Atributo não existente");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !exist.getSection().getDocument().getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			exist.setValue(sanitizedValue);
			exist.setName(sanitizedName);
			this.bs.persist(exist);
			return this.success(exist);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	/**
	 * Atualiza a descrição do Documento FORPDI
	 * 
	 * @param id
	 *            Id do atributo a ser atualizado
	 * @return description updated description
	 */
	@PostMapping(BASEPATH + "/document/update")
	@PreAuthorize(value = AccessLevels.HAS_ROLE_MANAGER) 
	public ResponseEntity<?> updateDescription(@RequestBody Document document, UserSession userSession) {
		try {
			Document existent =  bs.exists(document.getId(), Document.class);
			if (existent == null) {
				return this.fail("O documento não existe");
			}

			boolean isSystemAdmin = (userSession.getAccessLevel() >= AccessLevels.SYSTEM_ADMIN.getLevel());

			if (!isSystemAdmin) {
				Long loggedInUserId = userSession.getAuthData().getId();
				Company userCompany = this.companyBS.findCompanyByUserId(loggedInUserId);

				if (userCompany == null || !existent.getPlan().getCompany().getId().equals(userCompany.getId())) {
					return this.fail("Acesso negado.");
				}
			}

			String description = document.getDescription();
			if (GeneralUtils.isEmpty(description)) {
				return this.fail("A descrição não pode ser vazia");
			}

			String sanitizedDescription = SanitizeUtil.sanitize(description);
			existent.setDescription(sanitizedDescription);
			this.bs.persist(existent);
			return this.success(existent);
		} catch (Throwable e) {
			LOGGER.error("Unexpected runtime error", e);
			return this.fail("Ocorreu um erro inesperado ao atualizar o documento: " + e.getMessage());
		}
	}
}
