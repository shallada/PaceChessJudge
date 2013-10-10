package edu.neumont.chess.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventAdapter;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.piece.Piece;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class Square extends ChessGUICustomComponent {
	private Point location;
	private ChessGame game;
	
	public Square( ChessGame game, Point location ) {
		this.game = game;
		this.location = location;
		game.addChessListener( new ChessEventAdapter() {
			@Override
			public void boardUpdated(ChessEvent event, Board board) {
				updateSquare();
			}

			@Override
			public void gameStarted(ChessEvent event) {
				updateSquare();
			}
		});
		
		updateSquare();
		setPreferredSize(new Dimension(70,70));
	}
	
	private void updateSquare() {
		Board board = game.getBoard();
		Piece piece = board.getPiece( location );
		BufferedImage image = PieceViewer.getImage(piece);
		if( image != getImage() ) {
			setImage(image);
		}
	}
	
	public Point getLocation() {
		return location;
	}
}
