package edu.neumont.chess.players;

import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.ChessMove;
import edu.neumont.chess.movements.IllegalMoveException;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.Bishop;
import edu.neumont.chess.piece.Pawn;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.piece.Queen;
import edu.neumont.chess.ui.ChessAI.AIType;

public class FourMoveCheckmate extends RandomAIPlayer {
	private int nMoves = 0;
	private boolean foiled = false;
	
	public FourMoveCheckmate( ChessGame game, Team team ) {
		super( game, team, AIType.RANDOM_PIECE_EQUALITY );
	}
	
	@Override
	public void takeTurn() {
		nMoves++;
		if( nMoves > 4 || foiled )
			super.takeTurn();
		else {
			Piece piece;
			Move m = null;
			switch( nMoves ) {
				case 1:
					piece = getBoard().getPiece(ChessGame.toPoint("e"+(getTeam()==Team.white?2:7)));
					if( piece != null && piece.getClass() == Pawn.class && piece.getTeam() == this.getTeam() ) {
						m = ChessMove.createMove(getBoard(), piece, ChessGame.toPoint("e"+(getTeam()==Team.white?3:6)));
					}
					break;
				case 2:
					piece = getBoard().getPiece(ChessGame.toPoint("d"+(getTeam()==Team.white?1:8)));
					if( piece != null && piece.getClass() == Queen.class && piece.getTeam() == this.getTeam() ) {
						m = ChessMove.createMove(getBoard(), piece, ChessGame.toPoint("f"+(getTeam()==Team.white?3:6)));
					}
					break;
				case 3:
					piece = getBoard().getPiece(ChessGame.toPoint("f"+(getTeam()==Team.white?1:8)));
					if( piece != null && piece.getClass() == Bishop.class && piece.getTeam() == this.getTeam() ) {
						m = ChessMove.createMove(getBoard(), piece, ChessGame.toPoint("c"+(getTeam()==Team.white?4:5)));
					}
					break;
				case 4:
					piece = getBoard().getPiece(ChessGame.toPoint("f"+(getTeam()==Team.white?3:6)));
					if( piece != null && piece.getClass() == Queen.class && piece.getTeam() == this.getTeam() ) {
						m = ChessMove.createMove(getBoard(), piece, ChessGame.toPoint("f"+(getTeam()==Team.white?7:2)));
					}
					break;
			}
			if( m != null ) {
				try {
					getGame().performMove(this, m);
					System.out.println(m.toStandard());
				} catch (IllegalMoveException e) {
					foiled = true;
					super.takeTurn(); // go with random
				}
			}
			else {
				foiled = true;
				super.takeTurn(); // go with random
			}
		}
	}
}
