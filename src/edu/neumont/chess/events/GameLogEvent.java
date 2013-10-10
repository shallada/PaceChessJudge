package edu.neumont.chess.events;

import java.util.EventObject;

import edu.neumont.chess.model.GameLog;

public class GameLogEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public GameLogEvent( GameLog log ) {
		super( log );
	}
	
	@Override
	public GameLog getSource() {
		return (GameLog)super.getSource();
	}
}
