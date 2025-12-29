package org.forpdi.dashboard.indicators;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExportFiltersDtoTest {

	@Test
	void test_constructor_creates_dto_with_valid_parameters() {
		long regionId = 1L;
		long typeId = 2L;
		List<Long> companyIds = List.of(3L, 4L);
		String beginDate = "01/01/2023";
		String endDate = "31/12/2023";

		ExportFiltersDto dto = new ExportFiltersDto(regionId, typeId, companyIds, beginDate, endDate);

		assertEquals(regionId, dto.regionId());
		assertEquals(typeId, dto.typeId());
		assertEquals(companyIds, dto.companyIds());
		assertEquals(beginDate, dto.companyCreationBegin());
		assertEquals(endDate, dto.companyCreationEnd());
	}

	@Test
	void test_company_creation_begin_as_date_returns_null_for_null_input() {
		ExportFiltersDto dto = new ExportFiltersDto(1L, 1L, List.of(), null, "31/12/2023");

		Date result = dto.companyCreationBeginAsDate();

		assertNull(result);
	}
}