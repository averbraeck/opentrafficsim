package nl.tudelft.otsim.Simulators;

/**
 * Interface for scheduled objects that need to be shutdown when a simulation is terminated.
 * 
 * @author Peter Knoppers
 *
 */
public interface ShutDownAble {
	/**
	 * This method will be called when the simulator is shutdown.
	 */
	public void ShutDown();
}