package edu.neumont.chess.movements;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.model.Board.Square;
import edu.neumont.chess.piece.Piece;

public class SimpleMove implements Move {
	private Board board;
	private Piece piece;
	private Point from;
	private Point destination;
	
	public SimpleMove( Board board, Piece piece, Point from, Point destination ) {
		this.board = board;
		this.piece = piece;
		this.from = from;
		this.destination = destination;
	}
	
	public Point getFrom() {
		if( from == null )
			return null;
		return new Point(from.x, from.y);
	}
	
	public Square getFromSquare() {
		return board.getSquare(getFrom());
	}
	
	public Point getTo() {
		if( destination == null ) 
			return null;
		return new Point(destination.x, destination.y);
	}
	
	public Square getToSquare() {
		return board.getSquare(getTo());
	}
	
	@Override
	public void execute() {
		board.movePiece(piece, destination);
	}

	@Override
	public void undo() {
		board.movePiece(piece, from);
	}

	@Override
	public boolean isValid(Team team) {
		Square from = getFromSquare();
		Square to = getToSquare();
		
		if( from == null && to == null )
			return false;
		
		// placement
		if( from == null ) {
			// NOTE: we're going to completely disallow placements b/c placement is done internally
			// return to.getOccupant() == null;
			return false;
		}
		
		// move
		Piece fromPiece = from.getOccupant();
		if( fromPiece == null || fromPiece != piece || fromPiece.getTeam() != team )
			return false;
		
		// move off board
		if( to == null )
			return true;
		
		List<Point> destinations = Arrays.asList( fromPiece.getDestinations() );
		return destinations.contains(getTo());
	}
	
	public boolean isPlacement() {
		return board.getSquare(getFrom()) == null;
	}
	
	public boolean isMoveOffBoard() {
		return board.getSquare(getTo()) == null;
	}
	
	public boolean isMovement() {
		return !isPlacement() && !isMoveOffBoard();
	}
	
	@Override
	public String toString() {
		String pieceString = String.format( "%s %s", piece.getTeam().toString().toLowerCase(), piece.getClass().getSimpleName() );
		Square from = getFromSquare();
		if( from != null )
			pieceString += " on " + from.toString();
		if( isPlacement() )
			return String.format( "a %s is placed on %s", pieceString, getToSquare() );
		else if( isMoveOffBoard() )
			return String.format("the %s is removed from the board", pieceString );
		else
			return String.format("the %s is moved to %s", pieceString, getToSquare() );
	}
	
	@Override
	public String toStandard() {
		if( isPlacement() ) {
			return String.format( "%s%s", piece.toStandard(), getToSquare().toStandard() );
		}
		else if( isMovement() ) {
			return String.format( "%s %s", getFromSquare().toStandard(), getToSquare().toStandard() );
		}
		return "";
	}
}
