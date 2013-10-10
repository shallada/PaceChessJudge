package edu.neumont.chess.events;

import edu.neumont.chess.logic.GameState;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.Player;

public class ChessEventAdapter implements ChessEventListener {

	@Override
	public void boardUpdated(ChessEvent event, Board board) {
	}

	@Override
	public void gameOver(ChessEvent event) {
	}

	@Override
	public void gameStarted(ChessEvent event) {
	}

	@Override
	public void turnEnded(ChessEvent event, Player player) {
	}

	@Override
	public void turnStarting(ChessEvent event, Player player) {
	}

	@Override
	public void stateChanged(ChessEvent event, Team team, 
			GameState oldState, GameState newState) {
	}
	
	@Override
	public void pauseChanged(ChessEvent event) {
	}
}
