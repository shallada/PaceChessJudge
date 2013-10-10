package edu.neumont.chess.model;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.neumont.chess.events.GameLogEvent;
import edu.neumont.chess.events.GameLogEventListener;


public class GameLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private StringBuffer serialized = new StringBuffer();
	
	private transient ChessGame game;
	private transient JFrame frame;
	private transient JTextArea logArea;
	private transient List<GameLogEventListener> listeners = 
		new ArrayList<GameLogEventListener>();
	
	public GameLog( ChessGame game ) {
		this.game = game;
	}
	
	public void addGameLogEventListener( GameLogEventListener l ) {
		if( listeners == null )
			listeners = new ArrayList<GameLogEventListener>();
		listeners.add(l);
	}
	
	public void removeGameLogEventListener( GameLogEventListener l ) {
		if( listeners == null )
			listeners = new ArrayList<GameLogEventListener>();
		listeners.remove(l);
	}
	
	public void logMessage( String message ) {
		if( serialized.length() > 0 )
			serialized.append("\n");
		serialized.append( message );
		updateVisibleLog();
		logEntryAdded();
	}
	
	public void showLog( boolean show ) {
		if( show ) {
			JFrame frame = getLogFrame();
			frame.setVisible(true);
			frame.toFront();
		}
		else
			getLogFrame().dispose();
	}
	
	private JFrame getLogFrame() {
		if( frame == null ) {
			frame = new JFrame( "Log for game: " + game.getPlayer(Team.white) + " vs " + game.getPlayer(Team.black) );
			logArea = new JTextArea();
			logArea.setEditable(false);
			frame.add(new JScrollPane(logArea), BorderLayout.CENTER);
			frame.setSize(400, 500);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
		updateVisibleLog();
		return frame;
	}
	
	@Override
	public String toString() {
		return serialized.toString();
	}
	
	private void updateVisibleLog() {
		if( logArea != null ) {
			logArea.setText( toString() );
		}
	}
	
	protected void logEntryAdded() {
		GameLogEvent e = new GameLogEvent(this);
		if( listeners == null )
			listeners = new ArrayList<GameLogEventListener>();
		for( GameLogEventListener l : listeners )
			l.logEntryAdded(e);
	}
}
