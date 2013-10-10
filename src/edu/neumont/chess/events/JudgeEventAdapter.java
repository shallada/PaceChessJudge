package edu.neumont.chess.events;

public class JudgeEventAdapter implements JudgeEventListener {
	@Override
	public void gameBoardUpdated(GameEvent e) {
	}

	@Override
	public void gamesChanged(JudgeEvent e) {
	}

	@Override
	public void teamsChanged(JudgeEvent e) {
	}

	@Override
	public void gameStateChanged(GameEvent e) {
	}

	@Override
	public void timeBetweenTurnsChanged(JudgeEvent e) {
	}
	
	@Override
	public void autoPopupGameChanged(JudgeEvent e) {
	}
	
	@Override
	public void gamePauseChanged(GameEvent e) {
	}
}
