package com;

public enum GameScreen {
	
	LOBBY("lobby"),
	SETTINGS("settings"),
	WAITING_FOR("waitingfor"),
	REMOVE_DIAMONDS("removediamonds"),
	REMOVE_TOKEN("removetoken"),
	TAKE_FROM_BOX("takefrombox"),
	ACCUSE("accuse"),
	ACCUSED("accused"),
	GG("GG"),
	;
	
	private final String value;
	
	private GameScreen(String value) {
		this.value = value;
	}
	
	public String getValue() { return value; }
	
}

enum GameProgress {
	
    LOBBY("lobby"),
    SETTINGS("settings"),
    REMOVE_DIAMONDS("removediamonds"),
    REMOVE_TOKEN("firstplayerremovestoken"),
    TAKE_FROM_BOX("takefrombox"),
    ACCUSE("accuse"),
    GAME_OVER_GODFATHER_WINS("ggwin"),
    GAME_OVER_GODFATHER_LOSES("gglose"),
    ;
    
    private final String value;
	
    private GameProgress(String value) {
    	this.value = value;
    }
    
    public String getValue() { return value; }
    
}



