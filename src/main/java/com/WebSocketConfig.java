package com;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSocket
@EnableWebSocketSecurity
public class WebSocketConfig implements WebSocketConfigurer{

	@Autowired
	MyWebSocketHandler handler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// TODO Auto-generated method stub
		registry.addHandler(handler, "websocket")
			.setAllowedOrigins("http://localhost:5173")
//			.withSockJS()
			;
	}
	
	@Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .anyMessage()
                	.authenticated()
//                .permitAll()
                ;

        return messages.build();
    }
}

@SuppressWarnings({"unchecked", "rawtypes"})
@Component
class MyWebSocketHandler extends TextWebSocketHandler{
	
//	List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	Map<String, WebSocketSession> sessions = new HashMap<>();
	
	@Autowired
	@Lazy
	GameController gameController;
	
	@Autowired
	@Lazy
	MyUserRepository userRepository;
	
	private WebSocketSession findSessionByUsername(String username) {
		
		return sessions.get(username);
		
	}
	
	public void broadcast(Map<String, Object> data) throws Exception {
		
		for (String username : data.keySet()) {
			
			WebSocketSession session = findSessionByUsername(username);
			
			if (session == null)
				continue;
			
			String json = new ObjectMapper().writeValueAsString(data.get(username));
			
			System.err.println("broadcasting to " + username + " " + json);
			session.sendMessage(new TextMessage(json.getBytes()));
			
		}
		
	}
	
	private void log() {
		System.err.println("total sessions="+sessions.size());
		sessions.keySet().forEach(x->System.err.println("socket conn="+x));
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		try {
			System.err.println("websocket conn opened on " + session.getPrincipal().getName());
			sessions.put(session.getPrincipal().getName(), session);
			log();
			
			gameController.update();
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e.getMessage());
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// TODO Auto-generated method stub
		
		try {
			System.err.println("websocket conn closed on " + session.getPrincipal().getName());
			sessions.remove(session.getPrincipal().getName());
			log();
			
			gameController.update();
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e.getMessage());
		}
		
	}

	public void disconnect(String username){
		// TODO Auto-generated method stub
		WebSocketSession webSocketSession = sessions.get(username);
		
		if (webSocketSession != null) {
			try {
				webSocketSession.close();
				System.err.println("Disconnected " + username + " from websocket");
			} catch (IOException e) {
				System.err.println("Error when attempting to disconnected user. Exception is "  + e.getMessage());
			}
			
		}
	}
	
}