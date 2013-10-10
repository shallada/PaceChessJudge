package edu.neumont.chess.movements;

import edu.neumont.chess.model.Team;

public class IllegalMove implements Move {
	private String move = null;
	
	public IllegalMove( String move ) {
		this.move = move;
	}
	
	@Override
	public void execute() {
	}

	@Override
	public boolean isValid(Team team) {
		return false;
	}

	@Override
	public String toStandard() {
		return move;
	}

	@Override
	public void undo() {
	}

	@Override
	public String toString() {
		return "Illegal Move: " + move;
	}
}
