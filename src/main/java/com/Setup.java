package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Setup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int setupId;
	private int numPlayers;
	private int numKakia;
	private int numMata;
	private int numDriver;
	private int numJoker;
	
}

@Repository
interface SetupRepository extends JpaRepository<Setup, Integer>{

	Setup findByNumPlayers(int count);
	
}

@Service
class SetupService {
	
	@Autowired
	MyUserRepository userRepository;
	
	@Autowired
	SetupRepository setupRepository;
	
	/**
	 * 
	 */
	@PostConstruct
	void initSetup() {
		
		setupRepository.save(Setup.builder().numPlayers(2).numKakia(1).numMata(1).numDriver(1).numJoker(1).build()); // debug mode
		setupRepository.save(Setup.builder().numPlayers(3).numKakia(1).numMata(1).numDriver(1).numJoker(2).build()); // debug mode
		setupRepository.save(Setup.builder().numPlayers(4).numKakia(1).numMata(1).numDriver(1).numJoker(2).build()); // debug mode
		
		
		setupRepository.save(Setup.builder().numPlayers(6).numKakia(1).numMata(1).numDriver(1).numJoker(0).build());
		setupRepository.save(Setup.builder().numPlayers(7).numKakia(2).numMata(1).numDriver(1).numJoker(0).build());
		setupRepository.save(Setup.builder().numPlayers(8).numKakia(3).numMata(1).numDriver(1).numJoker(1).build());
		setupRepository.save(Setup.builder().numPlayers(9).numKakia(4).numMata(1).numDriver(1).numJoker(1).build());
		setupRepository.save(Setup.builder().numPlayers(10).numKakia(4).numMata(2).numDriver(1).numJoker(1).build());
		setupRepository.save(Setup.builder().numPlayers(11).numKakia(4).numMata(2).numDriver(2).numJoker(2).build());
		setupRepository.save(Setup.builder().numPlayers(12).numKakia(5).numMata(2).numDriver(2).numJoker(2).build());
		
		
		System.err.println("added " + setupRepository.count() + " setup configs");
		
		setupRepository.findAll().forEach(x->{
			
			try {
				System.err.println(new ObjectMapper().writeValueAsString(x));;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		
	}
	
}



