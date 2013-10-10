package edu.neumont.chess.events;

import java.util.EventObject;

import edu.neumont.chess.judge.Game;

public class GameEvent extends EventObject {
	private static final long serialVersionUID = 1;

	public GameEvent( Game source ) {
		super(source);
	}
	
	@Override
	public Game getSource() {
		return (Game)super.getSource();
	}
}
