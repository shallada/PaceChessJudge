package edu.neumont.chess.events;

import edu.neumont.chess.logic.GameState;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.Player;

public interface ChessEventListener {
	void turnStarting( ChessEvent event, Player player );
	void turnEnded( ChessEvent event, Player player );
	void boardUpdated( ChessEvent event, Board board );
	void gameStarted( ChessEvent event );
	void gameOver( ChessEvent event );
	void stateChanged( ChessEvent event, Team team, GameState oldState, GameState newState );
	void pauseChanged( ChessEvent event );
}
