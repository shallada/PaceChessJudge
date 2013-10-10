package edu.neumont.chess.model;

public enum Team {
	Black, White;
	
	public static Team WHITE = White;
	public static Team BLACK = Black;
	public static Team white = White;
	public static Team black = Black;
	
	public Team other() {
		if( this == Black )
			return White;
		else
			return Black;
	}
	
	public static Team getTeam( char color ) {
		color = Character.toLowerCase(color);
		if( color == 'l' )
			return White;
		else if( color == 'd' )
			return Black;
		else
			return null;
	}
	
	public String toStandard() {
		if( this == Black )
			return "d";
		else
			return "l";
	}
}
