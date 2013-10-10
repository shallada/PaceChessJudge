package edu.neumont.chess.players;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventAdapter;
import edu.neumont.chess.events.ChessEventListener;
import edu.neumont.chess.io.ChessScanner;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;

public class RemotePlayer extends Player {
	/** The time in milliseconds for a player to make his move. */
	public static final int MAX_TIME_FOR_TURN = 5000;
	
	private ChessScanner input;
	private PrintWriter output = null;
	private Process process;
	private int turnsTaken = 0;
	private String name;
	private ChessEventListener chessListener = new ChessEventAdapter() {
		private Move lastMove;
		public void turnStarting(ChessEvent event, Player player) {
			lastMove = getGame().getLastMove();
			if( player == RemotePlayer.this && lastMove != null ) {
				output.println( lastMove.toStandard() );
			}
		}
		@Override
		public void gameOver( ChessEvent event ) {
			Move last = getGame().getLastMove();
			if( lastMove != null && lastMove != last ) {
				lastMove = last;
				output.println( last.toStandard() );
				output.flush();
			}
		}
	};
	
	public RemotePlayer(ChessGame game, Team team, Process process, String name) {
		this(game, team, new Scanner(process.getInputStream()), process.getOutputStream());
		this.process = process;
		this.name = name;
		
		StandardErrorHandler handler = new StandardErrorHandler(process);
		handler.start();
	}
	
	public RemotePlayer(ChessGame game, Team team, Scanner input) {
		this( game, team, input, null );
	}
	
	public RemotePlayer(ChessGame game, Team team, Scanner input, OutputStream out) {
		super(game, team);
		this.input = new ChessScanner(game, input);
		if( out != null ) {
			this.output = new PrintWriter(out, true);
			game.addChessListener( chessListener );
		}
	}

	@Override
	public String toString() {
		return name == null ? super.toString() : name;
	}

	@Override
	public void takeTurn() {
		Thread t = new TurnTaker();
		t.start();
	}
	
	private class TurnTaker extends Thread {
		private int currentTurnsTaken;
		private Runnable killInput = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep( MAX_TIME_FOR_TURN );
				} catch (InterruptedException e) {
				}
				if( currentTurnsTaken == turnsTaken ) {
					if( process != null ) {
						process.destroy();
						getGame().getLog().logMessage(getTeam() + " took too long to respond: killed the process");
					}
				}
			}
		};

		public TurnTaker() {
			currentTurnsTaken = turnsTaken;
		}
		
		@Override
		public void run() {
			if( process != null )
				new Thread( killInput ).start();
			
			Move move = null;
			try {
				move = input.next();
			} catch( Exception e ) {
				// Ignore exceptions because if one occurs,
				// the player will forfeit due to move being null
			}
			
			turnsTaken++;
			try {
				getGame().performMove(RemotePlayer.this, move);
			} catch (IllegalMoveException e) {
				getGame().forfeit(RemotePlayer.this);
			}
		}
	}
	
	private class StandardErrorHandler extends Thread {
		private Process process;
		
		public StandardErrorHandler( Process process ) {
			this.process = process;
		}
		
		@Override
		public void run() {
			try {
				Scanner errorInput = new Scanner(process.getErrorStream());
				String line = null;
				do {
					line = errorInput.nextLine();
					if( line != null ) {
						getGame().getErrorLog().logMessage(line);
					}
				} while( line != null );
			}
			catch( Exception e ) {
			}
		}
	}
}
