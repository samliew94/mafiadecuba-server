package com;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class UserRoleService {
	
	@Autowired
	UserRolesRepository userRolesRepository;
	
	@Autowired
	RefRolesRepository refRolesRepository;
	
	@Autowired
	MyUserRepository userRepository;
	
	/**
	 * wipe userRolesRepository.<br/>
	 * initialize userRolesRepository populating all users.<br/>
	 * if that user happens to be host, he/she becomes Godfather by default<br/>
	 */
	public void reset() {
		
		userRolesRepository.deleteAll();
		
	}
	
	/**
	 * host by default is the godfather
	 */
	public void init() {
		userRepository.findAll().forEach(x->{
			
			insert(x, x.isHost() ? GameRoles.GODFATHER : null);
			
		});

	}
	
	public void insert(MyUser user, GameRoles gameRole) {
		
		UserRoles userRoles = UserRoles.builder()
			.user(user)
			.refRole(gameRole == null ? null : refRolesRepository.findByRoleCode(gameRole.getValue()))
			.build()
			;
		
		userRolesRepository.save(userRoles);
	}
	
	public UserRoles findGodfather() {
		
		return userRolesRepository.findByRefRole(refRolesRepository.findByRoleCode(GameRoles.GODFATHER.getValue()));
		
	}


	public void changeGodfather(String username) {
		
		UserRoles userRoleA = userRolesRepository.findByRefRole(refRolesRepository.findByRoleCode(GameRoles.GODFATHER.getValue()));
		UserRoles userRoleB = userRolesRepository.findByUser(userRepository.findByUsername(username));
		
		MyUser userA = userRoleA.getUser();
		MyUser userB = userRoleB.getUser();
		
		userRoleA.setUser(userB);
		userRoleB.setUser(userA);
		
		userRolesRepository.save(userRoleA);
		userRolesRepository.save(userRoleB);
		
		List<UserRoles> findAll = userRolesRepository.findAll();
		
		int a = 0;
		
	}


	public UserRoles findByUser(MyUser user) {
		// TODO Auto-generated method stub
		return userRolesRepository.findByUser(user);
	}

	public UserRoles findByRole(GameRoles role) {
		return userRolesRepository.findByRefRole(refRolesRepository.findByRoleCode(role.getValue()));
	}
	
	public void updateUserRole(UserRoles userRole, GameRoles role) {
		// TODO Auto-generated method stub
		userRole.setRefRole(refRolesRepository.findByRoleCode(role.getValue()));
		userRolesRepository.save(userRole);
	}

	public void updateUserRole(MyUser user, GameRoles thief) {
		
		UserRoles userRole = userRolesRepository.findByUser(user);
		userRole.setRefRole(refRolesRepository.findByRoleCode(thief.getValue()));
		userRolesRepository.save(userRole);
		
	}

	/**
	 * @return true if user's role matches with ANY of GameRole(s)
	 */
	public boolean compare(MyUser user, GameRoles ...roles ) {
		
		UserRoles userRole = userRolesRepository.findByUser(user);
		
		for (GameRoles role : roles)
			if (userRole.getRefRole().getRoleCode().equalsIgnoreCase(role.getValue()))
				return true;
		
		return false;
		
		
	}

	public List<UserRoles> findAll() {
		// TODO Auto-generated method stub
		return userRolesRepository.findAll();
	}
	
	public List<UserRoles> findAllByGameRoles(GameRoles ...gameRole) {
		
		List<String> list = Arrays.asList(gameRole).stream().map(x->x.getValue()).collect(Collectors.toList());
		
		List<RefRole> refRoles = refRolesRepository.findAllByRoleCodeIn(list);
		
		return userRolesRepository.findAllByRefRoleIn(refRoles);
		
	}

	
}