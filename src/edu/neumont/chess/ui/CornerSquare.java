package edu.neumont.chess.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JComponent;

@SuppressWarnings("serial") // this class should not be serialized
public class CornerSquare extends JComponent {
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Color saveColor = g.getColor();
		
		// Draw the black triangle
		g.setColor(Color.black);
		Polygon blackTri = new Polygon();
		blackTri.addPoint(0, 0);
		blackTri.addPoint(getWidth(),0);
		blackTri.addPoint(getWidth(),getHeight());
		g.fillPolygon(blackTri);
		
		// Draw the black triangle
		g.setColor(Color.white);
		Polygon whiteTri = new Polygon();
		whiteTri.addPoint(0, 0);
		whiteTri.addPoint(0,getHeight());
		whiteTri.addPoint(getWidth(),getHeight());
		g.fillPolygon(whiteTri);
		
		g.setColor(saveColor);
	}
}
