package edu.neumont.chess;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.ui.ChessGUI;
import edu.neumont.chess.ui.Mediator;


public class GUIBox {
	public static void main(String[] args) {
		String arg = args.length > 0 ? args[0] : "white";
		ChessGame game = new ChessGame();
		Team myTeam = null;
		if( arg.equals("white") )
			myTeam = Team.White;
		else if( arg.equals("black") )
			myTeam = Team.Black;
		
		if( myTeam != null ) {
			Mediator mediator = new Mediator(game);
			ChessGUI gui = new ChessGUI(game, "You are: " + myTeam);
			gui.createPlayer(myTeam);
			mediator.createPlayerFromConsole(myTeam.other());
			gui.setVisible(true);
			game.playGame();
		}
	}
}
