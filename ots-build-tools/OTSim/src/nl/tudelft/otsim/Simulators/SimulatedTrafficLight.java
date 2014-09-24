package nl.tudelft.otsim.Simulators;

import java.awt.Color;

/**
 * Simulate a traffic light.
 * 
 * @author Peter Knoppers
 */
public interface SimulatedTrafficLight extends SimulatedObject {

	/**
	 * Set the color of this SimulatedTrafficLight.
	 * <br /> This method should only be called by the
	 * {@link SimulatedTrafficLightController} that controls this 
	 * SimulatedTrafficLight.
	 * @param newColor Color; the new color of this SimulatedTrafficLight
	 */
	public void setColor(Color newColor);
	
	/**
	 * Retrieve the current color of this SimulatedTrafficLight.
	 * @return Color; the current color of this SimulatedTrafficLight
	 */
	public Color getColor();
	
	/**
	 * Retrieve the name of this SimulatedTrafficLight (as used by the
	 * {@link SimulatedTrafficLightController}.
	 * 
	 * @return String; name of this SimulatedTrafficLight
	 */
	public String name();

}