package com;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class AccusationService {
	
	@Autowired
	AccusationRepository accusationRepository;
	
	private MyUser curAccuse;
	
	public void reset() {
		
		accusationRepository.deleteAll();
		curAccuse = null;
		
	}
	
	public void insert(MyUser accused, String log) {
		
		Accusation accusation = Accusation.builder().user(accused).log(log).build();
		accusationRepository.save(accusation);
		
	}
	
	public Accusation findFirstOrderByAccusationIdDesc() {
		return accusationRepository.findFirstByOrderByAccusationIdDesc();
	}
	
	/**
	 * @return the previously accused players
	 */
	public Set<MyUser> previouslyAccused() {
		
		return accusationRepository.findAll().stream().map(x->x.getUser()).collect(Collectors.toSet());
		
	}
	
	public Set<String> previouslyAccusedUsernames() {
		
		return previouslyAccused().stream().map(x->x.getUsername()).collect(Collectors.toSet());
		
	}
	
	public MyUser getCurAccused() {
		return curAccuse;
	}
	
	public void setCurAccused(MyUser value) {
		curAccuse = value;
	}
	
}