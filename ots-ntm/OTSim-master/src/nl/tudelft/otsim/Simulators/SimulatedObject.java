package nl.tudelft.otsim.Simulators;

import java.awt.geom.Point2D;

import nl.tudelft.otsim.GUI.GraphicsPanel;

/**
 * This class is the common ancestor of all simulated objects that can be
 * painted on the screen and detect/touch each other.
 * 
 * @author Peter Knoppers
 */
public interface SimulatedObject {
	/**
	 * The paint method draws this SimulatedObject
	 * @param when Double; the time for which the object must be painted
	 * @param graphicsPanel GraphicsPanel to paint onto.
	 */
	abstract public void paint(double when, GraphicsPanel graphicsPanel);
	
	/**
	 * The outline method returns an array of Point2D.Double that give the
	 * outline of this SimulatedObject. The outline can be used to test for
	 * collisions and for drawing this SimulatedObject. Outline is a polygon 
	 * that does not self-intersect and it can not have holes. This polygon 
	 * need not be closed (i.e. the last point may be different from the first 
	 * point).
	 * @param when Double; time for which the outline must be returned
	 * @return Point2D.Double[]; array of points describing the outline. 
	 */
	abstract public Point2D.Double[] outline(double when);
}