package org.forpdi.planning.structure;

import org.forpdi.core.common.HibernateDAO;
import org.forpdi.planning.attribute.AttributeInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class StructureInstanceAttributeHelperTest {

	@Mock
	private HibernateDAO dao;

	@InjectMocks
	private StructureInstanceAttributeHelper helper;

	private StructureLevelInstance goal;
	private AttributeInstance attributeInstance;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		goal = mock(StructureLevelInstance.class);
		attributeInstance = mock(AttributeInstance.class);

		when(goal.getId()).thenReturn(1L);
	}

	@Test
	void testListAllAttributeInstanceByLevelInstancesEmpty() {
		List<AttributeInstance> result = helper.listAllAttributeInstanceByLevelInstances(Collections.emptyList());

		assertTrue(result.isEmpty());
	}
}
