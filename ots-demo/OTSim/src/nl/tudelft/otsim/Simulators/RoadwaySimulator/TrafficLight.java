package nl.tudelft.otsim.Simulators.RoadwaySimulator;

import java.awt.Color;
import java.awt.geom.Point2D;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.Simulators.SimulatedTrafficLight;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * Implement a traffic light for the {@link RoadwaySimulator}.
 * 
 * @author Peter Knoppers
 */
public class TrafficLight implements SimulatedTrafficLight {
	/** String; the name of this SimulatedTrafficLight */
	private final String name;
	final Point2D.Double[] outline;
	Color color = Color.RED;
	
	/**
	 * Create a SimulatedTrafficLight.
	 * @param id String; name of the new simulated traffic light
	 * @param outline {@link SimulatedObject}; used to obtain the outline of
	 * the new simulated traffic light 
	 */
	public TrafficLight(String id, SimulatedObject outline) {
		this.name = id;
		this.outline = Planar.closePolyline(outline.outline(0));
	}

	/**
	 * Set the color of this SimulatedTrafficLight.
	 * <br /> This method should only be called by the
	 * {@link nl.tudelft.otsim.Simulators.SimulatedTrafficLightController} that controls this 
	 * SimulatedTrafficLight.
	 * @param newColor Color; the new color of this SimulatedTrafficLight
	 */
	@Override
	public void setColor(Color newColor) {
		this.color = newColor;
	}
	
	@Override
	public void paint(double when, GraphicsPanel graphicsPanel) {
		graphicsPanel.setColor(color);
		graphicsPanel.setStroke(0);
		graphicsPanel.drawPolygon(outline);
	}

	@Override
	public Point2D.Double[] outline(double when) {
		return outline;
	}

	/**
	 * Retrieve the current color of this SimulatedTrafficLight.
	 * @return Color; the current color of this SimulatedTrafficLight
	 */
	@Override
	public Color getColor() {
		return color;
	}
	
	/**
	 * Retrieve the current color of this SimulatedTrafficLight.
	 * @return String; the name of the current color of this SimulatedTrafficLight
	 */
	public String getColor_r() {
		if (color.equals(Color.RED))
			return "Red";
		if (color.equals(Color.GREEN))
			return "Green";
		if (color.equals(Color.YELLOW))
			return "Yellow";
		return color.toString();
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Point2D.Double center(double when) {
		return null;
	}

}