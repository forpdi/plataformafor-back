package org.forrisco.risk.incident;

import org.forpdi.core.bean.DefaultParams;
import org.forpdi.core.common.*;
import org.forrisco.core.plan.PlanRisk;
import org.forrisco.core.unit.Unit;
import org.forrisco.risk.Risk;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentBSTest {
	@InjectMocks
	IncidentBS bs;
	@Mock
	HibernateDAO dao;
	@Mock
	EntityManager entityManager;
	@Mock
	Criteria criteria;

	@DisplayName("Deve salvar no banco de dados um incidente.")
	@Test
	void testSaveIncident() {
		Incident incidentToSave = new Incident();
		incidentToSave.setId(1L);

		bs.saveIncident(incidentToSave);

		verify(dao).persist(incidentToSave);
		assertFalse(incidentToSave.isDeleted(), "O Incidente salvo não deveria estar como deletado.");
	}

	@DisplayName("Deve deletar no banco de dados um incidente.")
	@Test
	void testDelete() {
		Incident incidentToDelete = new Incident();
		incidentToDelete.setId(1L);

		bs.delete(incidentToDelete);

		verify(dao).persist(incidentToDelete);
		assertTrue(incidentToDelete.isDeleted(), "O Incidente deletado deveria ter o status de deletado.");
	}

	@DisplayName("Deve retornar uma lista de incidentes de acordo com os ids e paginação. " +
		"Caso Page and PageSize válidos.")
	@Test
	void testPaginateIncidentsWithPageAndPageSizeNotNull() {

		List<Long> incidentsId = new ArrayList<>(List.of(1L, 3L, 6L));
		int page = 1;
		int pageSize = 5;
		Incident incident1 = new Incident();
		incident1.setId(1L);
		Incident incident2 = new Incident();
		incident2.setId(3L);
		Incident incident3 = new Incident();
		incident3.setId(6L);
		List<Incident> listOfIncidents = new ArrayList<>(List.of(incident1, incident2, incident3));


		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Incident.class)).thenReturn(listOfIncidents);
		when(criteria.uniqueResult()).thenReturn((long) listOfIncidents.size());

		PaginatedList<Incident> returnedIncidents = bs.pagitaneIncidents(incidentsId, page, pageSize);

		assertEquals(listOfIncidents, returnedIncidents.getList(),
			"A lista retornada de incidentes não corresponde à esperada.");
		assertEquals(listOfIncidents.size(), returnedIncidents.getTotal(),
			"O total de incidentes retornado não corresponde ao esperado.");
	}

	@DisplayName("Deve retornar uma lista de incidentes de acordo com os ids e paginação. Caso Page e PageSize Nulos.")
	@Test
	void testPaginateIncidentsWithPageAndPageSizeNull() {

		List<Long> incidentsId = new ArrayList<>(List.of(1L, 3L, 6L));

		Incident incident1 = new Incident();
		incident1.setId(1L);
		Incident incident2 = new Incident();
		incident2.setId(3L);
		Incident incident3 = new Incident();
		incident3.setId(6L);
		List<Incident> listOfIncidents = new ArrayList<>(List.of(incident1, incident2, incident3));


		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Incident.class)).thenReturn(listOfIncidents);
		when(criteria.uniqueResult()).thenReturn((long) listOfIncidents.size());

		PaginatedList<Incident> returnedIncidents = bs.pagitaneIncidents(incidentsId, null, null);

		assertEquals(listOfIncidents, returnedIncidents.getList(),
			"A lista retornada de incidentes não corresponde à esperada.");
		assertEquals(listOfIncidents.size(), returnedIncidents.getTotal(),
			"O total de incidentes retornado não corresponde ao esperado.");
	}

	@DisplayName("Deve retornar uma lista de incidentes de acordo com os ids e paginação. Caso Page zerado.")
	@Test
	void testPaginateIncidentsWithZeroPage() {

		List<Long> incidentsId = new ArrayList<>(List.of(1L, 3L, 6L));
		int pageSize = 5;
		Incident incident1 = new Incident();
		incident1.setId(1L);
		Incident incident2 = new Incident();
		incident2.setId(3L);
		Incident incident3 = new Incident();
		incident3.setId(6L);
		List<Incident> listOfIncidents = new ArrayList<>(List.of(incident1, incident2, incident3));


		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Incident.class)).thenReturn(listOfIncidents);
		when(criteria.uniqueResult()).thenReturn((long) listOfIncidents.size());

		PaginatedList<Incident> returnedIncidents = bs.pagitaneIncidents(incidentsId, 0, pageSize);

		assertEquals(listOfIncidents, returnedIncidents.getList(),
			"A lista retornada de incidentes não corresponde à esperada.");
		assertEquals(listOfIncidents.size(), returnedIncidents.getTotal(),
			"O total de incidentes retornado não corresponde ao esperado.");
	}

	@DisplayName("Deve retornar os incidentes a partir de vários riscos.")
	@Test
	void testListIncidentsByRiskListCase() {
		Risk risk1 = new Risk();
		risk1.setId(2L);
		Risk risk2 = new Risk();
		risk2.setId(7L);
		Risk risk3 = new Risk();
		risk2.setId(8L);

		PaginatedList<Risk> listRiskToList = new PaginatedList<>();
		listRiskToList.setList(List.of(risk1, risk2));
		listRiskToList.setTotal(2L);

		Incident incidentR1 = new Incident();
		incidentR1.setId(1L);
		incidentR1.setRisk(risk1);
		PaginatedList<Incident> listIncR1 = new PaginatedList<>();
		listIncR1.setList(List.of(incidentR1));
		listIncR1.setTotal(1L);

		Incident incidentR2 = new Incident();
		incidentR2.setId(3L);
		incidentR2.setRisk(risk2);
		PaginatedList<Incident> listIncR2 = new PaginatedList<>();
		listIncR2.setList(List.of(incidentR2));
		listIncR2.setTotal(1L);

		Incident incidentR3 = new Incident();
		incidentR2.setId(6L);
		incidentR2.setRisk(risk3);

		IncidentBS bsMock = mock(IncidentBS.class);
		doCallRealMethod().when(bsMock).listIncidentsByRisk(listRiskToList);
		when(bsMock.listIncidentsByRisk(risk1)).thenReturn(listIncR1);
		when(bsMock.listIncidentsByRisk(risk2)).thenReturn(listIncR2);

		PaginatedList<Incident> returnedListIncident = bsMock.listIncidentsByRisk(listRiskToList);

		assertEquals(2L, returnedListIncident.getTotal(), "O total de incidentes não foi o esperado.");
		assertTrue(returnedListIncident.getList().contains(incidentR1),
			"A lista retornada deve conter o incidente informado.");
		assertTrue(returnedListIncident.getList().contains(incidentR2),
			"A lista retornada deve conter o incidente informado.");
		assertFalse(returnedListIncident.getList().contains(incidentR3),
			"A lista retornada não deve conter o incidente informado.");

	}

	@DisplayName("Deve retornar os incidentes a partir de um risco")
	@Test
	void testListIncidentsByRisk() {
		Risk risk1 = new Risk();
		risk1.setId(2L);

		Incident incidentR1 = new Incident();
		incidentR1.setId(1L);
		incidentR1.setRisk(risk1);
		PaginatedList<Incident> listIncR1 = new PaginatedList<>();
		listIncR1.setList(List.of(incidentR1));
		listIncR1.setTotal(1L);

		IncidentBS bsMock = mock(IncidentBS.class);
		doCallRealMethod().when(bsMock).listIncidentsByRisk(risk1);
		when(bsMock.listIncidentsByRisk(risk1, -1)).thenReturn(listIncR1);

		PaginatedList<Incident> returnedIncidentList = bsMock.listIncidentsByRisk(risk1);

		assertTrue(returnedIncidentList.getList().contains(incidentR1), "A lista retornada deveria conter o incidente.");
		assertEquals(listIncR1.getTotal(), returnedIncidentList.getTotal(), "O tamanho da lista não é o esperado.");
	}

	@DisplayName("Deve retornar os incidentes a partir de um risco e ano.")
	@Test
	void listIncidentsByRiskAndYear() {
		Risk risk = new Risk();
		risk.setId(1L);
		risk.setBegin(new Date());
		Incident incidentR1 = new Incident();
		incidentR1.setId(1L);
		incidentR1.setRisk(risk);
		Incident incident2R1 = new Incident();
		incident2R1.setId(2L);
		incident2R1.setRisk(risk);

		List<Incident> listOfIncidents = new ArrayList<>(List.of(incidentR1, incident2R1));

		CriteriaBuilder builder = mock(CriteriaBuilder.class);
		CriteriaQuery<Incident> cq = mock(CriteriaQuery.class);
		Root<Incident> root = mock(Root.class);
		TypedQuery<Incident> tp = mock(TypedQuery.class);

		when(entityManager.getCriteriaBuilder()).thenReturn(builder);
		when(builder.createQuery(Incident.class)).thenReturn(cq);
		when(cq.from(Incident.class)).thenReturn(root);
		when(entityManager.createQuery(cq)).thenReturn(tp);
		when(tp.getResultList()).thenReturn(listOfIncidents);

		PaginatedList<Incident> returnedList = bs.listIncidentsByRisk(risk, 2024);

		assertTrue(returnedList.getList().contains(incidentR1), "A lista retornada deve conter o incidente 1");
		assertTrue(returnedList.getList().contains(incident2R1), "A lista retornada deve conter o incidente 2");
		assertEquals(listOfIncidents.size(), returnedList.getTotal(), "O tamanho da lista não é o esperado.");
	}

	@DisplayName("Deve retornar os incidentes a partir de várias unidades e uma data limite.")
	@Test
	void testListIncidentsByUnitsAndDateLimit() {
		Unit unit1 = new Unit();
		unit1.setId(1L);
		Incident incidentU1 = new Incident();
		incidentU1.setUnitId(unit1.getId());
		incidentU1.setBegin(new Date());

		Unit unit2 = new Unit();
		unit2.setId(2L);
		Incident incidentU2 = new Incident();
		incidentU2.setUnitId(unit2.getId());
		incidentU2.setBegin(new Date());

		List<Unit> listOfUnits = new ArrayList<>(List.of(unit1, unit2));
		List<Incident> listOfIncidents = new ArrayList<>(List.of(incidentU1, incidentU2));

		Calendar dateLimite = Calendar.getInstance();
		dateLimite.set(2025, Calendar.DECEMBER, 31);

		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Incident.class)).thenReturn(listOfIncidents);

		List<Incident> returnedList = bs.listIncidentsByUnitsAndDateLimit(listOfUnits, dateLimite.getTime());

		assertTrue(returnedList.contains(incidentU1), "O incidente 1 deveria estar no retorno");
		assertTrue(returnedList.contains(incidentU2), "O incidente 2 deveria estar no retorno");
		assertEquals(listOfIncidents.size(), returnedList.size(),
			"A lista retornada deveria corresponder ao esperado.");
	}

	@DisplayName("Deve retornar os incidentes a partir do plano de risco.")
	@Test
	void testListIncidentsByPlanRisk() {
		PlanRisk planRisk = new PlanRisk();
		planRisk.setId(2L);

		IncidentBean incident1 = new IncidentBean();
		incident1.setId(1L);
		IncidentBean incident2 = new IncidentBean();
		incident2.setId(3L);

		List<IncidentBean> listIncidents = new ArrayList<>(List.of(incident1, incident2));

		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.setProjection(any(ProjectionList.class))).thenReturn(criteria);
		when(criteria.setResultTransformer(IncidentBean.class)).thenReturn(criteria);
		when(dao.findByCriteria(criteria, IncidentBean.class)).thenReturn(listIncidents);

		List<IncidentBean> returnedList = bs.listIncidentsByPlanRisk(planRisk);

		assertEquals(listIncidents, returnedList, "A lista retornada deveria ser igual a esperada.");
	}

	@DisplayName("Deve retornar os incidentes a partir de um risco com filtro.")
	@Test
	void testListIncidentsByRiskAndAllParams() {
		Unit unit = new Unit();
		unit.setId(5L);

		Risk riskToListIncidents = new Risk();
		riskToListIncidents.setId(1L);
		riskToListIncidents.setUnit(unit);

		DefaultParams params = DefaultParams.createWithMaxPageSize();
		params.setTerm("Ação");
		params.setSortedBy(new String[]{"name", "asc"});

		Incident incident1 = new Incident();
		incident1.setRisk(riskToListIncidents);
		incident1.setAction("Incidente 1");
		incident1.setId(1L);

		Incident incident2 = new Incident();
		incident2.setRisk(riskToListIncidents);
		incident2.setAction("incidente 2");
		incident2.setId(2L);

		List<Incident> expectedList =
			new ArrayList<>(List.of(incident1, incident2));

		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Incident.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<Incident> returnedList = bs.listIncidentsByRisk(riskToListIncidents, params);

		assertEquals(expectedList, returnedList.getList(),
			"A lista retornada com incidentes não corresponde à esperada.");
		assertEquals(expectedList.size(), returnedList.getTotal(), "O número de incidentes não é o esperado.");
		verify(criteria, times(6)).add(any());
		verify(criteria).addOrder(any());
	}

	@DisplayName("Deve retornar os incidentes a partir de um risco com filtro. Caso filtro sem o termo e ordenação.")
	@Test
	void testListIncidentsByRiskAndAllParamsWithoutTermAndSort() {
		Unit unit = new Unit();
		unit.setId(5L);

		Risk riskToListIncidents = new Risk();
		riskToListIncidents.setId(1L);
		riskToListIncidents.setUnit(unit);

		DefaultParams params = DefaultParams.createWithMaxPageSize();

		Incident incident1 = new Incident();
		incident1.setRisk(riskToListIncidents);
		incident1.setAction("Incidente 1");
		incident1.setId(1L);

		Incident incident2 = new Incident();
		incident2.setRisk(riskToListIncidents);
		incident2.setAction("incidente 2");
		incident2.setId(2L);

		List<Incident> expectedList =
			new ArrayList<>(List.of(incident1, incident2));

		when(dao.newCriteria(Incident.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.createAlias(anyString(), anyString(), any(JoinType.class))).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Incident.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<Incident> returnedList = bs.listIncidentsByRisk(riskToListIncidents, params);

		assertEquals(expectedList, returnedList.getList(),
			"A lista retornada com incidentes não corresponde à esperada.");
		assertEquals(expectedList.size(), returnedList.getTotal(), "O número de incidentes não é o esperado.");
		verify(criteria, times(4)).add(any());
		verify(criteria, times(0)).addOrder(any());
	}
}