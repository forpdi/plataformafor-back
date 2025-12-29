package org.forpdi.system.accesslog;

import org.forpdi.core.common.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessLogController extends AbstractController {
	private static final String PATH =  BASEPATH + "/access-log";
	
	@Autowired
	private AccessLogBS bs;
	
	@GetMapping(PATH + "/fpdi-access")
	public ResponseEntity<?> fpdiAccess() {
		try {
			bs.fpdiAccess(userSession.getUser());
		} catch (Throwable e) {
			LOGGER.error("Access log error: ", e);
		}
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping(PATH + "/frisco-access")
	public ResponseEntity<?> friscoAccess() {
		try {
			bs.friscoAccess(userSession.getUser());
		} catch (Throwable e) {
			LOGGER.error("Access log error: ", e);
		}
		return ResponseEntity.noContent().build();
	}
}
