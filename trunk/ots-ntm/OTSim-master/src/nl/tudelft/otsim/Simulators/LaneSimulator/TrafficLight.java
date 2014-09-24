package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.Simulators.SimulatedTrafficLight;

/**
 * Single traffic light.
 */
public class TrafficLight extends RSU implements SimulatedTrafficLight {

    /** Light color. */
    protected lightColor color = lightColor.RED;
    private final String name;
    private final Point2D.Double outline[];
    private static int count = 0;
    private int mycount = count++;

    /**
     * Constructor that sets the traffic light as noticeable.
     * @param lane Lane where the traffic light is at.
     * @param position Position on the lane.
     * @param name String; name of this traffic light for communication with a {@link nl.tudelft.otsim.Simulators.SimulatedTrafficLightController}
     * @param outline Point2D.Double[] polygon of this traffic light
     */
    public TrafficLight(Lane lane, double position, String name, Point2D.Double outline[]) {
        super(lane, position, false, true);
        this.name = name;
        this.outline = outline;
        System.out.println("Created traffic light " + count);
    }
    
    /** Empty, needs to be implemented. */
    @Override
	public void init() {}
    
    /** 
     * Empty, needs to be implemented. 
     * @param vehicle Passing vehicle.
     */
    @Override
	public void pass(Vehicle vehicle) {}

    /** Empty, needs to be implemented. */
    @Override
	public void control() {}
    
    /** 
     * Returns whether the traffic light is currently green.
     * @return Whether the traffic light is green.
     */
    public boolean isGreen() {
        return color==lightColor.GREEN;
    }
    
    /** 
     * Returns whether the traffic light is currently yellow. 
     * @return Whether the traffic light is yellow.
     */
    public boolean isYellow() {
        return color==lightColor.YELLOW;
    }
    
    /** 
     * Returns whether the traffic light is currently red. 
     * @return Whether the traffic light is red.
     */
    public boolean isRed() {
        return color==lightColor.RED;
    }
    
    /** Sets the traffic light to green. */
    public void setGreen() {
        color = lightColor.GREEN;
    }
    
    /** Sets the traffic light to yellow. */
    public void setYellow() {
        color = lightColor.YELLOW;
    }
    
    /** Sets the traffic light to red. */
    public void setRed() {
        color = lightColor.RED;
    }
    
    /**
     * Attaches a simple fixed-time controller to the traffic light. The phase 
     * of the cycle is set at the beginning.
     * @param tGreen Green duration [s].
     * @param tYellow Yellow duration [s].
     * @param tRed Red duration [s].
     */
    public void attachSimpleController(double tGreen, double tYellow, double tRed) {
        model.addController(new simpleController(model, tGreen, tYellow, tRed, 0));
    }
    
    /**
     * Attaches a simple fixed-time controller to the traffic light.
     * @param tGreen Green duration [s].
     * @param tYellow Yellow duration [s].
     * @param tRed Red duration [s].
     * @param tPhase Time within the cycle to start at [s].
     */
    public void attachSimpleController(double tGreen, double tYellow, double tRed, double tPhase) {
        model.addController(new simpleController(model, tGreen, tYellow, tRed, tPhase));
    }

    /** Empty, needs to be implemented. */
    @Override
	public void noControl() {}
    
    /** Enumeration for traffic light colors. */
    protected enum lightColor {
        /** Light is red. */
        RED,
        /** Light is yellow (or orange). */
        YELLOW,
        /** Light is green. */
        GREEN
    }
    
    /**
     * Simple fixed-time controller to be attached to a traffic light.
     */
    private class simpleController extends Controller {

        /** Green duration [s]. */
        private double tGreen;
        
        /** Yellow duration [s]. */
        private double tYellow;
        
        /** Red duration [s]. */
        private double tRed;
        
        /** Time of last state switch. */
        private double tSwitch;

        /**
         * Constructs a simple traffic light controller and add it to the model.
         * @param model model object.
         * @param tGreen Green duration [s].
         * @param tYellow Yellow duration [s].
         * @param tRed Red duration [s].
         * @param tPhase Time within the cycle to start at [s].
         */
        public simpleController(Model model, double tGreen, double tYellow, double tRed, double tPhase) {
            super(model);
            // check whether input is valid
            if (tPhase<0 || tPhase>tGreen+tYellow+tRed) {
                throw new RuntimeException("Phase time "+tPhase+" outside of phase duration.");
            }
            if (tGreen<0 || tYellow<0 || tRed<0) {
                throw new RuntimeException("Negative time defined for traffic light.");
            }
            // Set phase
            if (tPhase<=tGreen) {
                setGreen();
                tSwitch = model.t() - tPhase;
            } else if (tPhase<=tGreen+tYellow) {
                setYellow();
                tSwitch = model.t() + tGreen - tPhase;
            } else if (tPhase<=tGreen+tYellow+tPhase) {
                setRed();
                tSwitch = model.t() + tGreen + tYellow - tPhase;
            }
            // Set times
            this.tGreen = tGreen;
            this.tYellow = tYellow;
            this.tRed = tRed;
        }

        /** Empty, needs to be implemented. */
        @Override
        public void init() {}

        /**
         * Changes the state of the traffic light according to present times.
         */
        @Override
        public void control() {
            if (isGreen() && model.t()-tSwitch>=tGreen) {
                setYellow();
                tSwitch = model.t();
            } else if (isYellow() && model.t()-tSwitch>=tYellow) {
                setRed();
                tSwitch = model.t();
            } else if (isRed() && model.t()-tSwitch>=tRed) {
                setGreen();
                tSwitch = model.t();
            }
        }

        /** Empty, needs to be implemented. */
        @Override
        public void noControl() {}
    }

	@Override
	public void paint(double when, GraphicsPanel graphicsPanel) {
		graphicsPanel.setColor(getColor());
		graphicsPanel.setStroke(0);
		graphicsPanel.drawPolygon(outline);
	}

	@Override
	public Double[] outline(double when) {
		return outline;
	}

	@Override
	public void setColor(Color newColor) {
		if (newColor.equals(Color.RED))
			setRed();
		else if (newColor.equals(Color.GREEN))
			setGreen();
		else if (newColor.equals(Color.YELLOW))
			setYellow();
		else
			throw new Error("Unknown color: " + newColor);
	}

	@Override
	public Color getColor() {
		if (color.equals(lightColor.RED))
			return Color.RED;
		if (color.equals(lightColor.GREEN))
			return Color.GREEN;
		if (color.equals(lightColor.YELLOW))
			return Color.YELLOW;
		throw new Error("this jTrafficLicht has an unknown color: " + color);
	}

	@Override
	public String name() {
		return name;
	}
	
	/**
	 * Return a textual description of the color of this jTrafficLight.
	 * @return String; color of this jTrafficLight
	 */
	public String getColor_r() {
		if (color.equals(lightColor.RED))
			return "Red";
		if (color.equals(lightColor.GREEN))
			return "Green";
		if (color.equals(lightColor.YELLOW))
			return "Yellow";
		throw new Error("this jTrafficLicht has an unknown color: " + color);	
	}
	
	@Override
	public String toString() {
		System.out.println("jTrafficLicht " + count + "," + mycount + " toString returns: " + name + "/" + getColor_r());
		return name + "/" + getColor_r();
	}
}