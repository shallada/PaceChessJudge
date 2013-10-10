package edu.neumont.chess.logic;

import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.King;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.piece.PieceFactory;
import edu.neumont.chess.players.Player;

public class ChessLogic {
	public static boolean isInCheck( Board board, Team team ) {
		King king = board.getKing(team);
		Piece[] attackingPieces = board.getPieces( team.other() );
		for( Piece piece : attackingPieces ) {
			Move[] moves = piece.pieceMoves();
			for( Move move : moves ) {
				move.execute();
				boolean killedKing = king.getLocation() == null;
				move.undo();
				if( killedKing )
					return true;
			}
		}
		
		return false;
	}
	
	public static boolean isInCheckMate( Board board, Team team ) {
		return isInCheck(board, team) && isStuck(board, team);
	}
	
	public static boolean isStuck( Board board, Team team ) {
		Piece[] savingPieces = board.getPieces(team);
		for( Piece piece : savingPieces ) {
			Move[] moves = piece.pieceMoves();
			for( Move move : moves ) {
				move.execute();
				boolean stillInCheck = isInCheck(board, team);
				move.undo();
				if( !stillInCheck )
					return false;
			}
		}
		
		return true;
	}
	
	public static int getScore( ChessGame game, Team team )
	{
		int score = 0;
		Player player = game.getPlayer(team);
		Player other = game.getPlayer(team.other());
		Piece[] captured = player.getCapturedPieces();
		Piece[] otherCaptured = other.getCapturedPieces();
		for( Piece piece : captured )
			score -= PieceFactory.pieceValue(piece.getClass());
		for( Piece piece : otherCaptured )
			score += PieceFactory.pieceValue(piece.getClass());
		
		if( game.getState(team) == GameState.CHECKMATE )
			score -= PieceFactory.pieceValue(King.class);
		if( game.getState(team.other()) == GameState.CHECKMATE )
			score += PieceFactory.pieceValue(King.class);
		if( game.getState(team) == GameState.FORFEIT )
			score -= PieceFactory.pieceValue(King.class);
		
		return score;
	}
}
