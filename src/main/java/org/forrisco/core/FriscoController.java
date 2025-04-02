package org.forrisco.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("frisco")
public class FriscoController {
	
	@GetMapping
	public String test() {
		return "frisco";
	}
}
