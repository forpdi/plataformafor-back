package org.forpdi.planning.fields.budget;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;

import org.forpdi.core.common.PaginatedList;
import org.forpdi.core.company.Company;
import org.forpdi.core.company.CompanyBS;
import org.forpdi.core.company.CompanyDomainContext;
import org.forpdi.core.bean.IdDto;
import org.forpdi.planning.fields.budget.dto.BudgetElementDao;
import org.forpdi.security.auth.UserSession;
import org.forpdi.security.authz.AccessLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class BudgetControllerTest {

  private BudgetController controller;
  private BudgetBS bs;
  private CompanyBS companyBS;
  private CompanyDomainContext domain;

  private static void inject(Object target, String field, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(field);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void setup() {
    controller = new BudgetController();
    bs = mock(BudgetBS.class);
    companyBS = mock(CompanyBS.class);
    domain = mock(CompanyDomainContext.class);

    inject(controller, "bs", bs);
    inject(controller, "companyBS", companyBS);
    inject(controller, "domain", domain);
  }

  private UserSession session(int accessLevel, long userId) {
    var authData = mock(org.forpdi.security.auth.AuthData.class);
    when(authData.getId()).thenReturn(userId);
    var sess = mock(UserSession.class);
    when(sess.getAccessLevel()).thenReturn(accessLevel);
    when(sess.getAuthData()).thenReturn(authData);
    return sess;
  }

  private BudgetElementDao dao(String subAction, String budgetLoa, Long id) {
    BudgetElementDao d = mock(BudgetElementDao.class);
    when(d.subAction()).thenReturn(subAction);
    when(d.budgetLoa()).thenReturn(budgetLoa);
    when(d.idBudgetElement()).thenReturn(id);
    return d;
  }

  @Test
  @DisplayName("save: SYSTEM_ADMIN usa Company do domain e cria com sucesso")
  void save_admin_success() {
    var userSession = session(AccessLevels.SYSTEM_ADMIN.getLevel(), 42L);
    Company comp = mock(Company.class);
    when(domain.getCompany()).thenReturn(comp);
    when(bs.budgetElementExistsBySubActionAndCompany("Ação X", comp)).thenReturn(null);

    BudgetElementDao d = dao("Ação X", "1.234,56", null);

    ResponseEntity<?> resp = controller.save(d, userSession);
    assertTrue(resp.getStatusCode().is2xxSuccessful(), "Deve retornar sucesso");

    verify(bs).saveBudgetElement(any(BudgetElement.class));
  }

  @Test
  @DisplayName("listBudgetActionPaginated: não-admin usa empresa do usuário e retorna lista")
  void list_paginated_non_admin_success() {
    var userSession = session(AccessLevels.COLABORATOR.getLevel(), 7L);
    Company comp = mock(Company.class);
    when(companyBS.findCompanyByUserId(7L)).thenReturn(comp);

    PaginatedList<BudgetElement> page = new PaginatedList<>();
    page.setList(List.of(mock(BudgetElement.class), mock(BudgetElement.class)));
    page.setTotal(2L);
    when(bs.listBudgetElementPaginated(eq(comp), any(), any(), any(), any())).thenReturn(page);

    ResponseEntity<?> resp = controller.listBudgetActionPaginated(userSession, null, 1, 10, List.of("subAction,asc"));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
  }

  @Test
  @DisplayName("update: SYSTEM_ADMIN atualiza com sucesso quando LOA >= comprometido + diferença")
  void update_admin_success() {
    var userSession = session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L);

    Company elemCompany = mock(Company.class); when(elemCompany.getId()).thenReturn(10L);
    BudgetElement elem = mock(BudgetElement.class);
    when(elem.getId()).thenReturn(123L);
    when(elem.getCompany()).thenReturn(elemCompany);
    when(elem.getBudgetLoa()).thenReturn(1000.0);
    when(elem.getBalanceAvailable()).thenReturn(800.0); // diferença = 200

    when(bs.budgetElementExistsById(123L)).thenReturn(elem);

    // nome único (ou mesmo id)
    when(bs.budgetElementExistsBySubActionAndCompany("Nome OK", elemCompany)).thenReturn(elem);

    // budgets vinculados somando committed = 300
    Budget b1 = mock(Budget.class); when(b1.getCommitted()).thenReturn(100.0);
    Budget b2 = mock(Budget.class); when(b2.getCommitted()).thenReturn(200.0);
    PaginatedList<Budget> budgets = new PaginatedList<>();
    budgets.setList(List.of(b1, b2));
    when(bs.listBudgetsByBudgetElement(elem)).thenReturn(budgets);

    BudgetElementDao d = dao("Nome OK", "700,00", 123L); // 700 >= diferenca(200), committedTotal(300) -> balance = 400

    ResponseEntity<?> resp = controller.update(d, userSession);
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).update(elem);
  }

  @Test
  @DisplayName("deleteBudget: sucesso quando não há vínculos")
  void delete_success_without_links() {
    var userSession = session(AccessLevels.SYSTEM_ADMIN.getLevel(), 1L);

    BudgetElement elem = mock(BudgetElement.class);
    when(bs.budgetElementExistsById(6L)).thenReturn(elem);

    PaginatedList<Budget> empty = new PaginatedList<>();
    empty.setList(List.of());
    when(bs.listBudgetsByBudgetElement(elem)).thenReturn(empty);

    ResponseEntity<?> resp = controller.deleteBudget(new IdDto(6L));
    assertTrue(resp.getStatusCode().is2xxSuccessful());
    verify(bs).deleteBudget(elem);
  }

}
