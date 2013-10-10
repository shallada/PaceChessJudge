package edu.neumont.chess.events;

public interface JudgeEventListener extends GameEventListener {
	void teamsChanged( JudgeEvent e );
	void gamesChanged( JudgeEvent e );
	void timeBetweenTurnsChanged( JudgeEvent e );
	void autoPopupGameChanged( JudgeEvent e );
}
