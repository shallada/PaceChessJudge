package edu.neumont.chess.logic;

public enum GameState {
	INITIALIZATION, INPROGRESS, CHECK, CHECKMATE, STALEMATE, FORFEIT, DRAW;
	
	public boolean isFinalState() {
		return this == CHECKMATE || 
				this == STALEMATE || 
				this == FORFEIT ||
				this == DRAW;
	}
}
