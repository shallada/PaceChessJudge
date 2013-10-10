package edu.neumont.chess.events;

import java.util.EventObject;

import edu.neumont.chess.model.ChessGame;

public class ChessEvent extends EventObject {
	private static final long serialVersionUID = 1;
	
	public ChessEvent( ChessGame source ) {
		super(source);
	}
	
	@Override
	public ChessGame getSource() {
		return (ChessGame)super.getSource();
	}
}
