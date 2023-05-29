package com;
/**
 * after accusation, there are several outcomes (returned in int):<br/><br/>
 * -1.accusation failed, likely an error
 * 0. a thief was caught, and all diamonds recovered.<br/>
 * 1. a thief was caught, but there are missing diamonds.<br/>
 * 2. a non-fbi is accused, there are still jokers.<br/>
 * 3. a non-fbi is accused, no more jokers.<br/>
 * 4. a fbi is accused<br/>
 *
 */
enum AccusationOutcome {
		
	/**
	 * probably a bug
	 */
	FAILED(-1),
	
	/**
	 * Godfather caught a thief, resulting in all diamonds recovered 
	 */
	RECOVERED_ALL_DIAMONDS(0),
	
	/**
	 * Godfather caught a thief, but there are still missing diamonds 
	 */
	CAUGHT_THIEVES(1),
	
	/**
	 * Godfather wrongly accused a player, no more jokers
	 */
	FALSE_ACCUSATION_NO_JOKERS_REMAINING(2),
	
	/**
	 * Godfather wrongly accused a player, still have jokers
	 */
	FALSE_ACCUSATION_WITH_JOKERS_REMAINING(3),
	
	/**
	 * Accused a FBI
	 */
	ACCUSED_MATA(4)
	
    ;
    
    private final int value;
	
    private AccusationOutcome(int value) {
    	this.value = value;
    }
    
    public int getValue() { return value; }
	 
 }