package edu.neumont.chess.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.neumont.chess.judge.Game;
import edu.neumont.chess.judge.Judge;

@SuppressWarnings("serial") // this class should not be serialized
public class TitleSquare extends JLabel {
	public static final int MAX_NAME_LENGTH = 10;
	
	private JPopupMenu context;
	
	public TitleSquare( String team ) {
		super( team.length() > MAX_NAME_LENGTH ? 
				team.substring(0,6)+"..." : team,
				CENTER );
		
		context = new TeamContextMenu(team);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON3 )
					context.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	public static JPopupMenu createTeamContextMenu( String teamName ) {
		return new TeamContextMenu(teamName);
	}
	
	private static class TeamContextMenu extends JPopupMenu {
		private JMenuItem removeTeamItem;
		private JMenuItem replayAllItem;
		private String team;

		public TeamContextMenu( String teamName ) {
			this.team = teamName;
			
			removeTeamItem = new JMenuItem("Remove team " + team + " from competition");
			removeTeamItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Judge judge = Judge.getJudge();
					judge.removeCompetitionTeam(team);
				}
			});
			this.add( removeTeamItem );
			
			replayAllItem = new JMenuItem("Rematch all " + team + "'s games");
			replayAllItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Game[] games = Judge.getJudge().getGames(team);
					for( Game game : games ) {
						Judge.getJudge().bringToFrontAndReplay(game);
					}
				}
			});
			this.add( replayAllItem );
		}
	}
}
