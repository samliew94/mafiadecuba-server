package com;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("game")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GameController {

	@Autowired
	@Lazy
	MyWebSocketHandler webSocketHandler;

	@Autowired
	LobbyService lobbyService;

	@Autowired
	MyUserRepository userRepository;

	@Autowired
	GameDetailsService gameDetailsService;

	@Autowired
	TurnDetailsRepository turnDetailsRepository;

	@Autowired
	SetupRepository setupRepository;

	@Autowired
	CigarBoxService cigarBoxService;

	@Autowired
	GameLogService gameLogService;

	GameScreen gameScreen = GameScreen.LOBBY; // this the default
	GameProgress gameProgress = GameProgress.LOBBY;

	/**
	 * publishes message to all users ;
	 */
	public void update() throws Exception {

		Map data = null;

		if (gameProgress == GameProgress.LOBBY)
			data = lobbyService.getLobbyData();
		else if (gameProgress == GameProgress.SETTINGS)
			data = lobbyService.getSettingsData();
		else if (gameProgress == GameProgress.REMOVE_DIAMONDS)
			data = gameDetailsService.getRemovesDiamondData();
		else if (gameProgress == GameProgress.REMOVE_TOKEN)
			data = gameDetailsService.getRemovedTokenData();
		else if (gameProgress == GameProgress.TAKE_FROM_BOX)
			data = gameDetailsService.getTakeFromBoxData();
		else if (gameProgress == GameProgress.ACCUSE)
			data = gameDetailsService.getAccuseData();
		else if (gameProgress == GameProgress.GAME_OVER_GODFATHER_WINS)
			data = gameDetailsService.getGameOverData(true);
		else if (gameProgress == GameProgress.GAME_OVER_GODFATHER_LOSES)
			data = gameDetailsService.getGameOverData(false);

		if (data == null)
			return;

		webSocketHandler.broadcast(data);

	}

	/**
	 * host changes seat order of a player
	 */
	@PostMapping("move")
	public void move(@RequestBody Map requestMap) throws Exception {

		System.err.println("game/move = " + requestMap);

		if (lobbyService.move(requestMap))
			update();
	}

	/**
	 * host clicks 'next' button after seat order confirmed.<br/>
	 * switch screen to settings
	 * 
	 */
	@PostMapping("tosettings")
	public void toSettings(Principal principal) throws Exception {

		System.err.println("game/tosettings");

		gameDetailsService.onToSettings(principal);

		gameProgress = GameProgress.SETTINGS;

		update();
	}

	/**
	 * host elects godfather in settings (default is the host)
	 */
	@PostMapping("changegodfather")
	public void changeGodfather(@RequestBody Map requestMap, Principal principal) throws Exception {

		System.err.println("game/changegodfather");

		gameDetailsService.onChangeGodfather((String) requestMap.get("username"));

		update();
	}

	/**
	 * host changes all player's screen to godfather removes diamonds
	 */
	@PostMapping("toremovediamonds")
	public void toRemoveDiamonds(Principal principal) throws Exception {

		System.err.println("game/toremovediamonds");

		if (principal == null || principal.getName().isBlank())
			return;

		if (!userRepository.findByIsHost(true).getUsername().equalsIgnoreCase(principal.getName()))
			return;

		gameProgress = GameProgress.REMOVE_DIAMONDS;

		update();
	}

	/**
	 * the godfather removes X number of diamonds from the cigar box (0-5)
	 */
	@PostMapping("initdiamonds")
	public void initDiamonds(@RequestBody Map requestBody, Principal principal) throws Exception {

		System.err.println("game/initdiamonds");

		gameDetailsService.onInitDiamonds(requestBody, principal);

		gameProgress = GameProgress.REMOVE_TOKEN;

		update();
	}

	/**
	 * The first player removes an identity token (only kaki or driver).<br/>
	 * if success, switch screens to take from box
	 */
	@PostMapping("removetoken")
	public void removeToken(@RequestBody Map requestBody, Principal principal) throws Exception {

		System.err.println("game/removetoken");

		if (gameDetailsService.onRemoveToken(requestBody, principal)) {

			gameProgress = GameProgress.TAKE_FROM_BOX;

			update();

		}

	}

	@PostMapping("takefrombox")
	public void takefrombox(@RequestBody Map requestBody, Principal principal) throws Exception {

		System.err.println("game/takefrombox");

		if (gameDetailsService.onTakeFromBox(requestBody, principal)) {

			gameProgress = GameProgress.TAKE_FROM_BOX;

			// it's godfather's turn bro
			if (turnDetailsRepository.findFirstByIsCompletedOrderByTurnDetailsIdAsc(false) == null)
				gameProgress = GameProgress.ACCUSE;

			update();

		}

	}

	@PostMapping("accuseplayer")
	public void accusePlayer(@RequestBody Map requestBody, Principal principal) throws Exception {

		System.err.println("game/accuseplayer");

		gameDetailsService.accusePlayer(requestBody, principal);

		update();
	}

	@PostMapping("accuse")
	public void accuse(@RequestBody Map requestBody, Principal principal) throws Exception {

		System.err.println("game/accuse");

		AccusationOutcome outcome = gameDetailsService.onAccused(requestBody, principal);

		if (outcome == AccusationOutcome.FAILED) {

			// retain accusation round

		} else if (outcome == AccusationOutcome.RECOVERED_ALL_DIAMONDS) {

			gameProgress = GameProgress.GAME_OVER_GODFATHER_WINS;

		} else if (outcome == AccusationOutcome.CAUGHT_THIEVES) {

			// retain accusation round

		} else if (outcome == AccusationOutcome.ACCUSED_MATA) {

			gameProgress = GameProgress.GAME_OVER_GODFATHER_LOSES;

		} else if (outcome == AccusationOutcome.FALSE_ACCUSATION_NO_JOKERS_REMAINING) {

			gameProgress = GameProgress.GAME_OVER_GODFATHER_LOSES;

		} else if (outcome == AccusationOutcome.FALSE_ACCUSATION_WITH_JOKERS_REMAINING) {

			// retain accusation round

		}

		update();

	}

	@PostMapping("tolobby")
	public void toLobby(HttpServletRequest request, Principal principal) throws Exception {

		System.err.println("game/tolobby");

		if (principal == null || principal.getName().isBlank())
			return;

		MyUser user = userRepository.findByIsHost(true);

		if (!user.getUsername().equalsIgnoreCase(principal.getName()))
			return;

		gameProgress = GameProgress.LOBBY;
		
		gameDetailsService.reset();
		
		update();

	}

	@PostMapping("kick")
	public void kick(@RequestBody Map requestMap, Principal principal) throws Exception {
		
		lobbyService.onKick(requestMap, principal);
		
		update();
		
	}
}
