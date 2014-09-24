package nl.tudelft.otsim.SpatialTools;

import java.awt.geom.Point2D;

import nl.tudelft.otsim.GUI.Main;

/**
 * 
 * @author Peter Knoppers
 * <br />
 * This class holds the parameters that define a circle.
 */
public class Circle {
	private Point2D.Double center;
	private double radius;
	
	/**
	 * Create a circle
	 * @param center Point2D.Double defining the center of the circle
	 * @param radius Double defining the radius of the circle
	 */
	Circle(Point2D.Double center, double radius) {
		this.center = new Point2D.Double(center.x, center.y);
		this.radius = radius;
	}
	
	/**
	 * Retrieve the center of the circle.
	 * @return Point2D.Double of the center of the circle
	 */
	public Point2D.Double center() {
		return center;
	}
	
	/**
	 * Retrieve the radius of the circle.
	 * @return Double defining the radius of the circle
	 */
	public double radius () {
		return radius;
	}
	
	/**
	 * Obtain a String describing the circle.
	 */
	@Override
	public String toString() {
		return String.format(Main.locale, "Circle at (%.3f,%.3f) r %.3f", center.x, center.y, radius);
	}

}