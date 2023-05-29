package com;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
//@RequestMapping("/")
public class HomeController {

	@GetMapping
	public ModelAndView redirect(ModelMap model) {
		return new ModelAndView("forward:/index.html", model);
	}
	
}
