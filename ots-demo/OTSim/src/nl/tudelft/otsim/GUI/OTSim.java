package nl.tudelft.otsim.GUI;

import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

/**
 * This is the environment for running OpenTraffic embedded in a web browser.
 * 
 * @author Peter Knoppers
 */
public class OTSim extends JApplet {
	private static final long serialVersionUID = 1L;
	JLabel initLabel;
	
	@Override
	public void init() {
		setLayout (new BorderLayout());
		add(initLabel = new JLabel("Loading OTSim resources ..."), BorderLayout.CENTER);
	}
	
	@Override
	public void start() {
		add(new Main(this), BorderLayout.CENTER);
		revalidate();		// very important!
		int count;
		final String paramHeader = "param";
		for (count = 0; null != getParameter(paramHeader + count); count++)
			;	// count the number of parameters
		String[] params = new String[count];
		for (int i = 0; i < count; i++)
			params[i] = getParameter(paramHeader + i);
		initLabel.setText("");
		Main.main(params);
	}
	
	/**
	 * Put a JMenuBar in the NORTH area of the JApplet window.
	 * @param menuBar JMenuBar; the menu bar to put in the NORTH area of the window
	 */
	public void setMenuBar(JMenuBar menuBar) {
		add (menuBar, BorderLayout.NORTH);
	}

}
