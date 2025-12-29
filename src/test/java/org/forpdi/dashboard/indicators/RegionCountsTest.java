package org.forpdi.dashboard.indicators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionCountsTest {

	@Test
	void testConstructorAndGetters() {
		Long regionId = 1L;
		String regionName = "South Region";
		long universityCount = 5;
		long instituteCount = 3;
		long otherCount = 1;

		RegionCounts regionCounts = new RegionCounts(regionId, regionName, universityCount, instituteCount, otherCount);

		assertEquals(regionId, regionCounts.getRegionId());
		assertEquals(regionName, regionCounts.getRegionName());
		assertEquals(universityCount, regionCounts.getUniversityCount());
		assertEquals(instituteCount, regionCounts.getInstituteCount());
	}

	@Test
	void testSetters() {
		RegionCounts regionCounts = new RegionCounts(1L, "South Region", 5, 3, 1);

		regionCounts.setRegionId(2L);
		regionCounts.setRegionName("North Region");
		regionCounts.setUniversityCount(10);
		regionCounts.setInstituteCount(7);

		assertEquals(2L, regionCounts.getRegionId());
		assertEquals("North Region", regionCounts.getRegionName());
		assertEquals(10, regionCounts.getUniversityCount());
		assertEquals(7, regionCounts.getInstituteCount());
	}

	@Test
	void testGetCompaniesCount() {
		RegionCounts regionCounts = new RegionCounts(1L, "Central Region", 8, 4, 1);

		long companiesCount = regionCounts.getCompaniesCount();

		assertEquals(12, companiesCount);
	}
}
