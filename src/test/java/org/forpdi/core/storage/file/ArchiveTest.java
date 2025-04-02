package org.forpdi.core.storage.file;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.forpdi.core.company.Company;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Entity(name = Archive.TABLE)
@Table(name = Archive.TABLE)

class ArchiveTest {

	@Test
	void test_create_archive_with_valid_data() {
		String archiveName = "test.pdf";
		Company company = new Company();
		company.setName("Test Company");

		Archive archive = new Archive();
		archive.setName(archiveName);
		archive.setCompany(company);

		assertEquals(archiveName, archive.getName());
		assertEquals(company, archive.getCompany());
		assertEquals("pdf", archive.getExtension());
	}

/*
	@Test

	void test_create_archive_with_null_name_throws_exception() {
		Company company = new Company();
		company.setName("Test Company");
		Archive archive = new Archive();
		archive.setCompany(company);

		assertThrows(IllegalArgumentException.class, () -> {
			archive.setName(null);
		});
	}
 */

}