package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccusationRepository extends JpaRepository<Accusation, Integer>{
	
	Accusation findFirstByOrderByAccusationIdDesc();
	List<Accusation> findAllByOrderByAccusationIdAsc();
	
}

