package org.forpdi.core.location;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CountyTest {


	@Test
	void create_county_with_valid_data() {
		String countyName = "Test County";
		UF uf = new UF();
		uf.setName("Test UF");
		uf.setAcronym("TU");

		County county = new County();

		county.setName(countyName);
		county.setUf(uf);

		assertEquals(countyName, county.getName());
		assertEquals(uf, county.getUf());
	}

/*
	@Test
	void create_county_with_null_name() {
		County county = new County();
		UF uf = new UF();

		assertThrows(IllegalArgumentException.class, () -> {
			county.setName(null);
			county.setUf(uf);
		});
	}
*/

}