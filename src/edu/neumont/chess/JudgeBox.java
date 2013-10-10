package edu.neumont.chess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.neumont.chess.events.JudgeEvent;
import edu.neumont.chess.events.JudgeEventAdapter;
import edu.neumont.chess.judge.Game;
import edu.neumont.chess.judge.Judge;
import edu.neumont.chess.judge.PlayerInfo;
import edu.neumont.chess.ui.CompetitionSquare;
import edu.neumont.chess.ui.CornerSquare;
import edu.neumont.chess.ui.Highlightable;
import edu.neumont.chess.ui.JudgeLayout;
import edu.neumont.chess.ui.TallySquare;
import edu.neumont.chess.ui.TitleSquare;

public class JudgeBox extends JFrame {
	private static final long serialVersionUID = 1;

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem loadJarItem;
	private JMenuItem loadJarsItem;
	private JMenuItem removeAllItem;
	private JMenuItem restartAllItem;
	private JMenuItem exitItem;
	private JMenu editMenu;
	private JMenuItem preferences;

	private JDialog preferencesDialog;
	private JPanel preferencesPanel;
	private JCheckBox autoPopupGame;
	private JPanel maxGamesPanel;
	private JSlider maxGamesSlider;
	private JPanel timeBetweenTurnsPanel;
	private JSlider timeBetweenTurnsSlider;
	
	private JPanel resultsPanel;
	//private GridLayout resultsLayout;
	private Highlightable[][] squares;
	private MouseListener squareMouseListener = new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent e) {
			for( int r=0; r < squares.length; r++ ) {
				for( int c=0; c < squares[r].length; c++ ) {
					squares[r][c].setHighlighted(false);
				}
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			if( e.getSource() instanceof TallySquare ) {
				TallySquare square = (TallySquare)e.getSource();
				int row = -1;
				int col = -1;
				for( int r=0; r < squares.length; r++ ) {
					for( int c=0; c < squares[r].length; c++ ) {
						if( squares[r][c] == square ) {
							row = r;
							col = c;
						}
					}
				}
				if( row != -1 && col != -1 ) {
					for( int r=0; r < squares.length; r++ )
						squares[r][col].setHighlighted(true);
					for( int c=0; c < squares[row].length; c++ )
						squares[row][c].setHighlighted(true);
				}
			}
		}
	};
	
	private void tryClose() {
		getJudge().stopGames();
		dispose();
	}

	public JudgeBox() {
		super("Chess Competition Judge");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				tryClose();
			}
		});
		
		getJudge().addJudgeEventListener( new JudgeEventAdapter() {
			private void gamesChanged() {
				redoGameStates();
			}

			@Override
			public void gamesChanged(JudgeEvent e) {
				gamesChanged();
			}

			@Override
			public void teamsChanged(JudgeEvent e) {
				gamesChanged();
			}
			
			@Override
			public void autoPopupGameChanged(JudgeEvent e) {
				boolean auto = e.getSource().getAutoPopupGame();
				if( autoPopupGame.isSelected() != auto )
					autoPopupGame.setSelected( auto );
			}
		});
		
		setJMenuBar( createMenuBar() );

		preferencesDialog = createMaxGamesDialog();
		
		LayoutManager resultsLayout = new JudgeLayout();
		resultsPanel = new JPanel(resultsLayout);
		resultsPanel.setMinimumSize(new Dimension(200,100));
		resultsPanel.setPreferredSize(new Dimension(200,100));
		add( resultsPanel, BorderLayout.CENTER );
		redoGameStates();
		pack();
		
		// Maximize??
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	private JDialog createMaxGamesDialog() {
		JDialog dialog = new JDialog( this, "Preferences", true );
		
		preferencesPanel = new JPanel();
		preferencesPanel.setLayout( new BoxLayout(preferencesPanel, BoxLayout.Y_AXIS) );
		dialog.add(preferencesPanel, BorderLayout.CENTER);

		autoPopupGame = new JCheckBox("Auto Popup Games: ", getJudge().getAutoPopupGame());
		autoPopupGame.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				getJudge().setAutoPopupGame(autoPopupGame.isSelected());
			}
		});
		preferencesPanel.add(autoPopupGame);
		
		maxGamesSlider = new JSlider( 1, 10, getJudge().getMaxRunningGames() );
		maxGamesSlider.setMajorTickSpacing(1);
		maxGamesSlider.setMinorTickSpacing(1);
		maxGamesSlider.setPaintTicks(true);
		maxGamesSlider.setPaintLabels(true);
		maxGamesSlider.setSnapToTicks(true);
		maxGamesSlider.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				getJudge().setMaxRunningGames( slider.getValue() );
			}
		});
		maxGamesPanel = new JPanel( new BorderLayout() );
		maxGamesPanel.setBorder(BorderFactory.createTitledBorder("Max Concurrent Games"));
		maxGamesPanel.add( maxGamesSlider, BorderLayout.CENTER );
		preferencesPanel.add( maxGamesPanel );
		

		Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(0, new JLabel("No Waiting"));
		labels.put(3000, new JLabel("3 Seconds"));
		labels.put(6000, new JLabel("6 Seconds"));
		labels.put(10000, new JLabel("10 Seconds"));
		timeBetweenTurnsSlider = new JSlider( 0, 10000, getJudge().getTimeBetweenTurns() );
		timeBetweenTurnsSlider.setLabelTable(labels);
		timeBetweenTurnsSlider.setMajorTickSpacing(1000);
		timeBetweenTurnsSlider.setMinorTickSpacing(100);
		timeBetweenTurnsSlider.setPaintTicks(true);
		timeBetweenTurnsSlider.setPaintLabels(true);
		timeBetweenTurnsSlider.setSnapToTicks(false);
		timeBetweenTurnsSlider.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				getJudge().setTimeBetweenTurns( slider.getValue() );
			}
		});
		timeBetweenTurnsPanel = new JPanel( new BorderLayout() );
		timeBetweenTurnsPanel.setPreferredSize(new Dimension(500, 75));
		timeBetweenTurnsPanel.setBorder(BorderFactory.createTitledBorder("Time Between Turns"));
		timeBetweenTurnsPanel.add( timeBetweenTurnsSlider, BorderLayout.CENTER );
		preferencesPanel.add( timeBetweenTurnsPanel );

		dialog.pack();
		return dialog;
	}
	
	private JMenuBar createMenuBar() {
		if( menuBar == null ) {
			menuBar = new JMenuBar();
			
			fileMenu = new JMenu( "File" );
			fileMenu.setMnemonic('F');
			menuBar.add(fileMenu);
			
			loadJarItem = new JMenuItem("Load a Jar");
			loadJarItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(getJudge().getLastLoadFolder());
					chooser.setDialogTitle("Choose a jar");
					chooser.setAcceptAllFileFilterUsed(false);
					chooser.setMultiSelectionEnabled(true);
					chooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));
					if( chooser.showOpenDialog(JudgeBox.this) == JFileChooser.APPROVE_OPTION ) {
						for( File selected : chooser.getSelectedFiles() )
							getJudge().addCompetitionTeam(selected);
					}
				}
			});
			fileMenu.add(loadJarItem);
			
			loadJarsItem = new JMenuItem("Load Jars from a Directory");
			loadJarsItem.setMnemonic('L');
			loadJarsItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(getJudge().getLastLoadFolder());
					chooser.setDialogTitle("Choose the directory in which the jars reside");
					chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					if( chooser.showOpenDialog(JudgeBox.this) == JFileChooser.APPROVE_OPTION ) {
						try {
							getJudge().loadCompetitionTeams(chooser.getSelectedFile());
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(JudgeBox.this, e1.getMessage());
						}
					}
				}
			});
			fileMenu.add( loadJarsItem );
			
			removeAllItem = new JMenuItem("Remove all competition teams");
			removeAllItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getJudge().removeAllTeams();
				}
			});
			fileMenu.add(removeAllItem);
			
			restartAllItem = new JMenuItem("Restart/Play All Games");
			restartAllItem.setMnemonic('R');
			restartAllItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getJudge().clearCompetitionData();
					getJudge().runUnplayedGames();
				}
			});
			fileMenu.add( restartAllItem );
			
			fileMenu.add(new JSeparator());
			exitItem = new JMenuItem("Exit");
			exitItem.setMnemonic('x');
			exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tryClose();
				}
			});
			fileMenu.add( exitItem );
			
			editMenu = new JMenu("Edit");
			editMenu.setMnemonic('E');
			menuBar.add( editMenu );
			
			preferences = new JMenuItem("Preferences");
			preferences.setMnemonic('P');
			preferences.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					preferencesDialog.setVisible(true);
				}
			});
			editMenu.add( preferences );
		}
		return menuBar;
	}
	
	public Judge getJudge() {
		return Judge.getJudge();
	}
	
	public void redoGameStates() {
		resultsPanel.removeAll();

		if( squares != null ) {
			for( int i=0; i < squares.length; i++ ) {
				for( int j=0; j < squares[i].length; j++ ) {
					if( squares[i][j] instanceof TallySquare )
						((TallySquare)squares[i][j]).removeMouseListener(squareMouseListener);
					else if( squares[i][j] instanceof CompetitionSquare )
						((CompetitionSquare)squares[i][j]).disassociateWithGame();
				}
			}
		}
		
		String[] teams = getTeams();
		if( teams.length == 0 ) {
			validate();
			repaint();
			return;
		}
		
		int gridSize = teams.length+1;
		
		int panelSize = gridSize * 150;
		panelSize = Math.max(panelSize, 200);
		resultsPanel.setPreferredSize(new Dimension(panelSize, panelSize*2/3));

		GridBagConstraints headerConstraints = new GridBagConstraints();
		headerConstraints.fill = GridBagConstraints.BOTH;
		headerConstraints.weightx = 0;
		headerConstraints.weighty = 0;
		headerConstraints.ipadx = 10;
		headerConstraints.ipady = 10;
		
		GridBagConstraints cellConstraints = new GridBagConstraints();
		cellConstraints.fill = GridBagConstraints.BOTH;
		cellConstraints.weightx = 1;
		cellConstraints.weighty = 1;
		
		// Add the empty cell in the top-left corner of the app
		CornerSquare corner = new CornerSquare();
		headerConstraints.gridx = 0;
		headerConstraints.gridy = 0;
		resultsPanel.add( corner, headerConstraints );
		
		// Add all the header cells
		for( int i=0; i < teams.length; i++ ) {
			JLabel black = new TitleSquare( teams[i] );
			black.setBackground(Color.BLACK);
			black.setOpaque(true);
			black.setForeground(Color.WHITE);
			headerConstraints.gridx = i+1;
			headerConstraints.gridy = 0;
			resultsPanel.add( black, headerConstraints );

			JLabel white = new TitleSquare( teams[i] );
			white.setBackground(Color.WHITE);
			white.setOpaque(true);
			white.setForeground(Color.BLACK);
			headerConstraints.gridx = 0;
			headerConstraints.gridy = i+1;
			resultsPanel.add( white, headerConstraints );
		}
		
		squares = new Highlightable[teams.length][teams.length];
		for( int i=0; i < teams.length; i++ ) {
			for( int j=0; j < teams.length; j++ ) {
				Game game = getGameForTeams( teams[i], teams[j] );
				JComponent square = null;
				if( game != null )
					square = new CompetitionSquare(game);
				else {
					square = new TallySquare(teams[i]);
					square.addMouseListener( squareMouseListener );
				}
				cellConstraints.gridx = j+1;
				cellConstraints.gridy = i+1;
				resultsPanel.add( square, cellConstraints );
				squares[i][j] = (Highlightable)square;
			}
		}
		
		validate();
	}
	
	private String[] getTeams() {
		PlayerInfo[] teams = getJudge().getTeams();
		HashSet<String> teamNames = new HashSet<String>();
		for( PlayerInfo team : teams ) {
			teamNames.add(team.getName());
		}
		String[] sortedTeams = teamNames.toArray( new String[0] );
		Arrays.sort( sortedTeams );
		return sortedTeams;
	}
	
	private Game getGameForTeams( String white, String black ) {
		return Judge.getJudge().getGame(white, black);
	}
	
	public static void main(String[] args) throws IOException {
		JudgeBox grid = new JudgeBox();
		grid.setVisible(true);
	}
}
