package edu.neumont.chess.players;

import java.util.ArrayList;
import java.util.Random;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.Pawn;
import edu.neumont.chess.piece.Piece;

public class PawnPreferredPlayer extends Player {
	private static Random rand = new Random();
	
	public PawnPreferredPlayer( ChessGame game, Team team ) {
		super(game, team);
	}
	
	@Override
	public void takeTurn() {
		Piece[] pieces = getPiecesWithMoves();
		ArrayList<Piece> pawns = new ArrayList<Piece>();
		for( Piece piece : pieces )
			if( piece.getClass() == Pawn.class )
				pawns.add(piece);
		
		Piece chosen = null;
		if( pawns.size() > 0 ) {
			chosen = pawns.get(rand.nextInt(pawns.size()));
		}
		else if( pieces.length > 0 ){
			chosen = pieces[rand.nextInt(pieces.length)];
		}
		
		if( chosen != null ) {
			Move[] allMoves = chosen.getMoves();
			if( allMoves.length == 0 ) {
				System.err.println(getTeam() + " getPieces with moves returned the piece at: " + chosen.getLocation());
			}
			else {
				Move chosenMove = allMoves[rand.nextInt(allMoves.length)];
				try {
					getGame().performMove(this, chosenMove);
					System.out.println(chosenMove.toStandard());
				} catch (IllegalMoveException e) {
					System.err.println("Invalid move?? " + chosenMove.toStandard());
				}
			}
		}
		else {
			System.err.println( getTeam() + " doesn't have any pieces that can move ... checkmate?");
		}
	}
}
