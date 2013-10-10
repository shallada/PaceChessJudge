package edu.neumont.chess.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventAdapter;
import edu.neumont.chess.events.GameLogEvent;
import edu.neumont.chess.events.GameLogEventListener;
import edu.neumont.chess.logic.GameState;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.GUIPlayer;
import edu.neumont.chess.players.Player;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class ChessGUI extends JFrame implements ChessUI {
	private ChessGame game;
	private JPanel chessGame;
	private JPanel chessBoard;
	private JPanel messagePanel;
	private JLabel messageLabel;
	private JTextArea logArea;
	private JTextArea errorLogArea;
	private JScrollPane logScroll;
	private JScrollPane errorLogScroll;
	private PieceViewer whiteCapturedPieces;
	private PieceViewer blackCapturedPieces;
	private GameStatePanel gameState;
	private Square[][] squares;
	private String defaultTitle;
	private JButton pauseButton;
	private JLabel scoreLabel;
	
	public ChessGUI( ChessGame game ) {
		this( game, "Super Chess" );
	}
	
	public ChessGUI( ChessGame game, String title ) {
		super(title);
		this.defaultTitle = title;
		refreshTitle();

		this.game = game;
		setLayout( new BorderLayout() );
		
		JPanel mainPanel = new JPanel( new BorderLayout() );
		
		messagePanel = new JPanel( new BorderLayout() );
		messageLabel = new JLabel("");
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		messageLabel.setForeground(Color.RED);
		messagePanel.add( new JLabel("Message Center: "), BorderLayout.WEST);
		mainPanel.add( messagePanel, BorderLayout.NORTH );
		
		// Create the game with the grid and the captured pieces
		chessGame = new JPanel( new BorderLayout() );
		mainPanel.add( chessGame, BorderLayout.CENTER );
		
		chessBoard = getChessBoard();
		chessGame.add( chessBoard, BorderLayout.CENTER );
		
		whiteCapturedPieces = new PieceViewer(game, Team.White);
		chessGame.add( whiteCapturedPieces, BorderLayout.SOUTH );
		
		blackCapturedPieces = new PieceViewer(game, Team.Black);
		chessGame.add( blackCapturedPieces, BorderLayout.NORTH );
		
		// Create the status panel
		gameState = new GameStatePanel();
		mainPanel.add( gameState, BorderLayout.EAST );
		
		game.addChessListener( new ChessEventAdapter() {
			@Override
			public void stateChanged(ChessEvent event, Team team,
					GameState oldState, GameState newState) {
				String msg = null;
				if( newState == GameState.CHECK ) {
					msg = team + " is in check";
				}
				else if( newState == GameState.CHECKMATE ) {
					msg = team + " is in checkmate";
				}
				
				if( msg != null ) {
					showMessage(msg);
				}
			}
			
			@Override
			public void turnEnded(ChessEvent event, Player player) {
				invalidate();
			}
			
			@Override
			public void turnStarting(ChessEvent event, Player player) {
				invalidate();
			}
			
			@Override
			public void pauseChanged(ChessEvent event) {
				refreshTitle();
			}
			
			@Override
			public void boardUpdated(ChessEvent event, Board board) {
				refreshScore();
			}
		});
		
		add( mainPanel, BorderLayout.CENTER );
		
		JSplitPane logPanel = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setFont( logArea.getFont().deriveFont(10f) );
		logScroll = new JScrollPane(logArea);
		logScroll.setPreferredSize( new Dimension(300, 300) );
		logPanel.setTopComponent(logScroll);
		errorLogArea = new JTextArea();
		errorLogArea.setEditable(false);
		errorLogArea.setForeground(Color.red);
		errorLogArea.setFont( errorLogArea.getFont().deriveFont(10f) );
		errorLogScroll = new JScrollPane(errorLogArea);
		errorLogScroll.setPreferredSize( new Dimension(300, 300) );
		logPanel.setBottomComponent(errorLogScroll);
		add( logPanel, BorderLayout.EAST );
		game.getLog().addGameLogEventListener( new GameLogEventListener() {
			@Override
			public void logEntryAdded(GameLogEvent e) {
				updateLog();
			}
		});
		game.getErrorLog().addGameLogEventListener( new GameLogEventListener() {
			@Override
			public void logEntryAdded(GameLogEvent e) {
				updateErrorLog();
			}
		});
		updateLog();
		updateErrorLog();
		
		JPanel pausePanel = new JPanel( new BorderLayout() );
		pauseButton = new JButton("");
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( ChessGUI.this.game.isPaused() )
					ChessGUI.this.game.resume();
				else
					ChessGUI.this.game.pause();
			}
		});
		refreshTitle();
		pausePanel.add(pauseButton, BorderLayout.EAST);
		
		scoreLabel = new JLabel( "White: 0     Black: 0" );
		scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD|Font.ITALIC));
		pausePanel.add( scoreLabel, BorderLayout.WEST);
		
		add( pausePanel, BorderLayout.SOUTH );
		
		pack();
		setDefaultCloseOperation( EXIT_ON_CLOSE );
	}
	
	private void refreshScore() {
		int white = 0;
		int black = 0;
		if( game != null ) {
			white = game.getScore(Team.white);
			black = game.getScore(Team.black);
		}
		scoreLabel.setText("White: " + white + "     Black: " + black );
	}
	
	private void refreshTitle() {
		if( game != null && game.isPaused() ) {
			super.setTitle( defaultTitle + " [PAUSED]" );
			if( pauseButton != null )
				pauseButton.setText( "Resume the Game" );
		}
		else {
			super.setTitle( defaultTitle );
			if( pauseButton != null )
				pauseButton.setText( "Pause the Game" );
		}
	}
	
	@Override
	public void setTitle(String title) {
		defaultTitle = title;
		refreshTitle();
	}
	
	private void updateLog() {
		logArea.setText(getGame().getLog().toString());
	}
	
	private void updateErrorLog() {
		errorLogArea.setText(getGame().getErrorLog().toString());
	}
	
	public void showMessage( String message ) {
		SwingUtilities.invokeLater( new Messager(message) );
	}
	
	public Player createPlayer(Team team) {
		return new GUIPlayer(game, team, this);
	}
	
	public ChessGame getGame() {
		return game;
	}
	
	private JPanel getChessBoard() {
		if( chessBoard == null ) {
			int rows = game.getBoard().getRows();
			int cols = game.getBoard().getCols();
			Color[] colors = new Color[] { Color.white, new Color(80, 80, 80) };
			chessBoard = new JPanel( new GridLayout(0, cols+2) );
			squares = new Square[rows][cols];
			
			chessBoard.add(new JLabel());
			for( int c=0; c < cols; c++ ) {
				chessBoard.add(new JLabel(Character.toString((char)(c+'A')), SwingConstants.CENTER));
			}
			chessBoard.add(new JLabel());
			boolean isWhite = true;
			for( int r=0; r < rows; r++ ) {
				chessBoard.add( new JLabel( (rows-r)+"", SwingConstants.CENTER ) );
				for( int c=0; c < cols; c++ ) {
					squares[r][c] = new Square( this.game, new Point( c, r ) );
					int colorIdx = isWhite ? 0 : 1;
					squares[r][c].setBackground( colors[colorIdx] );
					squares[r][c].addMouseListener( new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
						}
						@Override
						public void mouseReleased(MouseEvent e) {
							Square square = (Square)e.getSource();
							if( square.isEnabled() ) {
								squareClicked( square );
							}
						}
					});
					squares[r][c].setEnabled( false );
					squares[r][c].setHighlighted( false );
					chessBoard.add( squares[r][c] );
					isWhite = !isWhite;
				}
				isWhite = !isWhite;
				chessBoard.add( new JLabel((rows-r)+"", SwingConstants.CENTER) );
			}
			chessBoard.add(new JLabel());
			for( int c=0; c < cols; c++ ) {
				chessBoard.add(new JLabel(Character.toString((char)(c+'A')), SwingConstants.CENTER));
			}
			chessBoard.add(new JLabel());
		}
		
		return chessBoard;
	}
	
	public void disableAll() {
		for( int r=0; r < squares.length; r++ ) {
			for( int c=0; c < squares[r].length; c++ ) {
				enableSquare( new Point(c,r), false );
			}
		}
	}
	
	public void setHighlight( Point location, boolean highlight ) {
		if( location.y >= 0 && location.y < squares.length 
				&& location.x >= 0 && location.x < squares[location.y].length ) {
			squares[location.y][location.x].setEnabled(true);
			squares[location.y][location.x].setHighlighted(highlight);
		}
		else
			throw new IndexOutOfBoundsException("Location (" + location.x + "," + location.y+") " +
					"is out of the bounds of the grid.");
	}
	
	public void enableSquare( Point location ) {
		enableSquare( location, true );
	}
	
	public void enableSquare( Point location, boolean enable ) {
		enableSquare( location, enable, enable );
	}
	
	public void enableSquare( Point location, boolean enable, boolean highlight ) {
		if( location.y >= 0 && location.y < squares.length 
				&& location.x >= 0 && location.x < squares[location.y].length ) {
			squares[location.y][location.x].setEnabled(true);
			setHighlight(location, highlight);
		}
		else
			throw new IndexOutOfBoundsException("Location (" + location.x + "," + location.y+") " +
					"is out of the bounds of the grid.");
	}
	
	private void squareClicked( Square square ) {
		Player player = game.getCurrentPlayer();
		if( player instanceof GUIPlayer ) {
			GUIPlayer p = (GUIPlayer)player;
			if( p.getGUI() == this ) {
				p.squareClicked( square.getLocation() );
			}
		}
	}
	
	public static void main(String[] args) {
		ChessGame game = new ChessGame();
		ChessGUI gui = new ChessGUI(game);
		gui.createPlayer( Team.White );
		gui.createPlayer( Team.Black );
		gui.setVisible(true);
		
		game.playGame();
	}
	
	private Messager lastMessage = null;
	private class Messager implements Runnable {
		private String message;
		private Runnable clearMessage = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						if( lastMessage == Messager.this )
							messageLabel.setText("");
					}
				});
			}
		};
		
		public Messager( String message ) {
			lastMessage = this;
			this.message = message;
		}
		
		@Override
		public void run() {
			messageLabel.setText(message);
			messageLabel.paintImmediately(messageLabel.getBounds());
			
			Thread t = new Thread( clearMessage );
			t.start();
		}
	}
}
