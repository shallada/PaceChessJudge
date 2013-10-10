package edu.neumont.chess.io;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.neumont.chess.model.ChessGame;
import edu.neumont.chess.model.Team;
import edu.neumont.chess.movements.ChessMove;
import edu.neumont.chess.movements.ComplexMove;
import edu.neumont.chess.movements.IllegalMove;
import edu.neumont.chess.movements.Move;
import edu.neumont.chess.piece.Piece;
import edu.neumont.chess.piece.PieceFactory;

public class ChessScanner implements Iterator<Move>, Iterable<Move> {
	private static final int PIECE_PLACEMENT = 1;
	private static final int PIECE_TYPE = 2;
	private static final int PIECE_COLOR = 3;
	private static final int PIECE_LOCATION = 4;
	//private static final int PIECE_MOVEMENT = 5;
	private static final int PIECE_MOVE_FROM = 6;
	private static final int PIECE_MOVE_TO = 7;
	//private static final int PIECE_MOVE_CAPTURE = 8;
	private static final String square = "[a-h][1-8]";
	private static final Pattern movePattern = Pattern.compile(
			String.format("(?i)(([KQRBNP])([ld])(%s))|((%1$s)\\s*(%1$s)(\\*)?)", square) );
	
	private Scanner input;
	private Move next = null;
	private ChessGame game;
	
	public ChessScanner( ChessGame game, Scanner input ) {
		this.input = input;
		this.game = game;
	}
	
	public ChessScanner( ChessGame game, File inputFile ) throws FileNotFoundException {
		this( game, new Scanner( inputFile ) );
	}
	
	public ChessScanner( ChessGame game, String inputString ) {
		this( game, new Scanner( inputString ) );
	}

	@Override
	public boolean hasNext() {
		return getNextMove() != null;
	}

	@Override
	public Move next() {
		Move nextMove = getNextMove();
		this.next = null;
		return nextMove;
	}
	
	/**
     * The remove operation is not supported by this implementation of
     * <code>Iterator</code>.
     *
     * @throws UnsupportedOperationException if this method is invoked.
     * @see java.util.Iterator
     */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Resets the scanner to its initial state.
	 * @return this scanner
	 */
	public ChessScanner reset() {
		input.reset();
		next = null;
		return this;
	}
	
	private Move getNextMove() {
		while( next == null && input.hasNextLine() ) {
			String line = input.nextLine();
			next = parseMove(game, line);
		}
		return next;
	}
	
	public static Move parseMove( ChessGame game, String moveNotation ) {
		Matcher moveMatcher = movePattern.matcher(moveNotation);
		ArrayList<Move> moves = new ArrayList<Move>();
		while( moveMatcher.find() ) {
			Move newMove = null;
			if( moveMatcher.group(PIECE_PLACEMENT) != null ) {
				Team team = Team.getTeam(moveMatcher.group(PIECE_COLOR).charAt(0));
				Piece piece = PieceFactory.createPiece( game, 
						moveMatcher.group(PIECE_TYPE).charAt(0), team );
				Point location = ChessGame.toPoint(moveMatcher.group(PIECE_LOCATION));
				if( team != null && piece != null && location != null ) {
					newMove = ChessMove.createPlacement(game, piece, location);
				}
			}
			else {
				Point from = ChessGame.toPoint(moveMatcher.group(PIECE_MOVE_FROM));
				Point to = ChessGame.toPoint(moveMatcher.group(PIECE_MOVE_TO));
				Piece piece = game.getBoard().getPiece(from);
				if( piece != null && to != null ) {
					newMove = ChessMove.createMove(game, piece, to);
				}
				else {
					newMove = new IllegalMove(moveNotation);
				}
			}
			
			if( newMove != null ) {
				moves.add(newMove);
			}
		}
		if( moves.size() == 0 )
			return new IllegalMove(moveNotation);
		else if( moves.size() == 1 ) 
			return moves.get(0);
		else
			return new ComplexMove(moves);
	}

	@Override
	public Iterator<Move> iterator() {
		return this;
	}
}
