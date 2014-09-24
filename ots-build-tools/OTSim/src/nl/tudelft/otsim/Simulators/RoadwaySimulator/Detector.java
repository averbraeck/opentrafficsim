package nl.tudelft.otsim.Simulators.RoadwaySimulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.Simulators.SimulatedDetector;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * This class represents a vehicle detector in a simulator.
 * 
 * @author Peter Knoppers
 */
public class Detector implements SimulatedDetector {
	/** String; the name of this SimulatedDetector */
	private final String name;
	final private java.awt.geom.Point2D.Double[] polygon;
	private ArrayList<SimulatedObject> vehiclesInRange = new ArrayList<SimulatedObject> ();
	final Color occupiedColor = Color.ORANGE;
	final Color freeColor = Color.BLUE;
	int cumulativeCount = 0;
	Scheduler scheduler;
	ArrayList<java.lang.Double> detections = new ArrayList<java.lang.Double> ();
	
	/**
	 * Create a new SimulatedDetector.
	 * @param name String; name of the new SimulatedDetector
	 * @param outline {@link SimulatedObject}; used to retrieve the outline of
	 * the new SimulatedDetector 
	 * @param scheduler {@link Scheduler} of the new SimulatedDetector
	 */
	public Detector(String name, SimulatedObject outline, Scheduler scheduler) {
		this.name = name;
		this.polygon = outline.outline(0);
		this.scheduler = scheduler;
	}

	/**
	 * Signal that a vehicle has come into detection range of this detector.
	 * @param simulatedObject {@link SimulatedObject} the vehicle
	 */
	public void addVehicle(SimulatedObject simulatedObject) {
		if (vehiclesInRange.contains(simulatedObject)) {
			System.out.println("Vehicle " + simulatedObject.toString() + " already registered in detector " + toString());
			return;
		}
		vehiclesInRange.add(simulatedObject);
		detections.add(((SimpleVehicle) simulatedObject).getSpeed());
		cumulativeCount++;
	}
	
	/**
	 * Indicate that a vehicle has moved out of range.
	 * @param simulatedObject {@link SimulatedObject} identification of the vehicle
	 */
	public void removeVehicle(SimulatedObject simulatedObject) {
		if (vehiclesInRange.contains(simulatedObject))
			vehiclesInRange.remove(simulatedObject);
	}

	/** 
	 * Report the number of vehicles currently within range of this
	 * VehicleDetector. Before returning, this method will check that each 
	 * vehicle currently registered is still in range and remove those that 
	 * are not.
	 * @return Integer; the number of vehicles in range.
	 */
	public int currentCount() {
		// Determine which vehicles have gone out of range
		Point2D.Double[] myOutline = outline(0d);
		double when = scheduler.getSimulatedTime();
		ArrayList<SimulatedObject> removeThese = new ArrayList<SimulatedObject> ();
		for (SimulatedObject simulatedObject : vehiclesInRange)
			if (! Planar.polygonIntersectsPolygon(myOutline, simulatedObject.outline(when)))
				removeThese.add(simulatedObject);
		// Remove the vehicles that have gone out of range
		for(SimulatedObject simulatedObject : removeThese) {
			System.out.println("Vehicle " + simulatedObject.toString() + " is outside area of " + this.toString());
			removeVehicle(simulatedObject);
		}
		return vehiclesInRange.size();
	}
	
	/**
	 * Update the list of vehicles in range.
	 * @param now Double; the current time
	 * @return Boolean; true if no problems occurred; false if problems
	 * occurred and simulation should not continue
	 */
	public boolean step(double now) {
		currentCount();	// Force update of the list of vehicles in range
		return true;
	}

	@Override
	public void paint(double when, GraphicsPanel gp) {
		gp.setStroke(0F);
		gp.setColor(isOccupied() ? Color.CYAN : Color.BLUE);
		gp.drawPolygon(polygon);
		Point2D.Double position = center (when);
		gp.setColor(Color.BLACK);
		gp.drawString("" + cumulativeCount, position);
	}

	@Override
	public Double[] outline(double when) {
		return polygon;
	}
	
	@Override
	public String toString() {
		return "SimulatedDetector - detecting area: " + Planar.pointsToString(outline(0d));
	}
	
	/**
	 * Retrieve the cumulative count of this VehicleDetector.
	 * @return Integer; the cumulative count of this VehicleDetector
	 */
	public int getCumulativeCount_r() {
		return cumulativeCount;
	}

	@Override
	public boolean isOccupied() {
		return currentCount() > 0;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int vehicleCount() {
		return detections.size();
	}

	@Override
	public double spaceMeanSpeed() {
		if (detections.size() < 1)
			throw new Error("no detections");
		double reciprocalSum = 0d;
		for (double speed : detections)
			reciprocalSum += 1 / speed;
		return 1 / reciprocalSum / detections.size();
	}

	@Override
	public double timeMeanSpeed() {
		if (detections.size() < 1)
			throw new Error("no detections");
		double sum = 0;
		for (double speed : detections)
			sum += speed;
		return sum / detections.size();
	}

	@Override
	public ArrayList<java.lang.Double> speeds() {
		return detections;
	}

	@Override
	public void resetTotals() {
		detections.clear();
	}

	@Override
	public Point2D.Double center(double when) {
		return new Point2D.Double((polygon[0].x + polygon[2].x) / 2, (polygon[0].y + polygon[2].y) / 2);
	}
	
}