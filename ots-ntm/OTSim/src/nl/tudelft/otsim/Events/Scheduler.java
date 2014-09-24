package nl.tudelft.otsim.Events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import javax.swing.Timer;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.SchedulerController;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.Simulators.ShutDownAble;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.Simulators.LaneSimulator.LaneSimulator;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroSimulator;
import nl.tudelft.otsim.Simulators.RoadwaySimulator.RoadwaySimulator;

// TODO: Change/Replace this class by/to use DSOL

/**
 * Scheduler for OTSim.
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
 * 
 * @author Peter Knoppers
 */
public class Scheduler implements ActionListener {
	private static final int timerMillis = 100;
	private TreeSet<QueuedEvent> queue = new TreeSet<QueuedEvent>();
	Timer timer = new Timer(timerMillis, this);
	private long zeroTime;
	Simulator runningSimulation;
	final String simulatorType;
	private String configuration;
	GraphicsPanel graphicsPanel;
	private double simulatedTime = 0;
	private FastRun fastRun = null;
	private double stopTime = 0;
	private SchedulerController schedulerController = null;
	private SchedulerState schedulerState = SchedulerState.Stopped;
	
	/**
	 * Possible states of a Scheduler
	 * 
	 * @author Peter Knoppers
	 */
	public enum SchedulerState {
		/** Idle */
		Stopped,
		/** Fast running */
		FastRunning,
		/** Running at real-time speed (if available CPU power allows) */
		RealTimeRunning,
		/** Execute one event from the queue */
		ExecuteSingleEvent,
		/** Restarting using previously obtained simulation configuration (if there is one) */
		Restart,
		/** Reloading (re-)obtain the simulation configuration */
		Reload,
		/** Simulation end time reached */
		EndTimeReached,
		/** Simulation stop at time reached */
		StopTimeReached,
		/** Simulator error prevents simulating further */
		SimulatorError,
	};
		
	/** 
	 * Create a scheduler for a simulator.
	 * @param simulatorType String that identifies the type of Simulator to run
	 * @param graphicsPanel GraphicsPanel whose repaint method will be called
	 * to update the image of the simulation.
	 */
	public Scheduler(String simulatorType, GraphicsPanel graphicsPanel) {
		this(simulatorType, graphicsPanel, null);
        reloadSimulator();
	}
	
	/**
	 * Create a Scheduler with specified configuration (do not attempt to load
	 * the configuration using Main.configuration).
	 * @param simulatorType String that identifies the type of Simulator to run
	 * @param graphicsPanel GraphicsPanel whose repaint method will be called
	 * to update the image of the simulation.
	 * @param configuration String; the initial configuration for the Simulator
	 */
	public Scheduler(String simulatorType, GraphicsPanel graphicsPanel, String configuration) {
		this.simulatorType = simulatorType;
		this.configuration = configuration;
		this.graphicsPanel = graphicsPanel;
       if (null != configuration)
        	restartSimulator();
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
	
	/**
	 * Transfer this simulation to another GraphicsPanel.
	 * @param newGraphicsPanel GraphicsPanel; the new output device
	 */
	public void setGraphicsPanel (GraphicsPanel newGraphicsPanel) {
		if (null == newGraphicsPanel)
			throw new Error("The GraphicsPanel may not be null");
		this.graphicsPanel = newGraphicsPanel;
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
	private void startRealTime() {
		zeroTime = (new Date()).getTime() - (long) (simulatedTime * 1000);
		changeState(SchedulerState.RealTimeRunning);
		timer.start();
	}
	
	/**
	 * Stop the clock-driven event dispatcher.
	 */
	public void stopRealTime() {
		timer.stop();
	}
	
	private volatile boolean singleStepActive = false;
	/**
	 * Execute the next scheduled event of the simulator
	 * @return SchedulerState; null if simulation may continue; SimulatorError if an error was
	 * detected and simulation should be stopped; EndTimeReached if simulation has ended normally
	 */
	private SchedulerState singleStep() {
		SchedulerState result = null;
		if (singleStepActive)
			return null;	// Yes; the caller may try again; later
		try {
			singleStepActive = true;
			changeState (SchedulerState.ExecuteSingleEvent);
			runningSimulation.preStep();
			result = stepSimulator();
			runningSimulation.postStep();
			changeState (SchedulerState.Stopped);
			if (null != schedulerController)
				schedulerController.schedulerClockChanged(simulatedTime);
		}
		finally 
		{
			singleStepActive = false;
		}
		return result;
	}

	private SchedulerState stepSimulator() {
		Step stepObject = deQueueEvent();
		if (null == stepObject)
			return SchedulerState.EndTimeReached;
		return stepObject.step(simulatedTime);
	}
	
	/**
	 * Set the state of this Scheduler.
	 * @param newState SchedulerState; the new state of this Scheduler
	 */
	public void setState(SchedulerState newState) {
		endFastRun();
		stopRealTime();
		changeState(SchedulerState.Stopped);
		switch (newState) {
		case Stopped: /* already stopped */ break;
		case FastRunning: fastRun = new FastRun(this, 1); fastRun.start(); break;
		case RealTimeRunning: startRealTime(); break;
		case ExecuteSingleEvent: singleStep(); break;
		case Restart: restartSimulator(); break;
		case Reload: reloadSimulator(); break;
		case EndTimeReached: changeState(newState); break;
		case StopTimeReached: /* already stopped */ break;
		case SimulatorError: changeState(newState); break;
		}
	}
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		if (null == command) {
			timerTick();
			return;
		}
		throw new Error("Unhandled event: " + command);
	}
	
	/**
	 * Run the simulation until the indicated time.
	 * @param timeLimit Double; time where simulation must stop
	 * @return Boolean; true if no errors occurred in the simulator; false if
	 * the simulator reported a fatal error (simulation could not continue)
	 */
	public SchedulerState stepUpTo(double timeLimit) {
		runningSimulation.preStep();
		SchedulerState result = null;
		while ((null == result) && eventDue(timeLimit))
			result = stepSimulator();
		runningSimulation.postStep();
		if (null != schedulerController)
			schedulerController.schedulerClockChanged(simulatedTime);
		return result;
	}
	
	private void timerTick() {
		timer.stop();
		Date tickStarted = new Date();
		double stepUpTo = (tickStarted.getTime() - zeroTime) / 1000f;
		if ((stepUpTo >= stopTime) && (simulatedTime < stopTime))
			stepUpTo = stopTime;
		SchedulerState result = stepUpTo(stepUpTo);
		if ((null == result) && (stepUpTo == stopTime))
			result = SchedulerState.StopTimeReached;
		if ((null == result) && (stepUpTo != stopTime)) {
			timer.start();
			if (null != graphicsPanel)
				graphicsPanel.repaint();
		} else if (null != result)
			changeState(result);
	}
	
	private void endFastRun() {
		if (null != fastRun) {
			//System.out.println("Ending fastRun: thread isAlive: " + (fastRun.isAlive() ? "true" : "false") + "; sending interrupt");
			try {
				fastRun.interrupt();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//System.out.println("Ending fastRun: after sending interrupt thread isAlive: " + (fastRun.isAlive() ? "true" : "false") + "; calling join");
			try {
				fastRun.join();
				//System.out.println("Join returned");
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
		private volatile boolean shutDown = false;
		
		public FastRun(Scheduler scheduler, double refreshInterval) {
			this.scheduler = scheduler;
			this.refreshInterval = refreshInterval;
			changeState (SchedulerState.FastRunning); 
		}
		
		@Override
		public void run() {
			SchedulerState reasonForStopping = null;
			while (! shutDown) {
				double startTime = scheduler.simulatedTime;
				double timeLimit = startTime + refreshInterval;
				if ((timeLimit >= stopTime) && (simulatedTime < stopTime))
					timeLimit = stopTime;
				runningSimulation.preStep();
				while ((null == reasonForStopping) && eventDue(timeLimit)) {
					if (interrupted()) {
						System.out.println("FastRun received interrupt when about to step the simulator)");
						shutDown = true;
						break;
					}
					reasonForStopping = stepSimulator();
					if (null != reasonForStopping)
						System.out.println("StepSimulator returned " + reasonForStopping);
				}
				runningSimulation.postStep();
				if (null != schedulerController)
					schedulerController.schedulerClockChanged(simulatedTime);
				if (null != graphicsPanel)
					graphicsPanel.repaint(true);
				if (reasonForStopping != null) {
					break;
				}
				while((! shutDown) && (! graphicsPanel.paintComplete())) {
					// Painting CANNOT complete while the scheduler is waiting for this thread to shut down
					if (interrupted()) {
						System.out.println("FastRun received interrupt waiting for paintComplete");
						shutDown = true;
						break;
					}
					try {
						sleep(10);	// 0.01 seconds
					} catch (InterruptedException e) {
						shutDown = true;
					}
				}
				if (timeLimit == stopTime) {
					changeState (SchedulerState.StopTimeReached);
					shutDown = true;
				}
			}
			if (null != schedulerController) {
				schedulerController.schedulerClockChanged(simulatedTime);
				if (null != reasonForStopping)
					changeState(reasonForStopping);
			}
		}
	}
	
	/**
	 * Create a new Simulator.
	 * @param type String; type of Simulator to create
	 * @param configuration String; configuration text for the Simulator
	 * @param scheduler
	 * @return Simulator; the newly created Simulator
	 * @throws Exception
	 */
	private static Simulator createSimulator(String type, String configuration, Scheduler scheduler) throws Exception {
		if (type.equals(LaneSimulator.simulatorType)) {
			return new LaneSimulator(configuration, scheduler.getGraphicsPanel(), scheduler);
		}
		if (type.equals(RoadwaySimulator.simulatorType)) {
			return new RoadwaySimulator(configuration, scheduler.getGraphicsPanel(), scheduler);
		}
		if (type.equals(MacroSimulator.simulatorType)) {
			return new MacroSimulator(configuration, scheduler.getGraphicsPanel(), scheduler);
		}
		throw new Error("Do not know how to create a simulator of type " + type);
	}

	private void restartSimulator() {
		killSimulator();
		clear();
		changeState(SchedulerState.Stopped);
        try {
			runningSimulation = createSimulator(simulatorType, configuration, this);
		} catch (Exception e) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Could not start simulator:\r\n%s", WED.exeptionStackTraceToString(e));
		}
        simulatedTime = 0;
        changeState (SchedulerState.Stopped);
        if (null != graphicsPanel) {
        	graphicsPanel.setClient(runningSimulation);	// don't you forget it!
        	graphicsPanel.repaint();
        }
		if (null != schedulerController)
			schedulerController.schedulerClockChanged(simulatedTime);
	}
	
	private void changeState(SchedulerState newState) {
		schedulerState = newState;
		if (null != schedulerController) {
			schedulerController.schedulerStateChanged();
		}
		if (null != graphicsPanel)
			graphicsPanel.repaint();
	}

	/**
	 * Stop the running simulation and kill all sub-processes.
	 */
	public void killSimulator() {
		for (Step step : scheduledEvents())
			if (step instanceof ShutDownAble)
				((ShutDownAble) step).ShutDown();
	}
	
	private void reloadSimulator() {
		configuration = Main.configuration(simulatorType);
		restartSimulator();
	}
	
	/**
	 * Set/Change the time at which this Scheduler must stop the simulation.
	 * @param newStopTime Double; the new end time [s]
	 */
	public void setStopTime(double newStopTime) {
		stopTime = newStopTime;
	}
	
	/**
	 * Run a complete simulation and terminate.
	 */
	public void runSimulation() {
		stepUpTo(Double.MAX_VALUE);
		Main.mainFrame.actionPerformed(new ActionEvent(Main.mainFrame, 0, "Exit"));
	}
	
	/**
	 * Retrieve the current state of this Scheduler.
	 * @return {@link SchedulerState}; the current state of this Scheduler
	 */
	public SchedulerState getState() {
		return schedulerState;
	}

	/**
	 * Retrieve the {@link SchedulerController} of this Scheduler.
	 * @return {@link SchedulerController}; the SchedulerController of this
	 * Scheduler, or null if this Scheduler does not have a
	 * SchedulerController
	 */
	public javax.swing.JPanel getSchedulerController() {
		return schedulerController;
	}

	/**
	 * Set/Change the {@link SchedulerController} for this Scheduler
	 * @param newSchedulerController {@link SchedulerController} the new
	 * SchedulerController of this Scheduler (may be null)
	 */
	public void setSchedulerController(SchedulerController newSchedulerController) {
		schedulerController = newSchedulerController;		
	}
}