package com;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("foo")
@RestController
public class FooController {

	@GetMapping("foobarbaz")
	public ResponseEntity fooBarBaz() {
		return ResponseEntity.ok("fooBarBaz is bazBarFoo");
	}
	
}
