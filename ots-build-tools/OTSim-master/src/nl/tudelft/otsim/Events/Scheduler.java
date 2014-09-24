package nl.tudelft.otsim.Events;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;
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
import javax.swing.Timer;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.Simulators.Simulator;

/**
 * @author Peter Knoppers
 * <br />
 * Maintain a queue of time-scheduled events and call the {@link Step#step}
 * method when execution is due.
 * <br />
 * Events do not have to spaced at equidistant times.
 * <br />
 * If several events are scheduled for execution at the same time, the order
 * of execution is deterministic; (determined by the hashCode of the
 * scheduled object). Simulators should (aim to) not depend on the execution
 * order of simultaneously scheduled events.
 */
public class Scheduler extends JPanel implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private static final int timerMillis = 100;
	private TreeSet<QueuedEvent> queue = new TreeSet<QueuedEvent>();
	Timer timer = new Timer(timerMillis, this);
	private long zeroTime;
	Simulator runningSimulation;
	final String simulatorType;
	private String configuration;
	final GraphicsPanel graphicsPanel;
	private double simulatedTime = 0;
	private JButton buttonRealTime;
	private JButton buttonFast;
	private JButton buttonStop;
	private FastRun fastRun = null;
	private Clock clock;
	private JFormattedTextField endTime;
	private double stopTime = 0;
		
	/** 
	 * Create a scheduler for a simulator.
	 * @param simulatorType String that identifies the type of Simulator to run
	 * @param graphicsPanel GraphicsPanel whose repaint method will be called
	 * to update the image of the simulation.
	 */
	public Scheduler(String simulatorType, GraphicsPanel graphicsPanel) {
		this.simulatorType = simulatorType;
		this.configuration = null;
		this.graphicsPanel = graphicsPanel;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
        add(makeButton("Step", "Simulate one step", "Step", "Last_recor.png"), gbc);
        gbc.gridy++;
        add(buttonStop = makeButton("Stop", "Stop simulation", "Stop", "Stop.png"), gbc);
        gbc.gridy++;
        add(buttonRealTime = makeButton("Real time", "Try to run the simulator at real-time speed", "RealTime", "Play.png"), gbc);
        gbc.gridy++;
        add(buttonFast = makeButton("Fast", "Run as fast as possible", "RunFast", "Fast-forward.png"), gbc);
        gbc.gridy++;
        add(makeButton("Restart", "Reset the simulator", "Restart", "Rewind.png"), gbc);
        gbc.gridy++;
        add(makeButton("Reload", "Reload the configuration and restart", "Reload", "Refresh.png"), gbc);
        gbc.gridy++;
        add(clock = new Clock(this), gbc);
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
        reloadSimulator();
	}
	
	/**
	 * Return current simulated time.
	 * @return Double simulated time in seconds
	 */
	public double getSimulatedTime() {
		return simulatedTime;
	}
	
	/**
	 * Return the simulator associated with this Scheduler.
	 * @return Simulator associated with this Scheduler
	 */
	public Simulator getSimulator() {
		return runningSimulation;
	}
	
	/**
	 * Return the Component that must be repainted after the simulator
	 * associated with this Scheduler may have changed state.
	 * @return Component to be repainted
	 */
	public GraphicsPanel getGraphicsPanel() {
		return graphicsPanel;
	}
	
	class QueuedEvent implements Comparable<QueuedEvent> {
		private double timeDue;
		public Step simObject;		
		
		/**
		 * Insert an event into the queue
		 * @param timeDue Time when event is due
		 * @param object Object that is returned when an event is dequeued
		 */
		QueuedEvent(double timeDue, Step simObject) {
			this.timeDue = timeDue;
			this.simObject = simObject;
		}

		@Override
		public int compareTo(QueuedEvent arg0) {
			double diff = this.timeDue - arg0.timeDue;	
			if (0 != diff)
				return diff > 0 ? 1 : -1;
			return simObject.hashCode() - arg0.simObject.hashCode();
		}
		
		@Override
		public String toString() {
			return String.format("%.3f: %s", timeDue, simObject.toString());
		}
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
        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
        if (imageURL != null)
            button.setIcon(new ImageIcon(imageURL, caption));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(30);
    	return button;
    }
  
	/**
	 * Retrieve a list of all scheduled events ordered by time due.
	 * @return ArrayList&lt;Object&gt; of all scheduled event objects
	 */
	public ArrayList<Step> scheduledEvents() {
		ArrayList<Step> result = new ArrayList<Step>();
		for (QueuedEvent qe : queue)
			result.add(qe.simObject);
		return result;
	}
	
	/**
	 * Enqueue an event
	 * @param timeDue Time when the event is due
	 * @param stepObject Step; returned when the event is inspected or dequeued 
	 */
	public void enqueueEvent(double timeDue, Step stepObject) {
		queue.add(new QueuedEvent(timeDue, stepObject));
		//System.out.println(String.format ("new QueuedEvent %s, queue length is %d", state.toString(), queue.size()));
	}

	/**
	 * Obtain the length of the queue
	 * @return integer; the number of events in the queue
	 */
	public int queueLength() {
		return queue.size();
	}
	
	/**
	 * Remove all pending events from the queue.
	 */
	public void clear() {
		queue.clear();
	}
	
	/**
	 * Return the time when the next event is due.
	 * @return Double; time when the next event is due
	 */
	public double nextDue() {
		return queue.first().timeDue;
	}
	
	/**
	 * Determine if the next event is due at (or before) a specified time.
	 * @param now Double; the specified time
	 * @return Boolean; true if an event is due at, or before the specified; false otherwise
	 */
	public boolean eventDue(double now) {
		if (queue.isEmpty())
			return false;
		return queue.first().timeDue <= now;
	}
	
	/**
	 * Return the first due event from the queue and remove it from the queue.
	 * @return SimulatoedObject describing the event, or null if the queue is 
	 * empty
	 */
	public Step deQueueEvent() {
		if (queue.isEmpty())
			return null;
		QueuedEvent qe = queue.pollFirst();
		//System.out.println(String.format("deQueueEvent: time increases from %.3f to %.3f for %s", simulatedTime, qe.timeDue, qe.simObject.toString()));
		simulatedTime = qe.timeDue;
		return qe.simObject;
	}
	
	/**
	 * Start, or re-start the clock-driven event dispatcher.
	 * <br />
	 * The event dispatcher calls the stepUpTo method of the simulator of the
	 * scheduler at regular times (up to 10 times per second). The stepUpTo
	 * method should dequeue events and handle them until no more events are
	 * due up to the time specified in the call to stepUpTo.
	 * <br />
	 * The simulator is expected (but not required) to enqueue events during the
	 * execution of stepUpTo.
	 */
	public void startRealTime() {
		zeroTime = (new Date()).getTime() - (long) (simulatedTime * 1000);
		timer.start();
	}
	
	/**
	 * Stop the clock-driven event dispatcher.
	 */
	public void stopRealTime() {
		timer.stop();
	}
	
	/**
	 * Execute the next scheduled event of the simulator
	 * @return Boolean; true if simulation may continue; false if an error was
	 * detected and simulation should be stopped
	 */
	public boolean singleStep() {
		runningSimulation.preStep();
		boolean result = stepSimulator();
		runningSimulation.postStep();
		clock.repaint();
		return result;
	}

	private boolean stepSimulator() {
		Step stepObject = deQueueEvent();
		if (null == stepObject)
			return false;
		return stepObject.step(simulatedTime);
	}
	
	/**
	 * Handle an event.
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		if (null == command) {
			timerTick();
			return;
		}
		//System.out.println("actionPerformed: " + command);
		endFastRun();
		stopRealTime();
		buttonRealTime.setEnabled(true);
		buttonFast.setEnabled(true);
		buttonStop.setEnabled(true);
		if (command.equals("Stop"))
			buttonStop.setEnabled(false);
		else if (command.equals("RealTime")) {
			buttonRealTime.setEnabled(false);
			startRealTime();
		} else if (command.equals("RunFast")) {
			buttonFast.setEnabled(false);
			fastRun = new FastRun(this, 1);
			fastRun.start();
		} else if (command.equals("Step")) {
			singleStep();
			buttonStop.setEnabled(false);
		} else if (command.equals("Restart"))
			restartSimulator();
		else if (command.equals("Reload"))
			reloadSimulator();
		else
			throw new Error("Unhandled event: " + command);
        graphicsPanel.repaint();
	}
	
	private boolean stepUpTo(double timeLimit) {
		runningSimulation.preStep();
		boolean result = true;
		while (result && eventDue(timeLimit))
			result = stepSimulator();
		runningSimulation.postStep();
		clock.repaint();
		return result;
	}
	
	private void timerTick() {
		timer.stop();
		Date tickStarted = new Date();
		double stepUpTo = (tickStarted.getTime() - zeroTime) / 1000f;
		if ((stepUpTo >= stopTime) && (simulatedTime < stopTime))
			stepUpTo = stopTime;
		boolean result = stepUpTo(stepUpTo);
		if (result && (stepUpTo != stopTime)) {
			timer.start();
			graphicsPanel.repaint();
		} else
	        buttonStop.doClick();
	}
	
	private void endFastRun() {
		if (null != fastRun) {
			//System.out.println("Ending fastRun: thread isAlive: " + (fastRun.isAlive() ? "true" : "false") + "; sending interrupt");
			fastRun.interrupt();
			try {
				fastRun.join();
			} catch (InterruptedException e) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Caught a problem trying to stop the simulator\r\n%s", WED.exeptionStackTraceToString(e));
			}
			//System.out.println("After join: thread isAlive: " + (fastRun.isAlive() ? "true" : "false"));
			fastRun = null;
		}
	}
	
	class FastRun extends Thread {
		private final Scheduler scheduler;
		private final double refreshInterval;
		private boolean shutDown = false;
		
		public FastRun(Scheduler scheduler, double refreshInterval) {
			this.scheduler = scheduler;
			this.refreshInterval = refreshInterval;
			//System.out.println("Created thread");
		}
		
		@Override
		public void run() {
			while (! shutDown) {
				double startTime = scheduler.simulatedTime;
				double timeLimit = startTime + refreshInterval;
				if ((timeLimit >= stopTime) && (simulatedTime < stopTime))
					timeLimit = stopTime;
				runningSimulation.preStep();
				boolean result = true;
				while (result && eventDue(timeLimit)) {
					if (interrupted()) {
						shutDown = true;
						//System.out.println("thread detects interrupted (1)");
						break;
					}
					result = stepSimulator();
				}
				runningSimulation.postStep();
				clock.repaint();
				graphicsPanel.repaint(true);
				if (! result)
					break;
				// Painting CANNOT complete while the scheduler is waiting for this thread to shut down
				while((! shutDown) && (! graphicsPanel.paintComplete())) {
					if (interrupted()) {
						shutDown = true;
						//System.out.println("thread detects interrupted (2)");
						break;
					}
					try {
						sleep(10);
					} catch (InterruptedException e) {
						shutDown = true;
					}
				}
				if (timeLimit == stopTime) {
					//System.out.println("Stop time reached; posting \"Stop\" event");
					/*
					 * This is not simple!
					 * We want to simulate a click on the stop button. Calling
					 * the doClick method of the button directly blocks until
					 * the actionPerformed in the Scheduler is done. 
					 * If we call doClick directly this (fastRun) thread will
					 * be blocked at the time that the GUI thread sends the
					 * Interrupt to stop this fastRun thread. This results in
					 * an InterruptedException in the GUI thread and this
					 * fastRun thread won't be "ready" for join()ing, ever.
					 * The solution (found after hours of experimenting) is to
					 * create another thread to perform the click on the stop 
					 * button, so this fastRun thread is not blocked and will
					 * continue to run and become ready to be join()ed. 
					 */
			        java.awt.EventQueue.invokeLater(new Runnable() {
			            @Override
						public void run() {
			            	buttonStop.doClick();
			            }});
					break;
				}
			}
			//System.out.println("FastRun thread shutting down; waiting to be join()-ed");
		}
	}
	
	private void restartSimulator() {
		if (null != runningSimulation)
			runningSimulation.Shutdown();
		clear();
        try {
			runningSimulation = Main.createSimulator(simulatorType, configuration, this);
		} catch (Exception e) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Could not start simulator:\r\n%s", WED.exeptionStackTraceToString(e));
		}
        simulatedTime = 0;
        buttonStop.setEnabled(false);
        graphicsPanel.setClient(runningSimulation);	// don't your forget it!
        graphicsPanel.repaint();
        clock.repaint();
	}
	
	private void reloadSimulator() {
		configuration = Main.configuration(simulatorType);
		restartSimulator();
	}
	
	class Clock extends JComponent {
		private static final long serialVersionUID = 1L;
		private final Scheduler scheduler;
		private Dimension preferredSize = new Dimension(150, 36);
		private Icon icon = null;
		boolean fontSizeSet = false;
		
		public Clock(Scheduler scheduler) {
			this.scheduler = scheduler;
	        String imgLocation = "/resources/" + "Clock.png";
	        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
	        if (imageURL != null)
	            icon = new ImageIcon(imageURL);
		}
		
		@Override
		public void paintComponent (Graphics g) {
			double time = scheduler.getSimulatedTime();
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
				scheduler.endTime.setFont(font);
				scheduler.revalidate();
				fontSizeSet = true;
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		Object value = endTime.getValue();
		if (null != value) {
			//System.out.println("value of endTime is " + value);
			String valueString = (String) value;
			String fields[] = valueString.split("[:\\.]");
			int hours = Integer.parseInt(fields[0]);
			int minutes = Integer.parseInt(fields[1]);
			int seconds = Integer.parseInt(fields[2]);
			int millis = Integer.parseInt(fields[3]);
			stopTime = millis / 1000d + seconds + minutes * 60 + hours * 3600; 
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
		
}