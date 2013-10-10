package edu.neumont.chess.piece;

import java.awt.Point;
import java.util.ArrayList;

import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.ChessMove;
import edu.neumont.chess.movements.Move;

public class Pawn extends Piece {
	public Pawn( ChessGame game, Team team ) {
		super( game, team );
	}
	
	@Override
	public String toString() {
		return "P";
	}
	
	@Override
	public Move[] pieceMoves() {
		ArrayList<Move> moves = new ArrayList<Move>();
		Point start = getLocation();
		if( start != null ) {
			int homeRow = getTeam() == Team.White ? 6 : 1;
			int direction = getTeam() == Team.White ? -1 : 1;
	
			Board board = getBoard();
			
			// Non-capturing moves
			Point oneUp = new Point(start.x, start.y+direction);
			if( board.isValid(oneUp) && !board.isOccupied( oneUp ) ) {
				moves.add( ChessMove.createMove(board, this, oneUp) );
				
				// If I haven't moved yet we can try 1 or 2 steps forward
				if( start.y == homeRow ) {
					Point twoUp = new Point( oneUp.x, oneUp.y+direction );
					if( !board.isOccupied( twoUp ) ) {
						moves.add( ChessMove.createMove(board, this, twoUp) );
					}
				}
			}
			
			// Capturing moves
			Point upRight = new Point(oneUp.x+1, oneUp.y);
			if( board.isValid(upRight) ) {
				Piece right = board.getPiece( upRight );
				if( right != null && right.getTeam() != this.getTeam() )
					moves.add( ChessMove.createMove(board, this, upRight) );
			}
			Point upLeft = new Point(oneUp.x-1, oneUp.y);
			if( board.isValid(upLeft) ) {
				Piece left  = board.getPiece( upLeft );
				if( left != null && left.getTeam() != this.getTeam() )
					moves.add( ChessMove.createMove(board, this, upLeft) );
			}
			
//			// En passant
//			if( isEnPassant(upLeft) ) {
//				moves.add( ChessMove.enPassant(board, this, upLeft) );
//			}
//			else if( isEnPassant(upRight) ) {
//				moves.add( ChessMove.enPassant(board, this, upRight) );
//			}
		}
		return moves.toArray(new Move[0]);
	}
	
//	private boolean isEnPassant( Point destination ) {
//		boolean enPassant = false;
//		Point from = getLocation();
//		Move lastMove = getGame().getLastMove();
//		Board board = getBoard();
//		if( board.isValid(destination) && !board.isOccupied(destination) ) {
//			Point beside = new Point( destination.x, from.y );
//			Piece occupant = board.getPiece(beside);
//			if( occupant != null && occupant.getTeam() != getTeam() &&
//					occupant.getClass() == Pawn.class ) {
//				lastMove.undo();
//				Point previous = board.getLocation(occupant);
//				int diff = previous.y - beside.y;
//				// Did the pawn beside me move two spaces vertically?
//				enPassant = diff == 2 || diff == -2;
//				lastMove.execute();
//			}
//		}
//		
//		return enPassant;
//	}
}
