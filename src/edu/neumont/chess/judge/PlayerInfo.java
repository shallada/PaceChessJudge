package edu.neumont.chess.judge;

import java.io.File;
import java.io.Serializable;

public class PlayerInfo implements Serializable {
	private static final long serialVersionUID = 1;
	
	private File jar;
	private String name;

	public File getJar() {
		return jar;
	}

	public String getName() {
		return name;
	}
	
	public PlayerInfo( File jar ) {
		this.name = jar.getName();
		this.name = this.name.substring(0, this.name.lastIndexOf('.') );
		this.jar = jar;
	}
	
	public PlayerInfo( String name, String jar ) {
		this( new File(jar) );
		this.name = name;
	}
	
	public PlayerInfo( String jar ) {
		this( new File(jar) );
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null || !(obj instanceof PlayerInfo))
			return false;
		PlayerInfo other = (PlayerInfo)obj;
		return other.jar.equals(this.jar);
	}
	
	@Override
	public int hashCode() {
		return jar.hashCode();
	}
}
