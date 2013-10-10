package edu.neumont.chess.events;

import java.util.EventObject;

import edu.neumont.chess.judge.Judge;

public class JudgeEvent extends EventObject {
	private static final long serialVersionUID = 1;

	public JudgeEvent( Judge source ) {
		super( source );
	}
	
	@Override
	public Judge getSource() {
		return (Judge)super.getSource();
	}
}
