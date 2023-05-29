package com;
public enum GameRoles{
	
	GODFATHER("GF"),
	KAKIA("KK"),
	MATA("M"),
	DRIVER("D"),
	THIEF("T"),
	STREET_THIEF("ST"),
	;
	
	private final String value;
	
	private GameRoles(String value) {
		this.value = value;
	}
	
	public String getValue() { return value ;}
	
	public static GameRoles getByRoleCode(String code) {
		for (GameRoles gr : values()) {
			if (gr.value.equalsIgnoreCase(code))
				return gr;
		}
		
		return null;
	}
}