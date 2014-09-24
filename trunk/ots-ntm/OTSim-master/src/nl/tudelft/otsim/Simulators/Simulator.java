package nl.tudelft.otsim.Simulators;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.GraphicsPanelClient;


/**
 * Minimum set of methods to define a simulator for OpenTraffic.
 * 
 * @author Peter Knoppers
 */
public abstract class Simulator implements GraphicsPanelClient {
	/**
	 * Simulators must create/initialize themselves from a textual description.
	 * @param configuration String; the textual description
	 * @param graphicsPanel GraphicsPanel; the output device to draw on
	 * @param scheduler Scheduler; the Scheduler for the new Simulator
	 * @throws Exception 
	 */
	public Simulator(String configuration, GraphicsPanel graphicsPanel, Scheduler scheduler) throws Exception {
		throw new Error("This create must be overridden in the sub-class");
	}
	/**
	 * Empty constructor; required to exist.
	 */
	public Simulator() {
	}
	/**
	 * This method is called when the simulator is to be shut down. The
	 * Simulator should close all files and sockets when this method is called.
	 * <br /> All {@link nl.tudelft.otsim.Events.Step} events queued in the {@link Scheduler} of the
	 * simulator are removed (by the Scheduler) <b>after</b? the call to Shutdown.
	 * This ensures that the Simulator can call the 
	 * {@link nl.tudelft.otsim.Events.Scheduler#scheduledEvents} method of the Scheduler inside ShutDown
	 * to obtain a list of all pending events.
	 */
	abstract public void Shutdown ();
	/**
	 * This method is called before one or more calls to the 
	 * {@link nl.tudelft.otsim.Events.Step#step} method of 
	 * {@link nl.tudelft.otsim.Events.Step} objects in this Simulator.
	 */
	abstract public void preStep();
	/**
	 * This method is called after one or more calls to the
	 * {@link nl.tudelft.otsim.Events.Step#step} method of 
	 * {@link nl.tudelft.otsim.Events.Step} objects in this Simulator.
	 */
	abstract public void postStep();
	/**
	 * This method shall return the {@link Scheduler} of this Simulator.
	 * @return {@link Scheduler}; the Scheduler of this Simulator
	 */
	abstract public Scheduler getScheduler();
	/**
	 * A String that identifies the type of Simulator.
	 */
	String simulatorType;
}