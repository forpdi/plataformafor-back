package org.forpdi.core.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegionTest {


    @Test
    @DisplayName("Deve criar uma Region com nome válido")
    void test_create_region_with_valid_name() {
        String regionName = "Nordeste";
    
        Region region = new Region();
        region.setName(regionName);
    
        assertNotNull(region, "O objeto Region não deve ser nulo");
        assertEquals(regionName, region.getName(), "O nome da região deve ser igual ao fornecido");
        assertNotNull(region.getName(), "O nome da região não deve ser nulo");
    }
}
