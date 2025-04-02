package org.forpdi.core.communication;

import org.forpdi.core.common.*;
import org.hibernate.criterion.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunicationBSTest {
	@InjectMocks
	CommunicationBS bs;

	@Mock
	HibernateDAO dao;

	@Mock
	Criteria criteria;

	@Test
	@DisplayName("Deve salvar um novo comunicado e o retorna.")
	void testSaveNewCommunication() {
		Communication commToSave = new Communication();
		commToSave.setId(1L);
		commToSave.setTitle("Title");
		commToSave.setMessage("Message");

		Communication returnedComm = bs.saveNewCommunication(commToSave);

		assertEquals(commToSave, returnedComm, "A comunicado retornado deveria ser igual ao salvo.");
	}

	@Test
	@DisplayName("Deve consultar um comunicado pelo seu Id.")
	void testRetrieveCommunicationById() {
		Communication commExistent = new Communication();
		commExistent.setId(3L);
		commExistent.setTitle("Existent Comm");
		commExistent.setMessage("Existent Message.");


		when(dao.newCriteria(Communication.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(commExistent);

		Communication returnedComm = bs.retrieveCommunicationById(3L);

		assertEquals(commExistent, returnedComm, "O comunicado retornado não é o esperado");
	}

	@Test
	@DisplayName("Deve retornar uma lista de comunicados de acordo com a página e tamanho da página.")
	void testListCommunicationsWithPagesValuesValid() {

		Communication comm1 = new Communication();
		comm1.setId(1L);
		Communication comm2 = new Communication();
		comm2.setId(6L);
		List<Communication> expectedList = new ArrayList<>(List.of(comm1, comm2));

		when(dao.newCriteria(Communication.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Communication.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<Communication> returnedPaginatedList = bs.listCommunications(1, 5);

		assertEquals(expectedList, returnedPaginatedList.getList(),
			"A lista de comunicados retornada não corresponde a esperada.");
		assertEquals(expectedList.size(), (long) returnedPaginatedList.getTotal(),
			"A quantidade de elementos retornada não é a esperada.");
	}

	@Test
	@DisplayName("Deve retornar uma lista de comunicados de acordo com a página e tamanho da página.")
	void testListCommunicationsWithPagesValuesInvalid() {

		Communication comm1 = new Communication();
		comm1.setId(1L);
		Communication comm2 = new Communication();
		comm2.setId(6L);
		List<Communication> expectedList = new ArrayList<>(List.of(comm1, comm2));

		when(dao.newCriteria(Communication.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(criteria.setProjection(any(Projection.class))).thenReturn(criteria);
		when(dao.findByCriteria(criteria, Communication.class)).thenReturn(expectedList);
		when(criteria.uniqueResult()).thenReturn((long) expectedList.size());

		PaginatedList<Communication> returnedPaginatedList = bs.listCommunications(null, null);

		assertEquals(expectedList, returnedPaginatedList.getList(),
			"A lista de comunicados retornada não corresponde a esperada.");
		assertEquals(expectedList.size(), (long) returnedPaginatedList.getTotal(),
			"A quantidade de elementos retornada não é a esperada.");
	}

	@Test
	@DisplayName("Deve retornar uma instância do objeto Communication pela sua data de início.")
	void testRetrieveCommunicationByValidity() {

		Calendar calendar = Calendar.getInstance();

		Communication comm1 = new Communication();
		comm1.setId(1L);
		comm1.setTitle("Merry Christmas and Happy Holidays!");
		comm1.setMessage("Valid message");

		calendar.set(2024, Calendar.NOVEMBER, 11);
		comm1.setValidityBegin(calendar.getTime());
		calendar.set(2025, Calendar.JANUARY, 10);
		comm1.setValidityEnd(calendar.getTime());

		Communication comm2 = new Communication();
		comm2.setId(2L);
		calendar.set(2024, Calendar.SEPTEMBER, 10);
		comm2.setValidityBegin(calendar.getTime());
		calendar.set(2024, Calendar.OCTOBER, 10);

		when(dao.newCriteria(Communication.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(comm1);

		Communication returnedComm = bs.retrieveCommunicationByValidity(comm1.getValidityBegin());

		assertEquals(comm1, returnedComm, "O comunicado retornado não corresponde ao esperado.");
		assertNotEquals(comm2, returnedComm,
			"O comunicado retornado não deve corresponder a outro de validade diferente. ");
	}

	@Test
	@DisplayName("Deve retornar o comunicado ativo no momento da consulta.")
	void testRetrieveCommunicationByValidityBegin() {

		Calendar calendar = Calendar.getInstance();

		Communication comm1 = new Communication();
		comm1.setId(1L);
		comm1.setTitle("Merry Christmas and Happy Holidays!");
		comm1.setMessage("Valid message");

		calendar.set(2024, Calendar.NOVEMBER, 11);
		comm1.setValidityBegin(calendar.getTime());
		calendar.set(2025, Calendar.JANUARY, 10);
		comm1.setValidityEnd(calendar.getTime());

		when(dao.newCriteria(Communication.class)).thenReturn(criteria);
		when(criteria.add(any(Criterion.class))).thenReturn(criteria);
		when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
		when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
		when(criteria.uniqueResult()).thenReturn(comm1);

		Communication returnedComm = bs.retrieveCommunicationByValidity();

		assertEquals(comm1, returnedComm, "O comunicado retornado não corresponde ao esperado.");
	}

	@Test
	@DisplayName("Deve salvar um novo comunicado e o retorna.")
	void testEndCommunication() {
		Communication commToEnd = new Communication();
		commToEnd.setId(1L);
		commToEnd.setTitle("Communication to finalize");
		commToEnd.setMessage("Message");
		commToEnd.setShowPopup(true);

		bs.endCommunication(commToEnd);

		verify(dao).persist(commToEnd);
		assertFalse(commToEnd.getShowPopup(), "O comunicado encerrado não deve exibir o pop-up.");
	}
}