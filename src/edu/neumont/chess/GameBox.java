package edu.neumont.chess;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neumont.chess.io.ChessScanner;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.players.Player;
import edu.neumont.chess.ui.ChessAI;
import edu.neumont.chess.ui.ChessConsole;
import edu.neumont.chess.ui.ChessGUI;

public class GameBox {
	public static final String PLAYER_AI_RANDOM = "random";
	public static final String PLAYER_CONSOLE = "console";
	public static final String PLAYER_GUI = "gui";
	
	private static Map<String, String> params = 
		new HashMap<String, String>();
	private static ChessConsole console = null;
	private static ChessGUI gui = null;
	private static ChessAI ai = null;
	private static ChessGame game = new ChessGame();

	private static String[] stripParams( String[] args ) {
		List<String> remaining = new ArrayList<String>();
		List<String> toProcess = new ArrayList<String>( Arrays.asList( args ) );
		while( toProcess.size() > 0 ) {
			String top = toProcess.remove(0);
			if( top.startsWith("--") ) {
				String param = top.substring(2).toLowerCase();
				String value = toProcess.size()>0 ? toProcess.remove(0) : "";
				params.put(param, value);
			}
			else if( top.startsWith("-") ) {
				String param = top.substring(1).toLowerCase();
				String value = "";
				params.put(param, value);
			}
			else {
				remaining.add( toProcess.remove(0) );
			}
		}
		
		if( !params.containsKey("white") ) {
			params.put("white", PLAYER_GUI);
		}
		if( !params.containsKey("black") ) {
			params.put("black", PLAYER_GUI);
		}
		
		return remaining.toArray(new String[0]);
	}
	
	private static ChessGUI getGUI() {
		if( gui == null ) {
			gui = new ChessGUI(game);
			gui.setVisible(true);
		}
		return gui;
	}
	
	private static ChessConsole getConsole() {
		if( console == null ) {
			console = new ChessConsole(game);
		}
		return console;
	}
	
	private static ChessAI getAI() {
		if( ai == null ) {
			ai = new ChessAI(game);
		}
		return ai;
	}
	
	private static Player createPlayer( Team team ) {
		String teamKey = team.toString().toLowerCase();
		String type = PLAYER_GUI;
		if( params.containsKey(teamKey) ) {
			type = params.get(teamKey).toLowerCase();
		}
		
		Player player = null;
		if( type.equals(PLAYER_CONSOLE) ) {
			player = getConsole().createPlayer(team);
		}
		else if( type.equals(PLAYER_AI_RANDOM) ) {
			player = getAI().createPlayer(team);
		}
		else { // anything else (including GUI)
			player = getGUI().createPlayer(team);
		}
		
		return player;
	}
	
	private static void showHelp() {
		System.out.printf(
				"Usage: GameBox [options]\n" +
				"\n" +
				"where options include:\n" +
				"\t--help\t\t\tdisplay this help and exit\n" +
				"\t--setup <file>\t\treads commands from a file and executes them prior to the game\n" +
				"\t--white <player type>\tdefines where the input for the white player will come from\n" +
				"\t--black <player type>\tdefines where the input for the black player will come from\n" +
				"\n" +
				"player type can be:\n" +
				"\t%s\t\tcommands taken from a GUI\n" +
				"\t%s\t\tcommands taken from the console\n" +
				"\t%s\t\tcommands auto-generated from a random ai\n" +
				"\t<jar file>\tcommands taken from the output of the specified jar file\n",
				PLAYER_GUI, PLAYER_CONSOLE, PLAYER_AI_RANDOM );
		System.exit(0);
	}
	
	public static void main(String[] args) {
		args = stripParams(args);
		
		if( params.containsKey("help") || params.containsKey("h") ) {
			showHelp(); // this method exits the app
		}
		
		if( params.containsKey("setup") ) {
			String filename = params.get("setup");
			File setupFile = new File(filename);
			try {
				ChessScanner moves = new ChessScanner(game, setupFile);
				for( Move move : moves ) {
					move.execute();
				}
			} catch (FileNotFoundException e) {
				System.err.printf("Setup file [%s] was not found.\n", filename);
			}
		}
		
		createPlayer(Team.White);
		createPlayer(Team.Black);
		game.playGame();
	}
}
