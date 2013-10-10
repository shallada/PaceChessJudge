package edu.neumont.chess.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

@SuppressWarnings("serial") // this class shouldn't be serialized
public class Message extends JDialog {
	private Timer timer;
	private int maxWidth = 200;
	private int openCloseDuration = 0;
	private int hold = 0;
	private long startTime = 0;
	
	public Message( String message ) {
		this( message, 200, 30 );
	}
	
	public Message( String message, int width, int height ) {
		this( message, width, height, 1000, 1000 );
	}
	
	public Message( String message, int width, int height, int duration, int holdTime ) {
		this( message, width, height, new Font("Times New Roman", Font.BOLD, 12), duration, holdTime );
	}
	
	public Message( String message, int width, int height, Font font, int duration, int holdTime ) {
		this.openCloseDuration = duration;
		this.hold = holdTime;
		this.maxWidth = width;
		setUndecorated(true);
		timer = new Timer( 1, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date now = new Date();
				long duration = now.getTime() - startTime;
				if( duration < openCloseDuration ) {
					int size = (int)(openCloseDuration-duration) * maxWidth / openCloseDuration;
					setSize( maxWidth-size, getHeight() );
				}
				duration -= openCloseDuration;
				if( duration >= 0 && duration < hold ) {
					setSize( maxWidth, getHeight() );
				}
				duration -= hold;
				if( duration >= 0 ) {
					if( duration < openCloseDuration ) {
						int size = (int)(openCloseDuration-duration) * maxWidth / openCloseDuration;
						setSize( size, getHeight() );
					}
					else {
						timer.stop();
						Message.this.dispose();
					}
				}
			}
		});

		JPanel panel = new JPanel( new BorderLayout() );
		panel.setBorder( BorderFactory.createBevelBorder(BevelBorder.RAISED) );
		panel.add( new JLabel(message), BorderLayout.CENTER );
		setSize( 0, height );
		add( panel );
	}
	
	@Override
	public void setVisible(boolean b) {
		setSize( 0, getHeight() );
		super.setVisible(b);
		if( b ) {
			Date now = new Date();
			startTime = now.getTime();
			timer.start();
		}
	}
	
	public static void main(String[] args) {
		Message msg = new Message("Testing the message");
		msg.setLocation( 100, 100 );
		msg.setVisible(true);
	}
}
