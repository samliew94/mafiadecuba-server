package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface RefRolesRepository extends JpaRepository<RefRole, String>{
	
	RefRole findByRoleCode(String roleCode);

	List<RefRole> findAllByRoleCodeIn(List<String> roleCodes);
	
	List<RefRole> findAllByRoleCodeNotIn(List<String> roleCodes);
	
}