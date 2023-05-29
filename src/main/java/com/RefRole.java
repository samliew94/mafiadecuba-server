package com;

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
public class RefRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int refRoleId;
	
	private String roleCode;
	private String roleName;
	private String roleTextColor;
	
}


