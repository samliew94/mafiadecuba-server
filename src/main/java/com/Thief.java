package com;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Thief {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int thiefId;
	
	@JoinColumn(name = "username")
	@ManyToOne
	private MyUser user;
	
	private int diamondsStolen;
	
	private boolean isCaught;
	
}

interface ThiefRepository extends JpaRepository<Thief, Integer>{
	
	Thief findByUser(MyUser user);
	List<Thief> findAllByIsCaughtOrderByDiamondsStolenDesc(boolean isCaught);
	
}

@Service
class ThiefService {
	
	@Autowired
	ThiefRepository thiefRepository;
	
	public void reset() {
		 thiefRepository.deleteAll();
	}
	
	public void insert(MyUser user, int diamondsStolen) {
		
		Thief build = Thief.builder()
			.user(user)
			.diamondsStolen(diamondsStolen)
			.build();
		
		thiefRepository.save(build);
		
	}

	public Thief findByUser(MyUser targetUser) {
		return thiefRepository.findByUser(targetUser);
	}
	
	public List<Thief> findBestThieves(){
		
		List<Thief> thieves = thiefRepository.findAllByIsCaughtOrderByDiamondsStolenDesc(false);
		
		int highestScore = thieves.get(0).getDiamondsStolen();
		
		thieves.removeIf(x->x.isCaught() || x.getDiamondsStolen() != highestScore);
		
		return thieves;
		
	}

	public void caught(MyUser accused) {
		// TODO Auto-generated method stub
		Thief thief = findByUser(accused);
		thief.setCaught(true);
	}
	
}