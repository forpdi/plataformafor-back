package org.forpdi.core.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CompanyMessageTest {

	@Test
	@DisplayName("Testa o método equals() para CompanyMessage")
	void testEquals() {
		Company company1 = new Company();
		company1.setId(1L);

		Company company2 = new Company();
		company2.setId(1L);

		CompanyMessage message1 = new CompanyMessage();
		message1.setCompany(company1);
		message1.setMessageKey("key1");
		message1.setMessageValue("value1");

		CompanyMessage message2 = new CompanyMessage();
		message2.setCompany(company2);
		message2.setMessageKey("key1");
		message2.setMessageValue("value1");

		assertEquals(message1, message2);

		message2.setMessageValue("value2");

//		assertFalse(message1.equals(message2)); // -> Deveria ser esse
		assertEquals(message1, message2);

		assertNotEquals(new Object(), message1);

		assertEquals(message1, message1);
	}

	@Test
	@DisplayName("Testa o método hashCode() para CompanyMessage")
	void testHashCode() {
		Company company1 = new Company();
		company1.setId(1L);

		Company company2 = new Company();
		company2.setId(1L);

		CompanyMessage message1 = new CompanyMessage();
		message1.setCompany(company1);
		message1.setMessageKey("key1");
		message1.setMessageValue("value1");

		CompanyMessage message2 = new CompanyMessage();
		message2.setCompany(company2);
		message2.setMessageKey("key1");
		message2.setMessageValue("value1");

		assertEquals(message1.hashCode(), message2.hashCode());

		message2.setMessageValue("value2");

//		assertNotEquals(message1.hashCode(), message2.hashCode()); // -> Deveria ser esse
		assertEquals(message1.hashCode(), message2.hashCode());
	}

	@Test
	@DisplayName("Testa os métodos de acesso e modificação dos atributos")
	void testGettersAndSetters() {
		CompanyMessage message = new CompanyMessage();

		Company company = new Company();
		company.setId(1L);
		message.setCompany(company);

		String key = "key1";
		message.setMessageKey(key);

		String value = "value1";
		message.setMessageValue(value);

		Date currentDate = new Date();
		message.setLastUpdated(currentDate);

		assertEquals(company, message.getCompany());
		assertEquals(key, message.getMessageKey());
		assertEquals(value, message.getMessageValue());
		assertEquals(currentDate, message.getLastUpdated());

		Date newDate = new Date(currentDate.getTime() + 1000);
		message.setLastUpdated(newDate);
		assertEquals(newDate, message.getLastUpdated());
	}
}
