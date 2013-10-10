package edu.neumont.chess.movements;

public class IllegalMoveException extends Exception {
	private static final long serialVersionUID = 1;

	public IllegalMoveException() {
	}
	
	public IllegalMoveException( String message ) {
		super( message );
	}
}
