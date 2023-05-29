package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class JokerService {
	
	int jokers;
	
	@Autowired
	SetupRepository setupRepository;
	
	@Autowired
	MyUserRepository userRepository;
	
	public void reset() {
		
		jokers = setupRepository.findByNumPlayers((int)userRepository.count()).getNumJoker();
		
	}
	
	public int remove() {
		
		jokers -= 1;
		
		return jokers;
	}
	
	public int getJokers() {
		return jokers;
	}
	
} 