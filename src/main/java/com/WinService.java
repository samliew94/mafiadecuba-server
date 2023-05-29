package com;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
class UserWin {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userWinId;
	
	@JoinColumn(name = "username")
	@ManyToOne
	private MyUser user;
	
	private boolean isWin;
	
}

interface UserWinRepository extends JpaRepository<UserWin, Integer>{

	List<UserWin> findAllByUserIn(MyUser ... users);
	List<UserWin> findAllByIsWin(boolean isWin);
	UserWin findByUser(MyUser user);
}


@Service
public class WinService {

	@Autowired
	UserWinRepository userWinRepository;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	MyUserRepository userRepository;
	
	@Autowired
	RefRoleService refRoleService;
	
	@Autowired
	ThiefService thiefService;
	
	public void reset() {
		
		userWinRepository.deleteAll();
		
	}
	
	public void init() {
		
		for (MyUser user : userRepository.findAll()) {
			
			UserWin build = UserWin.builder()
				.isWin(false)
				.user(user)
				.build();
			
			userWinRepository.save(build);
			
		}
	}

	private void driverWins() {
		
		Set<MyUser> winners = userWinRepository.findAllByIsWin(true).stream().map(x->x.getUser()).collect(Collectors.toSet());
		
		// check if drivers win
		List<MyUser> users = userRepository.findAllByOrderBySeatOrderAsc();
		
		for (int i = 1; i < users.size(); i++) {
			
			MyUser user = users.get(i-1);
			MyUser user2 = users.get(i);
			
			UserRoles userRole = userRoleService.findByUser(user2);
			
			if (userRole.getRefRole().getRoleCode() == GameRoles.DRIVER.getValue()) {
				
				if (winners.contains(user)) {
					UserWin userWin = userWinRepository.findByUser(user2);
					userWin.setWin(true);
					userWinRepository.save(userWin);
				}
				
			}
			
		}
	}
	
	private void winByGameRoles(GameRoles ... gameRoles){
		
		List<MyUser> users = userRoleService.findAllByGameRoles(gameRoles)
				.stream().map(x->x.getUser()).collect(Collectors.toList());
		
		winByUser(users.toArray(MyUser[]::new));

	}
	
	private void winByUser(MyUser ... users) {
		
		List<UserWin> userWins = userWinRepository.findAllByUserIn(users);
		
		userWins.forEach(x->{
			x.setWin(true);
		});
		
		userWinRepository.saveAll(userWins);
		
	}
	
	public void godfatherAndKakiaWins() {
		winByGameRoles(GameRoles.GODFATHER, GameRoles.KAKIA);
		driverWins();
	}
	
	public void mataWins(MyUser user) {
		winByUser(user);
		driverWins();
	}
	
	public void thiefWins() {
		List<Thief> thieves = thiefService.findBestThieves();
		List<MyUser> users = thieves.stream().map(x->x.getUser()).collect(Collectors.toList());
		winByUser(users.toArray(MyUser[]::new));
		winByGameRoles(GameRoles.STREET_THIEF);
		driverWins();
	}
	
	public boolean findIsWinByUser(MyUser user) {
		
		return userWinRepository.findByUser(user).isWin();
		
	}
	
}

