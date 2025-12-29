// package org.forpdi.planning.fields;

// import static org.mockito.Mockito.*;
// import static org.junit.jupiter.api.Assertions.*;

// import org.forpdi.core.company.CompanyBS;
// import org.forpdi.core.user.UserBS;
// import org.forpdi.planning.fields.budget.BudgetBS;
// import org.forpdi.planning.fields.dto.BudgetFieldDto;
// import org.forpdi.planning.structure.StructureLevelInstance;
// import org.forpdi.security.auth.UserSession;
// import org.forpdi.core.common.PaginatedList;
// import org.forpdi.core.company.Company;
// import org.forpdi.planning.fields.budget.Budget;
// import org.forpdi.planning.fields.budget.BudgetElement;
// import org.forpdi.core.company.CompanyDomainContext;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;

// import org.springframework.http.ResponseEntity;

// public class FieldsControllerTest {

//     @Mock
//     private FieldsBS fieldsBS;
//     @Mock
//     private StructureBS structureBS;
//     @Mock
//     private CompanyBS companyBS;
//     @Mock
//     private BudgetBS budgetBS;
//     @Mock
//     private UserBS userBS;
//     @Mock
//     private CompanyDomainContext domainContext;
//     @Mock
//     private UserSession userSession;

//     private FieldsController fieldsController;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         fieldsController = new FieldsController();
//         fieldsController.setBs(fieldsBS);
//         fieldsController.setStructureBs(structureBS);
//         fieldsController.setCompanyBS(companyBS);
//         fieldsController.setBudgetElementBs(budgetBS);
//         fieldsController.setUserBs(userBS);
//         fieldsController.setDomain(domainContext);
//     }

//     @Test
//     void testSaveBudget_validInput_success() {
//         BudgetFieldDto dto = mock(BudgetFieldDto.class);
//         when(dto.instanceId()).thenReturn(1L);
//         when(dto.subAction()).thenReturn(1L);
//         when(dto.committed()).thenReturn("100.0");
//         when(dto.realized()).thenReturn("50.0");

//         Company company = mock(Company.class);
//         when(companyBS.findCompanyByUserId(anyLong())).thenReturn(company);
//         when(fieldsBS.saveBudget(any(Budget.class))).thenReturn(true);

//         ResponseEntity<?> response = fieldsController.save(dto, userSession);

//         assertEquals(200, response.getStatusCodeValue());
//     }

//     @Test
//     void testSaveBudget_invalidInstance_failure() {
//         BudgetFieldDto dto = mock(BudgetFieldDto.class);
//         when(dto.instanceId()).thenReturn(1L);

//         when(fieldsBS.retrieveLevelInstance(1L)).thenReturn(null); // Simulating invalid instance

//         ResponseEntity<?> response = fieldsController.save(dto, userSession);

//         assertEquals(400, response.getStatusCodeValue());
//         assertTrue(response.getBody().toString().contains("Estrutura inválida!"));
//     }

//     @Test
//     void testSaveBudget_invalidSubAction_failure() {
//         BudgetFieldDto dto = mock(BudgetFieldDto.class);
//         when(dto.instanceId()).thenReturn(1L);
//         when(dto.subAction()).thenReturn(999L); // Invalid SubAction

//         StructureLevelInstance instance = mock(StructureLevelInstance.class);
//         when(fieldsBS.retrieveLevelInstance(1L)).thenReturn(instance);
//         when(budgetBS.budgetElementExistsById(999L)).thenReturn(null); // Simulating invalid sub action

//         ResponseEntity<?> response = fieldsController.save(dto, userSession);

//         assertEquals(400, response.getStatusCodeValue());
//         assertTrue(response.getBody().toString().contains("Sub ação inválida!"));
//     }

//     @Test
//     void testSaveBudget_committedGreaterThanLoa_failure() {
//         BudgetFieldDto dto = mock(BudgetFieldDto.class);
//         when(dto.instanceId()).thenReturn(1L);
//         when(dto.subAction()).thenReturn(1L);
//         when(dto.committed()).thenReturn("1000.0"); // Greater than the available LOA value
//         when(dto.realized()).thenReturn("500.0");

//         StructureLevelInstance instance = mock(StructureLevelInstance.class);
//         when(fieldsBS.retrieveLevelInstance(1L)).thenReturn(instance);
//         BudgetElement budgetElement = mock(BudgetElement.class);
//         when(budgetBS.budgetElementExistsById(1L)).thenReturn(budgetElement);
//         when(budgetElement.getBudgetLoa()).thenReturn(500.0); // LOA value is 500.0

//         ResponseEntity<?> response = fieldsController.save(dto, userSession);

//         assertEquals(400, response.getStatusCodeValue());
//         assertTrue(response.getBody().toString().contains("Valor empenhado não pode ser maior que o valor do orçamento LOA!"));
//     }

//     @Test
//     void testSaveBudget_invalidRealized_failure() {
//         BudgetFieldDto dto = mock(BudgetFieldDto.class);
//         when(dto.instanceId()).thenReturn(1L);
//         when(dto.subAction()).thenReturn(1L);
//         when(dto.committed()).thenReturn("100.0");
//         when(dto.realized()).thenReturn("150.0"); // Realized greater than committed

//         StructureLevelInstance instance = mock(StructureLevelInstance.class);
//         when(fieldsBS.retrieveLevelInstance(1L)).thenReturn(instance);
//         BudgetElement budgetElement = mock(BudgetElement.class);
//         when(budgetBS.budgetElementExistsById(1L)).thenReturn(budgetElement);

//         ResponseEntity<?> response = fieldsController.save(dto, userSession);

//         assertEquals(400, response.getStatusCodeValue());
//         assertTrue(response.getBody().toString().contains("Valor do realizado não pode ser maior que o valor do empenhado!"));
//     }

//     @Test
//     void testSaveBudget_internalError_failure() {
//         BudgetFieldDto dto = mock(BudgetFieldDto.class);

//         when(fieldsBS.retrieveLevelInstance(anyLong())).thenThrow(new RuntimeException("Unexpected error"));

//         ResponseEntity<?> response = fieldsController.save(dto, userSession);

//         assertEquals(500, response.getStatusCodeValue());
//         assertTrue(response.getBody().toString().contains("Ocorreu um erro inesperado"));
//     }
// }
