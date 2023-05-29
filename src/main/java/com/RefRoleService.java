package com;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class RefRoleService {
	
	@Autowired
	RefRolesRepository refRolesRepository;
	
	@PostConstruct
	void initialization() {
		
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.GODFATHER.getValue()).roleName("Godfather").roleTextColor("text-white-500").build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.KAKIA.getValue()).roleName("Loyal Kakia").roleTextColor("text-white-500") .build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.MATA.getValue()).roleName("Mata").roleTextColor("text-blue-500").build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.DRIVER.getValue()).roleName("Driver").roleTextColor("text-green-500").build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.THIEF.getValue()).roleName("Thief").roleTextColor("text-red-500").build());
		refRolesRepository.save(RefRole.builder().roleCode(GameRoles.STREET_THIEF.getValue()).roleName("Street Thief").roleTextColor("text-red-500").build());
			
	}

	public RefRole findByGameRole(GameRoles gameRole) {
		// TODO Auto-generated method stub
		return refRolesRepository.findByRoleCode(gameRole.getValue());
	}

	public Map<String, Map> refRoleData() {
		
		List<RefRole> findAll = refRolesRepository.findAll();
		
		Map<String, Map> map = new HashMap<>();
		
		for(RefRole refRole : findAll) {
			
			Map roleMap = new HashMap<>();
			roleMap.put("roleName", refRole.getRoleName());
			roleMap.put("roleColor", refRole.getRoleTextColor());
			map.put(refRole.getRoleCode(), roleMap);
			
		}
		
		return map;
		
	}
	
	public List<RefRole> findAllByGameRolesIn(GameRoles ... gameRoles) {
		
		return refRolesRepository.findAllByRoleCodeIn(Arrays.asList(gameRoles).stream().map(x->x.getValue()).collect(Collectors.toList()));
	}
	
	public List<RefRole> findAllByRoleCodeNotIn(GameRoles ...gameRoles) {
		
		return refRolesRepository.findAllByRoleCodeNotIn(Arrays.asList(gameRoles).stream().map(x->x.getValue()).collect(Collectors.toList()));
	}

	
}