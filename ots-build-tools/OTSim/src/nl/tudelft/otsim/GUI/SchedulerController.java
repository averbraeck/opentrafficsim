package nl.tudelft.otsim.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Scheduler.SchedulerState;

/**
 * JPanel with controls for a {@link Scheduler}.
 * 
 * @author Peter Knoppers
 */
public class SchedulerController extends JPanel implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	private JButton buttonRealTime;
	private JButton buttonFast;
	private JButton buttonStop;
	private JButton buttonStep;
	private Clock clock;
	private JFormattedTextField endTime;
	private Scheduler scheduler;

	/**
	 * Create a new SchedulerController.
	 * @param scheduler {@link Scheduler}; the Scheduler that must be informed
	 * when the user clicks a button or edits the end time.
	 */
	public SchedulerController(Scheduler scheduler) {
		this.scheduler = scheduler;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;	// Documentation does not specify that this field defaults to 0
		gbc.gridy = 0;	// Documentation does not specify that this field defaults to 0
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
        add(buttonStep = makeButton("Step", "Simulate one step", "Step", "Last_recor.png"), gbc);
        gbc.gridy++;
        add(buttonStop = makeButton("Stop", "Stop simulation", "Stop", "Stop.png"), gbc);
        gbc.gridy++;
        add(buttonRealTime = makeButton("Real time", "Try to run the simulator at real-time speed", "RealTime", "Play.png"), gbc);
        gbc.gridy++;
        add(buttonFast = makeButton("Fast", "Run as fast as possible", "RunFast", "Fast-forward.png"), gbc);
        gbc.gridy++;
        add(makeButton("Restart", "Reset the simulator", "Restart", "Rewind.png"), gbc);
        gbc.gridy++;
        add(makeButton("Reload", "Reload the configuration and ", "Reload", "Refresh.png"), gbc);
        gbc.gridy++;
        add(clock = new Clock(), gbc);
        gbc.gridy++;        
        RegexFormatter df = new RegexFormatter("\\d\\d:[0-5]\\d:[0-5]\\d\\.\\d\\d\\d");
        endTime = new JFormattedTextField(df);
        MaskFormatter timeMask;
		try {
			timeMask = new MaskFormatter("##:##:##.###");
			timeMask.setPlaceholderCharacter('0');
			timeMask.setAllowsInvalid(false);
	        timeMask.install(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// wrap it with a caption in a sub-panel
		JLabel caption = new JLabel("Stop at:");
		JPanel subPanel = new JPanel();
		subPanel.add(caption);
		subPanel.add(endTime);
        add(subPanel, gbc);
        endTime.addPropertyChangeListener(this);
        schedulerStateChanged();
        scheduler.setSchedulerController(this);
	}
	
    /**
     * Create a JButton and initialize some of its properties
     * @param caption String; caption of the JButton
     * @param toolTipText String toolTipText of the JButton. If null, no
     * toolTipText is set
     * @param actionCommand String; actionCommand of the JButton. If non-null
     * <code>this</code> is added to the ActionListeners of the JButton
     * @return JButton; the newly created JButton
     */
    private JButton makeButton (String caption, String toolTipText, String actionCommand, String iconName) {
    	JButton button = new JButton(caption);
    	if (null != toolTipText)
    		button.setToolTipText(toolTipText);
    	if (null != actionCommand) {
    		button.setActionCommand(actionCommand);
    		button.addActionListener(this);
    	}
        // Try to load the image from the resources
        String imgLocation = "/nl/tudelft/otsim/Resources/" + iconName;
        java.net.URL imageURL = getClass().getResource(imgLocation);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL, caption));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(30);
    	return button;
    }
    
	class Clock extends JComponent {
		private static final long serialVersionUID = 1L;
		private Dimension preferredSize = new Dimension(150, 36);
		private Icon icon = null;
		boolean fontSizeSet = false;
		double currentTime = 0;
		
		public Clock() {
	        String imgLocation = "/resources/" + "Clock.png";
	        java.net.URL imageURL = getClass().getResource(imgLocation);
	        if (imageURL != null)
	            icon = new ImageIcon(imageURL);
		}
		
		@Override
		public void paintComponent (Graphics g) {
			double time = currentTime + 0.0005;
			int seconds = (int) Math.floor(time);
			int milliSeconds = (int) Math.floor((time - seconds) * 1000);
			String caption = String.format("%02d:%02d:%02d.%03d", seconds / 3600, seconds / 60 % 60, seconds % 60, milliSeconds);
			g.setColor(Color.BLACK);
			int fontSize = Math.min(getWidth() / 8, getHeight() / 2);
			Font font = new Font("SansSerif", Font.PLAIN, fontSize);
			g.setFont(font);
			Graphics2D g2d = (Graphics2D) g;
			FontMetrics fm = g2d.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(caption,  g2d);
			int offset;
			if (null != icon) {
				final int separation = 10;	// pixels
				int totalWidth = icon.getIconWidth() + (int) r.getWidth() + separation;
				offset = (getWidth() - totalWidth) / 2;
				icon.paintIcon(this, g, offset, (getHeight() - icon.getIconHeight()) / 2);
				offset += icon.getIconWidth() + separation;
			} else
				offset = (int) ((getWidth() - r.getWidth()) / 2);
			g.drawString(caption, offset, (int) ((getHeight() - r.getHeight()) / 2) + fm.getAscent());
			if (! fontSizeSet) {
				endTime.setFont(font);
				revalidate();
				fontSizeSet = true;
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}
		
		public void updateTime(double newTime) {
			if (! SwingUtilities.isEventDispatchThread())
				throw new Error("Clock time can only be updated in the EventDispatchThread");
			currentTime = newTime;
			repaint();
		}
		
	}

	/**
	 * Inform this SchedulerController that the state of the {@link Scheduler}
	 * may have changed.
	 */
	public void schedulerStateChanged() {
		final Scheduler.SchedulerState newState = scheduler.getState();
		if (SwingUtilities.isEventDispatchThread())
			updateControls(newState);
		else
			try {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateControls(newState);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private void updateControls(Scheduler.SchedulerState newState) {
		if (! SwingUtilities.isEventDispatchThread())
			throw new Error("Swing controls can only be updated in the EventDispatchThread");
		switch (newState) {
		case Stopped:
		case StopTimeReached:
			buttonRealTime.setEnabled(true);
			buttonFast.setEnabled(true);
			buttonStop.setEnabled(false);
			buttonStep.setEnabled(true);
			break;
		case FastRunning:
			buttonRealTime.setEnabled(true);
			buttonFast.setEnabled(false);
			buttonStop.setEnabled(true);
			buttonStep.setEnabled(false);
			break;
		case RealTimeRunning:
			buttonRealTime.setEnabled(false);
			buttonFast.setEnabled(true);
			buttonStop.setEnabled(true);
			buttonStep.setEnabled(false);
			break;
		case EndTimeReached:
			buttonRealTime.setEnabled(false);
			buttonFast.setEnabled(false);
			buttonStop.setEnabled(false);
			buttonStep.setEnabled(false);
			break;
		case ExecuteSingleEvent:
		case Reload:
		case Restart:
			// These states end automatically and another event will follow when this state ends.
			// FALL THROUGH
		case SimulatorError:
			buttonRealTime.setEnabled(false);
			buttonFast.setEnabled(false);
			buttonStop.setEnabled(false);
			break;
		default:
			break;
		}
		//System.out.println("newState=" + newState + ", Button states: Stop=" + buttonStop.isEnabled() + " Real time=" + buttonRealTime.isEnabled() + " Fast=" + buttonFast.isEnabled());
		repaint();
	}
	
	/**
	 * Inform this SchedulerController that the simulated time of the 
	 * {@link Scheduler} may have changed.
	 * @param newTime Double; the new simulated time to display on the clock
	 */
	public void schedulerClockChanged(final double newTime) {
		if (SwingUtilities.isEventDispatchThread())
			clock.updateTime(newTime);
		else
			try {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						clock.updateTime(newTime);
					}
				});
			} catch (Exception e) {
				System.out.println("Could not update the scheduler clock (due to InterruptedException)");
			}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		//System.out.println("actionPerformed: " + command);
		if (command.equals("Stop"))
			scheduler.setState(SchedulerState.Stopped);
		else if (command.equals("RealTime"))
			scheduler.setState(SchedulerState.RealTimeRunning);
		else if (command.equals("RunFast"))
			scheduler.setState(SchedulerState.FastRunning);
		else if (command.equals("Step"))
			scheduler.setState(SchedulerState.ExecuteSingleEvent);
		else if (command.equals("Restart"))
			scheduler.setState(SchedulerState.Restart);
		else if (command.equals("Reload"))
			scheduler.setState(SchedulerState.Reload);
		else
			throw new Error("Unhandled event: " + command);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object value = endTime.getValue();
		if (null != value) {
			//System.out.println("value of endTime is " + value);
			String valueString = (String) value;
			String fields[] = valueString.split("[:\\.]");
			int hours = Integer.parseInt(fields[0]);
			int minutes = Integer.parseInt(fields[1]);
			int seconds = Integer.parseInt(fields[2]);
			int millis = Integer.parseInt(fields[3]);
			scheduler.setStopTime (millis / 1000d + seconds + minutes * 60 + hours * 3600); 
		}
	}

	/** 
	 * RegexFormatter.
	 * <br /> Derived from <a href="http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm">http://www.java2s.com/Tutorial/Java/0240__Swing/RegexFormatterwithaJFormattedTextField.htm</a>
	 */
	class RegexFormatter extends DefaultFormatter {
		private static final long serialVersionUID = 1L;
		private Pattern pattern;
		
		/**
		 * Create a new RegexFormatter.
		 * @param pattern String; regular expression pattern that defines what
		 * this RexexFormatter will accept
		 * @throws PatternSyntaxException
		 */
		public RegexFormatter(String pattern) throws PatternSyntaxException {
			this.pattern = Pattern.compile(pattern);
		}
		
		@Override
		public Object stringToValue(String text) throws ParseException {
			Matcher matcher = pattern.matcher(text);
			if (matcher.matches())
				return super.stringToValue(text);
			throw new ParseException("Pattern did not match", 0);
		}
	}

	/**
	 * Retrieve the {@link Scheduler} of this SchedulerController.
	 * @return {@link Scheduler}; the Scheduler of this SchedulerController
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

}
