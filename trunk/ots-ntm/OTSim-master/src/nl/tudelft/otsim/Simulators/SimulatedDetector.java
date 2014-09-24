package nl.tudelft.otsim.Simulators;

import java.util.ArrayList;

/**
 * This interface represents a vehicle detector in a simulator.
 * 
 * @author Peter Knoppers
 */
public abstract interface SimulatedDetector extends SimulatedObject {
	/**
	 * Determine whether this SimulatedDetector is occupied by one or more
	 * vehicles.
	 * @return Boolean; true if one or more vehicles cover this
	 * SimulatedDetector; false if no vehicle covers this SimulatedDetector
	 */
	public abstract boolean isOccupied();
	
	/**
	 * Total vehicles detected since last call to resetTotals.
	 * @return Integer; the vehicle count
	 */
	public abstract int vehicleCount();
	
	/**
	 * Harmonic mean speed of vehicles detected since last call to resetTotals.
	 * @return Double; the harmonic mean speed since last call to resetTotals
	 */
	public abstract double spaceMeanSpeed();
	
	/**
	 * Time mean speed of vehicles detected since last call to resetTotals.
	 * @return Double; the time mean speed
	 */
	public abstract double timeMeanSpeed();
	
	/**
	 * Return all speed values since last call to resetTotals.
	 * @return ArrayList&lt;Double&gt;; the list of speed values
	 */
	public abstract ArrayList<Double> speeds();
	
	/**
	 * Clear the internal list of collected speed values.
	 */
	public void resetTotals();
	
	/**
	 * Retrieve the name of this SimulatedDetector. The name is used to
	 * communicate with the {@link SimulatedTrafficLightController}.
	 * @return String; the name of this SimulatedDetector
	 */
	public abstract String name();
	
}