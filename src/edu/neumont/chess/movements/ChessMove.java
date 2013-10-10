package edu.neumont.chess.movements;

import java.awt.Point;

import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.piece.Pawn;
import edu.neumont.chess.piece.Piece;

public class ChessMove {
	public static Move createPlacement( ChessGame game, Piece piece, Point destination ) {
		return createPlacement(game.getBoard(), piece, destination);
	}
	
	public static Move createPlacement( Board board, Piece piece, Point destination ) {
		// Placement is exactly like a move except the "from" is null
		return createMove(board, piece, destination);
	}
	
	public static Move createMove( ChessGame game, Piece piece, Point destination ) {
		return createMove( game.getBoard(), piece, destination );
	}
	
	public static Move createMove( Board board, Piece piece, Point destination ) {
		Point from = board.getLocation(piece);
		Move pieceMove = new SimpleMove(board, piece, from, destination);
		Piece captured = board.getPiece(destination);
		if( captured != null ) {
			Move captureMove = new SimpleMove(board, captured, destination, null);
			pieceMove = new ComplexMove( captureMove, pieceMove );
		}
		
		return pieceMove;
	}
	
	public static Move enPassant( Board board, Pawn pawn, Point destination ) {
		Point from = board.getLocation(pawn);
		Move pieceMove = new SimpleMove(board, pawn, from, destination);
		Point otherPawnLocation = new Point( destination.x, from.y );
		Piece otherPawn = board.getPiece( otherPawnLocation );
		if( otherPawn != null && otherPawn.getClass() == Pawn.class
				&& !board.isOccupied(destination) ) {
			Move captureMove = new SimpleMove(board, otherPawn, otherPawnLocation, null);
			pieceMove = new ComplexMove( captureMove, pieceMove );
		}
		else {
			throw new IllegalStateException( "En Passant not possible with the specified parameters" );
		}
		
		return pieceMove;
	}
}
