package edu.neumont.chess.ui;

import java.awt.Point;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventAdapter;
import edu.neumont.chess.logic.GameState;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.model.Board.Square;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.players.ConsolePlayer;
import edu.neumont.chess.players.Player;

public class ChessConsole implements ChessUI {
	private ChessGame game;
	private PrintWriter output;
	private Scanner input;
	
	// Modifications to how the board is displayed
	private Point[] locations;
	
	public ChessConsole( ChessGame game ) {
		this( game, System.in, System.out );
	}
	
	public ChessConsole( ChessGame game, InputStream in, OutputStream out ) {
		this.input = new Scanner( in );
		this.output = new PrintWriter( out, true );
		
		this.game = game;
		game.addChessListener( new ChessEventAdapter() {
			@Override
			public void boardUpdated(ChessEvent event, Board board) {
				redrawBoard();
			}
			
			@Override
			public void gameOver(ChessEvent event) {
				ChessGame game = event.getSource();
				output.println("Game Over!");
				if( game.getLoser() != null ) {
					Team loser = game.getLoser().getTeam();
					Team winner = game.getWinner().getTeam();
					output.println(winner + " wins!");
					if( game.getState(loser) == GameState.FORFEIT ) {
						output.println(loser + " forfeited.");
					}
					else if( game.getState(loser) == GameState.CHECKMATE ) {
						output.println("Checkmate");
					}
				}
				else if( game.getState(Team.White) == GameState.STALEMATE ){
					output.println("Stalemate");
				}
				else {
					output.println("Not sure what happened ...");
				}
			}
			
			@Override
			public void turnStarting(ChessEvent event, Player player) {
				output.println(player+"'s Turn");
			}
			
			@Override
			public void stateChanged(ChessEvent event, Team team,
					GameState oldState, GameState newState) {
				if( newState == GameState.CHECK ) {
					output.println(team + " is in check");
				}
			}
		});
	}
	
	public PrintWriter getOutput() {
		return output;
	}
	
	public Scanner getInput() {
		return input;
	}
	
	public static String center( String s, int width ) {
		int remaining = width - s.length();
		int leftSpaces = remaining / 2;
		int rightSpaces = width - s.length() - leftSpaces;
		String left = "";
		String right = "";
		if( leftSpaces > 0 )
			left = String.format( "%" + leftSpaces + "s", " " );
		if( rightSpaces > 0 ) {
			right = String.format( "%" + rightSpaces + "s", " " );
		}
		return String.format("%s%s%s", left, s, right);
	}
	
	private static String center( char c, int width ) {
		return center( Character.toString(c), width );
	}
	
	private static String center( int i, int width ) {
		return center( Integer.toString(i), width );
	}
	
	public static void drawBoard( Board board, PrintWriter output, Point[] selections ) {
		List<Point> locations = new ArrayList<Point>();
		if( selections != null ) {
			locations = Arrays.asList(selections);
		}
		String[][] displayBoard = new String[board.getRows()][board.getCols()];
		int maxLen = 0;
		for( int r=0; r < board.getRows(); r++ ) {
			for( int c=0; c < board.getCols(); c++ ) {
				Square square = board.getSquare( r, c );
				if( square.hasOccupant() ) {
					Piece piece = square.getOccupant();
					String display = piece.toString();
					Team team = piece.getTeam();
					if( team == Team.White )
						display += "l";
					else
						display += "d";
					displayBoard[r][c] = display;
				}
				else {
					displayBoard[r][c] = "-";
				}
				
				Point location = new Point( c, r );
				int idx = locations.indexOf(location);
				if( idx >= 0 ) {
					displayBoard[r][c] += "(" + (idx+1) + ")";
				}
				
				maxLen = Math.max(maxLen, displayBoard[r][c].length());
			}
		}
		
		if( maxLen < 6 )
			maxLen = 6;

		maxLen+=2;
		
		output.println();
		output.print( center( " ", maxLen ) );
		for( int c=0; c < board.getCols(); c++ ) {
			char letter = (char)(c+'A');
			output.print( center( letter, maxLen ) );
		}
		output.print("\n\n");
		for( int r=0; r < displayBoard.length; r++ ) {
			output.print( center( (7-r)+1, maxLen ) );
			for (int c = 0; c < displayBoard[r].length; c++) {
				output.print( center( displayBoard[r][c], maxLen ) );
			}
			output.print("\n\n");
		}
		output.print( center( " ", maxLen ) );
		for( int c=0; c < board.getCols(); c++ ) {
			char letter = (char)(c+'A');
			output.print( center( letter, maxLen ) );
		}
		output.println("\n");
	}
	
	public void redrawBoard() {
		drawBoard( game.getBoard(), output, locations );
	}
	
	public Player createPlayer(Team team) {
		return new ConsolePlayer(game, team, this);
	}
	
	public void clearConsole() {
		output.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
	}
	
	public void setSelection( Point[] locations ) {
		this.locations = locations;
	}
	
	public void clearSelection() {
		locations = null;
	}
	
	public static void main(String[] args) {
		ChessGame game = new ChessGame();
		if( gameSetup( game, args ) ) {
			game.playGame();
		}
	}
	
	private static boolean gameSetup( ChessGame game, String[] args ) {
		ChessConsole console = new ChessConsole(game);
		console.createPlayer(Team.White);
		console.createPlayer(Team.Black);
		return true;
	}

}
