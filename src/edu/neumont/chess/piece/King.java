package edu.neumont.chess.piece;

import java.awt.Point;
import java.util.ArrayList;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.Move;

public class King extends Piece {
	public King( ChessGame game, Team team ) {
		super( game, team );
	}
	
	@Override
	public String toString() {
		return "K";
	}
	
	@Override
	public Move[] pieceMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();
		Point start = getLocation();
		moves.addAll( getMoves(start, new Point( 1, 1), 1) );
		moves.addAll( getMoves(start, new Point( 1, 0), 1) );
		moves.addAll( getMoves(start, new Point( 1,-1), 1) );
		moves.addAll( getMoves(start, new Point( 0, 1), 1) );
		moves.addAll( getMoves(start, new Point( 0,-1), 1) );
		moves.addAll( getMoves(start, new Point(-1, 1), 1) );
		moves.addAll( getMoves(start, new Point(-1, 0), 1) );
		moves.addAll( getMoves(start, new Point(-1,-1), 1) );
		return moves.toArray(new Move[0]);
	}
}
