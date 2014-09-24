package nl.tudelft.otsim.Simulators;

import java.util.ArrayList;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.GraphicsPanelClient;
import nl.tudelft.otsim.Simulators.SimulatedModel;


/**
 * Minimum set of methods to define a simulator for OpenTraffic.
 * 
 * If a simulator requires a proper shutdown action (instead of simply 
 * forgetting each and every reference so it can be garbage collected), it must 
 * implement ShutDownAble.
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
	 * This method shall return the {@link SimulatedModel} associated to this Simulator.
	 * @return {@link SimulatedModel}; the Model of this Simulator
	 */
	abstract public SimulatedModel getModel();
	
	/**
	 * Retrieve a list of all movable objects.
	 * 
	 * @return ArrayList&lt;{@link SimulatedObject}&gt;; the list of all movable objects
	 */
	abstract public ArrayList<SimulatedObject> SampleMovables();
	
	/**
	 * A String that identifies the type of Simulator.
	 */
	String simulatorType;

}
