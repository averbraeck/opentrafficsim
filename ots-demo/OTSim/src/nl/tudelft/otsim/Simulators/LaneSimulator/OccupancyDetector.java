package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.Simulators.SimulatedDetector;

/**
 * Simple detector that can tell whether it is occupied by a vehicle or not.
 * 
 * @author W. Schakel
 */
public class OccupancyDetector extends RSU implements SimulatedDetector {
	final String name;
	final private Point2D.Double[] polygon;
	private ArrayList<java.lang.Double> detections = new ArrayList<java.lang.Double>();

    /**
     * Length of the detector [m].
     */
    protected double length;

    /**
     * List of vehicles that is at the detector.
     */
    protected java.util.ArrayList<Vehicle> vehicles = new java.util.ArrayList<Vehicle>();

    /**
     * Constructor which positions the detector on the network.
     * @param lane Lane where this detector is located.
     * @param x Start location (most upstream) of the detector [m].
     * @param name String; the name if the detector
     * @param length Length of the detector [m].
     * @param polygon Point2D.Double[]; array of points defining the outline of
     * the new OccupancyDetector
     */
    public OccupancyDetector(Lane lane, double x, String name, double length, Point2D.Double[] polygon) {
        super(lane, x, true, false);
        this.name = name;
        this.length = length;
        this.polygon = polygon;
    }

    /**
     * Adds a vehicle that passes the start of the detector with the front of
     * the nose to the list of vehicles on the detector.
     * @param vehicle Vehicle that passes the start of the detector.
     */
    @Override
    public void pass(Vehicle vehicle) {
        vehicles.add(vehicle);
        detections.add(vehicle.v);
        //System.out.println("Added vehicle " + vehicle + " to detector " + this);
        //System.out.println("count is now " + vehicles.size());
        //System.out.println("isOccupied is now " + (isOccupied() ? "true" : "false"));
    }

    /**
     * Removes vehicles from the list that have left the detector.
     */
    @Override
    public void control() {
        // Loop over the vehicles
        java.util.Iterator<Vehicle> iter = vehicles.iterator();
        while (iter.hasNext()) {
            Vehicle veh = iter.next();
            /*
             * The vehicle is downstream of the detector, which will return a
             * negative distance between the nose of the vehicle and the start
             * of the detector. If this distance is larger than the detector
             * length and vehicle length the vehicle rear has left the detector.
             */
            if ((this.lane.xAdj(veh.getLane()) + veh.x > x + length + veh.l) || ((! model.vehicles.contains(veh)) && (! model.lcVehicles.contains(veh)))) {
                iter.remove(); // safely remove using the iterator
                //System.out.println("Removed vehicle " + veh + " from VehicleDetector " + this);
            }
        }
    }

    /**
     * Returns whether there is are any vehicles on the detector.
     * @return Boolean; true if there are one or more vehicles on the detector;
     * false if there are no vehicles on the detector
     */
    @Override
	public boolean isOccupied() {
        return !vehicles.isEmpty();
    }

    @Override
    public void init() {}

    @Override
    public void noControl() {}

	@Override
	public void paint(double when, GraphicsPanel gp) {
		gp.setStroke(0F);
		gp.setColor(isOccupied() ? Color.CYAN : Color.BLUE);
		gp.drawPolygon(polygon);
	}

	@Override
	public Double[] outline(double when) {
		return polygon;
	}

	@Override
	public String name() {
		return name;
	}
	
	@Override
	public String toString() {
		return "VehicleDetector " + name + super.toString();
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
	public Double center(double when) {
		return null;
	}

}