package edu.neumont.chess.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.Player;
import edu.neumont.chess.players.RemotePlayer;

public class Mediator {
	private ChessGame game;
	
	public Mediator( ChessGame game ) {
		this.game = game;
	}

	public Player createPlayerFromProcess(Team team, Process process, String name) {
		return new RemotePlayer(game, team, process, name);
	}
	
	public Player createPlayerFromFile( Team team, File file ) throws FileNotFoundException {
		return new RemotePlayer(game, team, new Scanner(file));
	}
	
	public Player createPlayerFromConsole( Team team ) {
		Scanner input = new Scanner( System.in );
		RemotePlayer player = new RemotePlayer(game, team, input);
		game.addPlayer(player);
		return player;
	}
}
