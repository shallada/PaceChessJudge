package edu.neumont.chess.events;

import java.util.EventListener;

public interface GameLogEventListener extends EventListener {
	void logEntryAdded( GameLogEvent e );
}
