package edu.neumont.chess.judge;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import edu.neumont.chess.events.GameEvent;
import edu.neumont.chess.events.GameEventListener;
import edu.neumont.chess.events.JudgeEvent;
import edu.neumont.chess.events.JudgeEventAdapter;
import edu.neumont.chess.events.JudgeEventListener;
import edu.neumont.utils.ExtensionFilter;

public class Judge {
	private static FilenameFilter jarFilter = ExtensionFilter.JARS;
	private static final int DEFAULT_RUNNING_GAMES_MAX = 1;
	private static final int DEFAULT_TIME_BETWEEN_TURNS = 500;
	
	private static Judge judge;
	
	private static Preferences preferences = Preferences.userNodeForPackage(Judge.class);
	
	static {
		judge = new Judge();
		judge.runningGamesMax = preferences.getInt("maxRunningGames", 1);
		judge.timeBetweenTurns = preferences.getInt("timeBetweenTurns", 0);
		judge.autoPopupGame = preferences.getBoolean("autoPopupGames", true);
		judge.lastLoadFolder = preferences.get("lastLoadFolder", System.getProperty("user.home"));
		String loadedJars = preferences.get("loadedJars","");
		String[] split = loadedJars.split(",");
		for( String jar : split ) {
			File jarFile = new File(jar);
			if( jarFile.exists() )
				judge.addCompetitionTeam(jarFile, false, false);
		}
		
		new Thread( judge.monitor ).start();
	}
	
	public static void save() {
		Judge judge = getJudge();
		preferences.putInt("maxRunningGames", judge.getMaxRunningGames());
		preferences.putInt("timeBetweenTurns", judge.getTimeBetweenTurns());
		preferences.putBoolean("autoPopupGames", judge.getAutoPopupGame());
		StringBuilder loadedJars = new StringBuilder();
		ArrayList<String> copy = new ArrayList<String>( judge.loadedJars );
		for( String jar : copy ) {
			loadedJars.append( ( loadedJars.length() > 0 ? "," : "" ) + jar ); 
		}
		preferences.put("loadedJars", loadedJars.toString());
		String lastLoadFolder = judge.getLastLoadFolder() == null ? 
				System.getProperty("user.home") :
				judge.getLastLoadFolder();
		preferences.put("lastLoadFolder", lastLoadFolder);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static Judge getJudge() {
		return judge;
	}
	
	
	private int runningGamesMax = DEFAULT_RUNNING_GAMES_MAX;
	private int timeBetweenTurns = DEFAULT_TIME_BETWEEN_TURNS;
	private ArrayList<JudgeEventListener> listeners =
		new ArrayList<JudgeEventListener>();
	private String lastLoadFolder = null;
	private List<PlayerInfo> teams = new ArrayList<PlayerInfo>();
	private List<String> loadedJars = new ArrayList<String>();
	private boolean autoPopupGame = false;
	private List<Game> games = new ArrayList<Game>();
	private ArrayList<Game> gameQueue =
		new ArrayList<Game>();
	private GameQueueMonitor monitor =
		new GameQueueMonitor();
	private GameEventListener gameListener = new GameEventListener() {
		@Override
		public void gameBoardUpdated(GameEvent e) {
			ArrayList<JudgeEventListener> copy = new ArrayList<JudgeEventListener>( getListeners() );
			for( JudgeEventListener l : copy ) {
				l.gameBoardUpdated(e);
			}
		}
		
		@Override
		public void gameStateChanged(GameEvent e) {
			ArrayList<JudgeEventListener> copy = new ArrayList<JudgeEventListener>( getListeners() );
			for( JudgeEventListener l : copy ) {
				l.gameStateChanged(e);
			}
		}
		
		public void gamePauseChanged(GameEvent e) {
			ArrayList<JudgeEventListener> copy = new ArrayList<JudgeEventListener>( getListeners() );
			for( JudgeEventListener l : copy ) {
				l.gamePauseChanged(e);
			}
		}
	};

	private Judge() {
		// Make everyone go through the getJudge() static method
	}
	
	public String getLastLoadFolder() {
		return lastLoadFolder;
	}
	
	public boolean getAutoPopupGame() {
		return autoPopupGame;
	}
	
	public void setAutoPopupGame( boolean auto ) {
		if( this.autoPopupGame != auto ) {
			this.autoPopupGame = auto;
			save();
			
			JudgeEvent e = new JudgeEvent(this);
			ArrayList<JudgeEventListener> copy = new ArrayList<JudgeEventListener>( getListeners() );
			for( JudgeEventListener l : copy ) {
				l.autoPopupGameChanged(e);
			}
		}
	}
	
	public int getTimeBetweenTurns() {
		return timeBetweenTurns;
	}
	
	public void setTimeBetweenTurns( int time ) {
		timeBetweenTurns = time;
		save();

		JudgeEvent e = new JudgeEvent(this);
		ArrayList<JudgeEventListener> copy = new ArrayList<JudgeEventListener>( getListeners() );
		for( JudgeEventListener l : copy ) {
			l.timeBetweenTurnsChanged(e);
		}
	}
	
	public int getMaxRunningGames() {
		return runningGamesMax;
	}
	
	public void setMaxRunningGames( int max ) {
		runningGamesMax = max;
		save();
	}
	
	private synchronized ArrayList<JudgeEventListener> getListeners() {
		return listeners;
	}
	
	public void addJudgeEventListener( JudgeEventListener l ) {
		getListeners().add(l);
	}
	
	public void removeJudgeEventListener( JudgeEventListener l ) {
		getListeners().remove(l);
	}
	
	public void stopGames() {
		monitor.stop();
	}
	
	public void runUnplayedGames() {
		synchronized( GameQueueMonitor.class ) {
			gameQueue.clear();
			for( Game game : games ) {
				if( game.getResult() == GameResult.NOT_PLAYED ) {
					gameQueue.add(game);
				}
			}
		}
	}
	
	public Game getGame( String team1, String team2 ) {
		Game[] games = getGames();
		for( int i=0; i < games.length; i++ ) {
			String white = games[i].getWhite().getName();
			String black = games[i].getBlack().getName();
			if( white.equals(team1) && black.equals(team2) ) {
				return games[i];
			}
		}
		return null;
	}
	
	public void bringToFrontAndReplay( Game game ) {
		if( game != null ) {
			game.resetGame();
			synchronized( GameQueueMonitor.class ) {
				if( gameQueue.contains(game) )
					gameQueue.remove(game);
				gameQueue.add(game);
			}
		}
	}
	
	public void bringToFrontAndReplay( String team1, String team2 ) {
		bringToFrontAndReplay( getGame(team1, team2) );
	}
	
	public boolean addCompetitionTeam( String jar ) {
		return addCompetitionTeam(new File(jar));
	}
	
	public boolean addCompetitionTeam( File jar ) {
		return addCompetitionTeam(jar, true, true);
	}
	
	public void removeCompetitionTeam( String teamName ) {
		ArrayList<PlayerInfo> copy =
			new ArrayList<PlayerInfo>(teams);
		for( PlayerInfo player : copy )
			if( player.getName().equals(teamName) )
				removeCompetitionTeam(player);
	}
	
	public void removeCompetitionTeam( PlayerInfo team ) {
		if( this.loadedJars.remove(team.getJar().getAbsolutePath()) ) {
			validateGames(false);
			save();
			notifyGamesChanged();
		}
	}
	
	public void removeAllTeams() {
		loadedJars.clear();
		validateGames(true);
		save();
	}

	public void loadCompetitionTeams( String directory ) throws IOException {
		loadCompetitionTeams(new File(directory));
	}
	
	public void loadCompetitionTeams( File directory ) throws IOException {
		doLoad( directory );
		save();
	}
	
	private boolean addCompetitionTeam( File jar, boolean notify, boolean save ) {
		lastLoadFolder = jar.getParent();
		String name = jar.getAbsolutePath();
		if( !loadedJars.contains(name)) {
			loadedJars.add(name);
			validateGames(notify);
			if( save )
				save();
			return true;
		}
		return false;
	}
	
	private void notifyGamesChanged() {
		JudgeEvent e = new JudgeEvent(this);
		ArrayList<JudgeEventListener> listeners = 
			new ArrayList<JudgeEventListener>(getListeners());
		for( JudgeEventListener l : listeners ) {
			l.gamesChanged(e);
		}
	}
	
	private synchronized void validateGames( boolean notify ) {

		boolean modifiedGames = false;
		
		ArrayList<String> jarCopy = new ArrayList<String>(loadedJars);
		for( String jar : jarCopy ) {
			PlayerInfo info = new PlayerInfo(jar);
			if( !teams.contains(info) ) {
				teams.add(info);
				modifiedGames = true;
			}
		}
		
		ArrayList<PlayerInfo> copy =
			new ArrayList<PlayerInfo>( this.teams );
		for( int i=copy.size()-1; i>=0; i-- ) {
			PlayerInfo info = copy.get(i);
			if( !loadedJars.contains(info.getJar().getAbsolutePath()) ) {
				teams.remove(info);
				copy.remove(info);
			}
		}
		
		
		ArrayList<Game> toRemove = new ArrayList<Game>();
		synchronized (GameQueueMonitor.class) {
			for( PlayerInfo white : copy ) {
				for( PlayerInfo black : copy ) {
					if( white != black && 
							getGame(white.getName(), black.getName()) == null) {
						Game game = new Game(white, black);
						game.addGameEventListener(gameListener);
						games.add(game);
						modifiedGames = true;
					}
				}
			}

			for( int i=games.size()-1; i >= 0; i-- ) {
				Game game = games.get(i);
				PlayerInfo white = game.getWhite();
				PlayerInfo black = game.getBlack();
				if( !teams.contains(white) || !teams.contains(black) ) {
					toRemove.add(game);
					modifiedGames = true;
				}
			}
		}
		
		for( Game game : toRemove ) {
			games.remove(game);
			game.stopGame();
		}
		
		if( modifiedGames && notify )
			notifyGamesChanged();
	}
	
	private void doLoad( File directory ) {
		if( directory.exists() && directory.isDirectory() ) {
			boolean added = false;
			File[] jars = directory.listFiles( jarFilter );
			for( File jar : jars ) {
				added |= addCompetitionTeam(jar, false, false);
			}
			
			if( added )
				notifyGamesChanged();
		}
	}
	
	public void clearCompetitionData() {
		Game[] games = getGames();
		for( int i=0; i < games.length; i++ ) {
			games[i].resetGame();
		}
	}
	
	public void clearCompetitionData( String competitionTeam ) {
		Game[] games = getGames();
		for( int i=0; i < games.length; i++ ) {
			String white = games[i].getWhite().getName();
			String black = games[i].getBlack().getName();
			if( white.equals(competitionTeam) || black.equals(competitionTeam) ) {
				games[i].resetGame();
			}
		}
	}
	
	public void clearCompetitionData( String competitionTeam1, String competitionTeam2 ) {
		Game game = getGame(competitionTeam1, competitionTeam2);
		if( game != null )
			game.resetGame();
	}
	
	public synchronized Game[] getGames( String team ) {
		ArrayList<Game> gamesForTeam = new ArrayList<Game>();
		Game[] games = getGames();
		for( Game game : games ) {
			if( game.isPlayer(team) )
				gamesForTeam.add(game);
		}
		return gamesForTeam.toArray( new Game[0] );
	}
	
	public synchronized Game[] getGames() {
		return games.toArray(new Game[0]);
	}
	
	public synchronized PlayerInfo[] getTeams() {
		return teams.toArray(new PlayerInfo[0]);
	}
	
	private class GameQueueMonitor implements Runnable {
		private boolean running = true;
		private ArrayList<Game> runningGames = new ArrayList<Game>();
		
		public GameQueueMonitor() {
			addJudgeEventListener( new JudgeEventAdapter() {
				@Override
				public void gameStateChanged(GameEvent e) {
					Game game = e.getSource();
					GameResult result = game.getResult();
					if( result.isGameOver() ) {
						synchronized( GameQueueMonitor.class ) {
							runningGames.remove(game);
						}
					}
				}
			});
		}
		
		public void stop() {
			running = false;
			synchronized( GameQueueMonitor.class ) {
				for( Game game : runningGames ) {
					game.stopGame();
				}
				runningGames.clear();
			}
		}
		
		@Override
		public void run() {
			while( running ) {
				try {
					synchronized( GameQueueMonitor.class ) {
						if( gameQueue.size() > 0 && runningGames.size() < runningGamesMax ) {
							Game top = gameQueue.get(gameQueue.size()-1);
							gameQueue.remove(top);
							runningGames.add(top);
							Thread play = new Thread( new PlayGameThread(top) );
							play.start();
						}
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private static class PlayGameThread implements Runnable {
		private Game game;
		public PlayGameThread( Game game ) {
			this.game = game;
		}
		
		@Override
		public void run() {
			this.game.playGame();
			if( getJudge().getAutoPopupGame() ) {
				game.showGUI();
			}
		}
	}
}