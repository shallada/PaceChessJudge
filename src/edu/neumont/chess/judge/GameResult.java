package edu.neumont.chess.judge;

public enum GameResult {
	WHITE_WIN, BLACK_WIN, DRAW, NOT_PLAYED, IN_PROGRESS;
	
	public boolean isGameOver() {
		return this == WHITE_WIN || this == BLACK_WIN || this == DRAW;
	}
}
