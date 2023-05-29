package com;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("user")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController{
	
	@Autowired
	AuthenticationManager authManager;
	
	@Autowired
	MyUserRepository userRepository;
	
	@Autowired
	PasswordEncoder encoder;
	
	@PostMapping("login")
	public ResponseEntity<?> login(@RequestBody Map map, HttpServletRequest request) throws Exception{
		
		System.err.println("user/login");
		
		String username = (String) map.get("username");
		
		MyUser user = userRepository.findByUsername(username.trim().toUpperCase());
		
		if (user != null)
			return ResponseEntity.badRequest().body("Username already exist!");
		
		user = MyUser.builder()
				.username(username)
				.isHost(userRepository.count() == 0L)
				.seatOrder((int) userRepository.count())
				.build();
			
		userRepository.save(user);
			
		request.login(username, "");
		
		String msg = "login successful principal="+username;
		System.err.println(msg);
		
		return ResponseEntity.ok("login successful");
	}
	
	@PostMapping("logout")
	public void logout(HttpServletRequest request) throws ServletException {
		
		if (request.getUserPrincipal() != null)
			userRepository.delete(userRepository.findByUsername(request.getUserPrincipal().getName()));
		
		request.logout();
		
		System.err.println("logout successful");
	}
	
	@GetMapping("principal")
	public ResponseEntity<?> principal(Principal principal, HttpServletRequest request){
		
		System.err.println("user/principal");
		
		try {
			MyUser user = userRepository.findByUsername(principal.getName());
			if (user == null) {
				request.logout();
				String errorMessage = "user was not found in repository but is authenticated, performing automatic logout";
				System.err.println(errorMessage);
				return ResponseEntity.status(401).body(errorMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ResponseEntity.ok(principal == null ? "principal == null" : "principal != null ("+principal.getName()+")");
	}
	
}