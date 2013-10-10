package edu.neumont.chess.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventListener;
import edu.neumont.chess.logic.ChessLogic;
import edu.neumont.chess.logic.GameState;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.players.Player;

public class ChessGame {
	private static final int MAX_TURNS = 50;

	private GameLog log;
	private GameLog errorLog;
	private Board board;
	private Player[] players = new Player[2];
	private HashMap<Team, GameState> states = new HashMap<Team, GameState>();
	private ArrayList<ChessEventListener> listeners = 
		new ArrayList<ChessEventListener>();
	private ArrayList<Move> history =
		new ArrayList<Move>();
	private Team turn = Team.white;
	private int timeBetweenTurns = 0;
	private boolean paused = false;
	
	public ChessGame() {
		board = new Board( this );
		log = new GameLog( this );
		errorLog = new GameLog( this );
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void pause() {
		if( !paused ) {
			paused = true;
			notifyPauseChanged();
		}
	}
	
	private void notifyPauseChanged() {
		ArrayList<ChessEventListener> copy =
			new ArrayList<ChessEventListener>(listeners);
		for( ChessEventListener l : copy ) {
			l.pauseChanged(new ChessEvent(this));
		}
	}
	
	public void resume() {
		if( paused ) {
			paused = false;
			notifyPauseChanged();
		}
	}
	
	public int getTimeBetweenTurns() {
		return timeBetweenTurns;
	}
	
	public void setTimeBetweenTurns( int t ) {
		timeBetweenTurns = t;
	}
	
	public Player getWinner() {
		Player loser = getLoser();
		Player winner = null;
		if( loser != null ) {
			if( loser == players[0] )
				winner = players[1];
			else
				winner = players[0];
		}
		
		return winner;
	}
	
	public Player getLoser() {
		updateGameState();
		
		// Check if white lost
		GameState white = getState( Team.White );
		if( white == GameState.FORFEIT || white == GameState.CHECKMATE ) {
			return players[0];
		}
		
		// Check if black lost
		GameState black = getState( Team.Black );
		if( black == GameState.FORFEIT || black == GameState.CHECKMATE ) {
			return players[1];
		}
		
		// Otherwise, no one has won (stalemate, draw, or game not over)
		return null;
	}
	
	public GameLog getLog() {
		return log;
	}
	
	public GameLog getErrorLog() {
		return errorLog;
	}
	
	public Team getCurrentTurn() {
		return turn;
	}
	
	public Player getCurrentPlayer() {
		return getPlayer(turn);
	}
	
	public Player getPlayer( Team team ) {
		return players[ team == Team.White ? 0 : 1 ];
	}
	
	public void addPlayer( Player player ) {
		if( player.getTeam() == Team.White )
			players[0] = player;
		else
			players[1] = player;
	}
	
	public GameState getState( Team team ) {
		if( !states.containsKey(team) ) {
			states.put( team, GameState.INITIALIZATION );
		}
		return states.get(team);
	}
	
	public void setState( Team team, GameState state ) {
		GameState old = getState( team );
		if( old != state ) {
			log.logMessage(team + "'s state changed to " + state);
			states.put(team, state);
			for( ChessEventListener l : listeners ) {
				l.stateChanged(new ChessEvent(this), team, old, state);
			}
		}
	}
	
	public Board getBoard() {
		return board;
	}
	
	public void performMove( Player player, Move move ) throws IllegalMoveException {
		if( !isOver() && player.getTeam() == turn ) {
			String playerColor = turn.toString();
			String message = playerColor + "'s move: " + (move == null ? "null" : move.toString() );
			log.logMessage(message);
			if( move == null || !move.isValid(player.getTeam()) ) {
				log.logMessage(playerColor + "'s move was invalid");
				throw new IllegalMoveException( "The move: " );
			}
			history.add(move);
			move.execute();
			
			ArrayList<ChessEventListener> copy = new ArrayList<ChessEventListener>(listeners);
			for( ChessEventListener l : copy ) {
				l.boardUpdated(new ChessEvent(this), board);
			}
			
			for( ChessEventListener l : copy ) {
				l.turnEnded( new ChessEvent(this), player );
			}
			
			turn = turn.other();
			notifyNextStart();
		}
	}
	
	public Move getLastMove() {
		if( history.size() == 0 )
			return null;
		else
			return history.get(history.size()-1);
	}
	
	public void forfeit( Player player ) {
		if( !isOver() ) {
			setState( player.getTeam(), GameState.FORFEIT );
			notifyGameOver();
		}
	}
	
	private void notifyGameOver() {
		ArrayList<ChessEventListener> copy = new ArrayList<ChessEventListener>(listeners);
		for( ChessEventListener l : copy )
			l.gameOver( new ChessEvent(this) );
	}
	
	public void playGame() {
		if( players[0] == null || players[1] == null ) {
			throw new IllegalStateException( "Both players were not added to this game." );
		}
		
		for( ChessEventListener l : listeners ) {
			l.gameStarted( new ChessEvent(this) );
		}
		
		this.turn = Team.White;
		notifyNextStart();
	}
	
	private void notifyNextStart() {
		updateGameState();
		if( !isOver() ) {
			try {
				if( getTimeBetweenTurns() > 0 ) {
						Thread.sleep(getTimeBetweenTurns());
				}
				while( isPaused() ) {
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {}
			
			Player player = turn == Team.White ? players[0] : players[1];
			for( ChessEventListener l : listeners ) {
				l.turnStarting(new ChessEvent(this), player);
			}
			try {
				player.takeTurn();
			} catch( Exception e ) {
				forfeit(player);
			}
		} else {
			listeners.clear();
		}
	}
	
	public boolean isGameOver() {
		updateGameState();
		return isOver();
	}
	
	private boolean isOver() {
		return getState(Team.White).isFinalState() || getState(Team.Black).isFinalState();
	}
	
	/**
	 * Updates the state of the player whose turn it is
	 * This method must be called at the BEGINNING of a player's turn
	 */
	private void updateGameState() {
		Team upNext = turn;
		if( !isOver() ) {
			if( ChessLogic.isInCheckMate(board, upNext) ) {
				setState(upNext, GameState.CHECKMATE);
			}
			else if( ChessLogic.isStuck(board, upNext) ) {
				setState(Team.White, GameState.STALEMATE);
				setState(Team.Black, GameState.STALEMATE);
			}
			else if( ChessLogic.isInCheck(board, upNext) ) {
				setState(upNext, GameState.CHECK);
			}
			else if( history.size() >= (MAX_TURNS*2) ) {
				setState(Team.White, GameState.DRAW);
				setState(Team.Black, GameState.DRAW);
			}
			else {
				setState(upNext, GameState.INPROGRESS);
			}
			if( isOver() ) {
				notifyGameOver();
			}
		}
	}
	
	public void addChessListener( ChessEventListener l ) {
		listeners.add(l);
	}
	
	public void removeChessListener( ChessEventListener l ) {
		listeners.remove(l);
	}
	
	public static Point toPoint( String coordinate ) {
		char col = 'A';
		char row = '1';
		if( Pattern.matches("([a-hA-H])([1-8])", coordinate) ) {
			col = Character.toUpperCase( coordinate.charAt(0) );
			row = coordinate.charAt(1);
		}
		else if( Pattern.matches("([1-8])([a-hA-H])", coordinate) ) {
			row = coordinate.charAt(0);
			col = Character.toUpperCase( coordinate.charAt(1) );
		}
		else {
			return null;
		}

		// x = column (A-H)
		// y = row    (8-1) ... the row needs to be translated so that 1 is the top rather than the bottom
		return new Point( col-'A', 7-(row-'1') );
	}
	
	public static String toCoordinate( Point location ) {
		return String.format("%c%d", location.x+'A', 8-location.y);
	}

	public int getScore(Team team) {
		return ChessLogic.getScore( this, team );
	}
}
