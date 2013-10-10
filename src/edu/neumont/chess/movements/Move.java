package edu.neumont.chess.movements;

import edu.neumont.chess.model.Team;

public interface Move {
	void execute();
	void undo();
	boolean isValid(Team team);
	String toStandard();
}
