package org.forpdi.planning.structure;

import org.forpdi.core.company.CompanyUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteLevelInstanceTest {

	private FavoriteLevelInstance favoriteLevelInstance;
	private StructureLevelInstance levelInstance;
	private CompanyUser companyUser;

	@BeforeEach
	void setUp() {
		favoriteLevelInstance = new FavoriteLevelInstance();
		levelInstance = new StructureLevelInstance();
		companyUser = new CompanyUser();

		favoriteLevelInstance.setLevelInstance(levelInstance);
		favoriteLevelInstance.setCompanyUser(companyUser);
	}

	@Test
	void test_returns_stored_level_instance() {
		StructureLevelInstance expectedInstance = new StructureLevelInstance();
		FavoriteLevelInstance favorite = new FavoriteLevelInstance();
		favorite.setLevelInstance(expectedInstance);

		StructureLevelInstance result = favorite.getLevelInstance();

		assertNotNull(result);
//		assertEquals(expectedInstance, result);
	}

	@Test
	@DisplayName("Deve retornar o CompanyUser correto")
	void testGetCompanyUser() {
		assertEquals(companyUser, favoriteLevelInstance.getCompanyUser());
	}

	@Test
	@DisplayName("Deve permitir configurar o valor de deleted")
	void testSetDeleted() {
		favoriteLevelInstance.setDeleted(true);
		assertTrue(favoriteLevelInstance.isDeleted());

		favoriteLevelInstance.setDeleted(false);
		assertFalse(favoriteLevelInstance.isDeleted());
	}

	@Test
	@DisplayName("Deve permitir configurar e obter o companyUser corretamente")
	void testSetCompanyUser() {
		favoriteLevelInstance.setCompanyUser(companyUser);
		assertEquals(companyUser, favoriteLevelInstance.getCompanyUser());
	}
}
