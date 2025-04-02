package org.forpdi.system.reports.pdf.table;

import java.io.File;

import org.forpdi.core.properties.SystemConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempFilesManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TempFilesManager.class);
	
	public static File getTempDir() {
		File outputDir;
		final String storagePath = SystemConfigs.getStorePdfs();
		if (storagePath == null || storagePath.equals("") || storagePath.equals("${store.pdfs}")) {
			throw new IllegalStateException("store.pdfs not especified!");
		} else {
			outputDir = new File(storagePath);
			if (!outputDir.exists()) {
				if (!outputDir.mkdirs()) {
					throw new RuntimeException("Failed to create storage directory.");
				}
			} else if (!outputDir.isDirectory()) {
				throw new RuntimeException("The configured storage path is not a directory.");
			}
		}
		return outputDir;
	}
	
	public static void cleanTempDir(File tempDir, String termId) {
		for (File f : tempDir.listFiles()) {
			if (f.getName().contains(termId)) {
				deleteTempFile(f);
			}
		}
	}
	
	public static void deleteTempFile(File file) {
		if (!file.delete()) {
			LOGGER.warn("Cannot delete a temp file: " + file.getAbsolutePath());
		}
	}
}
