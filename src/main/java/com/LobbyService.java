package com;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LobbyService {
	
	@Autowired
	MyUserRepository userRepository;
	
	@Autowired
	TurnDetailsRepository turnDetailsRepository;
	
	@Autowired
	UserRolesRepository userRolesRepository;
	
	@Autowired
	RefRolesRepository refRolesRepository;
	
	@Autowired
	SetupRepository setupRepository;
	
	@Autowired
	FindByIndexNameSessionRepository sessionRepository;
	
	@Autowired
	@Lazy
	GameDetailsService gameDetailsService;
	
	@Autowired
	MyWebSocketHandler webSocketHandler;
	
	ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
	
	public Map getLobbyData() throws Exception {
		
		List<MyUser> users = userRepository.findAllByOrderBySeatOrderAsc();
		
		List<Map> playerList = new ArrayList<>();
		
		MyUser userHost = userRepository.findByIsHost(true);
		
		for(MyUser u : users) {
			
			Map player = new HashMap<>();
			player.put("username", u.getUsername());
			player.put("isHost", u.isHost());
			player.put("seatOrder", u.getSeatOrder());
			player.put("hostUsername", userHost.getUsername());
			playerList.add(player);
			
		}
		
		Map data = new HashMap<>();
		data.put("screen", GameScreen.LOBBY.getValue());
		data.put("players", playerList);
		
		Map responseMap = new HashMap<>();
		
		for(MyUser user : users) {
			
			Map map = new HashMap<>(data);
			map.put("isHost", user.isHost());
			responseMap.put(user.getUsername(), map);
			
		}
		
		return responseMap;
	}

	public Map getSettingsData() {
		
		List<MyUser> users = userRepository.findAllByOrderBySeatOrderAsc();
		
		Map data = new HashMap<>();
		
		// what screen should the users see?
		data.put("screen", GameScreen.SETTINGS.getValue());
		data.put("host", userRepository.findByIsHost(true).getUsername());
		data.put("godfather", userRolesRepository.findByRefRole(refRolesRepository.findByRoleCode(GameRoles.GODFATHER.getValue())).getUser().getUsername());
		data.put("isHost", false);
		
		Setup setup = setupRepository.findByNumPlayers((int) userRepository.count());
		
		data.put("numKakia", setup.getNumKakia());
		data.put("numMata", setup.getNumMata());
		data.put("numDriver", setup.getNumDriver());
		data.put("numJoker", setup.getNumJoker());
		data.put("players", users.stream().map(x->x.getUsername()).collect(Collectors.toList()));
		
		Map responseMap = new HashMap<>();
		
		for (MyUser user : users) {
			
			responseMap.put(user.getUsername(), data);
			
			if (user.isHost()) {
				
				Map deepCopy = new HashMap<>(data);
				deepCopy.put("isHost", true);
				responseMap.put(user.getUsername(), deepCopy);
				
			}
			
		}
			
		return responseMap;
	}

	public boolean move(Map requestMap) throws Exception {
		
		String username = (String) requestMap.get("username");
		boolean dir = (boolean) requestMap.get("dir");
		
		MyUser user = userRepository.findByUsername(username);
		
		if (dir && user.getSeatOrder() == 0)
			return false; // do nothing because user is at top
		else if (!dir && user.getSeatOrder() == ((int) userRepository.count())-1)
			return false; // do nothing beacuse user is at bottom
		
		MyUser user2 = userRepository.findBySeatOrder(dir ? user.getSeatOrder()-1 : user.getSeatOrder()+1);
		
		int seatOrder1 = user.getSeatOrder();
		int seatOrder2 = user2.getSeatOrder();
		
		user.setSeatOrder(seatOrder2);
		user2.setSeatOrder(seatOrder1);
		
		userRepository.save(user);
		userRepository.save(user2);
		
		return true;
		
	}
	
	public void onKick(Map requestBody, Principal principal) throws Exception {
		
		if (principal == null)
			return;
		
		MyUser user = userRepository.findByUsername(principal.getName());
		
		if (user == null || !user.isHost())
			return;
		
		String kickUsername = (String) requestBody.get("username");
		
		MyUser kickUser = userRepository.findByUsername(kickUsername);
				
		userRepository.delete(kickUser);
		webSocketHandler.disconnect(kickUsername);
		
		Map map = sessionRepository.findByPrincipalName(kickUsername);
		if (map != null && !map.isEmpty()) {
			
			Set<String> sessionIds = map.keySet();
			sessionIds.forEach(x->sessionRepository.deleteById(x));
			
		}
		
	}
	
	
	
	
	
}
