package org.forpdi.planning.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.forpdi.core.bean.IdDto;
import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.planning.document.dto.SectionIdDto;
import org.forpdi.planning.plan.PlanBS;
import org.forpdi.planning.plan.PlanMacro;
import org.forpdi.planning.structure.StructureBS;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.forpdi.system.reports.fpdi.LevelAttributesReportGenerator;
import org.forpdi.system.reports.fpdi.PDIDocumentReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;

class DocumentControllerTest {

  private DocumentController controller;

  private DocumentBS bs;
  private PlanBS planBS;
  private StructureBS structureBS;
  private PDIDocumentReportGenerator pdiDocGen;
  private LevelAttributesReportGenerator levelAttrsGen;
  private CompanyDomainContext domain;
  private CompanyBS companyBS;

  private MockHttpServletResponse mockResp;

  private static void inject(Object target, String field, Object value) {
    try {
      Field f = null;
      Class<?> c = target.getClass();
      while (c != null) {
        try {
          f = c.getDeclaredField(field);
          break;
        } catch (NoSuchFieldException e) {
          c = c.getSuperclass();
        }
      }
      if (f == null) throw new NoSuchFieldException(field);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private UserSession session(int accessLevel, long userId) {
    var authData = mock(org.forpdi.security.auth.AuthData.class);
    when(authData.getId()).thenReturn(userId);
    var sess = mock(UserSession.class);
    when(sess.getAccessLevel()).thenReturn(accessLevel);
    when(sess.getAuthData()).thenReturn(authData);
    return sess;
  }

  @BeforeEach
  void setup() {
    controller = new DocumentController();

    bs = mock(DocumentBS.class);
    planBS = mock(PlanBS.class);
    structureBS = mock(StructureBS.class);
    pdiDocGen = mock(PDIDocumentReportGenerator.class);
    levelAttrsGen = mock(LevelAttributesReportGenerator.class);
    domain = mock(CompanyDomainContext.class);
    companyBS = mock(CompanyBS.class);

    inject(controller, "bs", bs);
    inject(controller, "planBs", planBS);
    inject(controller, "structureBS", structureBS);
    inject(controller, "pdiDocumentReportGenerator", pdiDocGen);
    inject(controller, "levelAttributesReportGenerator", levelAttrsGen);
    inject(controller, "domain", domain);
    inject(controller, "companyBS", companyBS);

    mockResp = new MockHttpServletResponse();
    inject(controller, "response", (HttpServletResponse) mockResp);
  }

  // ---------------- retrieveByPlan ----------------

  @Test
  @DisplayName("retrieveByPlan: falha quando plano não existe")
  void retrieveByPlan_planNotFound() {
    when(planBS.retrievePlanMacroById(10L)).thenReturn(null);
    var resp = controller.retrieveByPlan(10L, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("retrieveByPlan: sucesso retorna DTO")
  void retrieveByPlan_success() {
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(100L);
    when(plan.getCompany()).thenReturn(company);
    when(planBS.retrievePlanMacroById(1L)).thenReturn(plan);

    var doc = mock(Document.class);
    when(bs.retrieveByPlan(plan)).thenReturn(doc);
    when(bs.listSectionDTOsByDocument(eq(doc), isNull())).thenReturn(List.of());

    var resp = controller.retrieveByPlan(1L, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
  }

  @Test
  @DisplayName("getSectionAttributes: seção inexistente")
  void getSectionAttributes_missing() {
    when(bs.retrieveSectionById(9L)).thenReturn(null);
    var resp = controller.getSectionAttributes(9L, null, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("getSectionAttributes: sucesso popula atributos")
  void getSectionAttributes_success() {
    var section = mock(DocumentSection.class);
    var doc = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(doc.getPlan()).thenReturn(plan);
    when(section.getDocument()).thenReturn(doc);

    when(bs.retrieveSectionById(5L)).thenReturn(section);
    when(bs.listAttributesBySection(section, null)).thenReturn(List.of());

    var resp = controller.getSectionAttributes(5L, null, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 99L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).listAttributesBySection(section, null);
  }

  @Test
  @DisplayName("saveSectionAttributes: nome só com caracteres especiais -> falha imediata")
  void saveSectionAttributes_specialOnlyName() {
    var given = new DocumentSection(); given.setName("***");
    var resp = controller.saveSectionAttributes(given, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("saveSectionAttributes: sucesso sanitiza e salva")
  void saveSectionAttributes_success() {
    var incoming = new DocumentSection();
    incoming.setId(10L);
    incoming.setName("Nome <b>ok</b>");
    incoming.setDocumentAttributes(List.of(new DocumentAttribute()));

    var existing = mock(DocumentSection.class);
    var doc = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(doc.getPlan()).thenReturn(plan);
    when(existing.getDocument()).thenReturn(doc);

    when(bs.retrieveSectionById(10L)).thenReturn(existing);
    when(bs.saveAttributes(anyList())).thenReturn(List.of());

    var resp = controller.saveSectionAttributes(incoming, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 7L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).persist(existing);
  }

  @Test
  @DisplayName("deleteSection: seção não encontrada")
  void deleteSection_missing() {
    when(bs.retrieveSectionById(1L)).thenReturn(null);
    var resp = controller.deleteSection(new SectionIdDto(1L), session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("deleteSection: com parent sem filhos -> parent vira leaf")
  void deleteSection_parentLeaf() {
    var parent = mock(DocumentSection.class);
    when(parent.getId()).thenReturn(99L);

    var section = mock(DocumentSection.class);
    when(section.getParent()).thenReturn(parent);

    var doc = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(doc.getPlan()).thenReturn(plan);
    when(section.getDocument()).thenReturn(doc);

    when(bs.retrieveSectionById(10L)).thenReturn(section);
    when(bs.retrieveSectionById(99L)).thenReturn(parent);
    when(bs.listSectionChild(parent)).thenReturn(List.of()); // sem filhos

    var resp = controller.deleteSection(new SectionIdDto(10L), session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs, atLeastOnce()).persist(section);
    verify(bs, atLeastOnce()).persist(parent);
  }


  @Test
  @DisplayName("retrieveFilledSectionsByPlan: plano macro não encontrado")
  void retrieveFilledSectionsByPlan_missingPlan() {
    when(planBS.retrievePlanMacroById(3L)).thenReturn(null);
    var resp = controller.retrieveFilledSectionsByPlan(3L, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("retrieveFilledSectionsByPlan: sucesso e setSectionsFilled chamado")
  void retrieveFilledSectionsByPlan_success() {
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(planBS.retrievePlanMacroById(3L)).thenReturn(plan);

    var document = mock(Document.class);
    when(bs.retrieveDocumentByPlan(plan)).thenReturn(document);

    PaginatedList<DocumentSection> roots = new PaginatedList<>();
    roots.setList(List.of(mock(DocumentSection.class)));
    when(bs.listRootSectionsByDocument(document)).thenReturn(roots);

    var resp = controller.retrieveFilledSectionsByPlan(3L, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).setSectionsFilled(anyList(), eq(3L));
  }

  @Test
  @DisplayName("exportDocument: sucesso escreve PDF no response")
  void exportDocument_success() throws Exception {
    // admin
    var bytes = "PDF".getBytes();
    when(pdiDocGen.generateReport(any())).thenReturn(new ByteArrayInputStream(bytes));

    var resp = controller.exportDocument("Titulo", "1,2,3", session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());

    // conteúdo escrito
    assertArrayEquals(bytes, mockResp.getContentAsByteArray());
    assertEquals("application/pdf", mockResp.getHeader("Content-Type"));
    assertTrue(mockResp.getHeader("Content-Disposition").contains("Titulo.pdf"));
  }

  // ---------------- exportLevelAttributes ----------------

  @Test
  @DisplayName("exportLevelAttributes: level inexistente")
  void exportLevelAttributes_missing() {
    when(structureBS.retrieveLevelInstance(9L)).thenReturn(null);
    var resp = controller.exportLevelAttributes(9L, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  // ---------------- deleteSectionAttribute ----------------

  @Test
  @DisplayName("deleteSectionAttribute: atributo não encontrado")
  void deleteSectionAttribute_missing() {
    when(bs.retrieveDocumentAttribute(5L)).thenReturn(null);
    var resp = controller.deleteSectionAttribute(new IdDto(5L), session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("deleteSectionAttribute: atributo em preTextSection -> falha")
  void deleteSectionAttribute_preText_fails() {
    var attr = mock(DocumentAttribute.class);
    var sec = mock(DocumentSection.class);
    when(sec.isPreTextSection()).thenReturn(true);
    when(attr.getSection()).thenReturn(sec);

    var doc = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(doc.getPlan()).thenReturn(plan);
    when(sec.getDocument()).thenReturn(doc);

    when(bs.retrieveDocumentAttribute(6L)).thenReturn(attr);

    var resp = controller.deleteSectionAttribute(new IdDto(6L), session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("deleteSectionAttribute: sucesso marca deleted e persiste")
  void deleteSectionAttribute_success() {
    var attr = mock(DocumentAttribute.class);
    var sec = mock(DocumentSection.class);
    var doc = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(doc.getPlan()).thenReturn(plan);
    when(sec.getDocument()).thenReturn(doc);
    when(attr.getSection()).thenReturn(sec);

    when(bs.retrieveDocumentAttribute(7L)).thenReturn(attr);

    var resp = controller.deleteSectionAttribute(new IdDto(7L), session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).persist(attr);
  }

  @Test
  @DisplayName("updateDescription: documento não existe")
  void updateDescription_missing() {
    var doc = new Document(); doc.setId(77L); doc.setDescription("x");
    when(bs.exists(77L, Document.class)).thenReturn(null);
    var resp = controller.updateDescription(doc, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("updateDescription: falha se descrição vazia")
  void updateDescription_empty() {
    var existent = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(existent.getPlan()).thenReturn(plan);

    when(bs.exists(99L, Document.class)).thenReturn(existent);

    var doc = new Document(); doc.setId(99L); doc.setDescription("");
    var resp = controller.updateDescription(doc, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is4xxClientError());
  }

  @Test
  @DisplayName("updateDescription: sucesso persiste")
  void updateDescription_success() throws Exception {
    var existent = mock(Document.class);
    var plan = mock(PlanMacro.class);
    var company = mock(Company.class); when(company.getId()).thenReturn(1L);
    when(plan.getCompany()).thenReturn(company);
    when(existent.getPlan()).thenReturn(plan);

    when(bs.exists(44L, Document.class)).thenReturn(existent);

    var doc = new Document(); doc.setId(44L); doc.setDescription("Nova descrição");
    var resp = controller.updateDescription(doc, session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).persist(existent);
  }
}
