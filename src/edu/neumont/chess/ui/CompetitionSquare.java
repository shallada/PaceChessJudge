package edu.neumont.chess.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import edu.neumont.chess.events.GameEvent;
import edu.neumont.chess.events.GameEventListener;
import edu.neumont.chess.judge.Game;
import edu.neumont.chess.judge.GameResult;
import edu.neumont.chess.judge.Judge;
import edu.neumont.chess.logic.GameState;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.piece.Bishop;
import edu.neumont.chess.piece.Knight;
import edu.neumont.chess.piece.Pawn;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.piece.Queen;
import edu.neumont.chess.piece.Rook;
import edu.neumont.chess.players.Player;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class CompetitionSquare extends JPanel implements Highlightable {
	public static final Color DEFAULT_BACKGROUND = new Color( 142, 142, 142 );
	public static final int MIN_FONT_SIZE = 8;
	public static final int MAX_FONT_SIZE = 32;
	
	private static final String IN_PROGRESS = "IN_PROGRESS";
	private static final String GAME_OVER = "GAME_OVER";
	private static final String GAME_PAUSED = "GAME_PAUSED";
	private static ArrayList<Class<? extends Piece>> pieceClasses;
	static {
		pieceClasses = new ArrayList<Class<? extends Piece>>();
		pieceClasses.add(Queen.class);
		pieceClasses.add(Rook.class);
		pieceClasses.add(Bishop.class);
		pieceClasses.add(Knight.class);
		pieceClasses.add(Pawn.class);
	}
	
	private TitledBorder gameBorder;
	private boolean highlighted = false;
	private boolean alwaysShowStats = false;
	private JFrame statsFrame = null;
	
	// Game State Components
	private ChessGUICustomComponent statusImage;
	private JPanel gameStats;
	private JPanel statusPanel;
	private JPanel pausedPanel;
	private JLabel gameStateLabel;
	private Map<Class<? extends Piece>, JLabel> capturedWhites;
	private Map<Class<? extends Piece>, JLabel> capturedBlacks;
	
	// Context Menu Components
	private JPopupMenu context;
	private JLabel gameLabel;
	private JMenuItem showGameItem;
	private JMenuItem showStatsItem;
	private JMenuItem showLogItem;
	private JMenuItem showErrorLogItem;
	private JMenuItem restartGameItem;
	private JMenuItem pauseGameItem;
	
	private Game game;
	private GameEventListener listener = new GameEventListener() {
		@Override
		public void gameBoardUpdated(GameEvent e) {
			updateGameStats();
		}
		
		@Override
		public void gameStateChanged(GameEvent e) {
			updateGameStats();
			boolean canShowGame = e.getSource().getResult() != GameResult.NOT_PLAYED;
			showGameItem.setEnabled(canShowGame);
			showStatsItem.setEnabled(canShowGame);
			showLogItem.setEnabled(canShowGame);
			showErrorLogItem.setEnabled(canShowGame);
			pauseGameItem.setEnabled(canShowGame);
			restartGameItem.setText(canShowGame ? "Rematch" : "Play the Game");
		}
		
		public void gamePauseChanged(GameEvent e) {
			pauseGameItem.setText(e.getSource().isPaused() ? "Resume the Game" : "Pause the Game");
			updateGameStats();
		}
	};
	
	public CompetitionSquare( Game game ) {
		this( game, false );
	}
	
	private CompetitionSquare( Game game, boolean alwaysShowStats ) {
		this.alwaysShowStats = alwaysShowStats;
		this.setLayout(new CardLayout());
		if( game != null )
			this.gameBorder = new TitledBorder(BorderFactory.createLineBorder(DEFAULT_BACKGROUND, 2), game.getWhiteName() + " vs " + game.getBlackName());
		else
			this.gameBorder = new TitledBorder("...");
		this.setBorder(this.gameBorder);
		this.game = game;
		if( game != null ) {
			game.addGameEventListener( listener );
		}
		this.capturedWhites = new HashMap<Class<? extends Piece>, JLabel>();
		this.capturedBlacks = new HashMap<Class<? extends Piece>, JLabel>();
		createAndAddContextMenu();
		createAndAddGameStats();
		
		listener.gameStateChanged(new GameEvent(game));
		resetFontSize();
	}
	
	@Override
	public boolean isHighlighted() {
		return highlighted;
	}
	
	@Override
	public void setHighlighted( boolean highlight ) {
		if( highlighted != highlight ) {
			highlighted = highlight;
			Font oldFont = gameBorder.getTitleFont();
			if( highlight ) {
				if( !oldFont.isBold() )
					gameBorder.setTitleFont(oldFont.deriveFont(Font.BOLD));
			}
			else {
				if( oldFont.isBold() )
					gameBorder.setTitleFont(oldFont.deriveFont(Font.PLAIN));
			}
			resetBorderColor();
			repaint();
		}
	}
	
	private void resetBorderColor() {
		setBorderColor( isHighlighted() ? Color.green : getBackground() );
		if( getBackground() == Color.black ) {
			gameBorder.setTitleColor(Color.white);
			gameStateLabel.setForeground(Color.white);
		}
		else {
			gameBorder.setTitleColor(Color.black);
			gameStateLabel.setForeground(Color.black);
		}
	}
	
	private void setBorderColor( Color color ) {
		gameBorder.setBorder(BorderFactory.createLineBorder(color, 2));
	}
	
	private void createAndAddGameStats() {
		gameStats = new JPanel();
		gameStats.setLayout( new GridLayout(3,0) );
		gameStats.setBackground(DEFAULT_BACKGROUND);
		
		Font capturedFont = new Font( Font.MONOSPACED, Font.BOLD|Font.ITALIC, 12 );
		for( Class<? extends Piece> pieceClass : pieceClasses ) {
			JLabel capturedWhite = new JLabel("0");
			capturedWhite.setHorizontalAlignment(JLabel.CENTER);
			capturedWhite.setForeground(Color.white);
			capturedWhite.setFont(capturedFont);
			capturedWhites.put( pieceClass, capturedWhite );
			gameStats.add(capturedWhite);
		}

		for( Class<? extends Piece> pieceClass : pieceClasses ) {
			ChessGUICustomComponent pieceImg = new ChessGUICustomComponent();
			pieceImg.setOpaque(false);
			String key = "top_" + pieceClass.getSimpleName();
			key = key.toLowerCase();
			pieceImg.setImage( PieceViewer.getImage( key ) );
			gameStats.add(pieceImg);
		}

		for( Class<? extends Piece> pieceClass : pieceClasses ) {
			JLabel capturedBlack = new JLabel("0");
			capturedBlack.setHorizontalAlignment(JLabel.CENTER);
			capturedBlack.setForeground(Color.black);
			capturedBlack.setFont(capturedFont);
			capturedBlacks.put( pieceClass, capturedBlack );
			gameStats.add(capturedBlack);
		}
		add( gameStats, IN_PROGRESS );
		
		statusPanel = new JPanel( new BorderLayout() );
		statusImage = new ChessGUICustomComponent();
		statusImage.setImage(PieceViewer.getImage(GameResult.NOT_PLAYED.toString().toLowerCase()));
		statusPanel.add(statusImage, BorderLayout.CENTER);
		gameStateLabel = new JLabel("Result: " + (game == null ? "Unknown" : game.getResult().toString()));
		statusPanel.add(gameStateLabel, BorderLayout.SOUTH);
		add( statusPanel, GAME_OVER );
		
		pausedPanel = new JPanel( new BorderLayout() );
		JLabel pausedLabel = new JLabel("Game Paused");
		pausedLabel.setHorizontalAlignment(JLabel.CENTER);
		pausedPanel.add(pausedLabel);
		add( pausedPanel, GAME_PAUSED );
	}
	
	private void createAndAddContextMenu() {
		context = new JPopupMenu();
		gameLabel = new JLabel();
		String white = game == null ? "" : game.getWhiteName();
		String black = game == null ? "" : game.getBlackName();
		gameLabel.setText( "Game: " + white + " vs " + black );
		gameLabel.setFont( gameLabel.getFont().deriveFont(gameLabel.getFont().getSize()+2f) );
		context.add( gameLabel );
		context.addSeparator();
		showGameItem = new JMenuItem("Show the game");
		showGameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( game != null )
					game.showGUI();
			}
		});
		context.add(showGameItem);
		
		showStatsItem = new JMenuItem("Show Game Statistics");
		showStatsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( game != null ) {
					if( statsFrame == null ) {
						statsFrame = new JFrame(
								game.getPlayer(Team.white) + 
								" vs " + game.getPlayer(Team.black));
						statsFrame.add( new CompetitionSquare(game, true) );
						statsFrame.setSize(400, 200);
						statsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					}
					statsFrame.setVisible(true);
					statsFrame.toFront();
				}
			}
		});
		if( !alwaysShowStats )
			context.add(showStatsItem);
		
		showLogItem = new JMenuItem("Show the log");
		showLogItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( game != null && game.getChessGame() != null ) {
					game.getChessGame().getLog().showLog(true);
				}
			}
		});
		context.add(showLogItem);
		
		showErrorLogItem = new JMenuItem("Show the error log");
		showErrorLogItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( game != null && game.getChessGame() != null ) {
					game.getChessGame().getErrorLog().showLog(true);
				}
			}
		});
		context.add(showErrorLogItem);
		
		pauseGameItem = new JMenuItem("Pause the Game");
		pauseGameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( game != null ) {
					if( game.isPaused() )
						game.resume();
					else
						game.pause();
				}
			}
		});
		context.add(pauseGameItem);
		
		restartGameItem = new JMenuItem("Restart the game");
		restartGameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restartGame();
			}
		});
		context.add(restartGameItem);
		
		MouseAdapter popupListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON3 && game != null ) {
					context.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton() != MouseEvent.BUTTON3 && game != null ) {
					restartGame();
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor( new Cursor(Cursor.HAND_CURSOR) );
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
			}
		};
		this.addMouseListener(popupListener);
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		resetFontSize();
	}
	
	private void resetFontSize() {
		BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dummy.createGraphics();
		
		// Reset the font size of the captured pieces labels
		float fontSize = MIN_FONT_SIZE;
		Font labelFont = new Font(Font.MONOSPACED, Font.PLAIN, (int)fontSize);
		boolean fits = true;
		do {
			labelFont = labelFont.deriveFont(fontSize);
			fontSize++;
			
			FontMetrics m = g.getFontMetrics(labelFont);
			int width = m.stringWidth("000-000-000") + 30;
			int height = (m.getHeight()+10)*3 + 20;
			fits = width < getWidth() && height < getHeight();
		} while( fits && fontSize < MAX_FONT_SIZE );
		fontSize--;
		labelFont = labelFont.deriveFont(fontSize);
		for( JLabel label : capturedWhites.values() ) {
			label.setFont(labelFont);
		}
		for( JLabel label : capturedBlacks.values() ) {
			label.setFont(labelFont);
		}
		
		// Reset the font size of the titled border
		fontSize = MIN_FONT_SIZE;
		Font titleFont = gameBorder.getTitleFont().deriveFont(fontSize);
		do {
			titleFont = titleFont.deriveFont(fontSize);
			fontSize++;
			
			FontMetrics m = g.getFontMetrics(titleFont);
			int width = m.stringWidth(gameBorder.getTitle()) + 40;
			fits = width < getWidth();
		} while( fits && fontSize <= MAX_FONT_SIZE );
		fontSize--;
		titleFont = gameBorder.getTitleFont().deriveFont(fontSize);
		gameBorder.setTitleFont(titleFont);
		
		fontSize = MIN_FONT_SIZE;
		Font stateFont = gameStateLabel.getFont().deriveFont(fontSize);
		do {
			stateFont = stateFont.deriveFont(fontSize);
			fontSize++;
			
			FontMetrics m = g.getFontMetrics(stateFont);
			int width = m.stringWidth(gameStateLabel.getText()) + 40;
			fits = width < getWidth();
		} while( fits && fontSize <= MAX_FONT_SIZE );
		fontSize--;
		stateFont = gameStateLabel.getFont().deriveFont(fontSize);
		gameStateLabel.setFont(stateFont);
	}
	
	public Game getGame() {
		return game;
	}
	
	public void restartGame() {
		Judge.getJudge().bringToFrontAndReplay(game);
	}
	
	public void disassociateWithGame() {
		if( game != null )
			game.removeGameEventListener( listener );
		game = null;
		updateGameStats();
	}
	
	private void updateGameStats() {
		
		String showCard = null;
		String imageKey = "X";
		Color bgColor = DEFAULT_BACKGROUND;
		if( game != null ) {
			for( Class<? extends Piece> pieceClass : pieceClasses ) {
				int captured = getCapturedCount(Team.black, pieceClass);
				capturedWhites.get(pieceClass).setText(captured+"");
				captured = getCapturedCount(Team.white, pieceClass);
				capturedBlacks.get(pieceClass).setText(captured+"");
			}

			showCard = GAME_OVER;
			imageKey = game.getResult().toString();
			if( game.getResult() == GameResult.BLACK_WIN )
				bgColor = Color.black;
			else if( game.getResult() == GameResult.WHITE_WIN )
				bgColor = Color.white;
			else if( game.getResult() == GameResult.IN_PROGRESS ) {
				showCard = IN_PROGRESS;
			}
		}
		statusImage.setImage(PieceViewer.getImage(imageKey));
		if( game != null ) {
			if( game.getResult().isGameOver() ) {
				String reason = game.getResult().toString();
				if( game.getResult() == GameResult.DRAW ) {
					if( game.getChessGame().getState(Team.white) == GameState.STALEMATE )
						reason = "Stalemate";
					else
						reason = "Draw on turns";
				}
				else {
					Player loser = game.getChessGame().getLoser();
					if( loser == null )
						reason = "Not sure who won ...";
					else {
						Team losingTeam = loser.getTeam();
						GameState state = game.getChessGame().getState(losingTeam);
						if( state == GameState.CHECKMATE )
							reason = losingTeam + " in checkmate";
						else if( state == GameState.FORFEIT )
							reason = losingTeam + " forfeit";
						else
							reason = "Not sure why " + losingTeam + " lost";
					}
				}
				gameStateLabel.setText( "Game State: " + reason );
			}
			else
				gameStateLabel.setText("Game State: " + game.getResult());
		}
		else
			gameStateLabel.setText("Game State: Unknown");
		
		if( showCard == null ) {
			showCard = GAME_OVER;
		}
		
		if( alwaysShowStats ) {
			showCard = IN_PROGRESS;
			bgColor = DEFAULT_BACKGROUND;
		}
		else if( game != null && game.isPaused() ) {
			showCard = GAME_PAUSED;
			bgColor = DEFAULT_BACKGROUND;
		}
		
		CardLayout cl = (CardLayout)(this.getLayout());
		cl.show(this, showCard);
		
		setBackground(bgColor);
		statusPanel.setBackground(bgColor);
		gameStats.setBackground(bgColor);
		pausedPanel.setBackground(bgColor);
		resetBorderColor();
	}
	
	private int getCapturedCount( Team team, Class<? extends Piece> pieceClass ) {
		ChessGame chessGame = game.getChessGame();
		if( chessGame == null )
			return 0;
		Player player = chessGame.getPlayer(team);
		Piece[] pieces = player.getPieces();
		int captured = 0;
		for( Piece piece : pieces ) {
			if( piece.getClass() == pieceClass && piece.getLocation() == null ) {
				captured++;
			}
		}
		return captured;
	}
}
