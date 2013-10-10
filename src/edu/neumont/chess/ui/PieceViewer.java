package edu.neumont.chess.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import edu.neumont.chess.events.ChessEvent;
import edu.neumont.chess.events.ChessEventAdapter;
import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.piece.Piece;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class PieceViewer extends ChessGUICustomComponent {
	private static HashMap<String, BufferedImage> pieceImages;
	static {
		pieceImages = new HashMap<String, BufferedImage>();

		URL unknownImageURL = PieceViewer.class.getResource("/images/unknown.jpg");
		BufferedImage unknownImage = null;
		try {
			unknownImage = ImageIO.read( unknownImageURL );
		} catch (IOException e) {
			e.printStackTrace();
			unknownImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		pieceImages.put("unknown", unknownImage);
	}
	
	private ChessGame game;
	private Team team;
	
	public PieceViewer( ChessGame game, Team team ) {
		Dimension preferred = new Dimension( 200, 50 );
		setPreferredSize( preferred );
		setMinimumSize( preferred );
		setMaximumSize( preferred );
		
		this.game = game;
		this.team = team;
		
		game.addChessListener( new ChessEventAdapter() {
			@Override
			public void boardUpdated(ChessEvent event, Board board) {
				repaint();
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		// Get the other team's pieces and see who's missing
		ArrayList<Piece> captured = new ArrayList<Piece>();
		if( game.getPlayer(team.other()) != null ) {
			Piece[] pieces = game.getPlayer(team.other()).getPieces();
			for( Piece piece : pieces ) {
				if( piece.getLocation() == null ) {
					captured.add( piece );
				}
			}
		}
		
		if( captured.size() > 0 ) {
			int buffer = 10;
			int maxWidth = (this.getWidth()-(captured.size()+1)*buffer) / captured.size();
			int maxHeight = this.getHeight() - buffer*2;
			int size = Math.min(maxWidth, maxHeight);
			int x = buffer;
			int y = buffer;
			for( Piece piece : captured ) {
				g.drawImage( getImage(piece), x, y, size, size, this );
				x += size + buffer;
			}
		}
	}
	
	public static BufferedImage getImage( String name ) {
		return loadImage(name);
	}
	
	public static BufferedImage getImage( Piece piece ) {
		if( piece == null )
			return null;
		
		return getImage( piece.getTeam(), piece.getClass() );
	}
	
	public static BufferedImage getImage( Team team, Class<? extends Piece> pieceClass ) {
		String key = team.toString() + '_' + pieceClass.getSimpleName();
		return getImage(key);
	}
	
	private static BufferedImage loadImage( String name ) {
		if( name == null ) name = "unknown";
		
		String key = name.toLowerCase();
		if( !pieceImages.containsKey(key) ) {
			String[] extensions = { "gif", "jpg", "jpeg", "png" };
			boolean found = false;
			for( String extension : extensions ) {
				URL url = PieceViewer.class.getResource("/images/"+key+"."+extension);
				if( url != null ) {
					try {
						pieceImages.put(key, ImageIO.read( url ));
						found = true;
						break;
					} catch (IOException e) {
					}
				}
			}
			if( !found )
				key = "unknown";
		}
		
		return pieceImages.get(key);
	}
}
