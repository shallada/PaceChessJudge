package edu.neumont.chess.movements;

import java.util.List;

import edu.neumont.chess.model.Team;


public class ComplexMove implements Move {
	private Move[] moves;
	
	public ComplexMove( Move ... moves ) {
		this.moves = moves;
	}
	
	public ComplexMove( List<Move> moves ) {
		this( moves.toArray(new Move[0]) );
	}

	@Override
	public void execute() {
		for( int i=0; i < moves.length; i++ ) {
			if( moves[i] != null )
				moves[i].execute();
		}
	}

	@Override
	public void undo() {
		for( int i=moves.length-1; i >= 0; i-- ) {
			if( moves[i] != null )
				moves[i].undo();
		}
	}

	@Override
	public boolean isValid( Team team ) {
		boolean valid = true;
		if( moves.length == 2 && moves[0] instanceof SimpleMove 
				&& moves[1] instanceof SimpleMove ) {
			SimpleMove first = (SimpleMove)moves[0];
			SimpleMove next = (SimpleMove)moves[1];
			valid = first.isMoveOffBoard() && next.isMovement() &&
					next.getTo().equals(first.getFrom()) &&
					next.isValid(team) && first.isValid(team.other());
		}
		else {
			for( int i=0; i < moves.length; i++ ) {
				if( moves[i] != null ) {
					valid &= moves[i].isValid(team);
				}
			}
		}
		return valid;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for( Move move : moves ) {
			if( move != null ) {
				if( str.length() > 0 )
					str.append(" and ");
				str.append(move.toString());
			}
		}
		
		return str.toString();
	}
	
	@Override
	public String toStandard() {
		String move = "";
		String save = "";
		for( int i=0; i < moves.length; i++ ) {
			if( moves[i] != null ) {
				String thisMove = save;
				save = "";
				if( moves[i] instanceof SimpleMove ) {
					SimpleMove m = (SimpleMove)moves[i];
					if( m.isMovement() )
						thisMove = m.toStandard() + thisMove;
					else if( m.isMoveOffBoard() )
						save = "*";
				}
				else {
					thisMove = moves[i].toStandard();
				}
				if( thisMove.length() > 0 )
					move += " " + thisMove;
			}
		}
		return move.trim();
	}
}
