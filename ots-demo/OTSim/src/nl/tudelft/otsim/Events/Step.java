package nl.tudelft.otsim.Events;

import nl.tudelft.otsim.Events.Scheduler.SchedulerState;

/**
 * Interface for objects that must be stepped by the Scheduler.
 * 
 * @author Peter Knoppers
 */
public interface Step {
	/**
	 * Compute the new state of this object.
	 * @param now Double; the simulated time
	 * @return Boolean; true if no fatal problems occurred; false if some
	 * problem occurred and further simulation is not possible or sensible
	 */
	public SchedulerState step(double now);
}