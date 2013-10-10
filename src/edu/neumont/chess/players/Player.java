package edu.neumont.chess.players;

import java.util.ArrayList;
import java.util.List;

import edu.neumont.chess.model.Board;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.piece.Piece;

public abstract class Player {
	private ChessGame game;
	private Team team;
	private Piece[] pieces;
	
	public Player( ChessGame game, Team team ) {
		Board board = game.getBoard();
		pieces = board.createChessTeam(team);
		this.team = team;
		this.game = game;
		this.game.addPlayer(this);
	}
	
	public final ChessGame getGame() {
		return game;
	}
	
	public final Board getBoard() {
		return getGame().getBoard();
	}
	
	public final Team getTeam() {
		return team;
	}
	
	public final Piece[] getPieces() {
		return pieces;
	}
	
	public final Piece[] getPiecesOnBoard() {
		List<Piece> pieces = new ArrayList<Piece>();
		for( Piece piece : this.pieces ) {
			if( piece.getLocation() != null )
				pieces.add(piece);
		}
		return pieces.toArray(new Piece[0]);
	}
	
	public final Piece[] getCapturedPieces() {
		List<Piece> pieces = new ArrayList<Piece>();
		for( Piece piece : this.pieces ) {
			if( piece.getLocation() == null )
				pieces.add(piece);
		}
		return pieces.toArray(new Piece[0]);
	}
	
	public final Piece[] getPiecesWithMoves() {
		List<Piece> movablePieces = new ArrayList<Piece>();
		for( Piece piece : getPiecesOnBoard() ) {
			if( piece.getMoves().length > 0 ) {
				movablePieces.add(piece);
			}
		}
		return movablePieces.toArray(new Piece[0]);
	}
	
	public abstract void takeTurn();


	@Override
	public String toString() {
		return getTeam().toString();
	}
}
