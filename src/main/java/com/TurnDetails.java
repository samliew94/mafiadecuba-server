package com;

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
public class TurnDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int turnDetailsId;
	
	/**
	 * default is null
	 */
	@JoinColumn(name = "username")
	@ManyToOne
	private MyUser user;
	
	/**
	 * default is false 
	 */
	private boolean isCompleted;
	
}

