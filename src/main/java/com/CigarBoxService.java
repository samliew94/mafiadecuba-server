package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
class CigarBoxService {
	
	@Autowired
	GameLogService gameLogService;
	
	@Autowired
	RefRoleService refRoleService;
	
	// how many diamonds are there initially?
	private int initialDiamonds;
	
	// how many diamonds are there left in the box?
	private int diamonds;
	
	// how many kakia tokens are there?
	private int numKakiaTokens;
	
	// how many mata tokens are there?
	private int numMataTokens;
	
	// how many driver tokens are there?
	private int numDriverTokens;

	
	public void init(int diamonds, int numKakiaTokens, int numMataTokens, int numDriverTokens) {
		
		this.initialDiamonds = diamonds;
		this.diamonds = diamonds;
		this.numKakiaTokens = numKakiaTokens;
		this.numMataTokens = numMataTokens;
		this.numDriverTokens = numDriverTokens;
		
	}

	public void removeToken(GameRoles gameRole) {
		
		if (gameRole == null) {
			gameLogService.insert(SecurityContextHolder.getContext().getAuthentication().getName() + " removed nothing");
			return;
		}
		
		if (gameRole == GameRoles.KAKIA)
			numKakiaTokens = 0;
		else if (gameRole == GameRoles.DRIVER)
			numDriverTokens = 0;
		
		RefRole refRole = refRoleService.findByGameRole(gameRole);
		
		gameLogService.insert(SecurityContextHolder.getContext().getAuthentication().getName() + " removed " + refRole.getRoleName());
		
	}
	
	public void deductToken(GameRoles gameRole) {
		
		if (gameRole == GameRoles.KAKIA)
			numKakiaTokens -= 1;
		else if (gameRole == GameRoles.MATA)
			numMataTokens -= 1;
		else if (gameRole == GameRoles.DRIVER)
			numDriverTokens -= 1;
		
	}
	
	public int getInitialDiamonds() {
		return initialDiamonds;
	}

	public int getDiamonds() {
		return diamonds;
	}

	public int getNumKakiaTokens() {
		return numKakiaTokens;
	}

	public int getNumMataTokens() {
		return numMataTokens;
	}

	public int getNumDriverTokens() {
		return numDriverTokens;
	}

	public void setDiamonds(int diamonds) {
		this.diamonds = diamonds;
	}

	
	
}