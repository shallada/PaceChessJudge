package edu.neumont.chess.model;

import java.awt.Point;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import edu.neumont.chess.piece.*;
import edu.neumont.chess.ui.ChessConsole;

public class Board {
	private Square[][] board;
	private ChessGame game;
	
	public Board( ChessGame game ) {
		int rows = 8;
		int cols = 8;
		
		this.game = game;
		
		// Set up the board with empty squares
		board = new Square[rows][cols];
		for( int r=0; r < rows; r++ ) {
			for( int c=0; c < cols; c++ ) {
				board[r][c] = new Square();
			}
		}
	}
	
	public int getRows() {
		return board.length;
	}
	
	public int getCols() {
		return board[0].length;
	}
	
	public Piece[] createChessTeam(Team team) {
		char r1 = team == Team.White ? '1' : '8';
		char r2 = team == Team.White ? '2' : '7';
		
		ArrayList<Piece> pieces = new ArrayList<Piece>();
		pieces.addAll( Arrays.asList( new Piece[] {
				addPiece( new Rook(game, team), 		ChessGame.toPoint("A"+r1) ),
				addPiece( new Knight(game, team), 		ChessGame.toPoint("B"+r1) ),
				addPiece( new Bishop(game, team), 		ChessGame.toPoint("C"+r1) ),
				addPiece( new Queen(game, team), 		ChessGame.toPoint("D"+r1) ),
				addPiece( new King(game, team), 		ChessGame.toPoint("E"+r1) ),
				addPiece( new Bishop(game, team), 		ChessGame.toPoint("F"+r1) ),
				addPiece( new Knight(game, team), 		ChessGame.toPoint("G"+r1) ),
				addPiece( new Rook(game, team), 		ChessGame.toPoint("H"+r1) ) } ) );
		for( char c='A'; c <= 'H'; c++ ) {
			String location = new String( new char[] { c, r2 } );
			pieces.add( addPiece( new Pawn(game, team), ChessGame.toPoint(location) ) );
		}
		
		boolean abort = false;
		for( Piece piece : pieces ) {
			if( piece == null ) {
				abort = true;
			}
		}
		
		if( abort ) {
			for( Piece piece : pieces ) {
				if( piece != null )
					removePiece(piece);
			}
			
			return null;
		}
		
		return pieces.toArray( new Piece[0] );
	}
	
	public Piece addPiece( Piece piece, Point location ) {
		Square square = getSquare(location);
		if( square != null && !square.hasOccupant() ) {
			square.setOccupant(piece);
			return piece;
		}
		else {
			return null;
		}
	}
	
	public boolean movePiece( Piece piece, Point newlocation ) {
		Point oldlocation = getLocation( piece );
		if( oldlocation == null ) {
			return addPiece( piece, newlocation ) != null;
		}
		else {
			Square old = getSquare( oldlocation );
			old.setOccupant(null);
			if( newlocation != null ) {
				Square square = getSquare( newlocation );
				if( !square.hasOccupant() ) {
					square.setOccupant(piece);
					return true;
				}
				else {
					return false;
				}
			}
			else
				return true;
		}
	}
	
	public void removePiece( Piece piece ) {
		Square square = getSquare( piece );
		if( square != null ) {
			square.removeOccupant();
		}
	}
	
	public Square getSquare( Piece piece ) {
		Point point = getLocation( piece );
		if( point != null ) {
			return getSquare( point );
		}
		else {
			return null;
		}
	}
	
	public Square getSquare( int row, int col ) {
		return getSquare( new Point(col, row) );
	}
	
	public Square getSquare( Point location ) {
		if( location == null ) {
			return null;
		}
		if( location.y < 0 || location.y >= board.length ||
				location.x < 0 || location.x >= board[location.y].length )
			return null;
		return board[location.y][location.x];
	}
	
	public Piece getPiece( Point location ) {
		Square square = getSquare(location);
		if( square != null )
			return square.getOccupant();
		return null;
	}
	
	public Point getLocation( Piece piece ) {
		for( int r=0; r < board.length; r++ ) {
			for( int c=0; c < board[r].length; c++ ) {
				Square square = board[r][c];
				if( square.hasOccupant() && square.getOccupant().equals(piece) )
					return new Point(c,r);
			}
		}
		return null;
	}
	
	public boolean isOccupied( Point location ) {
		return getPiece(location) != null;
	}
	
	public boolean isValid( Point location ) {
		boolean valid = false;
		if( location != null ) {
			valid = location.x >= 0 && location.x < getCols() &&
				location.y >= 0 && location.y < getRows();
		}
		return valid;
	}
	
	public Piece[] getPieces( Team team ) {
		ArrayList<Piece> pieces = new ArrayList<Piece>();
		for( int r=0; r < getRows(); r++ ) {
			for( int c=0; c < getCols(); c++ ) {
				Point location = new Point(c,r);
				Piece piece = getPiece(location);
				if( piece != null && piece.getTeam() == team )
					pieces.add(piece);
			}
		}
		
		return pieces.toArray(new Piece[0]);
	}
	
	public King getKing( Team team ) {
		Piece[] pieces = getPieces(team);
		for( Piece piece : pieces ) {
			if( piece instanceof King )
				return (King)piece;
		}
		
		throw new IllegalStateException( "Somehow team " + team + " doesn't have a King" );
	}
	
	public class Square {
		private Piece occupant;
		private Square() {
		}
		
		public boolean hasOccupant() {
			return occupant != null;
		}

		public Piece getOccupant() {
			return occupant;
		}
		public void setOccupant( Piece piece ) {
			this.occupant = piece;
		}
		public void removeOccupant() {
			setOccupant(null);
		}
		
		@Override
		public String toString() {
			return toStandard();
		}
		
		public String toStandard() {
			Point location = null;
			for( int r=0; r < getRows(); r++ ) {
				for( int c=0; c < getCols(); c++ ) {
					if( getSquare(r,c) == this ) {
						location = new Point(c,r);
					}
				}
			}
			if( location == null )
				return null;
			return ChessGame.toCoordinate(location);
		}
	}
	
	@Override
	public String toString() {
		StringWriter output = new StringWriter();
		PrintWriter writer = new PrintWriter(output);
		ChessConsole.drawBoard(this, writer, null);
		return output.toString();
	}
}
