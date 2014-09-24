package nl.tudelft.otsim.Simulators.RoadwaySimulator;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.ObjectInspector;
import nl.tudelft.otsim.Simulators.SimulatedDetector;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.Simulators.SimulatedTrafficLight;
import nl.tudelft.otsim.Simulators.SimulatedTrafficLightController;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.SpatialTools.Planar;

import java.awt.event.ActionListener;

/**
 * Really simple Roadway based Simulator.
 * 
 * @author Peter Knoppers
 */
public class RoadwaySimulator extends Simulator implements ActionListener {
	private ArrayList<SimulatedObject> borders = new ArrayList<SimulatedObject> ();
	private final Scheduler scheduler;
	private Map<String, SimulatedObject> trafficLights = new HashMap<String, SimulatedObject>();
	private Map<String, SimulatedObject> detectors = new HashMap<String, SimulatedObject>();
	/** Type of this Simulator */
	public static final String simulatorType = "Roadway simulator";
	private JCheckBox drawNetworkBackground;
	private JCheckBox drawScans;

	/**
	 * Create a new RoadwaySimulator from a textual description.
	 * @param networkDescription String describing the boundaries of the
	 * drive-able areas and the {@link Detector Detectors} 
	 * @param graphicsPanel {@link GraphicsPanel}; the output device on which
	 * the network, detectors and vehicles must be drawn
	 * @param scheduler Scheduler; the event scheduler for this RoadwaySimulator
	 */
	public RoadwaySimulator(String networkDescription, GraphicsPanel graphicsPanel, Scheduler scheduler) {
		this.scheduler = scheduler;
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 20;	// any value higher than number of rows in Scheduler will do
		gbc.fill = GridBagConstraints.HORIZONTAL;
		drawNetworkBackground = new JCheckBox("Draw background");
		drawNetworkBackground.addActionListener(this);
		scheduler.add(drawNetworkBackground, gbc);
		gbc.gridy++;
		drawScans = new JCheckBox("Draw scans");
		drawScans.addActionListener(this);
		scheduler.add(drawScans, gbc);
		String[] lines = networkDescription.split("\n");
		for (String line : lines) {
			String fields[] = line.split("\t");
			if (line.startsWith("Border\t"))
				borders.add(loadPolyLine(fields, 1, fields.length));
			else if (line.startsWith("Detector\t"))
				loadDetector(fields);
			else if (line.startsWith("TrafficLight\t"))
				loadTrafficLight(fields);
			else if (line.startsWith("TrafficLightController\t"))
				loadTrafficLightController(fields);
			else
				throw new Error("Bad line in networkDescription: " + line);
		}
		neighbors = new ArrayList<SimulatedObject>();
		// STUB Create some traffic
    	for (int i = 0; i < 15; i++)
    		neighbors.add(new SimpleVehicle(scheduler, this, 8 * i, new Point2D.Double(98, -14), - Math.PI / 2));
	}
	
	private void loadTrafficLightController(String fields[]) {
		// This depends on all TrafficLights and Detectors to be created BEFORE the SimulatedTrafficLightController
		SimulatedTrafficLightController tlc = new SimulatedTrafficLightController(scheduler, fields[4]);
		for (String tlName : fields[2].split(","))
			tlc.addTrafficLight((SimulatedTrafficLight) trafficLights.get(tlName));
		for (String dName : fields[3].split(","))
			tlc.addDetector((SimulatedDetector) detectors.get(dName));
	}

	private void loadTrafficLight(String fields[]) {
		trafficLights.put(fields[1], new TrafficLight(fields[1], loadPolyLine(fields, 3, fields.length)));
	}

	// BEWARE: for now the list of neighbors equals the entire list of active vehicles
	ArrayList<SimulatedObject> neighbors;
	
	/**
	 * Return a list of vehicles near a specified location.
	 * <br /> Currently this method cheats and simply returns all vehicles in the simulation.
	 * @param location Point2D.Double; center of search area
	 * @param maxDistance Double; maximum distance around location to search
	 * @return ArrayList&lt;{@link SimpleVehicle}&gt; list of <b>all</b> vehicles in the simulation
	 */
	public ArrayList<SimulatedObject> nearbyVehicles(Point2D.Double location, double maxDistance) {
		return neighbors;
	}
	
	/**
	 * Return the list of borders of this RoadwaySimulator.
	 * @return ArrayList&lt;Point2D.Double[]&gt;; the list of borders of this RoadwaySimulator
	 */
	public ArrayList<SimulatedObject> borders() {
		return borders;
	}

	/**
	 * Return the list of detectors in this RoadwaySimulator
	 * @return ArrayList&lt;{@link SimulatedObject}&gt;; the list of detector in this RoadwaySimulator
	 */
	public Collection<SimulatedObject> detectors() {
		return detectors.values();
	}

	/**
	 * Draw all simulated vehicles on a {@link GraphicsPanel}.
	 * @param time Double; simulated time at which the vehicle positions must be drawn
	 * @param gp {@link GraphicsPanel}; the output device to draw onto
	 */
	private void drawActiveVehicles(double time, GraphicsPanel gp) {
		for (Step simObject : scheduler.scheduledEvents())
			if (simObject instanceof SimpleVehicle)
				((SimulatedObject) simObject).paint(time, gp);				
	}

	/**
	 * Draw all simulated detectors on a {@link GraphicsPanel}.
	 * @param time Double; simulated time at which the detectors must be drawn
	 * (the color of the detector depends on its occupancy; hence it depends on
	 * the simulated time)
	 * @param gp {@link GraphicsPanel}; the output device to draw onto
	 */
	private void drawDetectors(double time, GraphicsPanel gp) {
		for (Step simObject : scheduler.scheduledEvents())
			if (simObject instanceof SimulatedDetector)
				((SimulatedObject) simObject).paint(time, gp);				
	}

	/*
	BufferedPolyLine head = null;
	BufferedPolyLine last = null;
	*/
	/**
	 * Remove the search beams that were created by simulated vehicles that
	 * search the optimal steering etc.
	 */
	/*
	public void clearTrails() {
		head = last = null;	// this should make all that memory ready for garbage collection
	}*/
	
	private static SimulatedObject loadPolyLine(String coordinates[], int start, int end) {
		return new BorderOutLine (Planar.coordinatesToPoints(coordinates, start, end));
	}
	
	private void loadDetector(String fields[]) {
		// Fields[2] is ignored; it lists the lane numbers that intersect this detector
		detectors.put(fields[1], new Detector(fields[1], loadPolyLine(fields, 3, fields.length), scheduler));
	}
	
	@Override
	public void repaintGraph(GraphicsPanel graphicsPanel) {
		if (drawNetworkBackground.isSelected())
			Main.mainFrame.model.network.repaintGraph(graphicsPanel);
		graphicsPanel.setColor(Color.RED);
		graphicsPanel.setStroke(0f);
		for(SimulatedObject border: borders)
			border.paint(0, graphicsPanel);
		double time = scheduler.getSimulatedTime();
		for (SimulatedObject sd : detectors.values())
			sd.paint(time, graphicsPanel);
		for (SimulatedObject stl : trafficLights.values())
			stl.paint(time, graphicsPanel);
		drawActiveVehicles(time, graphicsPanel);
		if (drawScans.isSelected())
			for (Step simObject : scheduler.scheduledEvents())
				if (simObject instanceof SimpleVehicle)
					((SimpleVehicle) simObject).paintScanTrail(graphicsPanel);
	}

	private Point2D.Double mouseDown = null;
	private ObjectInspector objectInspector = null;
	SimulatedObject selectedObject = null;

	private double distanceToSimulatedObject(SimulatedObject so, GraphicsPanel gp) {
    	Point2D.Double outline[] = so.outline(scheduler.getSimulatedTime());
    	if (null == outline)
    		return Double.MAX_VALUE;
    	Point2D.Double translated[] = new Point2D.Double[outline.length];
    	for (int index = 0; index < outline.length; index++)
    		translated[index] = gp.translate(outline[index]);
    	return Planar.distancePolygonToPoint(translated, mouseDown);
		
	}
	
	double selectClosest(Collection<SimulatedObject> set, double currentClosest, GraphicsPanel gp) {
        final int maxDistance = 10;	// pixels
        double bestDistance = currentClosest;
		for (SimulatedObject so : set) {
			double distance = distanceToSimulatedObject(so, gp);
			if ((distance < maxDistance) && (distance < currentClosest)) {
				selectedObject = so;
				bestDistance = distance;
			}
		}
		return bestDistance;
	}
	private double selectObject(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	System.out.println(String.format("Searching vehicle near %f,%f (rev %f,%f", p.x, p.y, graphicsPanel.reverseTranslate(p).getX(), graphicsPanel.reverseTranslate(p).getY()));
        double bestDistance = Double.MAX_VALUE;
        ArrayList<SimulatedObject> vehicles = new ArrayList<SimulatedObject>();
        for (Step s : scheduler.scheduledEvents())
        	if (s instanceof SimpleVehicle)
        		vehicles.add((SimpleVehicle) s);
        bestDistance = selectClosest(vehicles, bestDistance, graphicsPanel);
        bestDistance = selectClosest(trafficLights.values(), bestDistance, graphicsPanel);
        bestDistance = selectClosest(detectors.values(), bestDistance, graphicsPanel);
        return bestDistance;
	}

	@Override
	public void mousePressed(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (null != mouseDown){ // Ignore mouse presses that occur
            return;            	//  when user is already drawing a curve.
        }//    (This can happen if the user presses two mouse buttons at the same time.)
        
    	if (null != objectInspector) {
    		objectInspector.dispose();
    		objectInspector = null;
    	}
        mouseDown = new Point2D.Double(evt.getX(), evt.getY());
        if (selectObject(graphicsPanel, mouseDown) != Double.MAX_VALUE)
        	objectInspector = new ObjectInspector(selectedObject, this);
	}

	@Override
	public void mouseDragged(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (null == mouseDown)
            return;			// processing a gesture

        Point2D.Double position = new Point2D.Double(evt.getX(), evt.getY());

        ((GraphicsPanel) evt.getSource()).addPan(position.x - mouseDown.x, position.y - mouseDown.y);
        // Update "current" position
        mouseDown = position;
	}

	@Override
	public void mouseReleased(GraphicsPanel graphicsPanel, MouseEvent evt) {
        mouseDown = null;
	}

	@Override
	public void mouseMoved(GraphicsPanel graphicsPanel, MouseEvent evt) {
        Point2D.Double reversePosition = graphicsPanel.reverseTranslate(new Point2D.Double(evt.getX(), evt.getY()));
		Main.mainFrame.setStatus(-1, "Mouse pointer at %.2f,%.2f", reversePosition.x, reversePosition.y);
	}

	@Override
	public void setModified() {
		// No op
	}

	@Override
	public void preStep() {
		//clearTrails();
	}

	@Override
	public void postStep() {
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Generate a list of Red or Yellow simulated traffic lights.
	 * @return ArrayList&lt;{@link SimulatedObject}&gt;; the list of simulated
	 * traffic lights that show Red or Yellow
	 */
	public ArrayList<SimulatedObject> redOrYellowTrafficLights() {
		ArrayList<SimulatedObject> result = new ArrayList<SimulatedObject>();
		for (SimulatedObject stl : trafficLights.values()) {
			Color color = ((SimulatedTrafficLight) stl).getColor();
			if ((Color.RED == color) || (Color.YELLOW == color))
				 result.add(stl);
		}
		return result;
	}

	@Override
	public void Shutdown() {
		for (Step s : scheduler.scheduledEvents())
			if (s instanceof SimulatedTrafficLightController)
				((SimulatedTrafficLightController) s).shutdown();		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		scheduler.getGraphicsPanel().repaint();
	}

}

class BorderOutLine implements SimulatedObject, Step {

	private final Point2D.Double[] outline;
	
	public BorderOutLine(Point2D.Double outline[]) {
		this.outline = outline;
	}
	
	@Override
	public boolean step(double now) {
		return false;
	}

	@Override
	public void paint(double when, GraphicsPanel graphicsPanel) {
		graphicsPanel.setColor(Color.RED);
		graphicsPanel.drawPolyLine(outline);
	}

	@Override
	public java.awt.geom.Point2D.Double[] outline(double when) {
		return outline;
	}
	
}