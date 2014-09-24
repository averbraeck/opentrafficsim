package nl.tudelft.otsim.Simulators.RoadwaySimulator;

import java.awt.Color;
import java.awt.geom.Point2D;

import nl.tudelft.otsim.GUI.GraphicsPanel;

/**
 * Store a shape use to scan for collisions.
 * 
 * @author Peter Knoppers
 */
public class ScanTrail {
	final Color color;
	final Point2D.Double[] points;
	final boolean polygon;
	final ScanTrail previous;
	
	/**
	 * Create a new ScanTrail shape.
	 * @param color Color of the ScanTrail
	 * @param points Point2D.Double[]; the points that form the shape
	 * @param filled Boolean; true if the shape must be closed (drawn as a polygon);
	 * false if the shape is open (drawn as a poly line)
	 * @param previous ScanTrail; previously defined ScanTrail (to be linked into a
	 * linked list behind this one)
	 */
	public ScanTrail(Color color, Point2D.Double[] points, boolean filled, ScanTrail previous) {
		this.color = color;
		this.points = new Point2D.Double[points.length];
		// copy the list of points
		for(int i = 0; i < points.length; i++)
			this.points[i] = new Point2D.Double(points[i].x, points[i].y);
		this.previous = previous;
		polygon = filled;
	}
	
	/**
	 * Draw this ScanTrail onto a {@link GraphicsPanel}.
	 * @param gp {@link GraphicsPanel}; output device to draw on to
	 */
	public void paint(GraphicsPanel gp) {
		gp.setColor(color);
		if (polygon)
			gp.drawPolygon(points);
		else
			gp.drawPolyLine(points, false);
	}

}