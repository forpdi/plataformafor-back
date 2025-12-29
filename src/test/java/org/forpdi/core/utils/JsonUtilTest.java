package org.forpdi.core.utils;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

	static class Person {
		String name;
		int age;

		Person(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}

	@Test
	@DisplayName("Deve converter JSON para objeto com classe especificada")
	void testFromJsonWithClass() {
		String json = "{\"name\":\"Test\",\"age\":30}";

		Person person = JsonUtil.fromJson(json, Person.class);

		assertNotNull(person);
		assertEquals("Test", person.name);
		assertEquals(30, person.age);
	}

	@Test
	@DisplayName("Deve converter JSON para objeto com tipo genérico especificado")
	void testFromJsonWithType() {
		String json = "{\"key\":\"value\",\"number\":42}";
		Type type = new TypeToken<Map<String, Object>>() {}.getType();

		Map<String, Object> map = JsonUtil.fromJson(json, type);

		assertNotNull(map);
		assertEquals("value", map.get("key"));
		assertEquals(42.0, map.get("number")); 
	}

	@Test
	@DisplayName("Deve converter objeto para JSON")
	void testToJson() {

		Person person = new Person("Test", 30);

		String json = JsonUtil.toJson(person);

		assertNotNull(json);
		assertTrue(json.contains("\"name\":\"Test\""));
		assertTrue(json.contains("\"age\":30"));
	}

	@Test
	@DisplayName("Deve clonar um objeto JSON corretamente")
	void testJsonClone() {

		Person original = new Person("Original", 25);

		Person cloned = JsonUtil.jsonClone(original, Person.class);

		assertNotNull(cloned);
		assertEquals(original.name, cloned.name);
		assertEquals(original.age, cloned.age);
		assertNotSame(original, cloned);
	}
}
