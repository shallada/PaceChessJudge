package edu.neumont.chess;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.ui.ChessAI;
import edu.neumont.chess.ui.Mediator;

public class RandomAIBox {

	public static void main(String[] args) {
		if( args.length > 0 ) {
			System.err.println("Random AI Awake as " + args[0] + "!");
			ChessGame game = new ChessGame();
			Team myTeam = null;
			if( args[0].equals("white") )
				myTeam = Team.White;
			else if( args[0].equals("black") )
				myTeam = Team.Black;
			
			if( myTeam != null ) {
				Mediator mediator = new Mediator(game);
				ChessAI ai = new ChessAI(game);
				ai.createPlayer(myTeam);
				mediator.createPlayerFromConsole(myTeam.other());
				game.playGame();
			}
		}
	}

}
