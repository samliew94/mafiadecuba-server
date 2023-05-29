package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameLogRepository extends JpaRepository<GameLog, Integer>{

	List<GameLog> findAllByOrderByGameLogIdAsc();
	
}
