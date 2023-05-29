package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnDetailsRepository extends JpaRepository<TurnDetails, Integer>{

	TurnDetails findFirstByOrderByTurnDetailsIdAsc();
	TurnDetails findFirstByIsCompletedOrderByTurnDetailsIdAsc(boolean isCompleted);
	List<TurnDetails> findAllByOrderByTurnDetailsIdAsc(); 

	int countByIsCompleted(boolean isCompleted);
	
}