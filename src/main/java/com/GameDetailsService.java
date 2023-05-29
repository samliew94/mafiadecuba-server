package com;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings({"unchecked","rawtypes"})
@Service
public class GameDetailsService {
	
	@Autowired
	MyUserRepository userRepository;
	
	@Autowired
	TurnDetailsRepository turnDetailsRepository;
	
	@Autowired
	SetupRepository setupRepository;
	
	@Autowired
	CigarBoxService cigarBoxService;
	
	@Autowired
	GameLogService gameLogService;
	
	@Autowired
	UserRoleService userRoleService;
	
	@Autowired
	ThiefService thiefService;
	
	@Autowired
	GameLogRepository gameLogRepository;
	
	@Autowired
	AccusationService accusationService;
	
	@Autowired
	RefRoleService refRoleService;
	
	@Autowired
	JokerService jokerService;

	@Autowired
	WinService winService;
	
	Random random = new SecureRandom();

	/**
	 * wipe userRepository all gameData <br/>
	 * set host as the Godfather by default
	 */
	public void onToSettings(Principal principal) {
		
		if (principal == null || principal.getName().isBlank())
			return;
		
		MyUser host = userRepository.findByIsHost(true);
		
		if (!host.getUsername().equalsIgnoreCase(principal.getName()))
			return;
		
		userRoleService.init();
		winService.init();
	
	}
	
	public void reset() {
		
		userRoleService.reset();
		turnDetailsRepository.deleteAll();
		gameLogService.reset();
		thiefService.reset();
		accusationService.reset();
		jokerService.reset();
		winService.reset(); 
		
	}
	
	public void onChangeGodfather(String username) {
		
		userRoleService.changeGodfather(username);
		
	}
	
	/**
	 * all users except godfather sees "waiting for godfather..."<br/>
	 * godfather needs to set the initial diamond<br/>
	 */
	public Map getRemovesDiamondData() {
		
		List<MyUser> users = userRepository.findAll();
		
		Map responseMap = new HashMap<>();
		
		UserRoles godfather = userRoleService.findGodfather();
		
		for(MyUser user : users) {

			Map data = new HashMap<>();
			
			data.put("screen", GameScreen.WAITING_FOR.getValue());
			data.put("waitingFor", godfather.getUser().getUsername());
			
			if (user.getUsername().equalsIgnoreCase(godfather.getUser().getUsername())) {
				
				data.clear();
				data.put("screen", GameScreen.REMOVE_DIAMONDS.getValue());				
				
			}
			
			data.put("isHost", user.isHost());
			
			responseMap.put(user.getUsername(), data);
			
			
		}
		
		return responseMap;
	}
	
	public void onInitDiamonds(Map requestBody, Principal principal) {	
		
		if (principal == null || principal.getName().isBlank())
			return;
		
		MyUser user = userRepository.findByUsername(principal.getName());
		
		if (user == null)
			return;
		
		MyUser godfather = userRoleService.findGodfather().getUser();
		if (!godfather.getUsername().equalsIgnoreCase(principal.getName()))
			return;
		
		// note that the godfather will not be part of the deque at the end of this functions
		ArrayDeque<MyUser> users = new ArrayDeque<>(userRepository.findAllByOrderBySeatOrderAsc());
		
		while (true) {
			
			MyUser pop = users.pop();
			
			if (godfather.getUsername().equalsIgnoreCase(pop.getUsername()))
				break;
			
			// append user
			users.add(pop);
			
		}
		
		// users now contains the sorted order, starting from godfather
		for(MyUser u : users){
			
			TurnDetails details = TurnDetails.builder().user(u).build();
			turnDetailsRepository.save(details);
			
		}
		
		Setup setup = setupRepository.findByNumPlayers((int) userRepository.count());
		
		int totalDiamonds = (int) requestBody.get("totalDiamonds");
		
		String title = user.getUsername() + "(" + userRoleService.findByUser(godfather).getRefRole().getRoleCode() + ")";
		
		gameLogService.insert(title + " stores " + totalDiamonds + " diamonds.");
		
		cigarBoxService.init(totalDiamonds, setup.getNumKakia(), setup.getNumMata(), setup.getNumDriver());
	}
	
	/**
	 * first player removes a token from the game, then takes an object<br/>
	 * all other players see "waiting for ${username}<br/>
	 * 
	 */
	public Map getRemovedTokenData() {
		
		List<MyUser> users = userRepository.findAllByOrderBySeatOrderAsc();
		
		Map responseMap = new HashMap<>();
		
		String curUsername = turnDetailsRepository.findFirstByOrderByTurnDetailsIdAsc().getUser().getUsername();
		
		for(MyUser user : users) {
			
			Map map = new HashMap<>();
			map.put("screen", GameScreen.WAITING_FOR.getValue());
			map.put("waitingFor", curUsername);
			
			if (user.getUsername().equalsIgnoreCase(curUsername)) {
				
				map.clear();
				map.put("screen", GameScreen.REMOVE_TOKEN.getValue()); // only first player sees this screen
				
				Map<String, Object> removableTokens = new LinkedHashMap<>();
				removableTokens.put("", "NONE");
				removableTokens.put(GameRoles.KAKIA.getValue(), refRoleService.findByGameRole(GameRoles.KAKIA).getRoleName());
				removableTokens.put(GameRoles.DRIVER.getValue(), refRoleService.findByGameRole(GameRoles.DRIVER).getRoleName());
				map.put("removableTokens", removableTokens);
			}
			
			map.put("isHost", user.isHost());
			
			responseMap.put(user.getUsername(), map);
		}
		
		return responseMap;
	
	}
	
	public boolean onRemoveToken(Map requestBody, Principal principal) {
		
		if (principal == null || principal.getName().isBlank())
			return false;
		
		TurnDetails turnDetails = turnDetailsRepository.findAll().get(0);
		
		// only first player can return token;
		if (!turnDetails.getUser().getUsername().equalsIgnoreCase(principal.getName()))
			return false;
		
		String gameRole = (String) requestBody.get("roleCode");
		if(gameRole.isBlank())
			gameRole = null;
		
		cigarBoxService.removeToken(gameRole == null ? null : GameRoles.getByRoleCode(gameRole));
		
		return true;
		
	}
	
	/**
	 * depending on turnDetailsRepository, we can identify whose turn is it.<br/>
	 * If it isn't the player's turn, we show waiting...
	 * If it is the player's turn, we present them with options, all configured.
	 */
	public Map getTakeFromBoxData() {
		
		List<MyUser> users = userRepository.findAllByOrderBySeatOrderAsc();
		
		TurnDetails turnDetails = turnDetailsRepository.findFirstByIsCompletedOrderByTurnDetailsIdAsc(false);
		String curUsernameTurn = turnDetails.getUser().getUsername();
		
		boolean isLastPlayer = turnDetailsRepository.countByIsCompleted(false) == 1;
		
		Map responseMap = new HashMap<>();
		
		for(MyUser user : users) {
			
			Map map = new HashMap<>();
			map.put("screen", GameScreen.WAITING_FOR.getValue());
			map.put("waitingFor", curUsernameTurn);
			
			if (user.getUsername().equalsIgnoreCase(curUsernameTurn)) {
				
				map.clear();
				map.put("screen", GameScreen.TAKE_FROM_BOX.getValue());
				
				map.put("refRoleData", refRoleService.refRoleData()); // roleCode:{roleName, roleColor}
				
				map.put("remainingDiamonds", cigarBoxService.getDiamonds());
				
				Map mapRemainingTokens = new HashMap<>();
				
				for(RefRole refRole : refRoleService.findAllByRoleCodeNotIn(GameRoles.GODFATHER, GameRoles.THIEF, GameRoles.STREET_THIEF)){
					
					Map m = new HashMap<>();
					m.put("roleName", refRole.getRoleName());
					
					if (refRole.getRoleCode().equals(GameRoles.KAKIA.getValue()))
						m.put("remainingTokens", cigarBoxService.getNumKakiaTokens());
					else if (refRole.getRoleCode().equals(GameRoles.MATA.getValue()))
						m.put("remainingTokens", cigarBoxService.getNumMataTokens());
					else if (refRole.getRoleCode().equals(GameRoles.DRIVER.getValue()))
						m.put("remainingTokens", cigarBoxService.getNumDriverTokens());
					
					mapRemainingTokens.put(refRole.getRoleCode(), m);
					
				}
				
				map.put("remainingTokens", mapRemainingTokens);
				
				map.put("isLastPlayer", isLastPlayer);
				
			}
			
			map.put("isHost", user.isHost());
			
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
	}
	
	public boolean onTakeFromBox(Map requestBody, Principal principal) throws Exception {
		
		TurnDetails turnDetails = turnDetailsRepository.findFirstByIsCompletedOrderByTurnDetailsIdAsc(false);
		MyUser user = turnDetails.getUser();
		
		if (principal == null || !user.getUsername().equalsIgnoreCase(principal.getName()))
			return false;
		
		UserRoles userRole = userRoleService.findByUser(user);
		
		int diamondsStolen = requestBody.get("diamondsStolen") == null ? 0 : (int) requestBody.get("diamondsStolen");
		
		
		// user is a thief
		if (diamondsStolen > 0) {

			userRoleService.updateUserRole(userRole, GameRoles.THIEF);
			thiefService.insert(user, diamondsStolen);
			cigarBoxService.setDiamonds(cigarBoxService.getDiamonds() - diamondsStolen);
			
			String roleCode = userRole.getRefRole().getRoleCode();
			
			gameLogService.insert(user.getUsername() + "(" + roleCode + ") stole " + diamondsStolen + " diamonds. Remaining diamonds = " + cigarBoxService.getDiamonds());
			
		} else { // user is other role
			
			String selectedToken = ((String) requestBody.get("selectedRoleCode"));
			selectedToken = (selectedToken == null || selectedToken.isBlank()) ? null : selectedToken;
			
			// user is street urchin (thief)
			if (selectedToken == null) {
				
				userRoleService.updateUserRole(userRole, GameRoles.STREET_THIEF);
				gameLogService.insert(principal.getName()+" sees an empty box or decided to take nothing. Assigned as " + userRole.getRefRole().getRoleName());
			}
			else {
				GameRoles gameRole = GameRoles.getByRoleCode(selectedToken);
				userRoleService.updateUserRole(userRole, gameRole);
				gameLogService.insert(principal.getName() + " took " + userRole.getRefRole().getRoleName() + " token");
				cigarBoxService.deductToken(gameRole);
			}
			
		}
		
		// this user's turn is officially over
		turnDetails.setCompleted(true);
		
		turnDetailsRepository.save(turnDetails);
		
		List<TurnDetails> findAll = turnDetailsRepository.findAll();
		
		return true;
	}

	/**
	 * all users see who the godfather is accusing<br/>
	 * godfather sees remaining diamonds<br/>
	 * latest accusation are shown to everyplayer
	 */
	public Map getAccuseData() {
		
		List<MyUser> users = userRepository.findAllByOrderBySeatOrderAsc();
		
		Accusation latestAccusation = accusationService.findFirstOrderByAccusationIdDesc();
		
		UserRoles godfather = userRoleService.findByRole(GameRoles.GODFATHER);
		
		final Set<String> prevAccusedUsernames = accusationService.previouslyAccusedUsernames();
		List<MyUser> players = new ArrayList<>(users);
		players.removeIf(x->x.getUsername().equalsIgnoreCase(godfather.getUser().getUsername())); // godfather cannot be accused.
		players.removeIf(x->prevAccusedUsernames.contains(x.getUsername())); // those who were accused before cannot be accused.
		List<String> unaccused = players.stream().map(x->x.getUsername()).collect(Collectors.toList());
		

		Map responseMap = new HashMap<>();
		
		for(MyUser user : users) {
			
			Map map = new HashMap<>();
			map.put("screen", GameScreen.ACCUSE.getValue());
			map.put("lastAccusation", latestAccusation == null ? null : latestAccusation.getLog());
			map.put("accused", accusationService.getCurAccused() == null ? null : accusationService.getCurAccused().getUsername());
			map.put("players", unaccused);
			
			boolean isGodfather = user.getUsername().equalsIgnoreCase(godfather.getUser().getUsername());
			
			map.put("isGodfather", isGodfather);
			
			if (isGodfather) {
				
				map.put("remainingKakia", cigarBoxService.getNumKakiaTokens());
				map.put("remainingMata", cigarBoxService.getNumMataTokens());
				map.put("remainingDriver", cigarBoxService.getNumDriverTokens());
				map.put("diamonds", cigarBoxService.getDiamonds());
				map.put("initialDiamonds", cigarBoxService.getInitialDiamonds());
				
			}
			
			map.put("isHost", user.isHost());
			
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
	}
	
	public void accusePlayer(Map requestBody, Principal principal) {
		
		if (principal == null || principal.getName().isBlank())
			return;
		
		UserRoles godfather = userRoleService.findByRole(GameRoles.GODFATHER);
		if (!godfather.getUser().getUsername().equalsIgnoreCase(principal.getName()))
			return;
		
		String accusedUsername = (String) requestBody.get("accused");
		MyUser accused = userRepository.findByUsername(accusedUsername);
		
		accusationService.setCurAccused(accused);
	}
	
	/**
	 *  
	 */
	public AccusationOutcome onAccused(Map requestBody, Principal principal) {
		
		if (principal == null || principal.getName().isBlank())
			return AccusationOutcome.FAILED;
		
		UserRoles godfather = userRoleService.findByRole(GameRoles.GODFATHER);
		String godfatherUsername = godfather.getUser().getUsername();
		
		if (!principal.getName().equalsIgnoreCase(godfatherUsername))
			return AccusationOutcome.FAILED;
		
		String accusedUsername = (String) requestBody.get("accused");
		MyUser accused = userRepository.findByUsername(accusedUsername);
		
		String godfatherTitle = godfatherUsername + "(" + GameRoles.GODFATHER.getValue() + ")";
		String accusedTitle = accusedUsername + "(" + userRoleService.findByUser(accused).getRefRole().getRoleCode() + ")";
		
		if (userRoleService.compare(accused, GameRoles.THIEF)){
			
			int diamondsStolen = thiefService.findByUser(accused).getDiamondsStolen();
			int totalDiamondsOnHand = cigarBoxService.getDiamonds() + diamondsStolen;
			
			accusationService.insert(accused, godfatherTitle + " caught " + accusedTitle + " with " + diamondsStolen + " stolen diamonds!");
			thiefService.caught(accused);
			
			gameLogService.insert(godfatherTitle +" accused " + accusedTitle + " of being the thief");
			gameLogService.insert(accusedTitle + " was caught with " + diamondsStolen + " stolen diamonds");
			gameLogService.insert(godfatherTitle + " now has " + totalDiamondsOnHand + " diamonds");
			
			cigarBoxService.setDiamonds(totalDiamondsOnHand);
			
			if (cigarBoxService.getDiamonds() == cigarBoxService.getInitialDiamonds()) {
				gameLogService.insert(godfatherTitle + " has recovered all " + totalDiamondsOnHand + " diamonds!");
				winService.godfatherAndKakiaWins();
				return AccusationOutcome.RECOVERED_ALL_DIAMONDS;
			}
			
			return AccusationOutcome.CAUGHT_THIEVES;
			
		} else {
					
			int remainingJokers = jokerService.remove();
			
			if (userRoleService.compare(accused, GameRoles.KAKIA, GameRoles.DRIVER, GameRoles.STREET_THIEF)) {
				
				if (remainingJokers < 0){
					
					String log = godfatherTitle + " wrongly accused " + accusedTitle + ". Out of Jokers!";
					accusationService.insert(accused, log);
					gameLogService.insert(log);
					winService.thiefWins();
					
					return AccusationOutcome.FALSE_ACCUSATION_NO_JOKERS_REMAINING;
					
				} else {
					
					String log = godfatherTitle + " wrongly accused " + accusedTitle + ". " + remainingJokers + "x Jokers left";
					accusationService.insert(accused, log);
					gameLogService.insert(log);
					
					return AccusationOutcome.FALSE_ACCUSATION_WITH_JOKERS_REMAINING;
					
				}
				
			}
			else if (userRoleService.compare(accused, GameRoles.MATA)) {
				
				String log = godfatherTitle + " wrongly accused " + accusedTitle + ". Game Over!";
				accusationService.insert(accused, log);
				gameLogService.insert(log);
				winService.mataWins(accused);
				
				return AccusationOutcome.ACCUSED_MATA;
				
			}
			
		}
		
		return AccusationOutcome.FAILED;
		
	}

	public Map getGameOverData(boolean isWin) {
		
		List<MyUser> users = userRepository.findAll();
		
		List<String> logs = gameLogService.findAllLogOrderByGameLogIdAsc();
		
		Map data = new HashMap<>();
		data.put("screen", GameScreen.GG.getValue());
		data.put("isWin", false);
		data.put("logs", logs);
		
		final UserRoles godfather = userRoleService.findByRole(GameRoles.GODFATHER);
		
		final Map players = new LinkedHashMap<>();
		players.put(godfather.getUser().getUsername(), new HashMap<>() {{
			
			put("roleName", godfather.getRefRole().getRoleName());
			put("roleColor", godfather.getRefRole().getRoleTextColor());
			
		}});
		
		turnDetailsRepository.findAllByOrderByTurnDetailsIdAsc().forEach(x->{
			
			UserRoles userRoles = userRoleService.findByUser(x.getUser());
			MyUser user = userRoles.getUser();
			RefRole refRole = userRoles.getRefRole();
			
			Map map = new HashMap<>();
			map.put("roleCode", refRole.getRoleCode());
			map.put("roleName", refRole.getRoleName());
			map.put("roleColor", refRole.getRoleTextColor());
			
			if (refRole.getRoleCode().equals(GameRoles.THIEF.getValue())) {
				Thief thief = thiefService.findByUser(user);
				map.put("diamondsStolen", thief == null ? 0 : thief.getDiamondsStolen());
				map.put("isCaught", thief.isCaught());
			}
			
			players.put(user.getUsername(), map);
			
		});
		
		data.put("players", players);
		
		Map responseMap = new HashMap<>();
		
		for (MyUser user : users) {
			
			Map map = new HashMap<>(data);
			map.put("isHost", user.isHost());
			map.put("isWin", winService.findIsWinByUser(user));
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
		
	}

	

}