package edu.neumont.chess.piece;

import java.awt.Point;
import java.util.ArrayList;

import edu.neumont.chess.logic.ChessLogic;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.ChessMove;
import edu.neumont.chess.movements.Move;

public abstract class Piece {
	private Team team;
	private ChessGame game;
	protected ArrayList<Point> destinations;
	
	public Piece( ChessGame game, Team team ) {
		setGame( game );
		setTeam( team );
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public ChessGame getGame() {
		return game;
	}
	
	public void setGame( ChessGame game ) {
		this.game = game;
	}
	
	public Board getBoard() {
		if( getGame() == null )
			return null;
		return getGame().getBoard();
	}
	
	public Point getLocation() {
		if( getBoard() == null )
			return null;
		return getBoard().getLocation(this);
	}
	
	protected ArrayList<Move> getMoves( Point start, Point direction, int steps ) {
		ArrayList<Move> moves = new ArrayList<Move>();
		if( start != null ) {
			if( getLocation() != null && getLocation().equals(start) ) {
				start = new Point( start.x+direction.x, start.y+direction.y );
			}
			Board board = getBoard();
			int row = start.y;
			int col = start.x;
			if( row >= 0 && row < board.getRows() &&
					col >= 0 && col < board.getCols() ) {
				Point location = new Point( col, row );
				Piece occupant = board.getPiece(location);
				if( occupant != null ) {
					if( occupant.getTeam() != this.getTeam() ) {
						moves.add(ChessMove.createMove(board, this, location));
					}
				}
				else {
					Point next = new Point( start.x+direction.x, start.y+direction.y );
					moves.add(ChessMove.createMove(board, this, location));
					if( steps > 1 ) {
						moves.addAll( getMoves(next, direction, steps-1) );
					}
				}
			}
		}
		return moves;
	}

	public Move[] getMoves() {
		destinations = new ArrayList<Point>();
		Move[] potential = pieceMoves();
		Board board = getBoard();
		ArrayList<Move> validMoves = new ArrayList<Move>();
		for( Move m : potential ) {
			m.execute();
			if( !ChessLogic.isInCheck(board, getTeam()) ) {
				validMoves.add(m);
				destinations.add( getLocation() );
			}
			m.undo();
		}
		
		return validMoves.toArray(new Move[0]);
	}
	
	public Point[] getDestinations() {
		getMoves(); // make sure the destinations array is populated
		return destinations.toArray( new Point[0] );
	}
	
	@Override
	public abstract String toString();
	
	public abstract Move[] pieceMoves();
	
	public String toStandard() {
		return PieceFactory.pieceName(this) + getTeam().toStandard();
	}
}
