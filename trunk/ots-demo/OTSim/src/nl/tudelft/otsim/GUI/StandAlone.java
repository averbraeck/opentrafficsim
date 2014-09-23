package nl.tudelft.otsim.GUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

/**
 * This is the environment for running OpenTraffic as a stand alone application.
 * 
 * @author Peter Knoppers
 */
public class StandAlone extends JFrame {
	private static final long serialVersionUID = 1L;
	
	static private Container frame;
	
	/**
	 * Create the GUI window for running as a Java application
	 */
	public StandAlone () {
	    setMinimumSize(new Dimension(1000, 800));
	    setLocation(100, 100);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
        	@Override
			public void windowClosing(WindowEvent e) {
        		Main.mainFrame.closeProgramCheck();
        	}
        });
        setVisible(true);
	}
	
    /**
     * Create the main window for the stand-alone version of OTSim. 
     * @param args String[]; arguments to be passed onto the main method in {@link Main}
     */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
            	frame = new StandAlone();
	            frame.add(Main.mainFrame = new Main(frame), BorderLayout.CENTER);
	            Main.initialized = true;
	            Main.mainFrame.setVisible(true);
            }
        });
        // wait for start up of GUI
        while (! Main.initialized) {
    		System.out.println("Waiting for initialization of GUI");
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.err.println("Sleep interrupted");
				e.printStackTrace();
			}
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
				Main.main(args);
            }
        });
    }
	
	/**
	 * Put a JMenuBar in the NORTH area of the window.
	 * @param menuBar JMenuBar; the menu bar to put in the NORTH area of the window
	 */
	public void setMenuBar(JMenuBar menuBar) {
		add (menuBar, BorderLayout.NORTH);
	}
	

}
