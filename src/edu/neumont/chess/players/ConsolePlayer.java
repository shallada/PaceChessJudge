package edu.neumont.chess.players;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.ui.ChessConsole;

public class ConsolePlayer extends Player {
	private ChessConsole console;
	
	public ConsolePlayer( ChessGame game, Team team, ChessConsole console ) {
		super( game, team );
		this.console = console;
	}
	
	private Point[] selectTeamPieces() {
		ArrayList<Point> selections = new ArrayList<Point>();
		Board board = getBoard();
		for( Piece piece : getPieces() ) {
			Point location = board.getLocation(piece);
			if( location != null && piece.getMoves().length > 0 ) {
				selections.add(location);
			}
		}
		
		return selections.toArray( new Point[0] );
	}
	
	private void myTurn() {
		boolean turnTaken = false;
		while( !turnTaken ) {
			Piece piece = getPieceSelection();
			
			// piece will be null if the user forfeited
			if( piece != null ) {
				Move move = getPieceMove(piece);
				
				// move is null when they want to return to the piece selection menu
				if( move != null ) {
					try {
						getGame().performMove( this, move );
						turnTaken = true;
					} catch (IllegalMoveException e) {
						System.err.println("That was an invalid move!");
					}
				}
			}
			else {
				getGame().forfeit(this);
				turnTaken = true;
			}
		}
	}
	
	@Override
	public void takeTurn() {
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				myTurn();
			}
		});
		t.start();
	}
	
	private PrintWriter getOutput() {
		return console.getOutput();
	}
	
	private Scanner getInput() {
		return console.getInput();
	}
	
	private Move getPieceMove( Piece piece ) {
		Move move = null;
		boolean valid = false;
		console.clearConsole();
		Move[] moves = piece.getMoves();
		console.setSelection( piece.getDestinations() );
		do {
			console.redrawBoard();
			getOutput().print("Select a Destination (or 0 to unselect the selected piece): ");
			getOutput().flush();
			String choice = getInput().nextLine();
			if( choice.equals( "0" ) ) {
				valid = true;
			}
			else {
				int max = moves.length;
				try {
					int locationNumber = Integer.parseInt(choice);
					move = moves[locationNumber-1];
					valid = true;
				} catch( Exception e ) {
					console.clearConsole();
					getOutput().println("Invalid selection. Please choose a number between 0 and " + max);
				}
			}
		} while( !valid );
		console.clearSelection();

		return move;
	}
	
	private Piece getPieceSelection() {
		Piece piece = null;
		boolean valid = false;
		Point[] locations = selectTeamPieces();
		console.setSelection(locations);
		do {
			console.redrawBoard();
			getOutput().print("Select a Piece (or 0 to quit): ");
			getOutput().flush();
			String choice = getInput().nextLine();
			if( choice.equals( "0" ) ) {
				valid = true;
			}
			else {
				int max = locations.length;
				try {
					Board board = getBoard();
					int pieceNumber = Integer.parseInt(choice);
					piece = board.getPiece( locations[pieceNumber-1] );
					valid = true;
				} catch( Exception e ) {
					console.clearConsole();
					getOutput().println("Invalid selection. Please choose a number between 0 and " + max);
				}
			}
		} while( !valid );
		console.clearSelection();

		return piece;
	}
}
