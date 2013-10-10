package edu.neumont.chess.players;

import java.util.ArrayList;
import java.util.Arrays;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.ui.ChessAI.AIType;

public class RandomAIPlayer extends Player {
	private AIType type;
	public RandomAIPlayer( ChessGame game, Team team, AIType type ) {
		super( game, team );
		this.type = type;
	}
	
	@Override
	public void takeTurn() {
		Piece[] pieces = getPiecesWithMoves();
		if( pieces.length > 0 ) {
			Move[] moves = null;
			if( type == AIType.RANDOM_PIECE_EQUALITY ) {
				Piece randomPiece = pieces[(int)(Math.random()*pieces.length)];
				moves = randomPiece.getMoves();
			}
			else {
				ArrayList<Move> allMoves = new ArrayList<Move>();
				for( Piece piece : pieces ) {
					allMoves.addAll( Arrays.asList(piece.getMoves()) );
				}
				moves = allMoves.toArray( new Move[0] );
			}
			Move randomMove = moves[(int)(Math.random()*moves.length)];
			try {
				System.out.println(randomMove.toStandard());
				getGame().performMove(RandomAIPlayer.this, randomMove);
			} catch (IllegalMoveException e) {
				System.err.println("The random AI really screwed up by choosing an invalid move!");
				getGame().forfeit(RandomAIPlayer.this);
			}
		}
		else {
			throw new IllegalStateException("The player doesn't have any pieces on the board!");
		}
	}
	
	@Override
	public String toString() {
		return getTeam().toString();
	}
}
