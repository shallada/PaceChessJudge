package edu.neumont.chess.players;

import java.awt.Point;
import java.util.HashMap;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.ui.ChessGUI;

public class GUIPlayer extends Player {
	private Piece selectedPiece;
	private HashMap<Point, Move> moves;
	private ChessGUI gui;
	
	public GUIPlayer( ChessGame game, Team team, ChessGUI gui ) {
		super( game, team );
		this.gui = gui;
	}
	
	public ChessGUI getGUI() {
		return gui;
	}
	
	public void setSelectedPiece( Piece piece ) {
		this.selectedPiece = piece;
		this.moves = null;
		
		gui.disableAll();
		if( selectedPiece != null ) {
			if( selectedPiece.getTeam() == getTeam() ) {
				this.moves = new HashMap<Point, Move>();
				Move[] moves = piece.getMoves();
				Point[] destinations = piece.getDestinations();
				for( int i=0; i < destinations.length; i++ ) {
					this.moves.put(destinations[i], moves[i]);
					gui.enableSquare(destinations[i], true);
				}
			}
			gui.enableSquare( getBoard().getLocation(piece) );
		}
	}
	
	public void squareClicked( Point location ) {
		if( selectedPiece == null ) {
			setSelectedPiece( getBoard().getPiece(location) );
		}
		else if( moves != null && moves.containsKey(location) ){
			Move move = moves.get(location);
			setSelectedPiece(null);
			try {
				getGame().performMove(this, move);
				System.out.println(move.toStandard());
				System.out.flush();
			} catch (IllegalMoveException e) {
				e.printStackTrace(); // this shouldn't happen
			}
		}
		else {
			Point pieceLocation = selectedPiece.getLocation();
			
			// deselect the piece
			if( pieceLocation != null && pieceLocation.equals(location) ) {
				setSelectedPiece(null);
				highlightPieces();
			}
		}
	}

	@Override
	public void takeTurn() {
		setSelectedPiece( null );
		highlightPieces();
	}
	
	private void highlightPieces() {
		gui.disableAll();
		for( Piece p : getPieces() ) {
			Point[] locations = p.getDestinations();
			if( locations != null && locations.length > 0 ) {
				gui.enableSquare( p.getLocation(), true );
			}
		}
	}
}

