package edu.neumont.chess.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import edu.neumont.chess.events.GameEvent;
import edu.neumont.chess.events.JudgeEventAdapter;
import edu.neumont.chess.judge.Game;
import edu.neumont.chess.judge.Judge;
import edu.neumont.chess.model.Team;

import static edu.neumont.chess.ui.CompetitionSquare.MIN_FONT_SIZE;
import static edu.neumont.chess.ui.CompetitionSquare.MAX_FONT_SIZE;
import static edu.neumont.chess.ui.CompetitionSquare.DEFAULT_BACKGROUND;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class TallySquare extends JPanel implements Highlightable {
	private String team;
	private JLabel teamLabel;
	private JLabel whiteStats;
	private JLabel blackStats;
	private JLabel totalStats;
	private JLabel scoreStats;
	private JPopupMenu context;
	
	private boolean highlighted = false;

	public TallySquare( String team ) {
		this.team = team;
		this.setBackground(DEFAULT_BACKGROUND);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createLineBorder(DEFAULT_BACKGROUND, 2));
		
		teamLabel = new JLabel( team + ":" );
		teamLabel.setForeground(Color.black);
		add( teamLabel );
		whiteStats = new JLabel();
		whiteStats.setForeground(Color.white);
		add( whiteStats );
		blackStats = new JLabel();
		blackStats.setForeground(Color.black);
		add( blackStats );
		totalStats = new JLabel();
		totalStats.setForeground(new Color(200,200,200));
		add( totalStats );
		scoreStats = new JLabel();
		scoreStats.setForeground(Color.black);
		add(scoreStats);
		
		context = TitleSquare.createTeamContextMenu(team);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON3 ) {
					context.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		Judge.getJudge().addJudgeEventListener(new JudgeEventAdapter() {
			@Override
			public void gameBoardUpdated(GameEvent e) {
				if( e.getSource().isPlayer(TallySquare.this.team) )
					updateScore();
			}
			
			@Override
			public void gameStateChanged(GameEvent e) {
				if( e.getSource().isPlayer(TallySquare.this.team) )
					updateTally();
			}
		});
		updateTally();
		resetFontSizes();
	}
	
	private void updateTally() {
		Game[] games = Judge.getJudge().getGames(team);
		int wonAsWhite = 0;
		int lostAsWhite = 0;
		int tiedAsWhite = 0;
		int wonAsBlack = 0;
		int lostAsBlack = 0;
		int tiedAsBlack = 0;

		int score = 0;
		for( Game game : games ) {
			boolean isWhite = game.getWhiteName().equals(team);
			switch( game.getResult() ) {
				case WHITE_WIN:
					wonAsWhite += isWhite ? 1 : 0;
					lostAsBlack += isWhite ? 0 : 1;
					break;
				case BLACK_WIN:
					wonAsBlack += isWhite ? 0 : 1;
					lostAsWhite += isWhite ? 1 : 0;
					break;
				case DRAW:
					tiedAsWhite += isWhite ? 1 : 0;
					tiedAsBlack += isWhite ? 0 : 1;
					break;
			}
			score += game.getScore(isWhite ? Team.white : Team.black);
		}
		
		String format = "%2d - %2d - %2d";
		this.whiteStats.setText(
				String.format(format, wonAsWhite, lostAsWhite, tiedAsWhite));
		this.blackStats.setText(
				String.format(format, wonAsBlack, lostAsBlack, tiedAsBlack));
		this.totalStats.setText(
				String.format(format, wonAsWhite+wonAsBlack, 
						lostAsWhite+lostAsBlack, tiedAsWhite+tiedAsBlack));
		updateScore();
	}
	
	int hhh = 0;
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		resetFontSizes();
	}
	
	private void resetFontSizes() {
		BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dummy.createGraphics();
		
		float fontSize = MIN_FONT_SIZE;
		Font titleFont = new Font(Font.MONOSPACED, Font.BOLD|Font.ITALIC, (int)fontSize);
		Font labelFont = new Font(Font.MONOSPACED, Font.PLAIN, (int)(fontSize-2));
		boolean fits = true;
		int width;
		do {
			int height = 0;
			titleFont = titleFont.deriveFont(fontSize);
			FontMetrics m = g.getFontMetrics(titleFont);
			width = m.stringWidth(teamLabel.getText())+20;
			height = m.getHeight()+2;
			fits = width < getWidth();
			
			if( fits ) {
				labelFont = labelFont.deriveFont(fontSize-2);
				m = g.getFontMetrics(labelFont);
				width = m.stringWidth(totalStats.getText())+20;
				height += (m.getHeight()+2)*4;
				fits = width < getWidth() && height < getHeight();
			}
			fontSize++;
		} while( fits && fontSize <= MAX_FONT_SIZE );
		fontSize--;
		titleFont = titleFont.deriveFont(fontSize);
		teamLabel.setFont(titleFont);
		
		labelFont = labelFont.deriveFont(fontSize-2);
		whiteStats.setFont(labelFont);
		blackStats.setFont(labelFont);
		totalStats.setFont(labelFont);
		scoreStats.setFont(labelFont);
	}
	
	private void updateScore() {
		Game[] games = Judge.getJudge().getGames(team);
		int score = 0;
		for( Game game : games ) {
			boolean isWhite = game.getWhiteName().equals(team);
			score += game.getScore(isWhite ? Team.white : Team.black);
		}
		this.scoreStats.setText( "Score: " + score );
	}
	
	@Override
	public boolean isHighlighted() {
		return highlighted;
	}
	
	@Override
	public void setHighlighted( boolean highlight ) {
		if( highlighted != highlight ) {
			highlighted = highlight;
			if( highlight ) {
				setBorder(BorderFactory.createLineBorder(Color.green, 2));
			}
			else {
				setBorder(BorderFactory.createLineBorder(getBackground(), 2));
			}
			repaint();
		}
	}
}
