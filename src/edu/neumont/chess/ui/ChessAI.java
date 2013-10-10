package edu.neumont.chess.ui;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.FourMoveCheckmate;
import edu.neumont.chess.players.PawnPreferredPlayer;
import edu.neumont.chess.players.Player;
import edu.neumont.chess.players.RandomAIPlayer;

public class ChessAI implements ChessUI {
	private ChessGame game;
	
	public ChessAI( ChessGame game ) {
		this.game = game;
	}

	@Override
	public Player createPlayer(Team team) {
		return createPlayer(team, AIType.PAWN_PREFERRED);
	}
	
	public Player createPlayer(Team team, AIType type) {
		switch( type ) {
			case RANDOM_MOVE_EQUALITY:
			case RANDOM_PIECE_EQUALITY:
				return new RandomAIPlayer(game, team, type);
			case PAWN_PREFERRED:
				return new PawnPreferredPlayer(game, team);
			case FOUR_MOVE:
				return new FourMoveCheckmate(game, team);
			default:
				return null;
		}
	}
	
	public static enum AIType {
		RANDOM_PIECE_EQUALITY, RANDOM_MOVE_EQUALITY, PAWN_PREFERRED, FOUR_MOVE
	}
}
