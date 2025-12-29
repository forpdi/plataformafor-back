package org.forpdi.core.location;

import org.forpdi.core.bean.ListWrapper;
import org.forpdi.core.common.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationController extends AbstractController {

	private static final String PATH = BASEPATH + "/location";
	
	@Autowired
	private UFRepository ufRepository;
	
	@Autowired
	private CountyRepository countyRepository;

	@GetMapping(PATH + "/ufs")
	public ResponseEntity<?> listUfs() {
		try {
			var wrapper = new ListWrapper<>(ufRepository.findAll(Sort.by("name")));
			return this.success(wrapper);
		} catch (Throwable ex){
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}

	@GetMapping(PATH + "/counties/{ufId}")
	public ResponseEntity<?> listCounties(@PathVariable long ufId) {
		try {
			var wrapper = new ListWrapper<>(countyRepository.findByUfIdOrderByName(ufId));
			return this.success(wrapper);
		} catch (Throwable ex){
			LOGGER.error("Unexpected runtime error", ex);
			return this.fail(ex.getMessage());
		}
	}
}
