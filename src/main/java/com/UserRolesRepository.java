package com;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRolesRepository extends JpaRepository<UserRoles, Integer>{
	
	UserRoles findByUser(MyUser user);
	UserRoles findByRefRole(RefRole role);
	List<UserRoles>  findAllByRefRoleIn(List<RefRole> refRoles);
	
}