package edu.neumont.chess.ui;

import java.awt.Dimension;

import javax.swing.JLabel;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class GameStatePanel extends JLabel {
	public GameStatePanel() {
		Dimension preferred = new Dimension( 0, 0 );
		setPreferredSize( preferred );
		setMinimumSize( preferred );
		setMaximumSize( preferred );
	}
}
