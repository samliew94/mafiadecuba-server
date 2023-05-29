package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyUserRepository extends JpaRepository<MyUser, String>{

	MyUser findByUsername(String username);
	MyUser findByIsHost(boolean isHost);
	MyUser findBySeatOrder(int seatOrder);
	List<MyUser> findAllByOrderBySeatOrderAsc();
	
}


