package edu.neumont.chess.judge;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventAdapter;
import edu.neumont.chess.events.ChessEventListener;
import edu.neumont.chess.events.GameEvent;
import edu.neumont.chess.events.GameEventListener;
import edu.neumont.chess.events.JudgeEvent;
import edu.neumont.chess.events.JudgeEventAdapter;
import edu.neumont.chess.events.JudgeEventListener;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.Player;
import edu.neumont.chess.ui.ChessGUI;
import edu.neumont.chess.ui.Mediator;

public class Game {
	private List<GameEventListener> listeners = new ArrayList<GameEventListener>();

	public void addGameEventListener(GameEventListener l) {
		synchronized (Judge.class) {
			getListeners().add(l);
		}
	}

	public void removeGameEventListener(GameEventListener l) {
		synchronized (Judge.class) {
			getListeners().remove(l);
		}
	}

	private List<GameEventListener> getListeners() {
		if (listeners == null)
			listeners = new ArrayList<GameEventListener>();
		return listeners;
	}

	private PlayerInfo white;
	private PlayerInfo black;
	private GameResult result;

	private Process whiteProcess = null;
	private Process blackProcess = null;
	private ChessGame game = null;
	private ChessGUI gui = null;
	private JudgeEventListener judgeListener = null;
	private ChessEventListener chessListener = new ChessEventAdapter() {
		@Override
		public void gameOver(ChessEvent event) {
			Player winner = game.getWinner();
			if (winner == null)
				setResult(GameResult.DRAW);
			else if (winner.getTeam() == Team.White)
				setResult(GameResult.WHITE_WIN);
			else
				setResult(GameResult.BLACK_WIN);
			stopGame();
		}

		@Override
		public void boardUpdated(ChessEvent event, Board board) {
			synchronized (Judge.class) {
				GameEvent e = new GameEvent(Game.this);
				for (GameEventListener l : getListeners())
					l.gameBoardUpdated(e);
			}
		}

		@Override
		public void pauseChanged(ChessEvent event) {
			GameEvent e = new GameEvent(Game.this);
			for (GameEventListener l : getListeners())
				l.gamePauseChanged(e);
		}
	};

	Game(PlayerInfo white, PlayerInfo black) {
		this.white = white;
		this.black = black;
		result = GameResult.NOT_PLAYED;
	}

	public void resetGame() {
		stopGame();
		setResult(GameResult.NOT_PLAYED);
	}

	public void stopGame() {
		if (this.game != null)
			this.game.resume();
		if (this.whiteProcess != null) {
			try {
				this.whiteProcess.destroy();
			} catch (Exception localException) {
			}
			this.whiteProcess = null;
		}
		if (this.blackProcess != null) {
			try {
				this.blackProcess.destroy();
			} catch (Exception localException1) {
			}
			this.blackProcess = null;
		}
		if (this.gui != null)
			try {
				this.gui.dispose();
			} catch (Exception localException2) {
			}
	}

	// public void stopGame() {
	// if( game != null )
	// game.resume();
	// if( gui != null ) {
	// try {
	// gui.dispose();
	// } catch( Exception e ) {
	// }
	// }
	//
	// Thread stop = new Thread(new Runnable() {
	// @Override
	// public void run()
	// {
	// try {
	// Thread.sleep( 5000 );
	// }
	// catch( InterruptedException e1 ){}
	// if( whiteProcess != null ) {
	// try {
	// whiteProcess.destroy();
	// } catch( Exception e ) {
	// }
	// whiteProcess = null;
	// }
	// if( blackProcess != null ) {
	// try {
	// blackProcess.destroy();
	// } catch( Exception e ) {
	// }
	// blackProcess = null;
	// }
	// }
	// });
	// stop.start();
	// }

	public boolean isPaused() {
		if (game != null)
			return game.isPaused();
		return false;
	}

	public void pause() {
		if (game != null)
			game.pause();
	}

	public void resume() {
		if (game != null)
			game.resume();
	}

	public ChessGame getChessGame() {
		return game;
	}

	public void playGame() {
		synchronized (Judge.class) {
			if (judgeListener == null) {
				judgeListener = new JudgeEventAdapter() {
					@Override
					public void timeBetweenTurnsChanged(JudgeEvent e) {
						if (game != null)
							game.setTimeBetweenTurns(e.getSource()
									.getTimeBetweenTurns());
					}
				};
				Judge.getJudge().addJudgeEventListener(judgeListener);
			}

			resetGame();
			setResult(GameResult.IN_PROGRESS);

			try {
				ProcessBuilder whiteBuilder = new ProcessBuilder("java",
						"-jar", white.getJar().getAbsolutePath(), "white");
				whiteProcess = whiteBuilder.start();
				// whiteProcess = Runtime.getRuntime().exec( "java -jar " +
				// white.getJar().getAbsolutePath() + " white");
				ProcessBuilder blackBuilder = new ProcessBuilder("java",
						"-jar", black.getJar().getAbsolutePath(), "black");
				blackProcess = blackBuilder.start();
				// blackProcess = Runtime.getRuntime().exec( "java -jar " +
				// black.getJar().getAbsolutePath() + " black");
				if (game != null) {
					game.getLog().showLog(false);
					game.removeChessListener(chessListener);
				}
				game = new ChessGame();
				game.setTimeBetweenTurns(Judge.getJudge().getTimeBetweenTurns());

				Mediator mediator = new Mediator(game);
				mediator.createPlayerFromProcess(Team.White, whiteProcess,
						white.getName());
				mediator.createPlayerFromProcess(Team.Black, blackProcess,
						black.getName());
				game.addChessListener(chessListener);
				game.playGame();
			} catch (Exception e) {
				// TODO show message in GUI
				e.printStackTrace();
				if (whiteProcess == null)
					setResult(GameResult.BLACK_WIN);
				else if (blackProcess == null)
					setResult(GameResult.WHITE_WIN);
				else {
					setResult(GameResult.NOT_PLAYED);
					System.err
							.println("............. Something happened ...............");
				}
				stopGame();
			}
		}
	}

	public void showGUI() {
		if (game != null) {
			if (gui != null && gui.getGame() != game) {
				try {
					gui.dispose();
				} catch (Exception e) {
				}
				gui = null;
			}
			if (gui == null) {
				gui = new ChessGUI(game, white.getName() + " vs "
						+ black.getName());
				gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
			gui.setVisible(true);
			gui.toFront();
		}
	}

	public GameResult getResult() {
		return result;
	}

	public void setResult(GameResult result) {
		synchronized (Judge.class) {
			this.result = result;

			GameEvent e = new GameEvent(this);
			for (GameEventListener l : getListeners())
				l.gameStateChanged(e);
		}
	}

	public PlayerInfo getWhite() {
		return white;
	}

	public PlayerInfo getBlack() {
		return black;
	}

	public PlayerInfo getPlayer(Team team) {
		if (team == Team.White)
			return getWhite();
		else if (team == Team.Black)
			return getBlack();
		return null;
	}

	public String getWhiteName() {
		String whiteName = "";
		if (getWhite() != null)
			whiteName = getWhite().getName();
		return whiteName == null ? "" : whiteName;
	}

	public String getBlackName() {
		String blackName = "";
		if (getBlack() != null)
			blackName = getBlack().getName();
		return blackName == null ? "" : blackName;
	}

	public boolean isPlayer(String name) {
		return getWhiteName().equals(name) || getBlackName().equals(name);
	}

	public int getScore(Team team) {
		if (game != null)
			return game.getScore(team);
		else
			return 0;
	}
}
