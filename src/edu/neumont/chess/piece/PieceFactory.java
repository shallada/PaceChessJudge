package edu.neumont.chess.piece;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;

public class PieceFactory {
	public static final int TOTAL_PIECES_VALUE;
	
	private static final Map<Character, Class<? extends Piece>> pieceLookup;
	private static final Map<Class<? extends Piece>, Integer> pieceValueLookup;
	static {
		pieceLookup = new HashMap<Character, Class<? extends Piece>>();
		pieceLookup.put('K', King.class);
		pieceLookup.put('Q', Queen.class);
		pieceLookup.put('R', Rook.class);
		pieceLookup.put('B', Bishop.class);
		pieceLookup.put('N', Knight.class);
		pieceLookup.put('P', Pawn.class);
		
		pieceValueLookup = new HashMap<Class<? extends Piece>, Integer>();
		pieceValueLookup.put(King.class, 100);
		pieceValueLookup.put(Queen.class,  9);
		pieceValueLookup.put(Rook.class,   5);
		pieceValueLookup.put(Bishop.class, 3);
		pieceValueLookup.put(Knight.class, 3);
		pieceValueLookup.put(Pawn.class,   1);
		
		TOTAL_PIECES_VALUE = 
			pieceValue( Queen.class )
			+ pieceValue( Rook.class ) * 2
			+ pieceValue( Bishop.class ) * 2
			+ pieceValue( Knight.class ) * 2
			+ pieceValue( Pawn.class ) * 8;
	}
	
	public static int pieceValue( Class<? extends Piece> pieceClass ) {
		if( pieceValueLookup.containsKey(pieceClass) )
			return pieceValueLookup.get(pieceClass);
		return 0;
	}
	
	public static char pieceName( Class<? extends Piece> pieceClass ) {
		for( char c : pieceLookup.keySet() ) {
			if( pieceLookup.get(c) == pieceClass )
				return c;
		}
		return 'X';
	}
	
	public static char pieceName( Piece piece ) {
		if( piece == null )
			return '0';
		return pieceName( piece.getClass() );
	}
	
	public static Piece createPiece( ChessGame game, char pieceType, Team team ) {
		Piece piece = null;
		pieceType = Character.toUpperCase(pieceType);
		if( pieceLookup.containsKey(pieceType) ) {
			Class<? extends Piece> pieceClass = pieceLookup.get(pieceType);
			Constructor<? extends Piece> constructor;
			try {
				constructor = pieceClass.getConstructor(ChessGame.class, Team.class);
				piece = constructor.newInstance( game, team );
			} catch (Exception e) {
				e.printStackTrace();
				// If a problem happens, record it and then return null
			}
		}
		return piece;
	}
}
