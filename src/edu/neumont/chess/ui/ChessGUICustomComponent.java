package edu.neumont.chess.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class ChessGUICustomComponent extends JComponent implements Highlightable {
	private BufferedImage image;
	private boolean highlighted;

	public ChessGUICustomComponent() {
		setDoubleBuffered(true);
		setOpaque(true);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage( BufferedImage image ) {
		this.image = image;
		repaint();
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Color saveColor = g.getColor();
		if( isOpaque() ) {
			g.setColor( getBackground() );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}

		if( image != null ) {
			int MAX_WIDTH = getWidth() - 10;
			int MAX_HEIGHT = getHeight() - 10;
			double imgWidth = image.getWidth();
			double imgHeight = image.getHeight();
			int newHeight = MAX_HEIGHT;
			int newWidth = (int)(imgWidth / imgHeight * newHeight);
			if( newWidth > MAX_WIDTH ) {
				newWidth = MAX_WIDTH;
				newHeight = (int)(imgHeight / imgWidth * newWidth);
			}
			int x = (getWidth()-newWidth)/2;
			int y = (getHeight()-newHeight)/2;
			g.drawImage(image, x, y, newWidth, newHeight, this);
		}
		if( isHighlighted() ) {
			Graphics2D g2 = (Graphics2D)g;
			Stroke saveStroke = g2.getStroke();
			g2.setStroke( new BasicStroke(4) );
			g.setColor( Color.green );
			g.drawRoundRect( 0, 0, getWidth(), getHeight(), 15, 15 );
			g2.setStroke(saveStroke);
		}
		g.setColor(saveColor);
	}

	@Override
	public boolean isHighlighted() {
		return highlighted;
	}
	
	@Override
	public void setHighlighted(boolean highlighted) {
		if( isHighlighted() != highlighted ) {
			this.highlighted = highlighted;
			repaint();
		}
	}
}
