package edu.neumont.chess.piece;

import java.awt.Point;
import java.util.ArrayList;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.Move;

public class Queen extends Piece {
	public Queen( ChessGame game, Team team ) {
		super( game, team );
	}
	
	@Override
	public String toString() {
		return "Q";
	}
	
	@Override
	public Move[] pieceMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();
		Point start = getLocation();
		moves.addAll( getMoves(start, new Point( 1, 1), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point( 1, 0), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point( 1,-1), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point( 0, 1), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point( 0,-1), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point(-1, 1), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point(-1, 0), Integer.MAX_VALUE) );
		moves.addAll( getMoves(start, new Point(-1,-1), Integer.MAX_VALUE) );
		return moves.toArray(new Move[0]);
	}
}
