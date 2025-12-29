package org.forpdi.planning.structure.xml;

import org.forpdi.planning.attribute.AttributeTypeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttributeTypeMapTest {

	@Test
	@DisplayName("Deve retornar null para nomes inexistentes no mapa")
	void testGetInvalidName() {
		AttributeTypeFactory mockFactory = mock(AttributeTypeFactory.class);
		AttributeTypeFactory.getInstance();
		doNothing().when(mockFactory).each(any());

		AttributeTypeMap attributeTypeMap = new AttributeTypeMap();

		String result = attributeTypeMap.get("NonExistent");

		assertNull(result);
	}
}

