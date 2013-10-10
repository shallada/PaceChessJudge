package edu.neumont.chess.ui;

import edu.neumont.chess.model.Team;
import edu.neumont.chess.players.Player;

public interface ChessUI {
	Player createPlayer( Team team );
}
