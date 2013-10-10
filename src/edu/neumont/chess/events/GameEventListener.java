package edu.neumont.chess.events;

public interface GameEventListener {
	void gameStateChanged( GameEvent e );
	void gameBoardUpdated( GameEvent e );
	void gamePauseChanged( GameEvent e );
}
